import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { ApiConfigService } from './api-config.service';

export interface DashboardStats {
  totalBookings?: number;
  totalRevenue?: number;
  totalSites?: number;
  activeBookings?: number;
  [key: string]: any;
}

export interface BookingStats {
  totalBookings?: number;
  completedBookings?: number;
  cancelledBookings?: number;
  upcomingBookings?: number;
  [key: string]: any;
}

export interface RevenueStats {
  totalRevenue?: number;
  monthlyRevenue?: any;
  revenueByCategory?: any;
  [key: string]: any;
}

export interface EquipmentStats {
  totalEquipment?: number;
  lowStockItems?: number;
  totalSales?: number;
  topSellingItems?: any[];
  [key: string]: any;
}

export interface UserStats {
  totalUsers?: number;
  activeUsers?: number;
  newUsersThisMonth?: number;
  usersByRole?: any;
  [key: string]: any;
}

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {
  private http = inject(HttpClient);
  private apiConfig = inject(ApiConfigService);
  private apiUrl = this.apiConfig.getEndpointUrl('/analytics');

  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.apiUrl}/dashboard`)
      .pipe(
        catchError(error => this.handleError('getDashboardStats', error))
      );
  }

  getBookingStats(): Observable<BookingStats> {
    return this.http.get<BookingStats>(`${this.apiUrl}/bookings`)
      .pipe(
        catchError(error => this.handleError('getBookingStats', error))
      );
  }

  getRevenueStats(): Observable<RevenueStats> {
    return this.http.get<RevenueStats>(`${this.apiUrl}/revenue`)
      .pipe(
        catchError(error => this.handleError('getRevenueStats', error))
      );
  }

  getEquipmentStats(): Observable<EquipmentStats> {
    return this.http.get<EquipmentStats>(`${this.apiUrl}/equipment`)
      .pipe(
        catchError(error => this.handleError('getEquipmentStats', error))
      );
  }

  getUserStats(): Observable<UserStats> {
    return this.http.get<UserStats>(`${this.apiUrl}/users`)
      .pipe(
        catchError(error => this.handleError('getUserStats', error))
      );
  }

  private handleError(operation: string, error: any) {
    console.error(`${operation} error:`, error);
    const errorMessage = error?.error?.message || error?.message || 'Une erreur est survenue';
    return throwError(() => new Error(errorMessage));
  }
}
