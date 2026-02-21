import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, catchError, throwError } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiConfigService } from './api-config.service';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  roles?: string[];
}

export interface AuthResponse {
  token: string;
  user: {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    roles?: string[];
    role?: string;
  };
}

export interface User {
  id?: number;
  firstName: string;
  lastName: string;
  email: string;
  roles?: string[];
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private apiConfig = inject(ApiConfigService);
  private apiUrl = this.apiConfig.getEndpointUrl('/auth');
  private tokenKey = 'campconnect_token';
  private userKey = 'campconnect_user';
  private authStatus$ = new BehaviorSubject<boolean>(this.isAuthenticated());

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap(response => {
          this.setToken(response.token);
          this.setUser(response.user);
          this.authStatus$.next(true);
        }),
        catchError(error => this.handleError('login', error))
      );
  }

  register(data: RegisterRequest): Observable<AuthResponse> {
    const payload: any = {
      firstName: data.firstName,
      lastName: data.lastName,
      email: data.email,
      password: data.password,
      roles: data.roles ?? ['CAMPER'],
      active: true
    };
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, payload)
      .pipe(
        tap(response => {
          this.setToken(response.token);
          this.setUser(response.user);
          this.authStatus$.next(true);
        }),
        catchError(error => this.handleError('register', error))
      );
  }

  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/profile`)
      .pipe(
        catchError(error => this.handleError('getCurrentUser', error))
      );
  }

  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/user/${id}`)
      .pipe(
        catchError(error => this.handleError('getUserById', error))
      );
  }

  updateProfile(id: number, user: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/profile/${id}`, user)
      .pipe(
        tap(response => this.setUser(response)),
        catchError(error => this.handleError('updateProfile', error))
      );
  }

  changePassword(oldPassword: string, newPassword: string): Observable<{ message: string }> {
    const passwordRequest = {
      oldPassword,
      newPassword
    };
    
    return this.http.post<{ message: string }>(`${this.apiUrl}/change-password`, passwordRequest)
      .pipe(
        catchError(error => this.handleError('changePassword', error))
      );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    this.authStatus$.next(false);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getUser(): any {
    const userStr = localStorage.getItem(this.userKey);
    return userStr ? JSON.parse(userStr) : null;
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  isAdmin(): boolean {
    const user = this.getUser();
    const roles = user?.roles ?? (user?.role ? [user.role] : []);
    return roles.includes('ADMIN');
  }

  isProvider(): boolean {
    const user = this.getUser();
    const roles = user?.roles ?? (user?.role ? [user.role] : []);
    return roles.includes('PROVIDER');
  }

  isCamper(): boolean {
    const user = this.getUser();
    const roles = user?.roles ?? (user?.role ? [user.role] : []);
    return roles.includes('CAMPER');
  }

  getAuthStatus(): Observable<boolean> {
    return this.authStatus$.asObservable();
  }

  private setToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  private setUser(user: any): void {
    localStorage.setItem(this.userKey, JSON.stringify(user));
  }

  private handleError(operation: string, error: any) {
    console.error(`${operation} error:`, error);
    const errorMessage = error?.error?.message || error?.message || 'Une erreur est survenue';
    return throwError(() => new Error(errorMessage));
  }
}
