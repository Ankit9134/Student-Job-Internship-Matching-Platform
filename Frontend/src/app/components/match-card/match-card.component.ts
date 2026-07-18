import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatchScorePipe } from '../../pipes/match-score.pipe';
import { MatchCard } from '../../models/match.model';

@Component({
  selector: 'app-match-card',
  standalone: true,
  imports: [CommonModule, MatchScorePipe],
  templateUrl: './match-card.component.html',
})
export class MatchCardComponent {
  @Input({ required: true }) match!: MatchCard;
  @Input() alreadyApplied = false;
  @Output() applyClicked = new EventEmitter<number>();

  showBreakdown = false;

  toggleBreakdown(): void {
    this.showBreakdown = !this.showBreakdown;
  }

  onApply(): void {
    this.applyClicked.emit(this.match.listingId);
  }

  scoreTier(): 'high' | 'mid' | 'low' {
    if (this.match.score >= 75) return 'high';
    if (this.match.score >= 45) return 'mid';
    return 'low';
  }
}
