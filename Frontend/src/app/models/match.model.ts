import { ApplicationStatus, RoleType, WorkMode } from './student.model';

export interface MatchBreakdown {
  skillScore: number;
  gpaScore: number;
  authScore: number;
  matchedSkills: string[];
  missingSkills: string[];
}

export interface MatchCard {
  listingId: number;
  title: string;
  company: string;
  location: string;
  workMode: WorkMode;
  roleType: RoleType;
  sponsorshipOffered: boolean;
  score: number;
  breakdown: MatchBreakdown;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ApplicationRecord {
  id: number;
  listingId: number;
  listingTitle: string;
  companyName: string;
  status: ApplicationStatus;
  appliedAt: string;
}
