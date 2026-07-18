import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ListingService } from '../../services/listing.service';
import { SkillService } from '../../services/skill.service';
import { Listing } from '../../models/listing.model';
import { Skill } from '../../models/student.model';
import { PagedResponse } from '../../models/match.model';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin.component.html',
})
export class AdminComponent implements OnInit {
  private fb = inject(FormBuilder);
  private listingService = inject(ListingService);
  private skillService = inject(SkillService);

  listings: Listing[] = [];
  allSkills: Skill[] = [];
  loadingListings = false;
  showForm = false;
  saving = false;
  saveMessage = '';
  editingId: number | null = null;

  // Recruiter view
  selectedListing: Listing | null = null;
  topStudents: any[] = [];
  loadingStudents = false;

  // Skill weights: skillId -> 1 (nice) or 2 (required)
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
    this.listingService.browse().subscribe({
      next: l => { this.listings = l; this.loadingListings = false; },
      error: () => (this.loadingListings = false)
    });
  }

  openCreate(): void {
    this.editingId = null;
    this.skillWeights = {};
    this.listingForm.reset({ workMode: 'REMOTE', roleType: 'FULL_TIME', sponsorshipOffered: false });
    this.showForm = true;
    this.saveMessage = '';
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
    this.saveMessage = '';
  }

  cancelForm(): void {
    this.showForm = false;
    this.editingId = null;
  }

  toggleSkillWeight(skillId: number): void {
    const current = this.skillWeights[skillId];
    if (!current) this.skillWeights[skillId] = 2;        // required
    else if (current === 2) this.skillWeights[skillId] = 1; // nice-to-have
    else delete this.skillWeights[skillId];               // remove
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
    this.saveMessage = '';
    const raw = this.listingForm.getRawValue();
    const payload = { ...raw, skillWeights: this.skillWeights } as any;

    const req$ = this.editingId
      ? this.listingService.update(this.editingId, payload)
      : this.listingService.create(payload);

    req$.subscribe({
      next: () => {
        this.saving = false;
        this.saveMessage = this.editingId ? 'Listing updated!' : 'Listing created!';
        this.showForm = false;
        this.editingId = null;
        this.loadListings();
      },
      error: err => {
        this.saving = false;
        this.saveMessage = 'Error: ' + (err?.error?.message || 'unknown');
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
      error: () => (this.loadingStudents = false)
    });
  }

  closeStudents(): void {
    this.selectedListing = null;
    this.topStudents = [];
  }

  scoreColor(score: number): string {
    if (score >= 75) return 'text-emerald-600';
    if (score >= 45) return 'text-amber-600';
    return 'text-red-500';
  }
}
