import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { ApiConfigService } from './api-config.service';

export interface ContractAudit {
  id?: number;
  action: string;
  userId?: number;
  timestamp?: string;
  details?: string;
}

export interface Contract {
  id?: number;
  contractNumber?: string;
  booking?: any;
  owner?: any;
  camper?: any;
  startDate?: string;
  endDate?: string;
  terms?: string;
  status?: string;
  signedAt?: string;
  audits?: ContractAudit[];
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ContractService {
  private http = inject(HttpClient);
  private apiConfig = inject(ApiConfigService);
  private apiUrl = this.apiConfig.getEndpointUrl('/contracts');

  getAllContracts(): Observable<Contract[]> {
    return this.http.get<Contract[]>(this.apiUrl)
      .pipe(
        catchError(error => this.handleError('getAllContracts', error))
      );
  }

  getContractById(id: number): Observable<Contract> {
    return this.http.get<Contract>(`${this.apiUrl}/${id}`)
      .pipe(
        catchError(error => this.handleError('getContractById', error))
      );
  }

  getContractByBooking(bookingId: number): Observable<Contract> {
    return this.http.get<Contract>(`${this.apiUrl}/booking/${bookingId}`)
      .pipe(
        catchError(error => this.handleError('getContractByBooking', error))
      );
  }

  getContractsByOwner(ownerId: number): Observable<Contract[]> {
    return this.http.get<Contract[]>(`${this.apiUrl}/owner/${ownerId}`)
      .pipe(
        catchError(error => this.handleError('getContractsByOwner', error))
      );
  }

  getContractsByUser(userId: number): Observable<Contract[]> {
    return this.http.get<Contract[]>(`${this.apiUrl}/user/${userId}`)
      .pipe(
        catchError(error => this.handleError('getContractsByUser', error))
      );
  }

  getAuditLog(id: number): Observable<ContractAudit[]> {
    return this.http.get<ContractAudit[]>(`${this.apiUrl}/${id}/audit`)
      .pipe(
        catchError(error => this.handleError('getAuditLog', error))
      );
  }

  generateContract(bookingId: number): Observable<Contract> {
    const params = new HttpParams().set('bookingId', bookingId.toString());
    
    return this.http.post<Contract>(`${this.apiUrl}/generate`, {}, { params })
      .pipe(
        catchError(error => this.handleError('generateContract', error))
      );
  }

  signContract(id: number, userId: number, signature: string, ipAddress: string): Observable<Contract> {
    const signatureData = {
      userId,
      signature,
      ipAddress
    };
    
    return this.http.post<Contract>(`${this.apiUrl}/${id}/sign`, signatureData)
      .pipe(
        catchError(error => this.handleError('signContract', error))
      );
  }

  updateContractStatus(id: number, status: string): Observable<Contract> {
    const params = new HttpParams().set('status', status);
    
    return this.http.put<Contract>(`${this.apiUrl}/${id}/status`, {}, { params })
      .pipe(
        catchError(error => this.handleError('updateContractStatus', error))
      );
  }

  private handleError(operation: string, error: any) {
    console.error(`${operation} error:`, error);
    const errorMessage = error?.error?.message || error?.message || 'Une erreur est survenue';
    return throwError(() => new Error(errorMessage));
  }
}
