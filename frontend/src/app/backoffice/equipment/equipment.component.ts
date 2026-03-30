import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { EquipmentService } from '../../services/equipment.service';
import { AuthService } from '../../services/auth.service';

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
    imageUrl: '',
    isActive: true
  };

  public authService = inject(AuthService);
  private equipmentService = inject(EquipmentService);

  constructor() {}

  ngOnInit() {
    this.loadEquipment();
  }

  loadEquipment() {
    this.loading = true;
    this.equipmentService.getAllEquipment().subscribe({
      next: (data) => {
        this.equipment = data; // Directly use DTOs
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load equipment', err);
        alert('Erreur lors du chargement des équipements: ' + (err.error?.message || err.message));
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
        (e.name && e.name.toLowerCase().includes(term)) || 
        (e.category && e.category.toLowerCase().includes(term))
      );
    }
  }

  deleteItem(id: number) {
    if (!confirm('Supprimer cet équipement ?')) return;
    this.equipmentService.deleteEquipment(id).subscribe({
      next: () => {
        alert('Équipement supprimé.');
        this.loadEquipment();
      },
      error: (err) => alert('Erreur: ' + (err.error?.message || err.message))
    });
  }

  editItem(item: any) {
    this.editingItem = { ...item };
    
    setTimeout(() => {
      const modalElement = document.getElementById('editEquipmentModal');
      if (modalElement && (window as any).bootstrap) {
        const modal = (window as any).bootstrap.Modal.getOrCreateInstance(modalElement);
        modal.show();
      }
    }, 100);
  }

  updateItem() {
    if (!this.editingItem || !this.editingItem.id) return;
    
    // On prépare un objet propre correspondant à EquipmentDTO
    const itemToUpdate: any = {
      id: this.editingItem.id,
      name: this.editingItem.name,
      description: this.editingItem.description,
      category: this.editingItem.category,
      pricePerDay: this.editingItem.pricePerDay,
      stockQuantity: this.editingItem.stockQuantity,
      imageUrl: this.editingItem.imageUrl,
      specifications: this.editingItem.specifications,
      isActive: this.editingItem.isActive
    };
    
    // On récupère le providerId s'il existe
    const providerId = this.editingItem.providerId;

    this.equipmentService.updateEquipment(this.editingItem.id, itemToUpdate).subscribe({
      next: () => {
        const modalElement = document.getElementById('editEquipmentModal');
        if (modalElement && (window as any).bootstrap) {
          const modal = (window as any).bootstrap.Modal.getInstance(modalElement);
          modal?.hide();
        }
        alert('Équipement mis à jour !');
        this.loadEquipment();
        this.editingItem = null;
      },
      error: (err) => {
        console.error('Failed to update equipment', err);
        alert('Erreur lors de la mise à jour: ' + (err.error?.message || err.message));
      }
    });
  }

  createItem() {
    this.equipmentService.createEquipment(this.newItem as any).subscribe({
      next: () => {
        const modalElement = document.getElementById('addEquipmentModal');
        if (modalElement && (window as any).bootstrap) {
          const modal = (window as any).bootstrap.Modal.getInstance(modalElement);
          modal?.hide();
        }
        alert('Équipement créé avec succès !');
        this.newItem = { name: '', description: '', category: 'SHELTER', pricePerDay: 0, stockQuantity: 0, imageUrl: '', isActive: true };
        this.loadEquipment();
      },
      error: (err) => alert('Erreur lors de la création: ' + (err.error?.message || err.message))
    });
  }

  scrollToTop() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}
