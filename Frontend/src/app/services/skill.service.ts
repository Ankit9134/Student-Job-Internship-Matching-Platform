import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Skill } from '../models/student.model';

@Injectable({ providedIn: 'root' })
export class SkillService {
  private readonly base = `${environment.apiBaseUrl}/skills`;

  constructor(private http: HttpClient) {}

  list(): Observable<Skill[]> {
    return this.http.get<Skill[]>(this.base);
  }
}
