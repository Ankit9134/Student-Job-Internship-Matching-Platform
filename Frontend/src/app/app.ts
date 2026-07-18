import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './services/auth.service';
import {
  LucideGraduationCap, LucideUser, LucideSparkles, LucideClipboardList,
  LucideWrench, LucideLogOut, LucideMenu, LucideX
} from '@lucide/angular';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet, RouterLink, RouterLinkActive, CommonModule,
    LucideGraduationCap, LucideUser, LucideSparkles, LucideClipboardList,
    LucideWrench, LucideLogOut, LucideMenu, LucideX
  ],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  auth = inject(AuthService);
  mobileMenuOpen = signal(false);
  toggleMenu() { this.mobileMenuOpen.update(v => !v); }
  closeMenu() { this.mobileMenuOpen.set(false); }
}
