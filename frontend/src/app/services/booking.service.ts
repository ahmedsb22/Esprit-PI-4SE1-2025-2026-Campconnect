import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { ApiConfigService } from './api-config.service';

export interface Booking {
  id?: number;
  bookingNumber?: string;
  camper?: any;
  site?: any;
  startDate: string;
  endDate: string;
  guests: number;
  totalPrice?: number;
  status?: string;
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class BookingService {
  private http = inject(HttpClient);
  private apiConfig = inject(ApiConfigService);
  private apiUrl = this.apiConfig.getEndpointUrl('/bookings');

  getAllBookings(): Observable<Booking[]> {
    return this.http.get<Booking[]>(this.apiUrl)
      .pipe(
        catchError(error => this.handleError('getAllBookings', error))
      );
  }

  getBookingById(id: number): Observable<Booking> {
    return this.http.get<Booking>(`${this.apiUrl}/${id}`)
      .pipe(
        catchError(error => this.handleError('getBookingById', error))
      );
  }

  getMyBookings(userId: number): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.apiUrl}/my-bookings/${userId}`)
      .pipe(
        catchError(error => this.handleError('getMyBookings', error))
      );
  }

  getSiteBookings(siteId: number): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.apiUrl}/site/${siteId}`)
      .pipe(
        catchError(error => this.handleError('getSiteBookings', error))
      );
  }

  getUpcomingBookings(userId: number): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.apiUrl}/upcoming/${userId}`)
      .pipe(
        catchError(error => this.handleError('getUpcomingBookings', error))
      );
  }

  getPastBookings(userId: number): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.apiUrl}/past/${userId}`)
      .pipe(
        catchError(error => this.handleError('getPastBookings', error))
      );
  }

  checkAvailability(siteId: number, startDate: string, endDate: string): Observable<{ available: boolean }> {
    const params = new HttpParams()
      .set('siteId', siteId.toString())
      .set('startDate', startDate)
      .set('endDate', endDate);
    
    return this.http.get<{ available: boolean }>(`${this.apiUrl}/check-availability`, { params })
      .pipe(
        catchError(error => this.handleError('checkAvailability', error))
      );
  }

  calculatePrice(siteId: number, startDate: string, endDate: string, guests: number): Observable<{ totalPrice: number }> {
    const params = new HttpParams()
      .set('siteId', siteId.toString())
      .set('startDate', startDate)
      .set('endDate', endDate)
      .set('guests', guests.toString());
    
    return this.http.get<{ totalPrice: number }>(`${this.apiUrl}/calculate-price`, { params })
      .pipe(
        catchError(error => this.handleError('calculatePrice', error))
      );
  }

  createBooking(camperId: number, siteId: number, startDate: string, endDate: string, guests: number): Observable<Booking> {
    const bookingData = {
      camperId,
      siteId,
      startDate,
      endDate,
      guests
    };
    
    return this.http.post<Booking>(this.apiUrl, bookingData)
      .pipe(
        catchError(error => this.handleError('createBooking', error))
      );
  }

  updateBooking(id: number, booking: Partial<Booking>): Observable<Booking> {
    return this.http.put<Booking>(`${this.apiUrl}/${id}`, booking)
      .pipe(
        catchError(error => this.handleError('updateBooking', error))
      );
  }

  cancelBooking(id: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${id}`)
      .pipe(
        catchError(error => this.handleError('cancelBooking', error))
      );
  }

  private handleError(operation: string, error: any) {
    console.error(`${operation} error:`, error);
    const errorMessage = error?.error?.message || error?.message || 'Une erreur est survenue';
    return throwError(() => new Error(errorMessage));
  }
}
