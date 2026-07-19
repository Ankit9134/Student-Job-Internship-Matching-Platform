import { RoleType, Skill, WorkMode } from './student.model';

export interface Listing {
  id: number;
  title: string;
  companyName: string;
  description: string;
  location: string;
  workMode: WorkMode;
  minGpa: number | null;
  sponsorshipOffered: boolean;
  roleType: RoleType;
  requiredSkills: Skill[];
  niceToHaveSkills: Skill[];
  createdAt: string | null;
}

export interface ListingFilters {
  role?: string;
  location?: string;
  workMode?: WorkMode | '';
  sponsorshipNeeded?: boolean | null;
}
