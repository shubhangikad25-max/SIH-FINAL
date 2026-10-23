import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApaarService, Student, College } from '../../services/apaar.service';

@Component({
  selector: 'app-college',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="college-container">
      <!-- Login & Registration Screen -->
      <div *ngIf="!isAuthenticated" class="login-wrapper">
        <div class="login-card gov-card">
          <div class="login-emblem">🏛️</div>
          <h2>College Admin Portal</h2>
          <p class="sub-text">AICTE Regulation Bureau • Spring Boot 3 + Angular</p>

          <!-- Auth Mode Tabs -->
          <div class="auth-tabs">
            <button [class.active]="authMode === 'login'" (click)="authMode = 'login'; loginError = ''; registerNotice = ''">
              Institutional Sign In
            </button>
            <button [class.active]="authMode === 'register'" (click)="authMode = 'register'; loginError = ''; registerNotice = ''">
              Request Access / Register
            </button>
          </div>

          <!-- Google Sign-In Option -->
          <div class="google-btn-wrapper">
            <button type="button" (click)="continueWithGoogle()" class="google-signin-btn" [disabled]="loading">
              <svg class="google-icon" viewBox="0 0 48 48" width="18px" height="18px">
                <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"/>
                <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/>
                <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"/>
                <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"/>
              </svg>
              <span>Continue with Google</span>
            </button>
          </div>

          <div class="divider">
            <span>OR INSTITUTIONAL CREDENTIALS</span>
          </div>

          <!-- Error & Notice Banners -->
          <div *ngIf="loginError" class="login-err">⚠️ {{ loginError }}</div>
          <div *ngIf="registerNotice" class="login-notice">ℹ️ {{ registerNotice }}</div>

          <!-- Login Form -->
          <form *ngIf="authMode === 'login'" (ngSubmit)="login()" class="login-form">
            <div class="field">
              <label>Institutional Email</label>
              <input type="email" [(ngModel)]="email" name="email" class="text-input" placeholder="e.g. college@aicte.ac.in" required />
            </div>
            <div class="field">
              <label>Passkey / Password</label>
              <input type="password" [(ngModel)]="password" name="password" class="text-input" placeholder="Enter password" required />
            </div>

            <div class="helper-box">
              Test Accounts:<br>
              • Approved: <strong>college&#64;aicte.ac.in</strong> / <strong>123456</strong><br>
              • Pending Approval: <strong>pending.college&#64;aicte.ac.in</strong> / <strong>123456</strong>
            </div>

            <button type="submit" class="gov-btn-primary full-btn" [disabled]="loading">
              {{ loading ? 'Authenticating...' : 'Sign In to Dashboard' }}
            </button>
          </form>

          <!-- Registration Form -->
          <form *ngIf="authMode === 'register'" (ngSubmit)="register()" class="login-form">
            <div class="field">
              <label>Administrator Full Name</label>
              <input type="text" [(ngModel)]="regName" name="regName" class="text-input" placeholder="e.g. Dr. Rajesh Kumar" required />
            </div>
            <div class="field">
              <label>Official Institutional Email</label>
              <input type="email" [(ngModel)]="regEmail" name="regEmail" class="text-input" placeholder="e.g. registrar@diet.ac.in" required />
            </div>
            <div class="field">
              <label>AICTE Approved Institution</label>
              <select [(ngModel)]="regInstitutionId" name="regInstitutionId" class="text-input" required>
                <option value="COL001">Delhi Institute of Engineering & Technology (AICTE-DL-0012)</option>
                <option value="COL002">Maharashtra National Institute of Technology (AICTE-MH-0845)</option>
                <option value="COL003">Bangalore Technical & Science Academy (AICTE-KA-0491)</option>
                <option value="COL004">Tamil Nadu Engineering & Management Campus (AICTE-TN-0219)</option>
                <option value="COL005">Uttar Pradesh Council of Technology (AICTE-UP-0672)</option>
              </select>
            </div>
            <div class="field">
              <label>Set Secure Password (min 8 chars, letter + digit)</label>
              <input type="password" [(ngModel)]="regPassword" name="regPassword" class="text-input" required />
            </div>
            <div class="field">
              <label>Confirm Password</label>
              <input type="password" [(ngModel)]="regConfirmPassword" name="regConfirmPassword" class="text-input" required />
            </div>

            <p class="reg-disclaimer">
              🔒 <strong>AICTE Regulatory Process:</strong> Institutional Administrator registrations are saved in <em>Pending</em> status and must be validated by an authorized AICTE Regulatory Bureau administrator before access is granted.
            </p>

            <button type="submit" class="gov-btn-saffron full-btn" [disabled]="loading">
              {{ loading ? 'Submitting Registration...' : 'Submit Institutional Registration' }}
            </button>
          </form>
        </div>
      </div>

      <!-- Dashboard View (When Authenticated) -->
      <div *ngIf="isAuthenticated" class="dashboard-wrapper">
        <!-- Top institutional bar -->
        <div class="top-bar gov-card">
          <div class="bar-left">
            <div class="inst-icon">🏛️</div>
            <div>
              <h2>{{ college?.name || 'Delhi Institute of Engineering & Technology' }}</h2>
              <span class="code-badge">{{ college?.code || 'AICTE-DL-0012' }}</span>
              <span class="tag">AY 2026-27 Intake Management • Logged in: <strong>{{ userEmail }}</strong></span>
            </div>
          </div>
          <div class="bar-actions">
            <button (click)="showUpload = true" class="gov-btn-primary">📤 Bulk Upload CSV</button>
            <button (click)="logout()" class="logout-btn">Logout</button>
          </div>
        </div>

        <!-- 4 Metric Cards -->
        <div class="metrics-grid">
          <div class="metric-card gov-card">
            <span class="m-label">TOTAL ENROLLED</span>
            <div class="m-val">{{ enrolmentCount }}</div>
            <span class="m-sub">Sanctioned Intake: <strong>{{ college?.approvedIntake || 1200 }}</strong></span>
          </div>

          <div class="metric-card gov-card border-green">
            <span class="m-label text-green">VERIFIED RECORDS</span>
            <div class="m-val text-green">{{ verifiedCount }}</div>
            <span class="m-sub text-green">Synced with NAD & DigiLocker</span>
          </div>

          <div class="metric-card gov-card border-amber">
            <span class="m-label text-amber">PENDING VERIFICATION</span>
            <div class="m-val text-amber">{{ pendingCount }}</div>
            <span class="m-sub text-amber">Awaiting Institutional Review</span>
          </div>

          <div class="metric-card gov-card border-red">
            <span class="m-label text-red">DUPLICATE CONFLICTS</span>
            <div class="m-val text-red">{{ duplicateCount }}</div>
            <span class="m-sub text-red">Simultaneous active seat flags</span>
          </div>
        </div>

        <!-- AICTE Intake Anomaly Banner -->
        <div class="intake-warning" *ngIf="enrolmentCount > (college?.approvedIntake || 1200)">
          ⚠️ <strong>AICTE Regulatory Anomaly Detected: Admissions Exceed Sanctioned Intake</strong>
          <p>
            Current enrolment ({{ enrolmentCount }}) exceeds sanctioned intake cap ({{ college?.approvedIntake || 1200 }}) by
            <strong>+{{ enrolmentCount - (college?.approvedIntake || 1200) }} students</strong>.
            Eligible Supernumerary Quota (TFW 5%): <strong>{{ college?.supernumeraryQuota || 45 }} seats</strong>. Subject to statutory audit.
          </p>
        </div>

        <!-- Cross-Institution Duplicate Alert Section -->
        <div class="duplicate-alert-section gov-card" *ngIf="duplicateConflicts.length > 0 || duplicates.length > 0">
          <div class="dup-header">
            <h3>🚨 National Duplicate Alert: Cross-Institution Enrolment Conflicts</h3>
            <span class="conflict-badge">{{ duplicateConflicts.length || duplicates.length }} Conflicts Flagged</span>
          </div>

          <div class="dup-grid">
            <div *ngFor="let conf of duplicateConflicts" class="dup-card">
              <div class="dup-row">
                <span class="dup-id">{{ conf.apaarId }}</span>
                <span class="status-badge duplicate">{{ conf.status }}</span>
              </div>
              <strong class="text-bold">{{ conf.studentName }}</strong>
              <div class="dup-details">
                <span>{{ conf.flagReason }}</span>
                <p>Conflicting Institution: <strong>{{ conf.conflictingCollegeName }} ({{ conf.conflictingCollegeId }})</strong></p>
              </div>
              <div *ngIf="conf.status === 'FLAGGED'">
                <button (click)="resolveConflict(conf)" class="resolve-btn">Resolve Conflict</button>
              </div>
              <div *ngIf="conf.status === 'RESOLVED'" class="resolved-note">
                ✓ Resolved: {{ conf.resolutionNotes }}
              </div>
            </div>
          </div>
        </div>

        <!-- Student Registry Table -->
        <div class="table-card gov-card">
          <div class="table-header">
            <div>
              <h3>Institutional Student Registry</h3>
              <p>Persistent PostgreSQL-backed records with 12-digit APAAR academic identifiers</p>
            </div>
            <div class="search-box">
              <input
                type="text"
                [(ngModel)]="searchQuery"
                (ngModelChange)="filterStudents()"
                placeholder="Search by student name or 12-digit APAAR ID..."
              />
            </div>
          </div>

          <!-- Filters -->
          <div class="table-filters">
            <select [(ngModel)]="courseFilter" (ngModelChange)="filterStudents()">
              <option value="all">All Academic Courses</option>
              <option value="Computer Science">Computer Science & Eng</option>
              <option value="Electronics">Electronics & Comm</option>
              <option value="Information Technology">Information Technology</option>
              <option value="Mechanical">Mechanical Engineering</option>
            </select>

            <select [(ngModel)]="statusFilter" (ngModelChange)="filterStudents()">
              <option value="all">All Verification Statuses</option>
              <option value="verified">Verified Records</option>
              <option value="pending">Pending Audit</option>
              <option value="duplicate">Duplicate Conflicts</option>
            </select>
          </div>

          <!-- Table -->
          <div class="table-responsive">
            <table class="gov-table">
              <thead>
                <tr>
                  <th>12-Digit APAAR ID</th>
                  <th>Student Name</th>
                  <th>Course & Branch</th>
                  <th>Category</th>
                  <th>ABC Credits</th>
                  <th>Admission Date</th>
                  <th>Verification Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let s of filteredStudents">
                  <td class="font-mono text-bold">{{ s.apaarId }}</td>
                  <td>{{ s.name }}</td>
                  <td>{{ s.course }} (Yr {{ s.academicYear }})</td>
                  <td><span class="cat-badge">{{ s.category }}</span></td>
                  <td class="font-mono"><strong>{{ s.abcCredits }}</strong> Cr</td>
                  <td>{{ s.admissionDate }}</td>
                  <td>
                    <span class="status-badge" [ngClass]="s.status">{{ s.status }}</span>
                  </td>
                  <td>
                    <button *ngIf="s.status === 'pending'" (click)="verify(s.apaarId)" class="action-btn verify">Verify</button>
                    <button *ngIf="s.status === 'pending'" (click)="reject(s.apaarId)" class="action-btn reject">Reject</button>
                    <span *ngIf="s.status === 'verified'" class="text-green text-bold">✓ Approved</span>
                    <span *ngIf="s.status === 'duplicate'" class="text-red font-mono">Flagged</span>
                  </td>
                </tr>
                <tr *ngIf="filteredStudents.length === 0">
                  <td colspan="8" style="text-align: center; padding: 20px; color: #64748b;">
                    No student records matching current filters.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Bulk Upload Modal -->
        <div *ngIf="showUpload" class="modal-overlay">
          <div class="modal-card gov-card">
            <div class="modal-header">
              <h3>Bulk Enrolment CSV Upload</h3>
              <button (click)="showUpload = false" class="close-btn">✕</button>
            </div>
            <p class="sub-text">AICTE Intake Anomaly Validator • Spring Boot Ingestion</p>

            <div class="drop-zone">
              <p>Select Student CSV file (12-Digit APAAR ID, Name, Course, Gender, Year, Category, RollNo)</p>
              <input type="file" (change)="onFileSelected($event)" accept=".csv,.xlsx" style="margin-bottom: 15px;" />
              <div>
                <button (click)="executeFileUpload()" class="gov-btn-saffron" [disabled]="uploading || !selectedFile">
                  {{ uploading ? 'Processing & Validating Batch...' : 'Run Batch Validation' }}
                </button>
              </div>
            </div>

            <div *ngIf="uploadNotice" class="upload-notice" [ngClass]="{'has-errors': uploadErrorCount > 0}">
              {{ uploadNotice }}
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .college-container { max-width: 1100px; margin: 30px auto; padding: 0 20px; }
    .login-wrapper { display: flex; justify-content: center; padding: 40px 0; }
    .login-card { max-width: 440px; width: 100%; padding: 35px; text-align: center; box-sizing: border-box; }
    .login-emblem { font-size: 36px; margin-bottom: 8px; }
    .login-card h2 { font-size: 20px; color: #0F2C61; margin: 0; font-weight: 800; }
    .sub-text { font-size: 11px; color: #64748b; margin: 5px 0 16px; }
    
    .auth-tabs { display: flex; border-bottom: 2px solid #e2e8f0; margin-bottom: 20px; }
    .auth-tabs button { flex: 1; padding: 10px; background: none; border: none; font-size: 12px; font-weight: 700; color: #64748b; cursor: pointer; border-bottom: 2px solid transparent; margin-bottom: -2px; }
    .auth-tabs button.active { color: #0F2C61; border-color: #E65100; }

    .google-btn-wrapper { margin-bottom: 16px; }
    .google-signin-btn { width: 100%; display: flex; align-items: center; justify-content: center; gap: 10px; background: #fff; border: 1px solid #cbd5e1; padding: 10px; border-radius: 6px; font-size: 13px; font-weight: 600; color: #1e293b; cursor: pointer; transition: all 0.2s; box-shadow: 0 1px 2px rgba(0,0,0,0.05); }
    .google-signin-btn:hover { background: #f8fafc; border-color: #94a3b8; }
    .google-signin-btn:disabled { opacity: 0.6; cursor: not-allowed; }

    .divider { display: flex; align-items: center; text-align: center; margin: 16px 0; font-size: 10px; font-weight: 700; color: #94a3b8; }
    .divider::before, .divider::after { content: ''; flex: 1; border-bottom: 1px solid #e2e8f0; }
    .divider span { padding: 0 10px; }

    .field { text-align: left; margin-bottom: 12px; }
    .field label { display: block; font-size: 11px; font-weight: 700; color: #475569; margin-bottom: 4px; text-transform: uppercase; }
    .text-input { width: 100%; box-sizing: border-box; padding: 10px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 13px; }
    .helper-box { background: #fffbeb; border: 1px solid #fde68a; padding: 10px; border-radius: 6px; font-size: 11px; color: #92400e; margin-bottom: 15px; text-align: left; line-height: 1.5; }
    .login-err { background: #fee2e2; border: 1px solid #fca5a5; color: #991b1b; padding: 10px; border-radius: 6px; font-size: 12px; margin-bottom: 14px; text-align: left; }
    .login-notice { background: #eff6ff; border: 1px solid #bfdbfe; color: #1e40af; padding: 10px; border-radius: 6px; font-size: 12px; margin-bottom: 14px; text-align: left; }
    .reg-disclaimer { font-size: 11px; color: #475569; text-align: left; margin: 12px 0; line-height: 1.4; }

    .top-bar { display: flex; justify-content: space-between; align-items: center; padding: 20px; margin-bottom: 20px; }
    .bar-left { display: flex; align-items: center; gap: 15px; }
    .inst-icon { font-size: 32px; }
    .top-bar h2 { font-size: 18px; color: #0F2C61; margin: 0; font-weight: 800; }
    .code-badge { background: #eff6ff; color: #1e40af; padding: 2px 8px; border-radius: 4px; font-family: monospace; font-size: 11px; font-weight: 700; }
    .tag { font-size: 11px; color: #64748b; margin-left: 8px; }
    .bar-actions { display: flex; gap: 10px; }
    .logout-btn { background: #f1f5f9; border: 1px solid #cbd5e1; padding: 8px 14px; border-radius: 6px; font-size: 12px; cursor: pointer; font-weight: 600; }

    .metrics-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 15px; margin-bottom: 20px; }
    .metric-card { padding: 20px; }
    .m-label { font-size: 11px; font-weight: 700; color: #94a3b8; }
    .m-val { font-size: 32px; font-weight: 900; color: #0F2C61; margin: 5px 0; }
    .m-sub { font-size: 11px; color: #64748b; }
    .border-green { border-left: 4px solid #10b981; }
    .border-amber { border-left: 4px solid #f59e0b; }
    .border-red { border-left: 4px solid #ef4444; }
    .text-green { color: #059669; }
    .text-amber { color: #d97706; }
    .text-red { color: #dc2626; }

    .intake-warning { background: #fffbeb; border-left: 4px solid #f59e0b; padding: 14px 20px; border-radius: 6px; font-size: 12px; color: #92400e; margin-bottom: 20px; }
    .intake-warning p { margin: 3px 0 0; }

    .duplicate-alert-section { padding: 20px; border: 2px solid #fecaca; margin-bottom: 20px; background: #fff; }
    .dup-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #fee2e2; padding-bottom: 10px; margin-bottom: 15px; }
    .dup-header h3 { font-size: 15px; color: #991b1b; margin: 0; }
    .conflict-badge { background: #fee2e2; color: #991b1b; padding: 3px 8px; border-radius: 4px; font-size: 11px; font-weight: 700; }
    .dup-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 15px; }
    .dup-card { background: #fff5f5; border: 1px solid #fed7d7; padding: 14px; border-radius: 8px; font-size: 12px; }
    .dup-row { display: flex; justify-content: space-between; margin-bottom: 5px; }
    .dup-id { font-family: monospace; font-weight: 700; color: #991b1b; }
    .dup-details { background: #fff; padding: 8px; border-radius: 6px; border: 1px solid #fee2e2; font-size: 11px; margin: 8px 0; }
    .dup-details p { margin: 4px 0 0; color: #718096; }
    .resolve-btn { background: #0F2C61; color: #fff; border: none; padding: 6px 12px; border-radius: 4px; font-size: 11px; font-weight: 700; cursor: pointer; }
    .resolved-note { color: #065f46; font-size: 11px; font-weight: 700; }

    .table-card { padding: 20px; }
    .table-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
    .table-header h3 { font-size: 16px; color: #0F2C61; margin: 0; font-weight: 800; }
    .table-header p { font-size: 11px; color: #64748b; margin: 2px 0 0; }
    .search-box input { padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 12px; width: 280px; }
    .table-filters { display: flex; gap: 10px; margin-bottom: 15px; }
    .table-filters select { padding: 6px 10px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 12px; }
    .table-responsive { overflow-x: auto; }
    .gov-table { width: 100%; border-collapse: collapse; font-size: 12px; text-align: left; }
    .gov-table th { background: #0F2C61; color: #fff; padding: 10px; font-size: 11px; text-transform: uppercase; }
    .gov-table td { padding: 10px; border-bottom: 1px solid #f1f5f9; }
    .font-mono { font-family: monospace; }
    .text-bold { font-weight: 700; color: #0F2C61; }
    .cat-badge { background: #f3e8ff; color: #6b21a8; padding: 2px 6px; border-radius: 4px; font-size: 10px; font-weight: 600; }
    .status-badge { padding: 2px 8px; border-radius: 4px; font-size: 10px; font-weight: 700; text-transform: uppercase; }
    .status-badge.verified { background: #d1fae5; color: #065f46; }
    .status-badge.duplicate { background: #fee2e2; color: #991b1b; }
    .status-badge.pending { background: #fef3c7; color: #92400e; }
    .status-badge.rejected { background: #f1f5f9; color: #475569; }
    .action-btn { padding: 4px 8px; border: none; border-radius: 4px; font-size: 11px; font-weight: 700; cursor: pointer; margin-right: 4px; }
    .action-btn.verify { background: #10b981; color: #fff; }
    .action-btn.reject { background: #ef4444; color: #fff; }

    .modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 100; }
    .modal-card { max-width: 480px; width: 100%; padding: 25px; box-sizing: border-box; }
    .modal-header { display: flex; justify-content: space-between; align-items: center; }
    .modal-header h3 { margin: 0; font-size: 16px; color: #0F2C61; font-weight: 800; }
    .close-btn { background: none; border: none; font-size: 16px; cursor: pointer; color: #94a3b8; }
    .drop-zone { border: 2px dashed #cbd5e1; padding: 25px; text-align: center; border-radius: 8px; margin: 15px 0; }
    .upload-notice { background: #d1fae5; color: #065f46; padding: 12px; border-radius: 6px; font-size: 12px; font-weight: 600; margin-top: 10px; }
    .upload-notice.has-errors { background: #fef3c7; color: #92400e; }
  `]
})
export class CollegeComponent implements OnInit {
  isAuthenticated: boolean = false;
  authMode: 'login' | 'register' = 'login';
  loading: boolean = false;

  email: string = 'college@aicte.ac.in';
  password: string = '123456';
  loginError: string = '';
  registerNotice: string = '';

  // Register Fields
  regName: string = '';
  regEmail: string = '';
  regInstitutionId: string = 'COL001';
  regPassword: string = '';
  regConfirmPassword: string = '';

  college: College | null = null;
  students: Student[] = [];
  filteredStudents: Student[] = [];
  duplicates: Student[] = [];
  duplicateConflicts: any[] = [];

  enrolmentCount: number = 0;
  verifiedCount: number = 0;
  pendingCount: number = 0;
  duplicateCount: number = 0;
  userEmail: string = '';

  searchQuery: string = '';
  courseFilter: string = 'all';
  statusFilter: string = 'all';

  showUpload: boolean = false;
  selectedFile: File | null = null;
  uploading: boolean = false;
  uploadNotice: string = '';
  uploadErrorCount: number = 0;

  constructor(private apaarService: ApaarService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    if (this.apaarService.isLoggedIn) {
      const user = this.apaarService.currentUser;
      if (user && (user.role === 'INSTITUTION_ADMIN' || user.role === 'AICTE_ADMIN')) {
        this.isAuthenticated = true;
        this.userEmail = user.email;
        this.fetchData();
      }
    }
  }

  login() {
    this.loading = true;
    this.loginError = '';
    this.registerNotice = '';

    this.apaarService.login({ email: this.email, password: this.password }).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.success && res.token) {
          this.isAuthenticated = true;
          this.userEmail = res.email;
          this.fetchData();
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.loading = false;
        this.loginError = err.error?.message || 'Invalid institutional credentials. Please try again.';
        this.cdr.detectChanges();
      }
    });
  }

  register() {
    if (!this.regName || !this.regEmail || !this.regPassword) {
      this.loginError = 'All registration fields are required.';
      return;
    }

    if (this.regPassword !== this.regConfirmPassword) {
      this.loginError = 'Password and password confirmation do not match.';
      return;
    }

    this.loading = true;
    this.loginError = '';
    this.registerNotice = '';

    const payload = {
      fullName: this.regName,
      email: this.regEmail,
      password: this.regPassword,
      confirmPassword: this.regConfirmPassword,
      role: 'INSTITUTION_ADMIN',
      institutionId: this.regInstitutionId
    };

    this.apaarService.register(payload).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.approvalStatus === 'PENDING') {
          this.registerNotice = res.message || 'Registration submitted! Your Institution Administrator account is pending AICTE regulatory approval.';
          this.authMode = 'login';
        } else if (res.token) {
          this.isAuthenticated = true;
          this.userEmail = res.email;
          this.fetchData();
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.loading = false;
        this.loginError = err.error?.message || 'Registration request could not be processed.';
        this.cdr.detectChanges();
      }
    });
  }

  continueWithGoogle() {
    this.loading = true;
    this.loginError = '';

    // Attempt authentic Google login via backend API
    this.apaarService.googleLogin({ idToken: '' }).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.success && res.token) {
          this.isAuthenticated = true;
          this.userEmail = res.email;
          this.fetchData();
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.loading = false;
        // Displays clear configuration error without simulating mock authentication
        this.loginError = err.error?.message || 'Google OAuth 2.0 is not configured on this server. Please configure GOOGLE_CLIENT_ID environment variable or sign in with your institutional credentials.';
        this.cdr.detectChanges();
      }
    });
  }

  logout() {
    this.apaarService.logout();
    this.isAuthenticated = false;
    this.college = null;
    this.students = [];
    this.filteredStudents = [];
    this.cdr.detectChanges();
  }

  fetchData() {
    this.apaarService.getCollegeStats().subscribe(res => {
      if (res.success) {
        this.college = res.college;
        this.enrolmentCount = res.enrolmentCount;
        this.verifiedCount = res.verifiedCount;
        this.pendingCount = res.pendingCount;
        this.duplicateCount = res.duplicateCount;
        this.cdr.detectChanges();
      }
    });

    this.apaarService.getStudents().subscribe(res => {
      if (res.success && res.data) {
        this.students = res.data;
        this.filteredStudents = [...this.students];
        this.duplicates = this.students.filter(s => s.status === 'duplicate' || s.duplicateCollegeName);
        this.cdr.detectChanges();
      }
    });

    this.apaarService.getDuplicateConflicts().subscribe(res => {
      if (res.success && res.data) {
        this.duplicateConflicts = res.data;
        this.cdr.detectChanges();
      }
    });
  }

  filterStudents() {
    this.filteredStudents = this.students.filter(s => {
      const matchQ = !this.searchQuery || s.name.toLowerCase().includes(this.searchQuery.toLowerCase()) || s.apaarId.toLowerCase().includes(this.searchQuery.toLowerCase());
      const matchCourse = this.courseFilter === 'all' || s.course.toLowerCase().includes(this.courseFilter.toLowerCase());
      const matchStatus = this.statusFilter === 'all' || s.status.toLowerCase() === this.statusFilter.toLowerCase();
      return matchQ && matchCourse && matchStatus;
    });
  }

  verify(apaarId: string) {
    this.apaarService.updateStatus(apaarId, 'verified', this.userEmail, 'INSTITUTION_ADMIN').subscribe(() => {
      this.fetchData();
    });
  }

  reject(apaarId: string) {
    this.apaarService.updateStatus(apaarId, 'rejected', this.userEmail, 'INSTITUTION_ADMIN').subscribe(() => {
      this.fetchData();
    });
  }

  resolveConflict(conflict: any) {
    const notes = prompt('Enter resolution notes for ' + conflict.studentName + ':', 'Dual admission reconciled after state regulatory verification');
    if (notes) {
      this.apaarService.resolveConflict(conflict.id, notes).subscribe(() => {
        this.fetchData();
      });
    }
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  executeFileUpload() {
    if (!this.selectedFile) return;

    this.uploading = true;
    this.uploadNotice = 'Ingesting and validating batch against persistent repository...';
    this.uploadErrorCount = 0;
    this.cdr.detectChanges();

    this.apaarService.uploadFile(this.selectedFile).subscribe({
      next: (res) => {
        this.uploading = false;
        this.uploadNotice = res.message || 'Batch processed successfully!';
        this.uploadErrorCount = res.errorCount || 0;
        this.fetchData();
        this.cdr.detectChanges();
        setTimeout(() => {
          this.showUpload = false;
          this.uploadNotice = '';
          this.selectedFile = null;
          this.cdr.detectChanges();
        }, 3000);
      },
      error: (err) => {
        this.uploading = false;
        this.uploadNotice = 'Upload failed: ' + (err.error?.message || 'File processing error');
        this.uploadErrorCount = 1;
        this.cdr.detectChanges();
      }
    });
  }
}
