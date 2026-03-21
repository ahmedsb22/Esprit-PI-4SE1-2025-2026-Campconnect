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
    campingSiteId: null as number | null,
    camperId: null as number | null,
    checkInDate: '',
    checkOutDate: '',
    numberOfGuests: 1,
    status: 'PENDING',
    specialRequests: ''
  };
  
  availableSites: any[] = [];
  availableUsers: any[] = [];

  statusOptions = ['PENDING', 'CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED', 'COMPLETED'];

  private apiUrl = `${environment.apiUrl}/bookings`;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadBookings();
    this.loadAvailableSites();
    this.loadAvailableUsers();
  }
  
  loadAvailableSites() {
    this.http.get<any[]>(`${environment.apiUrl}/sites`).subscribe({
      next: (sites) => {
        this.availableSites = sites.map(s => ({ id: s.id, name: s.name }));
      },
      error: (err) => console.error('Failed to load sites', err)
    });
  }
  
  loadAvailableUsers() {
    this.http.get<any[]>(`${environment.apiUrl}/users`).subscribe({
      next: (users) => {
        this.availableUsers = users.map(u => ({ 
          id: u.id, 
          name: `${u.firstName || ''} ${u.lastName || ''}`.trim() || u.email 
        }));
      },
      error: (err) => console.error('Failed to load users', err)
    });
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
          camperId: b.camperId,
          siteName: b.campingSiteName ?? b.campingSite?.name ?? 'N/A',
          siteLocation: b.campingSiteLocation ?? b.campingSite?.location ?? '',
          campingSiteId: b.campingSiteId,
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
        alert('Erreur lors du chargement des réservations: ' + (err.error?.message || err.message));
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
    this.form = { 
      campingSiteId: null,
      camperId: null,
      checkInDate: '', 
      checkOutDate: '', 
      numberOfGuests: 1, 
      status: 'PENDING', 
      specialRequests: '' 
    };
    this.showForm = true;
  }

  openEdit(b: any) {
    this.editMode = true;
    this.selectedId = b.id;
    this.form = {
      campingSiteId: b.campingSiteId || null,
      camperId: b.camperId || null,
      checkInDate: b.checkInDate,
      checkOutDate: b.checkOutDate,
      numberOfGuests: b.numberOfGuests,
      status: b.status,
      specialRequests: b.specialRequests
    };
    this.showForm = true;
  }

  saveBooking() {
    // Validation
    if (!this.form.checkInDate || !this.form.checkOutDate) {
      alert('Les dates de check-in et check-out sont obligatoires');
      return;
    }
    if (this.form.numberOfGuests < 1) {
      alert('Le nombre de personnes doit être au moins 1');
      return;
    }
    if (!this.editMode && !this.form.campingSiteId) {
      alert('Veuillez sélectionner un site de camping');
      return;
    }
    
    const payload: any = {
      checkInDate: this.form.checkInDate,
      checkOutDate: this.form.checkOutDate,
      numberOfGuests: this.form.numberOfGuests,
      status: this.form.status,
      specialRequests: this.form.specialRequests || ''
    };
    
    // Pour la création, ajouter campingSiteId et camperId
    if (!this.editMode) {
      payload.campingSiteId = this.form.campingSiteId;
      payload.camperId = this.form.camperId || 1; // Par défaut si non spécifié
    }
    
    if (this.editMode && this.selectedId) {
      this.http.put(`${this.apiUrl}/${this.selectedId}`, payload).subscribe({
        next: () => { 
          alert('Réservation mise à jour avec succès !');
          this.showForm = false; 
          this.loadBookings(); 
        },
        error: (err) => { 
          console.error('Update error:', err);
          alert('Erreur lors de la mise à jour: ' + (err.error?.message ?? err.message)); 
        }
      });
    } else {
      this.http.post(this.apiUrl, payload).subscribe({
        next: () => { 
          alert('Réservation créée avec succès !');
          this.showForm = false; 
          this.loadBookings(); 
        },
        error: (err) => { 
          console.error('Create error:', err);
          alert('Erreur lors de la création: ' + (err.error?.message ?? err.message)); 
        }
      });
    }
  }

  updateStatus(id: number, status: string) {
    this.http.put(`${this.apiUrl}/${id}/status`, null, { params: { status } }).subscribe({
      next: () => {
        alert('Statut mis à jour avec succès !');
        this.loadBookings();
      },
      error: (err) => {
        console.error('Status update error:', err);
        alert('Erreur lors de la mise à jour du statut: ' + (err.error?.message ?? err.message));
      }
    });
  }

  deleteBooking(id: number) {
    if (!confirm('Supprimer cette réservation ?')) return;
    this.http.delete(`${this.apiUrl}/${id}`).subscribe({
      next: () => {
        alert('Réservation supprimée avec succès !');
        this.loadBookings();
      },
      error: (err) => {
        console.error('Delete error:', err);
        alert('Erreur lors de la suppression: ' + (err.error?.message ?? err.message));
      }
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
