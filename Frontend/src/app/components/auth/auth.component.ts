import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { LucideGraduationCap, LucideMail, LucideLock, LucideUser, LucideBriefcase, LucideLogIn, LucideUserPlus, LucideAlertCircle } from '@lucide/angular';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LucideGraduationCap, LucideMail, LucideLock, LucideUser, LucideBriefcase, LucideLogIn, LucideUserPlus, LucideAlertCircle],
  templateUrl: './auth.component.html',
})
export class AuthComponent {
  private fb = inject(FormBuilder);
  auth = inject(AuthService);
  private router = inject(Router);

  mode: 'login' | 'signup' = 'login';
  loading = false;
  error = '';

  readonly GraduationCap = LucideGraduationCap;
  readonly Mail = LucideMail;
  readonly Lock = LucideLock;
  readonly User = LucideUser;
  readonly Briefcase = LucideBriefcase;
  readonly LogIn = LucideLogIn;
  readonly UserPlus = LucideUserPlus;
  readonly AlertCircle = LucideAlertCircle;

  form = this.fb.group({
    fullName: [''],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    role: ['STUDENT'],
  });

  switchMode(m: 'login' | 'signup'): void {
    this.mode = m;
    this.error = '';
    this.form.reset({ role: 'STUDENT' });
    if (m === 'signup') this.form.get('fullName')!.setValidators(Validators.required);
    else this.form.get('fullName')!.clearValidators();
    this.form.get('fullName')!.updateValueAndValidity();
  }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;
    this.error = '';
    const { email, password, fullName, role } = this.form.getRawValue();

    const req$ = this.mode === 'login'
      ? this.auth.login({ email: email!, password: password! })
      : this.auth.signup({ email: email!, password: password!, fullName: fullName!, role: role as any });

    req$.subscribe({
      next: res => { this.loading = false; this.router.navigate([res.role === 'RECRUITER' ? '/admin' : '/profile']); },
      error: err => { this.loading = false; this.error = err?.error?.message || (this.mode === 'login' ? 'Invalid credentials' : 'Signup failed. Try again.'); }
    });
  }
}
