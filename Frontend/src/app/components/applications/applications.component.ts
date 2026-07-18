import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApplicationService } from '../../services/application.service';
import { ApplicationRecord } from '../../models/match.model';
import { ApplicationStatus } from '../../models/student.model';

@Component({
  selector: 'app-applications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './applications.component.html',
  styleUrls: ['./applications.component.scss']
})
export class ApplicationsComponent implements OnInit {
  studentId = 1; // demo student - swap for the authenticated user's id

  applications: ApplicationRecord[] = [];
  loading = false;
  statuses: ApplicationStatus[] = ['APPLIED', 'INTERVIEWING', 'OFFER', 'REJECTED'];

  constructor(private applicationService: ApplicationService) {}

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

  statusClass(status: ApplicationStatus): string {
    return status.toLowerCase();
  }
}
