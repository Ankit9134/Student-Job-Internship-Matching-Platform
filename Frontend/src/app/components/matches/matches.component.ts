import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { MatchService } from '../../services/match.service';
import { ApplicationService } from '../../services/application.service';
import { MatchCardComponent } from '../match-card/match-card.component';
import { MatchCard } from '../../models/match.model';

@Component({
  selector: 'app-matches',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatchCardComponent],
  templateUrl: './matches.component.html',
  styleUrls: ['./matches.component.scss']
})
export class MatchesComponent implements OnInit {
  private fb = inject(FormBuilder);
  private matchService = inject(MatchService);
  private applicationService = inject(ApplicationService);

  studentId = 1; // demo student - swap for the authenticated user's id

  matches: MatchCard[] = [];
  appliedListingIds = new Set<number>();
  loading = false;
  error = '';

  page = 0;
  pageSize = 10;
  totalPages = 0;
  sort: 'score' | 'recent' = 'score';

  filterForm = this.fb.group({
    role: [''],
    location: [''],
    workMode: [''],
    sponsorshipNeeded: [null as boolean | null]
  });

  constructor() {}

  ngOnInit(): void {
    this.loadApplied();
    this.fetchMatches();

    this.filterForm.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)))
      .subscribe(() => {
        this.page = 0;
        this.fetchMatches();
      });
  }

  fetchMatches(): void {
    this.loading = true;
    this.error = '';
    const filters = this.filterForm.value as any;

    this.matchService.getMatches(this.studentId, filters, this.page, this.pageSize, this.sort).subscribe({
      next: res => {
        this.matches = res.content;
        this.totalPages = res.totalPages;
        this.loading = false;
      },
      error: err => {
        this.error = 'Could not load matches: ' + (err?.error?.message || 'unknown error');
        this.loading = false;
      }
    });
  }

  private loadApplied(): void {
    this.applicationService.listForStudent(this.studentId).subscribe(apps => {
      this.appliedListingIds = new Set(apps.map(a => a.listingId));
    });
  }

  onApply(listingId: number): void {
    this.applicationService.markApplied(this.studentId, listingId).subscribe({
      next: () => this.appliedListingIds.add(listingId),
      error: () => { /* already applied or listing unavailable - no-op for MVP */ }
    });
  }

  setSort(sort: 'score' | 'recent'): void {
    this.sort = sort;
    this.page = 0;
    this.fetchMatches();
  }

  nextPage(): void {
    if (this.page + 1 < this.totalPages) {
      this.page++;
      this.fetchMatches();
    }
  }

  prevPage(): void {
    if (this.page > 0) {
      this.page--;
      this.fetchMatches();
    }
  }
}
