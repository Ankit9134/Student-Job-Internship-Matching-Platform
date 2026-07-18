import { Component, OnInit, inject, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormArray, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { StudentService } from '../../services/student.service';
import { SkillService } from '../../services/skill.service';
import { Skill, StudentProfileRequest } from '../../models/student.model';
import { LucideAngularModule, User, Briefcase, Zap, FileText, Save, AlertCircle, CheckCircle } from 'lucide-angular';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LucideAngularModule],
  templateUrl: './profile.component.html',
})
export class ProfileComponent implements OnInit {
  private fb = inject(FormBuilder);
  private studentService = inject(StudentService);
  private skillService = inject(SkillService);
  private cdr = inject(ChangeDetectorRef);

  readonly UserIcon = User;
  readonly BriefcaseIcon = Briefcase;
  readonly ZapIcon = Zap;
  readonly FileTextIcon = FileText;
  readonly SaveIcon = Save;
  readonly AlertIcon = AlertCircle;
  readonly CheckIcon = CheckCircle;

  allSkills: Skill[] = [];
  loadingProfile = signal(false);
  saving = signal(false);
  saveMessage = '';
  selectedResume: File | null = null;
  existingResumeUrl: string | null = null;

  profileForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    fullName: ['', Validators.required],
    gpa: [null as number | null, [Validators.min(0), Validators.max(10)]],
    gradYear: [null as number | null],
    workAuthStatus: ['CITIZEN', Validators.required],
    needsSponsorship: [false],
    preferredWorkMode: ['ANY'],
    preferredLocations: [''],
    skillIds: this.fb.array<FormControl<number>>([])
  });

  /** Loaded from localStorage after first save; null means student not yet created. */
  studentId: number | null = null;
  studentExists = false;

  ngOnInit(): void {
    this.skillService.list().subscribe(skills => (this.allSkills = skills));

    this.studentId = this.studentService.getSavedStudentId();
    if (this.studentId) {
      this.loadingProfile.set(true);
      this.studentService.getProfile(this.studentId).subscribe({
        next: profile => {
          this.studentExists = true;
          this.loadingProfile.set(false);
          this.profileForm.patchValue({
            email: profile.email,
            fullName: profile.fullName,
            gpa: profile.gpa,
            gradYear: profile.gradYear,
            workAuthStatus: profile.workAuthStatus,
            needsSponsorship: profile.needsSponsorship,
            preferredWorkMode: profile.preferredWorkMode,
            preferredLocations: profile.preferredLocations
          });
          this.setSkillIds(profile.skills.map(s => s.id));
          this.existingResumeUrl = profile.resumeUrl ? this.studentService.getResumeViewUrl(this.studentId!) : null;
        },
        error: () => {
          this.studentExists = false;
          this.studentId = null;
          localStorage.removeItem('studentId');
          this.loadingProfile.set(false);
        }
      });
    }
  }

  get skillIdsArray(): FormArray<FormControl<number>> {
    return this.profileForm.get('skillIds') as FormArray<FormControl<number>>;
  }

  isSkillSelected(skillId: number): boolean {
    return this.skillIdsArray.value.includes(skillId);
  }

  toggleSkill(skillId: number): void {
    const idx = this.skillIdsArray.value.indexOf(skillId);
    if (idx >= 0) {
      this.skillIdsArray.removeAt(idx);
    } else {
      this.skillIdsArray.push(this.fb.control(skillId, { nonNullable: true }));
    }
  }

  private setSkillIds(ids: number[]): void {
    this.skillIdsArray.clear();
    ids.forEach(id => this.skillIdsArray.push(this.fb.control(id, { nonNullable: true })));
  }

  private toastTimer: any;
  toastVisible = false;
  toastType: 'success' | 'error' = 'success';

  showToast(message: string, type: 'success' | 'error'): void {
    this.saveMessage = message;
    this.toastType = type;
    this.toastVisible = true;
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => (this.toastVisible = false), 4000);
  }

  onResumeSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedResume = input.files && input.files.length ? input.files[0] : null;
  }

  save(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.saveMessage = '';
    const saveTimeout = setTimeout(() => {
      if (this.saving()) {
        this.saving.set(false);
        this.showToast('Request timed out. Please try again.', 'error');
      }
    }, 15000);
    const raw = this.profileForm.getRawValue();
    const payload: StudentProfileRequest = {
      email: raw.email!,
      fullName: raw.fullName!,
      gpa: raw.gpa,
      gradYear: raw.gradYear,
      workAuthStatus: raw.workAuthStatus as any,
      needsSponsorship: !!raw.needsSponsorship,
      preferredWorkMode: raw.preferredWorkMode as any,
      preferredLocations: raw.preferredLocations || '',
      skillIds: raw.skillIds as number[]
    };

    const request$ = this.studentExists && this.studentId
      ? this.studentService.updateProfile(this.studentId, payload)
      : this.studentService.createProfile(payload);

    request$.subscribe({
      next: res => {
        clearTimeout(saveTimeout);
        this.studentExists = true;
        this.studentId = res.id;
        this.saving.set(false);
        console.log('SAVE SUCCESS: saving =', this.saving());
        this.showToast('Profile saved — your matches have been recalculated.', 'success');
        if (this.selectedResume && this.studentId) {
          this.studentService.uploadResume(this.studentId, this.selectedResume).subscribe({
            next: () => {
              this.existingResumeUrl = this.studentService.getResumeViewUrl(this.studentId!);
              this.selectedResume = null;
            },
            error: () => this.showToast('Profile saved but resume upload failed.', 'error')
          });
        }
      },
      error: err => {
        clearTimeout(saveTimeout);
        this.saving.set(false);
        this.showToast('Could not save profile: ' + (err?.error?.message || JSON.stringify(err?.error) || 'unknown error'), 'error');
        console.error('Save error full:', err);
      }
    });
  }
}
