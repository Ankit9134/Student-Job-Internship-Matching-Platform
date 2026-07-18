import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApplicationRecord } from '../models/match.model';
import { ApplicationStatus } from '../models/student.model';

@Injectable({ providedIn: 'root' })
export class ApplicationService {
  constructor(private http: HttpClient) {}

  markApplied(studentId: number, listingId: number): Observable<ApplicationRecord> {
    return this.http.post<ApplicationRecord>(
      `${environment.apiBaseUrl}/students/${studentId}/applications`,
      { listingId }
    );
  }

  listForStudent(studentId: number): Observable<ApplicationRecord[]> {
    return this.http.get<ApplicationRecord[]>(`${environment.apiBaseUrl}/students/${studentId}/applications`);
  }

  updateStatus(applicationId: number, status: ApplicationStatus): Observable<ApplicationRecord> {
    return this.http.patch<ApplicationRecord>(
      `${environment.apiBaseUrl}/applications/${applicationId}`,
      { status }
    );
  }
}
