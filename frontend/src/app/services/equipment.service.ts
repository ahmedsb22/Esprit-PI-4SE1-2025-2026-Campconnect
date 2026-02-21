import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { ApiConfigService } from './api-config.service';

export interface Equipment {
  id?: number;
  name: string;
  description?: string;
  category?: string;
  price?: number;
  stock?: number;
  sold?: number;
  images?: string[];
  site?: any;
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class EquipmentService {
  private http = inject(HttpClient);
  private apiConfig = inject(ApiConfigService);
  private apiUrl = this.apiConfig.getEndpointUrl('/equipment');

  getAllEquipment(): Observable<Equipment[]> {
    return this.http.get<Equipment[]>(this.apiUrl)
      .pipe(
        catchError(error => this.handleError('getAllEquipment', error))
      );
  }

  getEquipmentById(id: number): Observable<Equipment> {
    return this.http.get<Equipment>(`${this.apiUrl}/${id}`)
      .pipe(
        catchError(error => this.handleError('getEquipmentById', error))
      );
  }

  getEquipmentByCategory(category: string): Observable<Equipment[]> {
    return this.http.get<Equipment[]>(`${this.apiUrl}/category/${category}`)
      .pipe(
        catchError(error => this.handleError('getEquipmentByCategory', error))
      );
  }

  getAvailableEquipment(): Observable<Equipment[]> {
    return this.http.get<Equipment[]>(`${this.apiUrl}/available`)
      .pipe(
        catchError(error => this.handleError('getAvailableEquipment', error))
      );
  }

  getEquipmentBySite(siteId: number): Observable<Equipment[]> {
    return this.http.get<Equipment[]>(`${this.apiUrl}/site/${siteId}`)
      .pipe(
        catchError(error => this.handleError('getEquipmentBySite', error))
      );
  }

  searchEquipment(name?: string, category?: string, priceMin?: number, priceMax?: number): Observable<Equipment[]> {
    let params = new HttpParams();
    if (name) params = params.set('name', name);
    if (category) params = params.set('category', category);
    if (priceMin !== undefined) params = params.set('priceMin', priceMin.toString());
    if (priceMax !== undefined) params = params.set('priceMax', priceMax.toString());
    
    return this.http.get<Equipment[]>(`${this.apiUrl}/search`, { params })
      .pipe(
        catchError(error => this.handleError('searchEquipment', error))
      );
  }

  createEquipment(equipment: Equipment, siteId?: number): Observable<Equipment> {
    const params = siteId ? `?siteId=${siteId}` : '';
    return this.http.post<Equipment>(`${this.apiUrl}${params}`, equipment)
      .pipe(
        catchError(error => this.handleError('createEquipment', error))
      );
  }

  updateEquipment(id: number, equipment: Partial<Equipment>): Observable<Equipment> {
    return this.http.put<Equipment>(`${this.apiUrl}/${id}`, equipment)
      .pipe(
        catchError(error => this.handleError('updateEquipment', error))
      );
  }

  deleteEquipment(id: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${id}`)
      .pipe(
        catchError(error => this.handleError('deleteEquipment', error))
      );
  }

  updateStock(id: number, quantity: number): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/${id}/stock?quantity=${quantity}`, {})
      .pipe(
        catchError(error => this.handleError('updateStock', error))
      );
  }

  private handleError(operation: string, error: any) {
    console.error(`${operation} error:`, error);
    const errorMessage = error?.error?.message || error?.message || 'Une erreur est survenue';
    return throwError(() => new Error(errorMessage));
  }
}
