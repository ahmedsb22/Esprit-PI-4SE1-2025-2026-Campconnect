import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-bookings',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './bookings.component.html',
  styleUrls: ['./bookings.component.scss']
})
export class BookingsComponent implements OnInit {

  bookings: any[] = [];
  filteredBookings: any[] = [];
  searchTerm = '';
  filterStatus = '';
  loading = false;
  error = '';

  showForm = false;
  editMode = false;
  selectedId: number | null = null;

  form = {
    checkInDate: '',
    checkOutDate: '',
    numberOfGuests: 1,
    status: 'PENDING',
    specialRequests: ''
  };

  statusOptions = ['PENDING', 'CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED', 'COMPLETED'];

  private apiUrl = `${environment.apiUrl}/bookings`;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadBookings();
  }

  loadBookings() {
    this.loading = true;
    this.error = '';
    this.http.get<any[]>(this.apiUrl).subscribe({
      next: (data) => {
        this.bookings = data.map(b => ({
          id: b.id,
          reservationNumber: b.reservationNumber ?? `RES-${b.id}`,
          camper: (b.camperName ?? (b.camper ? (`${b.camper.firstName ?? ''} ${b.camper.lastName ?? ''}`).trim() : 'Anonymous')) || 'Anonymous',
          camperEmail: b.camperEmail ?? '',
          siteName: b.campingSiteName ?? b.campingSite?.name ?? 'N/A',
          siteLocation: b.campingSiteLocation ?? b.campingSite?.location ?? '',
          checkInDate: b.checkInDate ?? '-',
          checkOutDate: b.checkOutDate ?? '-',
          numberOfGuests: b.numberOfGuests ?? 1,
          totalPrice: b.totalPrice ?? 0,
          status: b.status ?? 'PENDING',
          specialRequests: b.specialRequests ?? ''
        }));
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load bookings.';
        this.loading = false;
        console.error(err);
      }
    });
  }

  applyFilter() {
    let result = [...this.bookings];
    if (this.searchTerm.trim()) {
      const s = this.searchTerm.toLowerCase();
      result = result.filter(b =>
        b.reservationNumber.toLowerCase().includes(s) ||
        b.camper.toLowerCase().includes(s) ||
        b.siteName.toLowerCase().includes(s)
      );
    }
    if (this.filterStatus) {
      result = result.filter(b => b.status === this.filterStatus);
    }
    this.filteredBookings = result;
  }

  openCreate() {
    this.editMode = false;
    this.selectedId = null;
    this.form = { checkInDate: '', checkOutDate: '', numberOfGuests: 1, status: 'PENDING', specialRequests: '' };
    this.showForm = true;
  }

  openEdit(b: any) {
    this.editMode = true;
    this.selectedId = b.id;
    this.form = {
      checkInDate: b.checkInDate,
      checkOutDate: b.checkOutDate,
      numberOfGuests: b.numberOfGuests,
      status: b.status,
      specialRequests: b.specialRequests
    };
    this.showForm = true;
  }

  saveBooking() {
    const payload: any = { ...this.form };
    if (this.editMode && this.selectedId) {
      this.http.put(`${this.apiUrl}/${this.selectedId}`, payload).subscribe({
        next: () => { this.showForm = false; this.loadBookings(); },
        error: (err) => { alert('Update failed: ' + (err.error?.message ?? err.message)); }
      });
    } else {
      this.http.post(this.apiUrl, payload).subscribe({
        next: () => { this.showForm = false; this.loadBookings(); },
        error: (err) => { alert('Create failed: ' + (err.error?.message ?? err.message)); }
      });
    }
  }

  updateStatus(id: number, status: string) {
    this.http.put(`${this.apiUrl}/${id}/status`, null, { params: { status } }).subscribe({
      next: () => this.loadBookings(),
      error: (err) => alert('Status update failed: ' + (err.error?.message ?? err.message))
    });
  }

  deleteBooking(id: number) {
    if (!confirm('Delete this booking?')) return;
    this.http.delete(`${this.apiUrl}/${id}`).subscribe({
      next: () => this.loadBookings(),
      error: (err) => alert('Delete failed: ' + (err.error?.message ?? err.message))
    });
  }

  cancelForm() {
    this.showForm = false;
    this.selectedId = null;
  }

  statusClass(status: string): string {
    const map: any = {
      PENDING: 'badge-warning',
      CONFIRMED: 'badge-success',
      CHECKED_IN: 'badge-info',
      CHECKED_OUT: 'badge-secondary',
      CANCELLED: 'badge-danger',
      COMPLETED: 'badge-primary'
    };
    return map[status] ?? 'badge-secondary';
  }
}
