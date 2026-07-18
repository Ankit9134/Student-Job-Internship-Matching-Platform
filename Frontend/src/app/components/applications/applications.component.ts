import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApplicationService } from '../../services/application.service';
import { StudentService } from '../../services/student.service';
import { ApplicationRecord } from '../../models/match.model';
import { ApplicationStatus } from '../../models/student.model';
import { LucideAngularModule, ClipboardList, CheckCircle, XCircle, Mic, Package, AlertCircle } from 'lucide-angular';

@Component({
  selector: 'app-applications',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './applications.component.html',
})
export class ApplicationsComponent implements OnInit {
  studentId: number;

  applications: ApplicationRecord[] = [];
  loading = true;
  statuses: ApplicationStatus[] = ['APPLIED', 'INTERVIEWING', 'OFFER', 'REJECTED'];

  readonly ClipboardList = ClipboardList;
  readonly CheckCircle = CheckCircle;
  readonly XCircle = XCircle;
  readonly Mic = Mic;
  readonly Package = Package;
  readonly AlertCircle = AlertCircle;

  constructor(private applicationService: ApplicationService, private studentService: StudentService) {
    this.studentId = this.studentService.getSavedStudentId() ?? 1;
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.applicationService.listForStudent(this.studentId).subscribe({
      next: apps => {
        this.applications = apps;
        this.loading = false;
      },
      error: () => (this.loading = false)
    });
  }

  updateStatus(app: ApplicationRecord, status: ApplicationStatus): void {
    this.applicationService.updateStatus(app.id, status).subscribe(updated => {
      app.status = updated.status;
    });
  }

  statusBadgeClass(status: ApplicationStatus): string {
    const map: Record<ApplicationStatus, string> = {
      APPLIED: 'border-blue-200 text-blue-700 bg-blue-50',
      INTERVIEWING: 'border-amber-200 text-amber-700 bg-amber-50',
      OFFER: 'border-emerald-200 text-emerald-700 bg-emerald-50',
      REJECTED: 'border-red-200 text-red-600 bg-red-50'
    };
    return map[status] ?? '';
  }

  statusLabel(status: ApplicationStatus): string {
    const map: Record<ApplicationStatus, string> = {
      APPLIED: '📨 Applied',
      INTERVIEWING: '🎤 Interviewing',
      OFFER: '🎉 Offer',
      REJECTED: '❌ Rejected'
    };
    return map[status] ?? status;
  }

  statusColor(status: ApplicationStatus): string {
    const map: Record<ApplicationStatus, string> = {
      APPLIED: 'text-blue-600',
      INTERVIEWING: 'text-amber-600',
      OFFER: 'text-emerald-600',
      REJECTED: 'text-red-500'
    };
    return map[status] ?? 'text-slate-700';
  }

  countByStatus(status: ApplicationStatus): number {
    return this.applications.filter(a => a.status === status).length;
  }

  statusClass(status: ApplicationStatus): string {
    return status.toLowerCase();
  }
}
