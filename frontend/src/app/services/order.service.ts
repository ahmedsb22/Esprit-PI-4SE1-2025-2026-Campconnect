import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { ApiConfigService } from './api-config.service';

export interface EquipmentOrder {
  id?: number;
  orderNumber?: string;
  user?: any;
  equipment?: any;
  quantity: number;
  totalPrice?: number;
  status?: string;
  deliveryDate?: string;
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private http = inject(HttpClient);
  private apiConfig = inject(ApiConfigService);
  private apiUrl = this.apiConfig.getEndpointUrl('/orders');

  getAllOrders(): Observable<EquipmentOrder[]> {
    return this.http.get<EquipmentOrder[]>(this.apiUrl)
      .pipe(
        catchError(error => this.handleError('getAllOrders', error))
      );
  }

  getOrderById(id: number): Observable<EquipmentOrder> {
    return this.http.get<EquipmentOrder>(`${this.apiUrl}/${id}`)
      .pipe(
        catchError(error => this.handleError('getOrderById', error))
      );
  }

  getMyOrders(userId: number): Observable<EquipmentOrder[]> {
    return this.http.get<EquipmentOrder[]>(`${this.apiUrl}/my-orders/${userId}`)
      .pipe(
        catchError(error => this.handleError('getMyOrders', error))
      );
  }

  createOrder(userId: number, equipmentId: number, quantity: number): Observable<EquipmentOrder> {
    const orderData = {
      userId,
      equipmentId,
      quantity
    };
    
    return this.http.post<EquipmentOrder>(this.apiUrl, orderData)
      .pipe(
        catchError(error => this.handleError('createOrder', error))
      );
  }

  updateOrderStatus(id: number, status: string): Observable<EquipmentOrder> {
    const params = new HttpParams().set('status', status);
    
    return this.http.put<EquipmentOrder>(`${this.apiUrl}/${id}/status`, {}, { params })
      .pipe(
        catchError(error => this.handleError('updateOrderStatus', error))
      );
  }

  cancelOrder(id: number, reason?: string): Observable<EquipmentOrder> {
    const data = reason ? { reason } : {};
    return this.http.put<EquipmentOrder>(`${this.apiUrl}/${id}/cancel`, data)
      .pipe(
        catchError(error => this.handleError('cancelOrder', error))
      );
  }

  getOrderHistory(userId: number): Observable<EquipmentOrder[]> {
    return this.http.get<EquipmentOrder[]>(`${this.apiUrl}/history/${userId}`)
      .pipe(
        catchError(error => this.handleError('getOrderHistory', error))
      );
  }

  private handleError(operation: string, error: any) {
    console.error(`${operation} error:`, error);
    const errorMessage = error?.error?.message || error?.message || 'Une erreur est survenue';
    return throwError(() => new Error(errorMessage));
  }
}
