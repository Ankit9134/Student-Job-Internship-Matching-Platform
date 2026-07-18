export type WorkAuthStatus = 'CITIZEN' | 'PERM_RESIDENT' | 'F1_OPT' | 'H1B_NEEDED' | 'OTHER';
export type WorkMode = 'REMOTE' | 'ONSITE' | 'HYBRID' | 'ANY';
export type RoleType = 'INTERNSHIP' | 'FULL_TIME';
export type ApplicationStatus = 'APPLIED' | 'INTERVIEWING' | 'OFFER' | 'REJECTED';

export interface Skill {
  id: number;
  name: string;
  category?: string;
}

export interface StudentProfileRequest {
  email: string;
  fullName: string;
  gpa: number | null;
  gradYear: number | null;
  workAuthStatus: WorkAuthStatus;
  needsSponsorship: boolean;
  preferredLocations: string;
  preferredWorkMode: WorkMode;
  skillIds: number[];
}

export interface StudentProfileResponse {
  id: number;
  email: string;
  fullName: string;
  gpa: number | null;
  gradYear: number | null;
  workAuthStatus: WorkAuthStatus;
  needsSponsorship: boolean;
  resumeUrl: string | null;
  preferredLocations: string;
  preferredWorkMode: WorkMode;
  skills: Skill[];
}
