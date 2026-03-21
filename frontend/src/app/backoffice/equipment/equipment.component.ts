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
    imageUrl: ''
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
    this.editingItem = { ...item };
    const modalElement = document.getElementById('editEquipmentModal');
    if (modalElement && (window as any).bootstrap) {
      const modal = new (window as any).bootstrap.Modal(modalElement);
      modal.show();
    }
  }

  updateItem() {
    this.http.put(`${this.apiUrl}/${this.editingItem.id}`, this.editingItem).subscribe({
      next: () => {
        const modalElement = document.getElementById('editEquipmentModal');
        if (modalElement && (window as any).bootstrap) {
          const modal = (window as any).bootstrap.Modal.getInstance(modalElement);
          if (modal) modal.hide();
        }
        this.loadEquipment();
      },
      error: (err) => console.error('Failed to update equipment', err)
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
        this.newItem = { name: '', description: '', category: 'SHELTER', pricePerDay: 0, stockQuantity: 0, imageUrl: '' };
        this.loadEquipment();
      },
      error: (err) => console.error('Failed to create equipment', err)
    });
  }
}
