import { Pipe, PipeTransform } from '@angular/core';

export type MatchScoreFormat = 'percent' | 'stars' | 'label';

@Pipe({
  name: 'matchScore',
  standalone: true
})
export class MatchScorePipe implements PipeTransform {
  transform(score: number | null | undefined, format: MatchScoreFormat = 'percent'): string {
    if (score === null || score === undefined || isNaN(score)) {
      return format === 'stars' ? '☆☆☆☆☆' : 'N/A';
    }

    const clamped = Math.max(0, Math.min(100, score));

    switch (format) {
      case 'stars': {
        const stars = Math.round(clamped / 20); // 0-100 -> 0-5
        return '★'.repeat(stars) + '☆'.repeat(5 - stars);
      }
      case 'label':
        return this.label(clamped);
      case 'percent':
      default:
        return `${Math.round(clamped)}% match`;
    }
  }

  private label(score: number): string {
    if (score >= 85) return 'Excellent match';
    if (score >= 65) return 'Strong match';
    if (score >= 45) return 'Partial match';
    return 'Weak match';
  }
}
