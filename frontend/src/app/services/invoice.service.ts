import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { ApiConfigService } from './api-config.service';

export interface Payment {
  id?: number;
  invoice?: any;
  amount: number;
  method: string;
  transactionId: string;
  status?: string;
  createdAt?: string;
}

export interface Invoice {
  id?: number;
  invoiceNumber: string;
  booking?: any;
  order?: any;
  user?: any;
  amount: number;
  taxAmount?: number;
  totalAmount: number;
  status?: string;
  dueDate?: string;
  payments?: Payment[];
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class InvoiceService {
  private http = inject(HttpClient);
  private apiConfig = inject(ApiConfigService);
  private apiUrl = this.apiConfig.getEndpointUrl('/invoices');

  getAllInvoices(): Observable<Invoice[]> {
    return this.http.get<Invoice[]>(this.apiUrl)
      .pipe(
        catchError(error => this.handleError('getAllInvoices', error))
      );
  }

  getInvoiceById(id: number): Observable<Invoice> {
    return this.http.get<Invoice>(`${this.apiUrl}/${id}`)
      .pipe(
        catchError(error => this.handleError('getInvoiceById', error))
      );
  }

  getInvoiceByNumber(invoiceNumber: string): Observable<Invoice> {
    return this.http.get<Invoice>(`${this.apiUrl}/number/${invoiceNumber}`)
      .pipe(
        catchError(error => this.handleError('getInvoiceByNumber', error))
      );
  }

  getOverdueInvoices(): Observable<Invoice[]> {
    return this.http.get<Invoice[]>(`${this.apiUrl}/overdue`)
      .pipe(
        catchError(error => this.handleError('getOverdueInvoices', error))
      );
  }

  generateInvoiceForBooking(bookingId: number): Observable<Invoice> {
    return this.http.post<Invoice>(`${this.apiUrl}/booking/${bookingId}`, {})
      .pipe(
        catchError(error => this.handleError('generateInvoiceForBooking', error))
      );
  }

  generateInvoiceForOrder(orderId: number): Observable<Invoice> {
    return this.http.post<Invoice>(`${this.apiUrl}/order/${orderId}`, {})
      .pipe(
        catchError(error => this.handleError('generateInvoiceForOrder', error))
      );
  }

  recordPayment(invoiceId: number, amount: number, method: string, transactionId: string): Observable<Payment> {
    const paymentData = {
      amount,
      method,
      transactionId
    };
    
    return this.http.post<Payment>(`${this.apiUrl}/${invoiceId}/payment`, paymentData)
      .pipe(
        catchError(error => this.handleError('recordPayment', error))
      );
  }

  updateInvoiceStatus(id: number, status: string): Observable<Invoice> {
    const params = new HttpParams().set('status', status);
    
    return this.http.put<Invoice>(`${this.apiUrl}/${id}/status`, {}, { params })
      .pipe(
        catchError(error => this.handleError('updateInvoiceStatus', error))
      );
  }

  private handleError(operation: string, error: any) {
    console.error(`${operation} error:`, error);
    const errorMessage = error?.error?.message || error?.message || 'Une erreur est survenue';
    return throwError(() => new Error(errorMessage));
  }
}
