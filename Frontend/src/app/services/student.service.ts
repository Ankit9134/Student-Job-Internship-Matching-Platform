import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { StudentProfileRequest, StudentProfileResponse } from '../models/student.model';

@Injectable({ providedIn: 'root' })
export class StudentService {
  private readonly base = `${environment.apiBaseUrl}/students`;

  constructor(private http: HttpClient) {}

  createProfile(req: StudentProfileRequest): Observable<StudentProfileResponse> {
    return this.http.post<StudentProfileResponse>(this.base, req);
  }

  updateProfile(studentId: number, req: StudentProfileRequest): Observable<StudentProfileResponse> {
    return this.http.put<StudentProfileResponse>(`${this.base}/${studentId}/profile`, req);
  }

  getProfile(studentId: number): Observable<StudentProfileResponse> {
    return this.http.get<StudentProfileResponse>(`${this.base}/${studentId}`);
  }

  uploadResume(studentId: number, file: File): Observable<StudentProfileResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<StudentProfileResponse>(`${this.base}/${studentId}/resume`, formData);
  }
}
