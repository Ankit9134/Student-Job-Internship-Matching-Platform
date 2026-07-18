import { Routes } from '@angular/router';
import { ProfileComponent } from './components/profile/profile.component';
import { MatchesComponent } from './components/matches/matches.component';
import { ApplicationsComponent } from './components/applications/applications.component';
import { AdminComponent } from './components/admin/admin.component';

export const routes: Routes = [
  { path: '', redirectTo: 'profile', pathMatch: 'full' },
  { path: 'profile', component: ProfileComponent },
  { path: 'matches', component: MatchesComponent },
  { path: 'applications', component: ApplicationsComponent },
  { path: 'admin', component: AdminComponent },
];
