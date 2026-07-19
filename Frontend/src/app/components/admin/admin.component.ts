import { Component, OnInit, inject, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ListingService } from '../../services/listing.service';
import { SkillService } from '../../services/skill.service';
import { Listing } from '../../models/listing.model';
import { Skill } from '../../models/student.model';
import { PagedResponse } from '../../models/match.model';
import { LucideAngularModule, Plus, Pencil, Users, X, Briefcase, MapPin, Building2, GraduationCap, CheckCircle, XCircle, ClipboardList, Trash2, AlertTriangle, Loader, UserCheck } from 'lucide-angular';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LucideAngularModule],
  templateUrl: './admin.component.html',
})
export class AdminComponent implements OnInit {
  private fb = inject(FormBuilder);
  private listingService = inject(ListingService);
  private skillService = inject(SkillService);

  listings: Listing[] = [];
  allSkills: Skill[] = [];
  loadingListings = true;
  showForm = false;
  saving = false;
  editingId: number | null = null;
  deletingId: number | null = null;
  confirmDeleteId: number | null = null;

  // Toast
  toast: { message: string; type: 'success' | 'error' } | null = null;
  private toastTimer: any;

  // Recruiter view
  selectedListing: Listing | null = null;
  topStudents: any[] = [];
  loadingStudents = false;

  selectedStudent: any = null;
  applicantCounts: Record<number, number> = {};

  // Applicants panel
  applicantsListing: Listing | null = null;
  applicants: any[] = [];
  loadingApplicants = false;
  selectedApplicant: any = null;

  readonly PlusIcon = Plus;
  readonly PencilIcon = Pencil;
  readonly UsersIcon = Users;
  readonly XIcon = X;
  readonly BriefcaseIcon = Briefcase;
  readonly MapPinIcon = MapPin;
  readonly Building2Icon = Building2;
  readonly GraduationCapIcon = GraduationCap;
  readonly CheckCircleIcon = CheckCircle;
  readonly XCircleIcon = XCircle;
  readonly ClipboardListIcon = ClipboardList;
  readonly Trash2Icon = Trash2;
  readonly AlertTriangleIcon = AlertTriangle;
  readonly LoaderIcon = Loader;
  readonly UserCheckIcon = UserCheck;

  @ViewChild('formSection') formSection!: ElementRef;

  skillWeights: Record<number, number> = {};

  listingForm = this.fb.group({
    title: ['', Validators.required],
    companyName: ['', Validators.required],
    description: [''],
    location: [''],
    workMode: ['REMOTE', Validators.required],
    minGpa: [null as number | null],
    sponsorshipOffered: [false],
    roleType: ['FULL_TIME', Validators.required],
  });

  ngOnInit(): void {
    this.loadListings();
    this.skillService.list().subscribe(s => (this.allSkills = s));
  }

  loadListings(): void {
    this.loadingListings = true;
    forkJoin({
      listings: this.listingService.browse(),
      counts: this.listingService.getAllApplicantCounts()
    }).subscribe({
      next: ({ listings, counts }) => {
        this.listings = listings;
        this.applicantCounts = counts;
        this.loadingListings = false;
      },
      error: () => {
        this.loadingListings = false;
        this.showToast('Failed to load listings', 'error');
      }
    });
  }

  showToast(message: string, type: 'success' | 'error'): void {
    clearTimeout(this.toastTimer);
    this.toast = { message, type };
    this.toastTimer = setTimeout(() => (this.toast = null), 3000);
  }

  openCreate(): void {
    this.editingId = null;
    this.skillWeights = {};
    this.listingForm.reset({ workMode: 'REMOTE', roleType: 'FULL_TIME', sponsorshipOffered: false });
    this.showForm = true;
  }

  openEdit(listing: Listing): void {
    this.editingId = listing.id;
    this.skillWeights = {};
    listing.requiredSkills?.forEach(s => (this.skillWeights[s.id] = 2));
    listing.niceToHaveSkills?.forEach(s => (this.skillWeights[s.id] = 1));
    this.listingForm.patchValue({
      title: listing.title,
      companyName: listing.companyName,
      description: listing.description,
      location: listing.location,
      workMode: listing.workMode,
      minGpa: listing.minGpa,
      sponsorshipOffered: listing.sponsorshipOffered,
      roleType: listing.roleType,
    });
    this.showForm = true;
    setTimeout(() => this.formSection?.nativeElement?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 50);
  }

  confirmDelete(id: number): void {
    this.confirmDeleteId = id;
  }

