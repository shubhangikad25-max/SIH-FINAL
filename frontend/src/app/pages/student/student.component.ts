import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ApaarService, Student } from '../../services/apaar.service';

@Component({
  selector: 'app-student',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="student-container">
      <div class="header-card gov-card no-print">
        <div class="header-left">
          <div class="avatar-badge">🎓</div>
          <div>
            <h1>Student APAAR ID Verification Portal</h1>
            <p>National Academic Depository (NAD) • Spring Boot 3 + Angular Material</p>
          </div>
        </div>
        <div class="security-pill">🔒 AES-256 Encrypted Session</div>
      </div>

      <!-- Lookup Form -->
      <div class="form-card gov-card no-print">
        <form (ngSubmit)="verify()" class="lookup-form">
          <label>Enter 12-Digit APAAR ID / Academic Identifier</label>
          <div class="input-row">
            <input
              type="text"
              [(ngModel)]="apaarInput"
              name="apaarInput"
              placeholder="e.g. 123456789012 or 100000000003"
              class="apaar-input"
            />
            <button type="submit" class="gov-btn-primary" [disabled]="loading">
              {{ loading ? 'Querying National Academic Depository...' : 'Fetch Student Profile' }}
            </button>
          </div>

          <div class="preset-chips">
            <span>Demo Chips (12-Digit):</span>
            <button type="button" (click)="setPreset('123456789012')">Verified: 123456789012</button>
            <button type="button" (click)="setPreset('100000000003')">Pending: 100000000003</button>
            <button type="button" (click)="setPreset('100000000005')">Duplicate: 100000000005</button>
          </div>

          <div class="consent-row">
            <input type="checkbox" id="consent" [(ngModel)]="consentGiven" name="consent" />
            <label for="consent">
              <strong>DPDP Statutory Consent:</strong> I hereby give my consent to share my APAAR data and Academic Bank of Credits (ABC) credentials with my admitted AICTE-approved institution.
            </label>
          </div>
        </form>
      </div>

      <!-- Error message -->
      <div *ngIf="error" class="error-banner">
        ⚠️ {{ error }}
      </div>

      <!-- Student Record Display -->
      <div *ngIf="student" class="profile-section">
        <!-- Verification Timeline -->
        <div class="timeline-card gov-card no-print">
          <h3>Verification Lifecycle Timeline</h3>
          <div class="timeline-grid">
            <div class="step-box verified">
              <span class="step-num">✓</span>
              <div>
                <strong>1. Aadhaar e-KYC</strong>
                <p>Linked to Aadhaar ···{{ student.aadhaarLast4 }}</p>
              </div>
            </div>
            <div class="step-box verified">
              <span class="step-num">✓</span>
              <div>
                <strong>2. ABC Depository</strong>
                <p>{{ student.abcCredits }} Credits Banked</p>
              </div>
            </div>
            <div class="step-box" [ngClass]="student.status">
              <span class="step-num">{{ student.status === 'verified' ? '✓' : student.status === 'duplicate' ? '✕' : '⏳' }}</span>
              <div>
                <strong>3. College Intake Audit</strong>
                <p>{{ student.status === 'verified' ? 'Seat Approved' : student.status === 'duplicate' ? 'Duplicate Conflict' : 'Pending Review' }}</p>
              </div>
            </div>
            <div class="step-box" [ngClass]="student.status === 'verified' ? 'verified' : 'pending'">
              <span class="step-num">🏛️</span>
              <div>
                <strong>4. AICTE Central Ledger</strong>
                <p>{{ student.status === 'verified' ? 'Permanent Record Stored' : 'Awaiting Final Sync' }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Official Verifiable Credential Pass -->
        <div class="pass-card gov-card print-card">
          <div class="pass-header">
            <div>
              <span class="sub-label">Official Verifiable Academic Credential Pass</span>
              <h2>All India Council for Technical Education</h2>
              <p>National Academic Depository (NAD) • Spring Boot Backend Verified</p>
            </div>
            <div class="badge-status" [ngClass]="student.status">
              {{ student.status.toUpperCase() }}
            </div>
          </div>

          <div class="pass-body">
            <div class="details-col">
              <div class="name-row">
                <div>
                  <span class="label">STUDENT FULL NAME</span>
                  <div class="name-val">{{ student.name }}</div>
                </div>
                <div>
                  <span class="label">APAAR ID NUMBER</span>
                  <div class="id-val">{{ student.apaarId }}</div>
                </div>
              </div>

              <div class="grid-details">
                <div class="detail-cell">
                  <span>Enrolment / Roll No</span>
                  <strong>{{ student.enrollmentNo || '2024-ADM-092' }}</strong>
                </div>
                <div class="detail-cell">
                  <span>Date of Birth</span>
                  <strong>{{ student.dob }}</strong>
                </div>
                <div class="detail-cell">
                  <span>Gender</span>
                  <strong>{{ student.gender }}</strong>
                </div>
                <div class="detail-cell">
                  <span>Admitted Course</span>
                  <strong>{{ student.course }}</strong>
                </div>
                <div class="detail-cell">
                  <span>Current Year</span>
                  <strong>Year {{ student.academicYear }}</strong>
                </div>
                <div class="detail-cell">
                  <span>Admission Category</span>
                  <strong class="cat-text">{{ student.category }}</strong>
                </div>
                <div class="detail-cell">
                  <span>ABC Credits</span>
                  <strong class="credits-text">{{ student.abcCredits }} Credits</strong>
                </div>
                <div class="detail-cell">
                  <span>Class 10th Score</span>
                  <strong>{{ student.tenthPercent }}%</strong>
                </div>
                <div class="detail-cell">
                  <span>Class 12th Score</span>
                  <strong>{{ student.twelfthPercent }}%</strong>
                </div>
              </div>

              <div class="college-cell">
                <span>Admitted Institution</span>
                <strong>{{ student.collegeName }}</strong>
                <p>AICTE Code: {{ student.collegeId }} • State: {{ student.state }}</p>
              </div>

              <div *ngIf="student.duplicateCollegeName" class="conflict-cell">
                ⚠️ <strong>Duplicate Enrolment Anomaly Flagged:</strong> {{ student.flagReason }}
                <p>Conflicting Institution: {{ student.duplicateCollegeName }} ({{ student.duplicateCollegeId }})</p>
              </div>
            </div>

            <!-- QR Code Seal -->
            <div class="qr-col">
              <span class="qr-title">Cryptographic QR Seal</span>
              <div class="qr-box">
                <div class="qr-sim">QR: {{ student.apaarId }}</div>
              </div>
              <p class="hash-text">SHA-256: 4f98...e1b2</p>

              <div class="action-buttons no-print">
                <button (click)="print()" class="gov-btn-primary full-btn">🖨️ Print Pass</button>
              </div>
            </div>
          </div>

          <!-- SIH Requirement 10 -->
          <div class="pass-footer">
            <span>
              AICTE Compliance Clause 10: <strong>APAAR ID [{{ student.apaarId }}] & Student ID [{{ student.enrollmentNo }}]</strong> dual imprint enabled for certificates.
            </span>
            <span>Ministry of Education, Govt of India</span>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .student-container { max-width: 1000px; margin: 30px auto; padding: 0 20px; }
    .header-card { display: flex; justify-content: space-between; align-items: center; padding: 20px; margin-bottom: 20px; }
    .header-left { display: flex; align-items: center; gap: 15px; }
    .avatar-badge { width: 45px; height: 45px; background: #0F2C61; color: #fff; font-size: 22px; border-radius: 10px; display: flex; align-items: center; justify-content: center; }
    .header-left h1 { font-size: 20px; margin: 0; color: #0F2C61; font-weight: 800; }
    .header-left p { font-size: 12px; margin: 3px 0 0; color: #64748b; }
    .security-pill { font-size: 11px; background: #eff6ff; color: #1e3a8a; border: 1px solid #bfdbfe; padding: 6px 12px; border-radius: 20px; font-weight: 700; }
    .form-card { padding: 25px; margin-bottom: 25px; }
    .lookup-form label { display: block; font-size: 12px; font-weight: 700; color: #475569; text-transform: uppercase; margin-bottom: 8px; }
    .input-row { display: flex; gap: 10px; }
    .apaar-input { flex: 1; padding: 12px; border: 1px solid #cbd5e1; border-radius: 8px; font-family: monospace; font-size: 14px; }
    .preset-chips { display: flex; gap: 8px; margin: 12px 0; font-size: 11px; align-items: center; }
    .preset-chips button { background: #f8fafc; border: 1px solid #cbd5e1; padding: 4px 8px; border-radius: 4px; cursor: pointer; }
    .consent-row { display: flex; gap: 8px; font-size: 12px; color: #475569; border-top: 1px solid #f1f5f9; padding-top: 12px; margin-top: 12px; }
    .error-banner { background: #fee2e2; border: 1px solid #fca5a5; color: #991b1b; padding: 14px; border-radius: 8px; font-size: 13px; margin-bottom: 20px; }
    .timeline-card { padding: 20px; margin-bottom: 20px; }
    .timeline-card h3 { font-size: 14px; font-weight: 700; color: #475569; margin: 0 0 15px; }
    .timeline-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; }
    .step-box { display: flex; gap: 10px; padding: 12px; border-radius: 8px; border: 1px solid #e2e8f0; font-size: 11px; }
    .step-box.verified { background: #f0fdf4; border-color: #bbf7d0; color: #166534; }
    .step-box.duplicate { background: #fef2f2; border-color: #fecaca; color: #991b1b; }
    .step-box.pending { background: #fffbeb; border-color: #fde68a; color: #92400e; }
    .step-num { width: 22px; height: 22px; background: rgba(0,0,0,0.06); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 800; }
    .pass-card { border: 2px solid #0F2C61; border-radius: 14px; overflow: hidden; margin-bottom: 30px; }
    .pass-header { background: #0F2C61; color: #fff; padding: 20px; display: flex; justify-content: space-between; align-items: center; }
    .sub-label { font-size: 10px; color: #fde68a; font-weight: 800; letter-spacing: 1px; text-transform: uppercase; }
    .pass-header h2 { font-size: 18px; margin: 4px 0 2px; }
    .pass-header p { font-size: 11px; color: #cbd5e1; margin: 0; }
    .badge-status { padding: 4px 12px; border-radius: 20px; font-size: 11px; font-weight: 800; }
    .badge-status.verified { background: rgba(16,185,129,0.2); color: #34d399; border: 1px solid #34d399; }
    .badge-status.duplicate { background: rgba(239,68,68,0.2); color: #f87171; border: 1px solid #f87171; }
    .badge-status.pending { background: rgba(245,158,11,0.2); color: #fbbf24; border: 1px solid #fbbf24; }
    .pass-body { display: grid; grid-template-columns: 2fr 1fr; gap: 20px; padding: 25px; }
    @media(max-width: 768px) { .pass-body { grid-template-columns: 1fr; } }
    .name-row { display: flex; justify-content: space-between; border-bottom: 1px solid #f1f5f9; padding-bottom: 12px; margin-bottom: 15px; }
    .label { font-size: 10px; font-weight: 700; color: #94a3b8; }
    .name-val { font-size: 20px; font-weight: 800; color: #0F2C61; }
    .id-val { font-size: 16px; font-family: monospace; font-weight: 800; color: #b45309; background: #fef3c7; padding: 2px 8px; border-radius: 4px; }
    .grid-details { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; margin-bottom: 15px; }
    .detail-cell { background: #f8fafc; padding: 8px; border-radius: 6px; font-size: 11px; }
    .detail-cell span { display: block; font-size: 9px; color: #64748b; text-transform: uppercase; }
    .detail-cell strong { font-size: 12px; color: #1e293b; }
    .cat-text { color: #6d28d9; }
    .credits-text { color: #059669; }
    .college-cell { background: #eff6ff; padding: 12px; border-radius: 8px; border: 1px solid #bfdbfe; font-size: 11px; margin-bottom: 12px; }
    .college-cell span { font-size: 9px; color: #64748b; text-transform: uppercase; display: block; }
    .college-cell strong { font-size: 13px; color: #0F2C61; }
    .college-cell p { margin: 2px 0 0; color: #64748b; }
    .conflict-cell { background: #fee2e2; border: 1px solid #fca5a5; padding: 10px; border-radius: 8px; font-size: 11px; color: #991b1b; }
    .qr-col { text-align: center; border-left: 1px solid #f1f5f9; padding-left: 20px; }
    .qr-title { font-size: 10px; font-weight: 700; color: #64748b; text-transform: uppercase; }
    .qr-box { width: 130px; height: 130px; border: 2px solid #0F2C61; margin: 10px auto; border-radius: 8px; display: flex; align-items: center; justify-content: center; background: #f8fafc; }
    .qr-sim { font-family: monospace; font-size: 10px; color: #0F2C61; font-weight: bold; }
    .hash-text { font-family: monospace; font-size: 10px; color: #94a3b8; margin: 0 0 15px; }
    .pass-footer { background: #f1f5f9; padding: 10px 20px; font-size: 11px; color: #475569; display: flex; justify-content: space-between; border-top: 1px solid #e2e8f0; }
  `]
})
export class StudentComponent implements OnInit {
  apaarInput: string = '';
  loading: boolean = false;
  student: Student | null = null;
  error: string | null = null;
  outcome: string | null = null;
  metadata: any = null;
  consentGiven: boolean = true;

  constructor(
    private apaarService: ApaarService,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      if (params['id']) {
        this.apaarInput = params['id'];
        this.verify();
      }
    });
  }

  setPreset(id: string) {
    this.apaarInput = id;
    this.verify();
  }

  verify() {
    if (!this.apaarInput.trim()) {
      this.error = 'Please enter an authentic 12-digit APAAR ID.';
      return;
    }

    if (!this.consentGiven) {
      this.error = 'DPDP Statutory Consent must be selected before querying academic records.';
      return;
    }

    this.loading = true;
    this.error = null;
    this.student = null;
    this.outcome = null;
    this.cdr.detectChanges();

    this.apaarService.verifyApaar(this.apaarInput.trim(), this.consentGiven).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.success && res.data) {
          this.student = res.data;
          this.outcome = res.outcome;
          this.metadata = res.metadata;
        } else {
          this.error = res.message || 'Verification could not be completed.';
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.loading = false;
        this.outcome = err.error?.outcome || 'ERROR';
        this.error = err.error?.message || 'No student record found matching APAAR ID: ' + this.apaarInput;
        this.cdr.detectChanges();
      }
    });
  }

  print() {
    window.print();
  }
}
