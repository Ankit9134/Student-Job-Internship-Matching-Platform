import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Listing } from '../models/listing.model';
import { PagedResponse, MatchCard } from '../models/match.model';

@Injectable({ providedIn: 'root' })
export class ListingService {
  private readonly base = `${environment.apiBaseUrl}/listings`;

  constructor(private http: HttpClient) {}

  browse(page = 0, size = 20): Observable<Listing[]> {
    return this.http.get<Listing[]>(this.base, { params: { page, size } as any });
  }

  get(id: number): Observable<Listing> {
    return this.http.get<Listing>(`${this.base}/${id}`);
  }

  create(payload: Partial<Listing> & { skillWeights: Record<number, number> }): Observable<Listing> {
    return this.http.post<Listing>(this.base, payload);
  }

  update(id: number, payload: Partial<Listing> & { skillWeights: Record<number, number> }): Observable<Listing> {
    return this.http.put<Listing>(`${this.base}/${id}`, payload);
  }

  /** Recruiter-side (stretch): top matching students for a listing. */
  getMatchingStudents(listingId: number, page = 0, size = 10) {
    return this.http.get<PagedResponse<any>>(`${this.base}/${listingId}/matches`, {
      params: { page, size } as any
    });
  }
}
