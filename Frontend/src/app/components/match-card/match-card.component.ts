import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatchScorePipe } from '../../pipes/match-score.pipe';
import { MatchCard } from '../../models/match.model';
import { LucideAngularModule, MapPin, Monitor, Home, Building2, GraduationCap, Briefcase, CheckCircle, XCircle, ChevronDown, ChevronUp, Zap, TrendingUp, Shield, Loader } from 'lucide-angular';

@Component({
  selector: 'app-match-card',
  standalone: true,
  imports: [CommonModule, MatchScorePipe, LucideAngularModule],
  templateUrl: './match-card.component.html',
})
export class MatchCardComponent {
  @Input({ required: true }) match!: MatchCard;
  @Input() alreadyApplied = false;
  @Input() loading = false;
  @Output() applyClicked = new EventEmitter<number>();

  showBreakdown = false;

  readonly MapPin = MapPin;
  readonly Monitor = Monitor;
  readonly Home = Home;
  readonly Building2 = Building2;
  readonly GraduationCap = GraduationCap;
  readonly Briefcase = Briefcase;
  readonly CheckCircle = CheckCircle;
  readonly XCircle = XCircle;
  readonly ChevronDown = ChevronDown;
  readonly ChevronUp = ChevronUp;
  readonly Zap = Zap;
  readonly TrendingUp = TrendingUp;
  readonly Shield = Shield;
  readonly Loader = Loader;

  toggleBreakdown() { this.showBreakdown = !this.showBreakdown; }
  onApply() { this.applyClicked.emit(this.match.listingId); }

  scoreTier(): 'high' | 'mid' | 'low' {
    if (this.match.score >= 75) return 'high';
    if (this.match.score >= 45) return 'mid';
    return 'low';
  }

  workModeIcon() {
    if (this.match.workMode === 'REMOTE') return this.Home;
    if (this.match.workMode === 'ONSITE') return this.Building2;
    return this.Monitor;
  }
}
