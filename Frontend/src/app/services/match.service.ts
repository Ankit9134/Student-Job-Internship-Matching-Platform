import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { MatchCard, PagedResponse } from '../models/match.model';
import { ListingFilters } from '../models/listing.model';

@Injectable({ providedIn: 'root' })
export class MatchService {
  private readonly base = `${environment.apiBaseUrl}/students`;

  constructor(private http: HttpClient) {}

  getMatches(
    studentId: number,
    filters: ListingFilters,
    page = 0,
    size = 10,
    sort: 'score' | 'recent' = 'score'
  ): Observable<PagedResponse<MatchCard>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort);

    if (filters.role) params = params.set('role', filters.role);
    if (filters.location) params = params.set('location', filters.location);
    if (filters.workMode) params = params.set('workMode', filters.workMode);
    if (filters.sponsorshipNeeded !== null && filters.sponsorshipNeeded !== undefined) {
      params = params.set('sponsorshipNeeded', filters.sponsorshipNeeded);
    }

    return this.http.get<PagedResponse<MatchCard>>(`${this.base}/${studentId}/matches`, { params });
  }

  explainMatch(studentId: number, listingId: number): Observable<MatchCard> {
    return this.http.get<MatchCard>(`${this.base}/${studentId}/matches/${listingId}/explain`);
  }
}
