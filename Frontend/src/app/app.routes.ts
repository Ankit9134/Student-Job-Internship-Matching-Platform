import { Routes } from '@angular/router';
import { studentGuard, recruiterGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', loadComponent: () => import('./components/auth/auth.component').then(m => m.AuthComponent) },
  { path: 'profile', loadComponent: () => import('./components/profile/profile.component').then(m => m.ProfileComponent), canActivate: [studentGuard] },
  { path: 'matches', loadComponent: () => import('./components/matches/matches.component').then(m => m.MatchesComponent), canActivate: [studentGuard] },
  { path: 'applications', loadComponent: () => import('./components/applications/applications.component').then(m => m.ApplicationsComponent), canActivate: [studentGuard] },
  { path: 'admin', loadComponent: () => import('./components/admin/admin.component').then(m => m.AdminComponent), canActivate: [recruiterGuard] },
  { path: '**', redirectTo: 'login' },
];
