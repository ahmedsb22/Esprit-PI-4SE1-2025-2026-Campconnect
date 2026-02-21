import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { EquipmentService } from '../../services/equipment.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-equipment',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './equipment.component.html',
  styleUrls: ['./equipment.component.scss']
})
export class EquipmentComponent implements OnInit, AfterViewInit {
  equipment: any[] = [];
  loading = true;
  selectedCategory = 'all';
  
  filters = {
    priceRange: '',
    availability: '',
    sortBy: ''
  };

  categories = [
    { id: 'all', name: 'All Equipment', icon: 'bi-grid-3x3-gap' },
    { id: 'tents', name: 'Tents & Shelters', icon: 'bi-house-door' },
    { id: 'sleeping', name: 'Sleeping Gear', icon: 'bi-moon' },
    { id: 'cooking', name: 'Cooking & Food', icon: 'bi-fire' },
    { id: 'lighting', name: 'Lighting', icon: 'bi-lightbulb' },
    { id: 'safety', name: 'Safety & Tools', icon: 'bi-shield-check' }
  ];

  sampleEquipment = [
    {
      id: 1,
      name: 'Family Tent (6 Person)',
      category: 'tents',
      description: 'Spacious waterproof tent with easy setup. Perfect for family camping.',
      price: 35,
      image: 'assets/img/placeholder.svg',
      availability: 'available',
      specs: ['6 persons', 'Waterproof']
    },
    {
      id: 2,
      name: 'Premium Sleeping Bag',
      category: 'sleeping',
      description: 'Warm and comfortable sleeping bag rated for 0°C to 15°C.',
      price: 12,
      image: 'assets/img/placeholder.svg',
      availability: 'available',
      specs: ['0-15°C', 'Compact']
    },
    {
      id: 3,
      name: 'Portable Camping Stove',
      category: 'cooking',
      description: 'Compact gas stove with wind protection. Includes cookware set.',
      price: 18,
      image: 'assets/img/placeholder.svg',
      availability: 'limited',
      specs: ['Gas powered', 'Cookware']
    },
    {
      id: 4,
      name: 'LED Camping Lantern',
      category: 'lighting',
      description: 'Rechargeable LED lantern with 48-hour battery life. Multiple brightness levels.',
      price: 8,
      image: 'assets/img/placeholder.svg',
      availability: 'available',
      specs: ['48h battery', 'Adjustable']
    },
    {
      id: 5,
      name: 'Hiking Backpack 50L',
      category: 'safety',
      description: 'Ergonomic backpack with rain cover and multiple compartments.',
      price: 15,
      image: 'assets/img/placeholder.svg',
      availability: 'available',
      specs: ['50L capacity', 'Rain cover']
    },
    {
      id: 6,
      name: 'Portable Cooler 40L',
      category: 'cooking',
      description: 'Insulated cooler box keeps food and drinks cold for up to 3 days.',
      price: 20,
      image: 'assets/img/placeholder.svg',
      availability: 'available',
      specs: ['40L capacity', '3 days cold']
    },
    {
      id: 7,
      name: 'Folding Camp Chair',
      category: 'tents',
      description: 'Lightweight and comfortable folding chair with cup holder.',
      price: 5,
      image: 'assets/img/placeholder.svg',
      availability: 'available',
      specs: ['Foldable', 'Cup holder']
    },
    {
      id: 8,
      name: 'First Aid Kit',
      category: 'safety',
      description: 'Comprehensive first aid kit with all essential medical supplies.',
      price: 10,
      image: 'assets/img/placeholder.svg',
      availability: 'available',
      specs: ['Comprehensive', 'Portable']
    }
  ];

  private orderApiUrl = `${environment.apiUrl}/equipment-orders`;

  constructor(private equipmentService: EquipmentService, private http: HttpClient) {}

  ngOnInit() {
    this.loadEquipment();
  }

  ngAfterViewInit() {
    setTimeout(() => {
      this.initFrontofficeScripts();
    }, 100);
  }

  loadEquipment() {
    this.equipmentService.getAvailableEquipment().subscribe({
      next: (equipment) => {
        this.equipment = equipment.length > 0 ? equipment.map(item => this.mapEquipment(item)) : this.sampleEquipment;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading equipment:', err);
        this.equipment = this.sampleEquipment;
        this.loading = false;
      }
    });
  }

  filterByCategory(category: string) {
    this.selectedCategory = category;
  }

  applyFilters() {
    this.equipmentService.getAllEquipment()
      .subscribe({
        next: (equipment) => {
          const priceMax = this.filters.priceRange ? Number(this.filters.priceRange.split('-')[1]) : undefined;
          this.equipment = equipment.map(item => this.mapEquipment(item)).filter(item => {
            const matchesCategory = this.selectedCategory === 'all' ? true : item.category === this.selectedCategory;
            const matchesAvailability = this.filters.availability ? (this.filters.availability === 'available' ? item.availability === 'available' : item.availability === 'limited') : true;
            const matchesPrice = priceMax ? item.price <= priceMax : true;
            return matchesCategory && matchesAvailability && matchesPrice;
          });
        },
        error: (err) => {
          console.error('Error filtering equipment:', err);
          this.equipment = [];
        }
      });
  }

  resetFilters() {
    this.filters = {
      priceRange: '',
      availability: '',
      sortBy: ''
    };
    this.selectedCategory = 'all';
  }

  rentEquipment(equipmentId: number) {
    const payload = {
      equipment: { id: equipmentId },
      quantity: 1
    };
    this.http.post(this.orderApiUrl, payload).subscribe({
      next: () => alert('✅ Equipment rented successfully! View it in the backoffice under Orders.'),
      error: (err: HttpErrorResponse) => {
        console.error('Rental failed', err);
        alert(err.error?.message || 'Rental failed. Please try again.');
      }
    });
  }

  scrollToTop() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  subscribeNewsletter() {
    alert('Newsletter subscription - to be implemented');
  }

  private mapEquipment(item: any) {
    // Backend field is imageUrl (single string), not images[]
    const image = item?.imageUrl || 'assets/img/placeholder.svg';
    const avail = item?.availableQuantity ?? item?.stockQuantity ?? 1;
    const availability = avail > 2 ? 'available' : avail > 0 ? 'limited' : 'limited';
    const specs = item?.specifications ? [item.specifications] : ['Quality checked', 'Ready to rent'];
    return {
      id: item.id,
      name: item.name,
      category: item.category?.toLowerCase() || 'tents',
      description: item.description,
      price: item.pricePerDay ?? item.price ?? 0,
      image,
      availability,
      specs
    };
  }

  initFrontofficeScripts() {
    // Initialize AOS if available
    if ((window as any).AOS) {
      (window as any).AOS.init({
        duration: 800,
        easing: 'ease-in-out',
        once: true
      });
    }
  }

  get filteredEquipment() {
    if (this.selectedCategory === 'all') {
      return this.equipment;
    }
    return this.equipment.filter(item => item.category === this.selectedCategory);
  }

  getCategoryName(categoryId: string): string {
    const category = this.categories.find(c => c.id === categoryId);
    return category ? category.name : categoryId;
  }
}
