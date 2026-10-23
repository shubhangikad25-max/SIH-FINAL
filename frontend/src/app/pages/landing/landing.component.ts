import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="landing-container">
      <!-- AICTE Header Banner -->
      <section class="hero-section">
        <div class="hero-inner">
          <div class="pill-badge">
            <span class="live-dot"></span>
            <span class="sih-tag">SIH26263 • SPRING BOOT + ANGULAR</span>
            <span>Approval Process & Academic Data Synchronization 2026-2027</span>
          </div>

          <h1 class="hero-title">One Nation, One Student ID</h1>
          <h2 class="hero-subtitle">APAAR Verification Platform for AICTE-Approved Institutions</h2>
          <p class="hero-desc">
            Enterprise Indian e-Governance architecture powered by <strong>Java 21, Spring Boot 3, and Angular Material</strong> to audit approved intake, manage Supernumerary and NRI quotas, and intercept cross-institution duplicate admissions.
          </p>

          <!-- 3 Portal Cards -->
          <div class="portals-grid">
            <div class="portal-card" (click)="navigate('/student')">
              <div class="portal-icon">🎓</div>
              <h3>Student Portal</h3>
              <p>Direct APAAR ID verification, view ABC credits, and print digital academic pass.</p>
              <div class="card-footer">Launch Portal ➔</div>
            </div>

            <div class="portal-card amber-border" (click)="navigate('/college')">
              <div class="portal-icon amber-icon">🏛️</div>
              <h3>College Admin</h3>
              <p>Manage enrolments, upload batches, audit intake capacity, and resolve duplicate flags.</p>
              <div class="card-footer text-amber">Manage Enrolments ➔</div>
            </div>

            <div class="portal-card green-border" (click)="navigate('/aicte')">
              <div class="portal-icon green-icon">🛡️</div>
              <h3>AICTE Super Admin</h3>
              <p>Pan-India state analytics, excess enrolment alerts, Kafka logs, and Apache POI Excel reports.</p>
              <div class="card-footer text-green">National Dashboard ➔</div>
            </div>
          </div>

          <!-- Quick Search Box -->
          <div class="search-box-card gov-card">
            <form (ngSubmit)="searchApaar()" class="search-form">
              <input
                type="text"
                [(ngModel)]="searchId"
                name="searchId"
                placeholder="Enter 12-digit APAAR ID (e.g. 123456789012)..."
                class="search-input"
              />
              <button type="submit" class="gov-btn-saffron">Verify Now</button>
            </form>
            <div class="demo-pills">
              <span>Quick Test Presets (12-Digit):</span>
              <button (click)="setDemo('123456789012')">Verified: <strong>123456789012</strong></button>
              <button (click)="setDemo('100000000003')">Pending: <strong>100000000003</strong></button>
              <button (click)="setDemo('100000000005')">Duplicate: <strong>100000000005</strong></button>
            </div>
          </div>
        </div>
      </section>

      <!-- Institutional Notice Board (Inspired by Screenshot) -->
      <section class="notice-section">
        <div class="notice-inner">
          <div class="notice-col-left gov-card">
            <div class="notice-header">
              <h3>AICTE Approval Process & Student Record Directives 2026-2027</h3>
              <span class="clause-tag">APH Clause 8.4</span>
            </div>
            <div class="notice-items">
              <div class="notice-item">
                <div>
                  <h4>Mandatory APAAR ID Integration for All Admitted Students</h4>
                  <p>Institutions must validate every student's 12-digit APAAR ID prior to admission confirmation.</p>
                </div>
                <button class="gov-btn-saffron small-btn" (click)="navigate('/college')">Upload Gateway</button>
              </div>
              <div class="notice-item">
                <div>
                  <h4>Approved Intake vs Actual Enrolment Verification</h4>
                  <p>Automated real-time reconciliation to detect and prevent unauthorized admissions exceeding sanctioned intake.</p>
                </div>
                <button class="gov-btn-saffron small-btn" (click)="navigate('/college')">Intake Audit</button>
              </div>
              <div class="notice-item">
                <div>
                  <h4>Supernumerary Seats (TFW), NRI & Foreign National (FN) Tracking</h4>
                  <p>Demarcation of regular vs supernumerary categories to guarantee statutory compliance under regulation guidelines.</p>
                </div>
                <button class="gov-btn-saffron small-btn" (click)="navigate('/aicte')">Inspect Quotas</button>
              </div>
            </div>
          </div>

          <div class="notice-col-right gov-card">
            <div class="notice-header">
              <h3>AICTE Technical Support & Facilitation</h3>
            </div>
            <div class="support-card bg-amber-light">
              <h4>Helpline for APAAR ID Integration:</h4>
              <p class="phone-num">📞 (011) 29581333, 29581338, 29581342</p>
              <p class="sub-text">Available 09:30 AM to 06:00 PM (Mon-Fri)</p>
            </div>
            <div class="support-card bg-blue-light">
              <h4>Live Video Support Facilitation:</h4>
              <p class="sub-text">Connect with AICTE Technical Officers for live onboarding and bulk upload guidance.</p>
              <button class="gov-btn-primary full-btn" (click)="alertSupport()">Open Live Video Session</button>
            </div>
          </div>
        </div>
      </section>

      <!-- National Statistics -->
      <section class="stats-section">
        <div class="stats-inner">
          <div class="stat-card">
            <h2>9,840</h2>
            <p>Approved Institutions</p>
            <span class="stat-badge green">100% Onboarded</span>
          </div>
          <div class="stat-card">
            <h2>34.2 Lakh</h2>
            <p>Total Enrolled Students</p>
            <span class="stat-badge blue">AY 2026-27 Intake</span>
          </div>
          <div class="stat-card">
            <h2>92.4%</h2>
            <p>APAAR Verified Records</p>
            <span class="stat-badge green">DigiLocker Synced</span>
          </div>
          <div class="stat-card">
            <h2>14,210</h2>
            <p>Duplicates Intercepted</p>
            <span class="stat-badge red">Ghost Seats Blocked</span>
          </div>
        </div>
      </section>
    </div>
  `,
  styles: [`
    .landing-container { min-height: 100vh; }
    .hero-section { background: linear-gradient(to bottom, #E9F3FC, #F8FAFC); padding: 50px 20px; text-align: center; }
    .hero-inner { max-width: 1100px; margin: 0 auto; }
    .pill-badge { display: inline-flex; align-items: center; gap: 8px; background: #fff; padding: 6px 16px; border-radius: 50px; font-size: 12px; font-weight: 600; color: #0F2C61; border: 1px solid #bfdbfe; margin-bottom: 20px; }
    .live-dot { width: 8px; height: 8px; background: #10B981; border-radius: 50%; }
    .sih-tag { background: #fef3c7; color: #92400e; padding: 2px 8px; border-radius: 4px; font-weight: 800; font-size: 10px; }
    .hero-title { font-size: 42px; font-weight: 900; color: #0F2C61; margin: 0; }
    .hero-subtitle { font-size: 26px; font-weight: 800; color: #D97706; margin: 10px 0 20px; }
    .hero-desc { font-size: 15px; color: #475569; max-width: 800px; margin: 0 auto 35px; line-height: 1.6; }
    .portals-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 20px; margin-bottom: 35px; }
    .portal-card { background: #fff; border: 2px solid #bfdbfe; border-radius: 14px; padding: 25px; text-align: left; cursor: pointer; transition: transform 0.2s, box-shadow 0.2s; box-shadow: 0 4px 12px rgba(15,44,97,0.06); }
    .portal-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(15,44,97,0.12); }
    .amber-border { border-color: #fde68a; }
    .green-border { border-color: #a7f3d0; }
    .portal-icon { font-size: 28px; margin-bottom: 12px; }
    .portal-card h3 { font-size: 18px; font-weight: 700; color: #0F2C61; margin: 0 0 8px; }
    .portal-card p { font-size: 12px; color: #64748b; margin: 0 0 16px; line-height: 1.5; }
    .card-footer { font-size: 12px; font-weight: 700; color: #0F2C61; border-top: 1px solid #f1f5f9; padding-top: 12px; }
    .text-amber { color: #d97706; }
    .text-green { color: #059669; }
    .search-box-card { max-width: 700px; margin: 0 auto; padding: 20px; }
    .search-form { display: flex; gap: 10px; }
    .search-input { flex: 1; padding: 12px 16px; border: 1px solid #cbd5e1; border-radius: 8px; font-family: monospace; font-size: 14px; }
    .demo-pills { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 12px; font-size: 11px; align-items: center; justify-content: center; }
    .demo-pills button { background: #f1f5f9; border: 1px solid #cbd5e1; border-radius: 6px; padding: 4px 8px; cursor: pointer; font-size: 11px; }
    .notice-section { padding: 40px 20px; max-width: 1100px; margin: 0 auto; }
    .notice-inner { display: grid; grid-template-columns: 2fr 1fr; gap: 20px; }
    @media(max-width: 768px) { .notice-inner { grid-template-columns: 1fr; } }
    .notice-col-left, .notice-col-right { padding: 25px; }
    .notice-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #e2e8f0; padding-bottom: 12px; margin-bottom: 20px; }
    .notice-header h3 { font-size: 16px; font-weight: 700; color: #0F2C61; margin: 0; }
    .clause-tag { background: #eff6ff; color: #1e3a8a; padding: 4px 10px; border-radius: 4px; font-size: 11px; font-weight: 700; }
    .notice-item { display: flex; justify-content: space-between; align-items: center; padding: 14px; border: 1px solid #e2e8f0; border-radius: 8px; margin-bottom: 12px; gap: 10px; }
    .notice-item h4 { font-size: 13px; font-weight: 700; color: #0F2C61; margin: 0 0 4px; }
    .notice-item p { font-size: 11px; color: #64748b; margin: 0; }
    .small-btn { padding: 6px 12px; font-size: 11px; }
    .support-card { padding: 15px; border-radius: 8px; margin-bottom: 15px; }
    .bg-amber-light { background: #fffbeb; border: 1px solid #fef3c7; }
    .bg-blue-light { background: #eff6ff; border: 1px solid #dbeafe; }
    .phone-num { font-family: monospace; font-weight: 700; font-size: 12px; margin: 5px 0; }
    .sub-text { font-size: 11px; color: #64748b; margin: 0 0 10px; }
    .full-btn { width: 100%; font-size: 12px; }
    .stats-section { background: #fff; border-top: 1px solid #e2e8f0; border-bottom: 1px solid #e2e8f0; padding: 40px 20px; }
    .stats-inner { max-width: 1100px; margin: 0 auto; display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; text-align: center; }
    .stat-card { padding: 20px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; }
    .stat-card h2 { font-size: 32px; font-weight: 900; color: #0F2C61; margin: 0 0 5px; }
    .stat-card p { font-size: 12px; font-weight: 600; color: #64748b; margin: 0 0 10px; }
    .stat-badge { font-size: 10px; font-weight: 700; padding: 3px 8px; border-radius: 4px; }
    .green { background: #d1fae5; color: #065f46; }
    .blue { background: #dbeafe; color: #1e40af; }
    .red { background: #fee2e2; color: #991b1b; }
  `]
})
export class LandingComponent {
  searchId: string = '';

  constructor(private router: Router) {}

  navigate(path: string) {
    this.router.navigateByUrl(path);
  }

  setDemo(id: string) {
    this.searchId = id;
    this.searchApaar();
  }

  searchApaar() {
    if (this.searchId.trim()) {
      this.router.navigate(['/student'], { queryParams: { id: this.searchId.trim() } });
    }
  }

  alertSupport() {
    alert('Connecting to AICTE Live Video Support Facilitation Desk...');
  }
}
