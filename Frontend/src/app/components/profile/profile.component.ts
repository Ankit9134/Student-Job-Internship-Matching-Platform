import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormArray, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { StudentService } from '../../services/student.service';
import { SkillService } from '../../services/skill.service';
import { Skill, StudentProfileRequest } from '../../models/student.model';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile.component.html',
})
export class ProfileComponent implements OnInit {
  private fb = inject(FormBuilder);
  private studentService = inject(StudentService);
  private skillService = inject(SkillService);

  allSkills: Skill[] = [];
  saving = false;
  saveMessage = '';
  selectedResume: File | null = null;

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
      this.studentService.getProfile(this.studentId).subscribe({
        next: profile => {
          this.studentExists = true;
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
        },
        error: () => { this.studentExists = false; }
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

  onResumeSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedResume = input.files && input.files.length ? input.files[0] : null;
  }

  save(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    this.saving = true;
    this.saveMessage = '';
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
        this.studentExists = true;
        this.studentId = res.id;
        this.saving = false;
        this.saveMessage = 'Profile saved — your matches have been recalculated.';
        if (this.selectedResume && this.studentId) {
          this.studentService.uploadResume(this.studentId, this.selectedResume).subscribe();
        }
      },
      error: err => {
        this.saving = false;
        this.saveMessage = 'Could not save profile: ' + (err?.error?.message || JSON.stringify(err?.error) || 'unknown error');
        console.error('Save error:', err?.error);
      }
    });
  }
}
