import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-equipment',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './equipment.component.html',
  styleUrls: ['./equipment.component.scss']
})
export class EquipmentComponent implements OnInit {
  equipment: any[] = [];
  filteredEquipment: any[] = [];
  searchTerm = '';
  loading = false;
  editingItem: any = null;
  newItem = {
    name: '',
    description: '',
    category: 'SHELTER',
    pricePerDay: 0,
    stockQuantity: 0,
    availableQuantity: 0,
    imageUrl: '',
    isActive: true
  };

  private apiUrl = `${environment.apiUrl}/equipment`;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadEquipment();
  }

  loadEquipment() {
    this.loading = true;
    this.http.get<any[]>(this.apiUrl).subscribe({
      next: (data) => {
        this.equipment = data;
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load equipment', err);
        this.loading = false;
      }
    });
  }

  applyFilter() {
    if (!this.searchTerm) {
      this.filteredEquipment = [...this.equipment];
    } else {
      const term = this.searchTerm.toLowerCase();
      this.filteredEquipment = this.equipment.filter(e => 
        e.name.toLowerCase().includes(term) || 
        e.category.toLowerCase().includes(term)
      );
    }
  }

  deleteItem(id: number) {
    if (!confirm('Supprimer cet équipement ?')) return;
    this.http.delete(`${this.apiUrl}/${id}`).subscribe({
      next: () => this.loadEquipment(),
      error: (err) => console.error('Failed to delete equipment', err)
    });
  }

  editItem(item: any) {
    console.log('Opening edit modal for item:', item);
    this.editingItem = { ...item };
    console.log('editingItem after assignment:', this.editingItem);
    
    setTimeout(() => {
      const modalElement = document.getElementById('editEquipmentModal');
      console.log('Modal element found:', !!modalElement);
      
      if (modalElement && (window as any).bootstrap) {
        try {
          const modal = (window as any).bootstrap.Modal.getOrCreateInstance(modalElement);
          console.log('Modal instance:', !!modal);
          modal.show();
          console.log('Modal shown');
        } catch (e) {
          console.error('Error showing modal:', e);
        }
      } else {
        console.warn('Bootstrap not available or modal element not found');
      }
    }, 100);
  }

  updateItem() {
    if (!this.editingItem || !this.editingItem.id) {
      console.error('No equipment selected for update');
      return;
    }
    
    console.log('Sending update request for ID:', this.editingItem.id, 'Data:', this.editingItem);
    
    this.http.put(`${this.apiUrl}/${this.editingItem.id}`, this.editingItem).subscribe({
      next: (response) => {
        console.log('Update successful:', response);
        const modalElement = document.getElementById('editEquipmentModal');
        if (modalElement && (window as any).bootstrap) {
          const modal = (window as any).bootstrap.Modal.getInstance(modalElement);
          if (modal) modal.hide();
        }
        this.editingItem = null;
        this.loadEquipment();
      },
      error: (err) => {
        console.error('Failed to update equipment:', err);
        if (err.error && err.error.message) {
          alert('Erreur: ' + err.error.message);
        } else {
          alert('Erreur lors de la mise à jour');
        }
      }
    });
  }

  createItem() {
    this.http.post(this.apiUrl, this.newItem).subscribe({
      next: () => {
        const modalElement = document.getElementById('addEquipmentModal');
        if (modalElement && (window as any).bootstrap) {
          const modal = (window as any).bootstrap.Modal.getInstance(modalElement);
          if (modal) modal.hide();
        }
        this.newItem = { name: '', description: '', category: 'SHELTER', pricePerDay: 0, stockQuantity: 0, availableQuantity: 0, imageUrl: '', isActive: true };
        this.loadEquipment();
      },
      error: (err) => console.error('Failed to create equipment', err)
    });
  }
}
