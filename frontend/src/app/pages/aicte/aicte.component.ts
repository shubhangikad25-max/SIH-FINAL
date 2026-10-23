import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApaarService, College } from '../../services/apaar.service';

@Component({
  selector: 'app-aicte',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="aicte-container">
      <div class="header-card gov-card">
        <div class="header-left">
          <div class="avatar-badge">🛡️</div>
          <div>
            <h1>AICTE Central Super Admin Dashboard</h1>
            <p>Department of Higher Education • AICTE Regulation Bureau • Spring Boot 3 + Angular</p>
          </div>
        </div>

        <div class="header-actions">
          <button (click)="downloadExcel()" class="gov-btn-saffron">
            📊 Export Apache POI Excel (.xlsx)
          </button>
          <button (click)="printReport()" class="gov-btn-primary">
            🖨️ Export PDF Report
          </button>
        </div>
      </div>

      <div *ngIf="exportToast" class="export-toast">
        {{ exportToast }}
      </div>

      <!-- National KPI Cards (Explicitly Labelled as Synthetic Demo Benchmark) -->
      <div class="kpi-grid">
        <div class="kpi-card gov-card">
          <span>APPROVED INSTITUTIONS</span>
          <h2>{{ colleges.length || 5 }} <small class="kpi-sub">Active</small></h2>
          <small class="text-green">All 28 States & UTs Active • <span class="demo-chip">DEMO BENCHMARK: 9,840</span></small>
        </div>
        <div class="kpi-card gov-card">
          <span>PERSISTENT ENROLMENTS</span>
          <h2 class="text-blue">{{ totalEnrolled }} <small class="kpi-sub">Records</small></h2>
          <small class="text-amber"><span class="demo-chip">DEMO BENCHMARK: 34.6 L</span></small>
        </div>
        <div class="kpi-card gov-card">
          <span>APAAR VERIFIED RECORDS</span>
          <h2 class="text-green">{{ verifiedEnrolled }} <small class="kpi-sub">Verified</small></h2>
          <small class="text-green">NAD & DigiLocker Synced • <span class="demo-chip">DEMO BENCHMARK: 31.1 L</span></small>
        </div>
        <div class="kpi-card gov-card">
          <span>DUPLICATE CONFLICTS</span>
          <h2 class="text-red">{{ duplicateEnrolled }} <small class="kpi-sub">Flags</small></h2>
          <small class="text-red">Kafka Risk Engine Flags • <span class="demo-chip">DEMO DATA</span></small>
        </div>
      </div>

      <!-- Pending Institution Administrator Approvals Section -->
      <div class="pending-section gov-card" *ngIf="pendingInstitutions.length > 0">
        <div class="pending-header">
          <div>
            <h3>⏳ Pending Institution Administrator Registrations</h3>
            <p class="sub-text">AICTE Regulation Bureau Authorization Gateway (Required before institutional portal access)</p>
          </div>
          <span class="pending-count-pill">{{ pendingInstitutions.length }} Pending Authorization</span>
        </div>

        <div class="table-responsive">
          <table class="gov-table">
            <thead>
              <tr>
                <th>Administrator Name</th>
                <th>Institutional Email</th>
                <th>Assigned Institution</th>
                <th>Registered Date</th>
                <th>Authorization Status</th>
                <th>Decision Action</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let p of pendingInstitutions">
                <td class="text-bold">{{ p.fullName }}</td>
                <td>{{ p.email }}</td>
                <td><span class="font-mono text-bold">{{ p.institutionId }}</span></td>
                <td>{{ p.createdAt | date:'mediumDate' }}</td>
                <td><span class="status-pill audit">PENDING AICTE APPROVAL</span></td>
                <td>
                  <button (click)="approveInstitution(p.id)" class="action-btn verify">✓ Approve</button>
                  <button (click)="rejectInstitution(p.id)" class="action-btn reject">✕ Reject</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- State-Wise Official Bar Chart -->
      <div class="state-card gov-card">
        <div class="chart-header">
          <div>
            <h3>Pan-India State-Wise APAAR Verification Progress <span class="demo-tag">[DEMO DATA - SIH BENCHMARK]</span></h3>
            <p class="sub-text">Comparative student verification statistics across major technical education states</p>
          </div>
          <div class="chart-legend">
            <span class="legend-item"><span class="legend-badge total-badge"></span> Total Sanctioned Enrolments</span>
            <span class="legend-item"><span class="legend-badge verified-badge"></span> Verified APAAR Records</span>
          </div>
        </div>

        <div class="bar-chart-wrapper">
          <!-- Y-Axis Scale -->
          <div class="y-axis">
            <span>600k</span>
            <span>450k</span>
            <span>300k</span>
            <span>150k</span>
            <span>0</span>
          </div>

          <!-- Chart Plot Area -->
          <div class="chart-plot">
            <div class="grid-line" style="bottom: 100%;"></div>
            <div class="grid-line" style="bottom: 75%;"></div>
            <div class="grid-line" style="bottom: 50%;"></div>
            <div class="grid-line" style="bottom: 25%;"></div>
            <div class="grid-line baseline" style="bottom: 0%;"></div>

            <!-- Standing Bar Columns -->
            <div class="columns-container">
              <div *ngFor="let s of stateData" class="state-column-group">
                <!-- Dual Standing Bars -->
                <div class="dual-bars">
                  <!-- Bar 1: Total Enrolments -->
                  <div class="standing-bar total-bar" 
                       [style.height.%]="(s.total / maxStateVal * 100)"
                       [title]="s.state + ' Total Enrolment: ' + (s.total | number)">
                  </div>

                  <!-- Bar 2: Verified Records -->
                  <div class="standing-bar verified-bar" 
                       [style.height.%]="(s.verified / maxStateVal * 100)"
                       [title]="s.state + ' Verified APAAR: ' + (s.verified | number)">
                  </div>
                </div>

                <!-- X-Axis Labels -->
                <div class="x-axis-info">
                  <span class="state-name">{{ s.state }}</span>
                  <span class="state-sync">{{ ((s.verified / s.total) * 100).toFixed(1) }}% Sync</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- College-Wise Master Compliance Table -->
      <div class="table-card gov-card">
        <div class="table-header">
          <div>
            <h3>College-Wise Intake Compliance Master Table</h3>
            <p>Auditing sanctioned intake caps, actual enrolments, supernumerary (TFW) & NRI quotas</p>
          </div>
          <div class="filter-row">
            <input type="text" [(ngModel)]="searchCollege" (ngModelChange)="filterColleges()" placeholder="Filter colleges by name or AICTE code..." />
          </div>
        </div>

        <div class="table-responsive">
          <table class="gov-table">
            <thead>
              <tr>
                <th>Code</th>
                <th>Institution Name</th>
                <th>State</th>
                <th>Approved Intake</th>
                <th>Actual Enrolment</th>
                <th>Intake Variance</th>
                <th>Supernumerary (TFW)</th>
                <th>NRI Quota</th>
                <th>Foreign National</th>
                <th>Regulatory Status</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let c of filteredColleges">
                <td class="font-mono text-bold">{{ c.code }}</td>
                <td>{{ c.name }}</td>
                <td>{{ c.state }}</td>
                <td class="text-center font-mono">{{ c.approvedIntake }}</td>
                <td class="text-center font-mono text-bold">{{ c.actualEnrolment }}</td>
                <td class="text-center">
                  <span class="variance-badge" [ngClass]="(c.actualEnrolment - c.approvedIntake) > 0 ? 'over' : 'ok'">
                    {{ (c.actualEnrolment - c.approvedIntake) > 0 ? '+' + (c.actualEnrolment - c.approvedIntake) + ' Over' : 'Compliant' }}
                  </span>
                </td>
                <td class="text-center">{{ c.supernumeraryQuota }}</td>
                <td class="text-center">{{ c.nriCount }}</td>
                <td class="text-center">{{ c.fnCount }}</td>
                <td>
                  <span class="status-pill" [ngClass]="(c.actualEnrolment - c.approvedIntake) > 0 ? 'audit' : 'ok'">
                    {{ (c.actualEnrolment - c.approvedIntake) > 0 ? 'AUDIT MANDATED' : 'APPROVED' }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Real-Time Anomaly Stream & Audit Ledger -->
      <div class="logs-grid">
        <div class="log-box gov-card">
          <div class="box-head">
            <h3>⚡ Kafka Duplicate Anomaly Stream</h3>
            <span class="stream-pill">TOPIC: aicte.apaar.duplicate-events</span>
          </div>
          <div class="log-entries">
            <div class="log-item critical">
              <div class="log-meta">
                <span>Just Now</span>
                <strong>SIMULTANEOUS_ENROLMENT</strong>
              </div>
              <p>APAAR ID: <strong>100000000005</strong> • Mohammad Farhan Akhtar</p>
              <small>Dual enrollment flagged across Delhi (COL001) & UP (COL005) colleges</small>
            </div>
            <div class="log-item high">
              <div class="log-meta">
                <span>12 mins ago</span>
                <strong>OVER_INTAKE_ANOMALY</strong>
              </div>
              <p>College: <strong>AICTE-DL-0012</strong> • Delhi Institute of Eng & Tech</p>
              <small>Admissions (1,240) exceed sanctioned intake (1,200) by +40 seats</small>
            </div>
            <div class="log-item critical">
              <div class="log-meta">
                <span>28 mins ago</span>
                <strong>SIMULTANEOUS_ENROLMENT</strong>
              </div>
              <p>APAAR ID: <strong>100000000012</strong> • Ananya Banerjee</p>
              <small>Dual active enrollment across Delhi & Karnataka institutions</small>
            </div>
          </div>
        </div>

        <div class="log-box gov-card">
          <div class="box-head">
            <h3>📜 Immutable System Audit Ledger</h3>
            <span class="stream-pill blue">JPA AUDIT LOGS</span>
          </div>
          <div class="log-entries">
            <div *ngFor="let log of auditLogs" class="log-item normal">
              <div class="log-meta">
                <span>{{ log.timestamp | date:'short' }}</span>
                <strong>{{ log.action }}</strong>
              </div>
              <p>APAAR ID: <strong>{{ log.apaarId || 'SYSTEM' }}</strong> ({{ log.studentName }})</p>
              <small>Actor: {{ log.actor }} • IP: {{ log.ipAddress }} • {{ log.outcome }}</small>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .aicte-container { max-width: 1100px; margin: 30px auto; padding: 0 20px; }
    .header-card { display: flex; justify-content: space-between; align-items: center; padding: 20px; margin-bottom: 20px; }
    .header-left { display: flex; align-items: center; gap: 15px; }
    .avatar-badge { width: 45px; height: 45px; background: #0F2C61; color: #fff; font-size: 22px; border-radius: 10px; display: flex; align-items: center; justify-content: center; }
    .header-left h1 { font-size: 20px; margin: 0; color: #0F2C61; font-weight: 800; }
    .header-left p { font-size: 11px; margin: 3px 0 0; color: #64748b; }
    .header-actions { display: flex; gap: 10px; }
    .export-toast { background: #d1fae5; color: #065f46; border: 1px solid #6ee7b7; padding: 12px; border-radius: 8px; font-size: 12px; font-weight: 700; margin-bottom: 20px; }
    
    .kpi-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 15px; margin-bottom: 20px; }
    .kpi-card { padding: 20px; }
    .kpi-card span { font-size: 11px; font-weight: 700; color: #94a3b8; }
    .kpi-card h2 { font-size: 30px; font-weight: 900; color: #0F2C61; margin: 4px 0; }
    .kpi-sub { font-size: 13px; font-weight: 600; color: #64748b; }
    .demo-chip { font-size: 9px; font-weight: 700; background: #fef3c7; color: #92400e; padding: 1px 5px; border-radius: 3px; }
    .demo-tag { font-size: 10px; font-weight: 700; background: #fef3c7; color: #92400e; padding: 2px 6px; border-radius: 4px; margin-left: 8px; vertical-align: middle; }
    
    .text-green { color: #059669; }
    .text-blue { color: #2563eb; }
    .text-amber { color: #d97706; }
    .text-red { color: #dc2626; }

    .pending-section { padding: 20px; margin-bottom: 20px; border-left: 4px solid #f59e0b; background: #fff; }
    .pending-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #f1f5f9; padding-bottom: 10px; margin-bottom: 15px; }
    .pending-header h3 { font-size: 15px; color: #0F2C61; margin: 0; font-weight: 800; }
    .pending-count-pill { background: #fef3c7; color: #92400e; padding: 4px 10px; border-radius: 4px; font-size: 11px; font-weight: 700; }

    .state-card { padding: 22px; margin-bottom: 20px; }
    .chart-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; flex-wrap: wrap; gap: 10px; }
    .chart-header h3 { font-size: 16px; color: #0F2C61; margin: 0 0 4px; font-weight: 800; }
    .sub-text { font-size: 11px; color: #64748b; margin: 0; }
    .chart-legend { display: flex; gap: 16px; font-size: 11px; font-weight: 700; color: #475569; align-items: center; }
    .legend-item { display: flex; align-items: center; gap: 6px; }
    .legend-badge { width: 12px; height: 12px; border-radius: 2px; display: inline-block; }
    .total-badge { background: #0F2C61; }
    .verified-badge { background: #10B981; }
    .bar-chart-wrapper { display: flex; gap: 12px; margin-top: 20px; padding-right: 15px; }
    .y-axis { display: flex; flex-direction: column; justify-content: space-between; font-size: 11px; font-weight: 600; color: #64748b; text-align: right; height: 210px; padding-bottom: 35px; min-width: 40px; }
    .chart-plot { flex: 1; position: relative; height: 210px; margin-bottom: 35px; border-bottom: 2px solid #cbd5e1; }
    .grid-line { position: absolute; left: 0; right: 0; border-top: 1px dashed #e2e8f0; }
    .grid-line.baseline { border-top: 2px solid #94a3b8; }
    .columns-container { display: flex; justify-content: space-around; align-items: flex-end; height: 100%; position: relative; z-index: 2; padding: 0 10px; }
    .state-column-group { display: flex; flex-direction: column; align-items: center; justify-content: flex-end; height: 100%; width: 18%; position: relative; }
    .dual-bars { display: flex; gap: 6px; align-items: flex-end; height: 170px; width: 100%; justify-content: center; }
    .standing-bar { width: 32px; border-radius: 2px 2px 0 0; position: relative; cursor: pointer; transition: opacity 0.2s; }
    .standing-bar:hover { opacity: 0.85; }
    .total-bar { background: #0F2C61; }
    .verified-bar { background: #10B981; }
    .x-axis-info { position: absolute; bottom: -32px; text-align: center; white-space: nowrap; }
    .state-name { font-size: 12px; font-weight: 700; color: #0F2C61; display: block; }
    .state-sync { font-size: 10px; font-weight: 700; color: #059669; display: block; }
    @media (max-width: 768px) {
      .standing-bar { width: 18px; }
      .dual-bars { gap: 3px; }
      .state-name { font-size: 10px; }
      .state-sync { font-size: 9px; }
    }
    .table-card { padding: 20px; margin-bottom: 20px; }
    .table-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
    .table-header h3 { font-size: 15px; color: #0F2C61; margin: 0; font-weight: 800; }
    .table-header p { font-size: 11px; color: #64748b; margin: 2px 0 0; }
    .filter-row input { padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 12px; }
    .table-responsive { overflow-x: auto; }
    .gov-table { width: 100%; border-collapse: collapse; font-size: 12px; text-align: left; }
    .gov-table th { background: #0F2C61; color: #fff; padding: 10px; font-size: 10px; text-transform: uppercase; }
    .gov-table td { padding: 10px; border-bottom: 1px solid #f1f5f9; }
    .font-mono { font-family: monospace; }
    .text-bold { font-weight: 700; color: #0F2C61; }
    .text-center { text-align: center; }
    .variance-badge { padding: 2px 6px; border-radius: 4px; font-size: 10px; font-weight: 700; }
    .variance-badge.over { background: #fee2e2; color: #991b1b; }
    .variance-badge.ok { background: #d1fae5; color: #065f46; }
    .status-pill { padding: 3px 8px; border-radius: 4px; font-size: 10px; font-weight: 700; }
    .status-pill.audit { background: #fee2e2; color: #991b1b; }
    .status-pill.ok { background: #eff6ff; color: #1e40af; }
    .action-btn { padding: 4px 8px; border: none; border-radius: 4px; font-size: 11px; font-weight: 700; cursor: pointer; margin-right: 4px; }
    .action-btn.verify { background: #10b981; color: #fff; }
    .action-btn.reject { background: #ef4444; color: #fff; }
    .logs-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
    @media(max-width: 768px) { .logs-grid { grid-template-columns: 1fr; } }
    .log-box { padding: 20px; }
    .box-head { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #e2e8f0; padding-bottom: 10px; margin-bottom: 15px; }
    .box-head h3 { font-size: 14px; color: #0F2C61; margin: 0; font-weight: 800; }
    .stream-pill { font-size: 10px; font-family: monospace; background: #fee2e2; color: #991b1b; padding: 2px 6px; border-radius: 4px; font-weight: 700; }
    .stream-pill.blue { background: #eff6ff; color: #1e40af; }
    .log-entries { display: flex; flex-direction: column; gap: 10px; }
    .log-item { padding: 10px; border-radius: 6px; font-size: 11px; border: 1px solid #e2e8f0; }
    .log-item.critical { background: #fff5f5; border-color: #fed7d7; }
    .log-item.high { background: #fffaf0; border-color: #feebc8; }
    .log-item.normal { background: #f8fafc; }
    .log-item p { margin: 4px 0 2px; color: #4a5568; }
    .log-item small { color: #a0aec0; }
    .log-meta { display: flex; justify-content: space-between; color: #718096; margin-bottom: 2px; }
  `]
})
export class AicteComponent implements OnInit {
  colleges: College[] = [];
  filteredColleges: College[] = [];
  searchCollege: string = '';
  auditLogs: any[] = [];
  pendingInstitutions: any[] = [];
  exportToast: string = '';
  maxStateVal: number = 600000;

  totalEnrolled: number = 50;
  verifiedEnrolled: number = 42;
  duplicateEnrolled: number = 4;

  stateData = [
    { state: 'Maharashtra', total: 420000, verified: 395000 },
    { state: 'Tamil Nadu', total: 380000, verified: 362000 },
    { state: 'Karnataka', total: 310000, verified: 290000 },
    { state: 'Delhi NCR', total: 290000, verified: 268000 },
    { state: 'Uttar Pradesh', total: 540000, verified: 475000 },
  ];

  constructor(private apaarService: ApaarService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.apaarService.getAllColleges().subscribe(res => {
      if (res.success && res.data) {
        this.colleges = res.data;
        this.filteredColleges = [...this.colleges];
        this.cdr.detectChanges();
      }
    });

    this.apaarService.getNationalStats().subscribe(res => {
      if (res.success) {
        this.totalEnrolled = res.totalEnrolled;
        this.verifiedEnrolled = res.verifiedCount;
        this.duplicateEnrolled = res.duplicateCount;
        this.cdr.detectChanges();
      }
    });

    this.apaarService.getAuditLogs().subscribe(res => {
      if (res.success && res.data) {
        this.auditLogs = res.data;
        this.cdr.detectChanges();
      }
    });

    this.fetchPendingInstitutions();
  }

  fetchPendingInstitutions() {
    this.apaarService.getPendingInstitutions().subscribe(res => {
      if (res.success && res.data) {
        this.pendingInstitutions = res.data;
        this.cdr.detectChanges();
      }
    });
  }

  approveInstitution(userId: number) {
    this.apaarService.approveInstitution(userId).subscribe(() => {
      this.exportToast = '✅ Institution Administrator approved! Account activated.';
      this.fetchPendingInstitutions();
      this.cdr.detectChanges();
      setTimeout(() => { this.exportToast = ''; this.cdr.detectChanges(); }, 3000);
    });
  }

  rejectInstitution(userId: number) {
    this.apaarService.rejectInstitution(userId).subscribe(() => {
      this.exportToast = 'Institution Administrator registration rejected.';
      this.fetchPendingInstitutions();
      this.cdr.detectChanges();
      setTimeout(() => { this.exportToast = ''; this.cdr.detectChanges(); }, 3000);
    });
  }

  filterColleges() {
    this.filteredColleges = this.colleges.filter(c =>
      !this.searchCollege || c.name.toLowerCase().includes(this.searchCollege.toLowerCase()) || c.code.toLowerCase().includes(this.searchCollege.toLowerCase())
    );
  }

  downloadExcel() {
    this.apaarService.downloadExcelReport().subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'AICTE_National_Compliance_Report_2026.xlsx';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);

      this.exportToast = '✅ Apache POI generated government-standard .xlsx report downloaded successfully from Spring Boot backend!';
      this.cdr.detectChanges();
      setTimeout(() => {
        this.exportToast = '';
        this.cdr.detectChanges();
      }, 4000);
    });
  }

  printReport() {
    window.print();
  }
}
