import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, of } from 'rxjs';
import { ApiConfigService } from './api-config.service';

export interface CampingSite {
  id?: number;
  name: string;
  description: string;
  location: string;
  address: string;
  pricePerNight: number;
  capacity: number;
  category?: string;
  imageUrl?: string;
  hasWifi?: boolean;
  hasParking?: boolean;
  hasRestrooms?: boolean;
  hasShowers?: boolean;
  hasElectricity?: boolean;
  hasPetFriendly?: boolean;
  isActive?: boolean;
  isVerified?: boolean;
  rating?: number;
  reviewCount?: number;
  owner?: any;
  amenities?: { name: string }[];
  images?: { url: string }[];
}

@Injectable({
  providedIn: 'root'
})
export class SiteService {
  private http = inject(HttpClient);
  private apiConfig = inject(ApiConfigService);
  private apiUrl = this.apiConfig.getEndpointUrl('/sites');

  getAllSites(): Observable<CampingSite[]> {
    return this.http.get<CampingSite[]>(this.apiUrl);
  }

  /** Falls back to getAllSites() if /active endpoint doesn't exist */
  getActiveSites(): Observable<CampingSite[]> {
    return this.http.get<CampingSite[]>(`${this.apiUrl}/active`).pipe(
      catchError(() => this.getAllSites())
    );
  }

  /**
   * Search with optional filters.
   * Falls back to getAllSites() if /search endpoint doesn't exist.
   */
  searchSites(
    category?: string,
    location?: string,
    minPrice?: number,
    maxPrice?: number
  ): Observable<CampingSite[]> {
    let params = new HttpParams();
    if (category) params = params.set('category', category);
    if (location)  params = params.set('location', location);
    if (minPrice != null) params = params.set('minPrice', String(minPrice));
    if (maxPrice != null) params = params.set('maxPrice', String(maxPrice));

    return this.http.get<CampingSite[]>(`${this.apiUrl}/search`, { params }).pipe(
      catchError(() => this.getAllSites())
    );
  }

  getSiteById(id: number): Observable<CampingSite> {
    return this.http.get<CampingSite>(`${this.apiUrl}/${id}`);
  }

  createSite(site: CampingSite): Observable<CampingSite> {
    return this.http.post<CampingSite>(this.apiUrl, site);
  }

  updateSite(id: number, site: CampingSite): Observable<CampingSite> {
    return this.http.put<CampingSite>(`${this.apiUrl}/${id}`, site);
  }

  deleteSite(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
