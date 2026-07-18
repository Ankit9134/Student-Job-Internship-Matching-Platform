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
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {
  private fb = inject(FormBuilder);
  private studentService = inject(StudentService);
  private skillService = inject(SkillService);

  /** Demo student id - in a real app this comes from the authenticated session. */
  studentId = 1;

  allSkills: Skill[] = [];
  saving = false;
  saveMessage = '';
  selectedResume: File | null = null;

  profileForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    fullName: ['', Validators.required],
    gpa: [null as number | null, [Validators.min(0), Validators.max(4)]],
    gradYear: [null as number | null],
    workAuthStatus: ['CITIZEN', Validators.required],
    needsSponsorship: [false],
    preferredWorkMode: ['ANY'],
    preferredLocations: [''],
    skillIds: this.fb.array<FormControl<number>>([])
  });

  constructor() {}

  ngOnInit(): void {
    this.skillService.list().subscribe(skills => (this.allSkills = skills));

    this.studentService.getProfile(this.studentId).subscribe({
      next: profile => {
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
      error: () => {
        // No existing profile yet - that's fine, the form starts empty for a new student.
      }
    });
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

    this.studentService.updateProfile(this.studentId, payload).subscribe({
      next: () => {
        this.saving = false;
        this.saveMessage = 'Profile saved — your matches have been recalculated.';
        if (this.selectedResume) {
          this.studentService.uploadResume(this.studentId, this.selectedResume).subscribe();
        }
      },
      error: err => {
        this.saving = false;
        this.saveMessage = 'Could not save profile: ' + (err?.error?.message || 'unknown error');
      }
    });
  }
}
