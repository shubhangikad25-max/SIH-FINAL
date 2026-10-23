import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';

export interface Student {
  id?: number;
  apaarId: string;
  name: string;
  dob: string;
  gender: string;
  mobile: string;
  aadhaarLast4: string;
  collegeId: string;
  collegeName: string;
  course: string;
  academicYear: number;
  abcCredits: number;
  tenthPercent: number;
  twelfthPercent: number;
  status: string;
  category: string;
  enrollmentNo: string;
  admissionDate: string;
  digilockerVerified: boolean;
  state: string;
  duplicateCollegeName?: string;
  duplicateCollegeId?: string;
  flagReason?: string;
  qrVerificationToken?: string;
}

export interface College {
  id: string;
  name: string;
  code: string;
  state: string;
  city: string;
  approvedIntake: number;
  actualEnrolment: number;
  verifiedCount: number;
  pendingCount: number;
  duplicateCount: number;
  supernumeraryQuota: number;
  nriCount: number;
  fnCount: number;
}

export interface AuthUser {
  id: number;
  email: string;
  fullName: string;
  role: string; // STUDENT, INSTITUTION_ADMIN, AICTE_ADMIN, VERIFIER
  institutionId?: string;
  approvalStatus: string;
}

@Injectable({
  providedIn: 'root',
})
export class ApaarService {
  private userSubject = new BehaviorSubject<AuthUser | null>(null);
  public user$ = this.userSubject.asObservable();

  private get baseUrl(): string {
    if (typeof window !== 'undefined') {
      const customUrl = (window as any).API_URL || localStorage.getItem('API_URL');
      if (customUrl) return customUrl;
      if (window.location.hostname !== 'localhost') {
        return 'https://apaar-backend.onrender.com/api';
      }
    }
    return 'http://localhost:8085/api';
  }

  constructor(private http: HttpClient) {
    this.restoreSession();
  }

  private restoreSession() {
    if (typeof window !== 'undefined') {
      const storedUser = sessionStorage.getItem('apaar_user');
      const token = sessionStorage.getItem('apaar_token');
      if (storedUser && token) {
        try {
          this.userSubject.next(JSON.parse(storedUser));
        } catch (e) {
          this.logout();
        }
      }
    }
  }

  get token(): string | null {
    if (typeof window !== 'undefined') {
      return sessionStorage.getItem('apaar_token');
    }
    return null;
  }

  get currentUser(): AuthUser | null {
    return this.userSubject.value;
  }

  get isLoggedIn(): boolean {
    return !!this.token && !!this.currentUser;
  }

  private getHeaders(): HttpHeaders {
    let headers = new HttpHeaders();
    if (this.token) {
      headers = headers.set('Authorization', `Bearer ${this.token}`);
    }
    return headers;
  }

  // --- AUTHENTICATION ---

