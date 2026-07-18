import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged, catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { MatchService } from '../../services/match.service';
import { ApplicationService } from '../../services/application.service';
import { StudentService } from '../../services/student.service';
import { AuthService } from '../../services/auth.service';
import { MatchCardComponent } from '../match-card/match-card.component';
import { MatchCard } from '../../models/match.model';
import { LucideAngularModule, Filter, TrendingUp, Clock, Search, AlertCircle, ChevronLeft, ChevronRight } from 'lucide-angular';

@Component({
  selector: 'app-matches',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatchCardComponent, LucideAngularModule],
  templateUrl: './matches.component.html',
})
export class MatchesComponent implements OnInit {
  private fb = inject(FormBuilder);
  private matchService = inject(MatchService);
  private applicationService = inject(ApplicationService);
  private studentService = inject(StudentService);

  private authService = inject(AuthService);

  readonly FilterIcon = Filter;
  readonly TrendingUpIcon = TrendingUp;
  readonly ClockIcon = Clock;
  readonly SearchIcon = Search;
  readonly AlertIcon = AlertCircle;
  readonly ChevronLeftIcon = ChevronLeft;
  readonly ChevronRightIcon = ChevronRight;

  studentId: number | null = null;

  matches: MatchCard[] = [];
  appliedListingIds = new Set<number>();
  loading = true;
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
    this.studentId = this.studentService.getSavedStudentId();

    const load = (id: number) => {
      this.studentId = id;
      this.matchService.getMatches(id, {}, 0, this.pageSize, this.sort).subscribe({
        next: res => { this.matches = res.content; this.totalPages = res.totalPages; this.loading = false; },
        error: err => { this.error = 'Could not load matches: ' + (err?.error?.message || 'unknown error'); this.loading = false; }
      });
      this.applicationService.listForStudent(id).pipe(catchError(() => of([]))).subscribe(applied => {
        this.appliedListingIds = new Set(applied.map((a: any) => a.listingId));
      });
    };

    if (this.studentId) {
      load(this.studentId);
    } else {
      this.authService.me().subscribe({
        next: res => {
          if (res.studentId) { load(res.studentId); }
          else { this.error = 'Please save your profile first.'; this.loading = false; }
        },
        error: () => { this.error = 'Please save your profile first.'; this.loading = false; }
      });
    }

    this.filterForm.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)))
      .subscribe(() => { this.page = 0; this.fetchMatches(); });
  }

  fetchMatches(): void {
    if (!this.studentId) return;
    this.loading = true;
    this.error = '';
    const filters = this.filterForm.value as any;

    this.matchService.getMatches(this.studentId!, filters, this.page, this.pageSize, this.sort).subscribe({
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
    if (!this.studentId) return;
    this.applicationService.listForStudent(this.studentId).subscribe(apps => {
      this.appliedListingIds = new Set(apps.map(a => a.listingId));
    });
  }
  onApply(listingId: number): void {
    this.applicationService.markApplied(this.studentId!, listingId).subscribe({
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
