import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-my-bookings',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './my-bookings.component.html',
  styleUrls: ['./my-bookings.component.scss']
})
export class MyBookingsComponent implements OnInit {
  bookings: any[] = [];
  filteredBookings: any[] = [];
  loading = true;
  error = '';
  filterStatus = '';
  cancellingId: number | null = null;

  private apiUrl = `${environment.apiUrl}/bookings`;

  statusOptions = [
    { value: '', label: 'All Bookings' },
    { value: 'PENDING', label: 'Pending' },
    { value: 'CONFIRMED', label: 'Confirmed' },
    { value: 'CHECKED_IN', label: 'Checked In' },
    { value: 'CHECKED_OUT', label: 'Checked Out' },
    { value: 'CANCELLED', label: 'Cancelled' },
    { value: 'COMPLETED', label: 'Completed' }
  ];

  constructor(private http: HttpClient, private authService: AuthService) {}

  ngOnInit(): void {
    this.loadBookings();
  }

  loadBookings(): void {
    this.loading = true;
    this.error = '';

    // Try to get bookings for the current user via profile endpoint
    this.authService.getProfile().subscribe({
      next: (user) => {
        if (user.id) {
          this.http.get<any[]>(`${this.apiUrl}/my-bookings/${user.id}`).subscribe({
            next: (data) => this.handleBookingsResponse(data),
            error: () => this.fetchAllAndFilter()
          });
        } else {
          this.fetchAllAndFilter();
        }
      },
      error: () => this.fetchAllAndFilter()
    });
  }

  private fetchAllAndFilter(): void {
    // Fallback: fetch all and let server-side auth filter them
    this.http.get<any[]>(this.apiUrl).subscribe({
      next: (data) => this.handleBookingsResponse(data),
      error: (err) => {
        this.error = err.error?.message || 'Failed to load bookings.';
        this.loading = false;
      }
    });
  }

  private handleBookingsResponse(data: any[]): void {
    this.bookings = data.map(b => ({
      id: b.id,
      reservationNumber: b.reservationNumber ?? `RES-${b.id}`,
      siteName: b.campingSiteName ?? b.campingSite?.name ?? 'N/A',
      siteLocation: b.campingSiteLocation ?? b.campingSite?.location ?? '',
      checkInDate: b.checkInDate ?? '-',
      checkOutDate: b.checkOutDate ?? '-',
      numberOfGuests: b.numberOfGuests ?? 1,
      totalPrice: b.totalPrice ?? 0,
      status: b.status ?? 'PENDING',
      specialRequests: b.specialRequests ?? '',
      createdAt: b.createdAt ?? ''
    }));
    this.applyFilter();
    this.loading = false;
  }

  applyFilter(): void {
    this.filteredBookings = this.filterStatus
      ? this.bookings.filter(b => b.status === this.filterStatus)
      : [...this.bookings];
  }

  cancelBooking(id: number): void {
    if (!confirm('Are you sure you want to cancel this booking?')) return;
    this.cancellingId = id;
    this.http.delete(`${this.apiUrl}/${id}`).subscribe({
      next: () => {
        this.cancellingId = null;
        this.bookings = this.bookings.map(b =>
          b.id === id ? { ...b, status: 'CANCELLED' } : b
        );
        this.applyFilter();
      },
      error: (err) => {
        this.cancellingId = null;
        alert(err.error?.message || 'Could not cancel booking.');
      }
    });
  }

  nights(checkIn: string, checkOut: string): number {
    if (!checkIn || checkIn === '-' || !checkOut || checkOut === '-') return 0;
    const diff = new Date(checkOut).getTime() - new Date(checkIn).getTime();
    return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)));
  }

  statusClass(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'status-pending',
      CONFIRMED: 'status-confirmed',
      CHECKED_IN: 'status-checkedin',
      CHECKED_OUT: 'status-checkedout',
      CANCELLED: 'status-cancelled',
      COMPLETED: 'status-completed'
    };
    return map[status] ?? 'status-pending';
  }

  statusIcon(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'bi-clock',
      CONFIRMED: 'bi-check-circle',
      CHECKED_IN: 'bi-door-open',
      CHECKED_OUT: 'bi-door-closed',
      CANCELLED: 'bi-x-circle',
      COMPLETED: 'bi-star'
    };
    return map[status] ?? 'bi-circle';
  }

  scrollToTop(): void {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}