  login(credentials: { email: string; password: string }): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/auth/login`, credentials).pipe(
      tap(res => {
        if (res.success && res.token) {
          const user: AuthUser = {
            id: res.id,
            email: res.email,
            fullName: res.fullName,
            role: res.role,
            institutionId: res.institutionId,
            approvalStatus: res.approvalStatus
          };
          if (typeof window !== 'undefined') {
            sessionStorage.setItem('apaar_token', res.token);
            sessionStorage.setItem('apaar_user', JSON.stringify(user));
          }
          this.userSubject.next(user);
        }
      })
    );
  }

  register(data: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/auth/register`, data).pipe(
      tap(res => {
        if (res.success && res.token) {
          const user: AuthUser = {
            id: res.id,
            email: res.email,
            fullName: res.fullName,
            role: res.role,
            institutionId: res.institutionId,
            approvalStatus: res.approvalStatus
          };
          if (typeof window !== 'undefined') {
            sessionStorage.setItem('apaar_token', res.token);
            sessionStorage.setItem('apaar_user', JSON.stringify(user));
          }
          this.userSubject.next(user);
        }
      })
    );
  }

  googleLogin(payload: { idToken?: string; code?: string }): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/auth/google`, payload).pipe(
      tap(res => {
        if (res.success && res.token) {
          const user: AuthUser = {
            id: res.id,
            email: res.email,
            fullName: res.fullName,
            role: res.role,
            institutionId: res.institutionId,
            approvalStatus: res.approvalStatus
          };
          if (typeof window !== 'undefined') {
            sessionStorage.setItem('apaar_token', res.token);
            sessionStorage.setItem('apaar_user', JSON.stringify(user));
          }
          this.userSubject.next(user);
        }
      })
    );
  }

  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/forgot-password`, { email });
  }

  resetPassword(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/reset-password`, data);
  }

  logout(): void {
    if (typeof window !== 'undefined') {
      sessionStorage.removeItem('apaar_token');
      sessionStorage.removeItem('apaar_user');
    }
    this.userSubject.next(null);
    this.http.post(`${this.baseUrl}/auth/logout`, {}).subscribe({
      next: () => {},
      error: () => {}
    });
  }

  // --- APAAR ID VERIFICATION ---

  verifyApaar(apaarId: string, consent: boolean = true): Observable<any> {
    return this.http.get(`${this.baseUrl}/verify/${encodeURIComponent(apaarId)}?consent=${consent}`);
  }

  verifyQrToken(token: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/verify/qr/${encodeURIComponent(token)}`);
  }

  updateStatus(apaarId: string, status: string, actorEmail?: string, role?: string): Observable<any> {
    return this.http.post(
      `${this.baseUrl}/verify/${encodeURIComponent(apaarId)}/status`,
      { status, actorEmail, role },
      { headers: this.getHeaders() }
    );
  }

  // --- COLLEGE PORTAL ---

  getCollegeStats(collegeId?: string): Observable<any> {
    const q = collegeId ? `?collegeId=${encodeURIComponent(collegeId)}` : '';
    return this.http.get(`${this.baseUrl}/college/stats${q}`, { headers: this.getHeaders() });
  }

  getStudents(params?: any): Observable<any> {
    let url = `${this.baseUrl}/college/students`;
    if (params) {
      const q = new URLSearchParams(params).toString();
      url += `?${q}`;
    }
    return this.http.get(url, { headers: this.getHeaders() });
  }

  bulkUpload(students: Student[], collegeId?: string): Observable<any> {
    const q = collegeId ? `?collegeId=${encodeURIComponent(collegeId)}` : '';
    return this.http.post(`${this.baseUrl}/college/bulk-upload${q}`, students, { headers: this.getHeaders() });
  }

  uploadFile(file: File, collegeId?: string): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    const q = collegeId ? `?collegeId=${encodeURIComponent(collegeId)}` : '';
    return this.http.post(`${this.baseUrl}/college/upload-file${q}`, formData, { headers: this.getHeaders() });
  }

  getDuplicateConflicts(collegeId?: string): Observable<any> {
    const q = collegeId ? `?collegeId=${encodeURIComponent(collegeId)}` : '';
    return this.http.get(`${this.baseUrl}/college/duplicate-conflicts${q}`, { headers: this.getHeaders() });
  }

  resolveConflict(conflictId: number, notes?: string): Observable<any> {
    return this.http.post(
      `${this.baseUrl}/college/duplicate-conflicts/${conflictId}/resolve`,
      { notes },
      { headers: this.getHeaders() }
    );
  }

  // --- AICTE SUPER ADMIN ---

  getAllColleges(): Observable<any> {
    return this.http.get(`${this.baseUrl}/aicte/colleges`, { headers: this.getHeaders() });
  }

  getNationalStats(): Observable<any> {
    return this.http.get(`${this.baseUrl}/aicte/stats`, { headers: this.getHeaders() });
  }

  getAuditLogs(): Observable<any> {
    return this.http.get(`${this.baseUrl}/aicte/logs`, { headers: this.getHeaders() });
  }

  getPendingInstitutions(): Observable<any> {
    return this.http.get(`${this.baseUrl}/aicte/pending-institutions`, { headers: this.getHeaders() });
  }

  approveInstitution(userId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/aicte/institutions/${userId}/approve`, {}, { headers: this.getHeaders() });
  }

  rejectInstitution(userId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/aicte/institutions/${userId}/reject`, {}, { headers: this.getHeaders() });
  }

  downloadExcelReport(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/aicte/export-excel`, {
      responseType: 'blob',
      headers: this.getHeaders()
    });
  }

  // --- AICTE SAHAYAK CHATBOT ---

  sahayakQuery(query: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/public/sahayak/query`, { query }, { headers: this.getHeaders() });
  }
}
