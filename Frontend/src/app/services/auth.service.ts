import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';

export interface AuthResponse {
  token: string;
  role: string;
  fullName: string;
  userId: number;
  studentId: number | null;
}

export interface SignupRequest {
  email: string;
  password: string;
  fullName: string;
  role: 'STUDENT' | 'RECRUITER';
}

export interface LoginRequest {
  email: string;
  password: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly base = `${environment.apiBaseUrl}/auth`;

  constructor(private http: HttpClient, private router: Router) {}

  signup(req: SignupRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.base}/signup`, req).pipe(
      tap(res => this.store(res))
    );
  }

  login(req: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.base}/login`, req).pipe(
      tap(res => this.store(res))
    );
  }

  private store(res: AuthResponse): void {
    localStorage.setItem('token', res.token);
    localStorage.setItem('role', res.role);
    localStorage.setItem('fullName', res.fullName);
    localStorage.setItem('userId', String(res.userId));
    if (res.studentId) {
      localStorage.setItem('studentId', String(res.studentId));
    } else {
      localStorage.removeItem('studentId');
    }
  }

  logout(): void {
    localStorage.clear();
    this.router.navigate(['/login']);
  }

  getToken(): string | null { return localStorage.getItem('token'); }
  getRole(): string | null { return localStorage.getItem('role'); }
  getFullName(): string | null { return localStorage.getItem('fullName'); }
  isLoggedIn(): boolean { return !!this.getToken(); }
  isStudent(): boolean { return this.getRole() === 'STUDENT'; }
  isRecruiter(): boolean { return this.getRole() === 'RECRUITER'; }
}
