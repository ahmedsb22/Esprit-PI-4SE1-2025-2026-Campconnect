import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { ApiConfigService } from './api-config.service';

export interface User {
  id?: number;
  firstName: string;
  lastName: string;
  email: string;
  password?: string;
  roles?: string[];
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private http = inject(HttpClient);
  private apiConfig = inject(ApiConfigService);
  private apiUrl = this.apiConfig.getEndpointUrl('/users');

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(this.apiUrl)
      .pipe(
        catchError(error => this.handleError('getAllUsers', error))
      );
  }

  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`)
      .pipe(
        catchError(error => this.handleError('getUserById', error))
      );
  }

  createUser(user: Partial<User>): Observable<User> {
    return this.http.post<User>(this.apiUrl, user)
      .pipe(
        catchError(error => this.handleError('createUser', error))
      );
  }

  updateUser(id: number, user: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}`, user)
      .pipe(
        catchError(error => this.handleError('updateUser', error))
      );
  }

  updateUserStatus(id: number, active: boolean): Observable<{ message: string; user: User }> {
    const params = new HttpParams().set('active', active.toString());
    
    return this.http.put<{ message: string; user: User }>(`${this.apiUrl}/${id}/status`, {}, { params })
      .pipe(
        catchError(error => this.handleError('updateUserStatus', error))
      );
  }

  deleteUser(id: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${id}`)
      .pipe(
        catchError(error => this.handleError('deleteUser', error))
      );
  }

  private handleError(operation: string, error: any) {
    console.error(`${operation} error:`, error);
    const errorMessage = error?.error?.message || error?.message || 'Une erreur est survenue';
    return throwError(() => new Error(errorMessage));
  }
}