  cancelDelete(): void {
    this.confirmDeleteId = null;
  }

  deleteListing(id: number): void {
    this.deletingId = id;
    this.confirmDeleteId = null;
    this.listingService.delete(id).subscribe({
      next: () => {
        this.listings = this.listings.filter(l => l.id !== id);
        if (this.selectedListing?.id === id) this.closeStudents();
        this.deletingId = null;
        this.showToast('Listing deleted successfully', 'success');
      },
      error: err => {
        this.deletingId = null;
        this.showToast('Delete failed: ' + (err?.error?.message || 'Unknown error'), 'error');
      }
    });
  }

  cancelForm(): void {
    this.showForm = false;
    this.editingId = null;
  }

  toggleSkillWeight(skillId: number): void {
    const current = this.skillWeights[skillId];
    if (!current) this.skillWeights[skillId] = 2;
    else if (current === 2) this.skillWeights[skillId] = 1;
    else delete this.skillWeights[skillId];
  }

  skillWeightLabel(skillId: number): string {
    const w = this.skillWeights[skillId];
    if (w === 2) return 'Required';
    if (w === 1) return 'Nice';
    return '';
  }

  skillButtonClass(skillId: number): string {
    const w = this.skillWeights[skillId];
    if (w === 2) return 'px-3 py-1.5 rounded-full text-xs font-semibold bg-indigo-600 text-white border border-indigo-600 cursor-pointer transition-all';
    if (w === 1) return 'px-3 py-1.5 rounded-full text-xs font-semibold bg-amber-400 text-white border border-amber-400 cursor-pointer transition-all';
    return 'px-3 py-1.5 rounded-full text-xs font-medium bg-white text-slate-600 border border-slate-300 hover:border-indigo-400 cursor-pointer transition-all';
  }

  save(): void {
    if (this.listingForm.invalid) { this.listingForm.markAllAsTouched(); return; }
    this.saving = true;
    const raw = this.listingForm.getRawValue();
    const payload = { ...raw, skillWeights: this.skillWeights } as any;
    const wasEditing = this.editingId;

    const req$ = wasEditing
      ? this.listingService.update(wasEditing, payload)
      : this.listingService.create(payload);

    req$.subscribe({
      next: (saved: Listing) => {
        this.saving = false;
        if (wasEditing) {
          this.listings = this.listings.map(l => l.id === wasEditing ? saved : l);
          this.showToast('Listing updated successfully', 'success');
        } else {
          this.listings = [saved, ...this.listings];
          this.showToast('Listing created successfully', 'success');
        }
        this.showForm = false;
        this.editingId = null;
      },
      error: err => {
        this.saving = false;
        this.showToast('Save failed: ' + (err?.error?.message || 'Unknown error'), 'error');
      }
    });
  }

  viewTopStudents(listing: Listing): void {
    this.selectedListing = listing;
    this.loadingStudents = true;
    this.topStudents = [];
    this.listingService.getMatchingStudents(listing.id).subscribe({
      next: (res: PagedResponse<any>) => {
        this.topStudents = res.content;
        this.loadingStudents = false;
      },
      error: () => {
        this.loadingStudents = false;
        this.showToast('Failed to load students', 'error');
      }
    });
  }

  closeStudents(): void {
    this.selectedListing = null;
    this.topStudents = [];
  }

  viewApplicants(listing: Listing): void {
    this.applicantsListing = listing;
    this.loadingApplicants = true;
    this.applicants = [];
    this.listingService.getApplicants(listing.id).subscribe({
      next: (data) => { this.applicants = data; this.loadingApplicants = false; },
      error: () => { this.loadingApplicants = false; this.showToast('Failed to load applicants', 'error'); }
    });
  }

  closeApplicants(): void {
    this.applicantsListing = null;
    this.applicants = [];
    this.selectedApplicant = null;
  }

  statusClass(status: string): string {
    const map: Record<string, string> = {
      APPLIED: 'bg-blue-50 text-blue-700 border-blue-100',
      INTERVIEWING: 'bg-amber-50 text-amber-700 border-amber-100',
      OFFER: 'bg-emerald-50 text-emerald-700 border-emerald-100',
      REJECTED: 'bg-red-50 text-red-600 border-red-100',
    };
    return map[status] ?? 'bg-slate-100 text-slate-600';
  }

  scoreColor(score: number): string {
    if (score >= 75) return 'text-emerald-600';
    if (score >= 45) return 'text-amber-600';
    return 'text-red-500';
  }
}
