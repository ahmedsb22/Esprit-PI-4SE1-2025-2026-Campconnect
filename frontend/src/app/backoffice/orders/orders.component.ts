import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.scss']
})
export class OrdersComponent implements OnInit, AfterViewInit {
  orders: any[] = [];
  stats = {
    pending: 0,
    processing: 0,
    completed: 0,
    cancelled: 0
  };

  private apiUrl = `${environment.apiUrl}/equipment-orders`;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadOrders();
  }

  ngAfterViewInit() {
    setTimeout(() => {
      this.initDataTable();
      this.initBackofficeScripts();
    }, 100);
  }

  loadOrders() {
    this.http.get<any[]>(this.apiUrl).subscribe({
      next: (orders) => {
        this.orders = orders.map(order => ({
          id: order.id,
          orderId: `ORD-${order.id}`,
          customer: order.customerName || 'Anonymous',
          equipmentName: order.equipmentName || 'N/A',
          reservationNumber: order.reservationNumber || '-',
          quantity: order.quantity ?? 1,
          pricePerDay: order.pricePerDay ?? 0,
          subtotal: order.subtotal ?? 0,
          status: (order.status || 'ACTIVE').toLowerCase()
        }));
        this.refreshStats();
        this.refreshTable();
      },
      error: (err) => console.error('Failed to load orders', err)
    });
  }

  viewOrder(id: number) {
    const order = this.orders.find(o => o.id === id);
    if (order) {
      alert(`Order ${order.orderId}\nEquipment: ${order.equipmentName}\nQty: ${order.quantity}\nSubtotal: ${order.subtotal} TND`);
    }
  }

  confirmOrder(id: number) {
    if (!id) {
      return;
    }
    this.http.put(`${this.apiUrl}/${id}/status`, null, { params: { status: 'PAID' } as any }).subscribe({
      next: () => this.loadOrders(),
      error: (err) => console.error('Failed to confirm order', err)
    });
  }

  cancelOrder(id: number) {
    if (!id) return;
    if (confirm('Cancel this order?')) {
      this.http.put(`${this.apiUrl}/${id}/status`, null, { params: { status: 'CANCELLED' } as any }).subscribe({
        next: () => this.loadOrders(),
        error: (err) => console.error('Failed to cancel order', err)
      });
    }
  }

  completeOrder(id: number) {
    if (!id) {
      return;
    }
    this.http.put(`${this.apiUrl}/${id}/status`, null, { params: { status: 'DELIVERED' } as any }).subscribe({
      next: () => this.loadOrders(),
      error: (err) => console.error('Failed to complete order', err)
    });
  }

  printReceipt(id: number) {
    const order = this.orders.find(o => o.id === id);
    if (order) {
      alert(`Receipt for order ${order.orderId}\nAmount: ${order.amount} TND`);
    }
  }

  refreshStats() {
    this.stats.pending = this.orders.filter(o => o.status === 'active').length;
    this.stats.processing = 0;
    this.stats.completed = 0;
    this.stats.cancelled = this.orders.filter(o => o.status === 'cancelled').length;
  }

  refreshTable() {
    if ((window as any).$ && (window as any).$.fn.DataTable) {
      const table = document.querySelector('#dataTable') as any;
      if (table && (window as any).$.fn.DataTable.isDataTable(table)) {
        (window as any).$(table).DataTable().clear().destroy();
      }
      setTimeout(() => this.initDataTable(), 0);
    }
  }

  scrollToTop() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  initDataTable() {
    if ((window as any).$ && (window as any).$.fn.DataTable) {
      const table = document.querySelector('#dataTable') as any;
      if (table && !(window as any).$.fn.DataTable.isDataTable(table)) {
        (window as any).$(table).DataTable({ order: [[0, 'desc']] });
      }
    }
  }

  initBackofficeScripts() {
    const sidebarToggle = document.getElementById('sidebarToggle');
    const sidebarToggleTop = document.getElementById('sidebarToggleTop');
    const body = document.body;

    if (sidebarToggle) {
      sidebarToggle.addEventListener('click', () => {
        body.classList.toggle('sidebar-toggled');
        document.querySelector('.sidebar')?.classList.toggle('toggled');
      });
    }

    if (sidebarToggleTop) {
      sidebarToggleTop.addEventListener('click', () => {
        body.classList.toggle('sidebar-toggled');
        document.querySelector('.sidebar')?.classList.toggle('toggled');
      });
    }
  }
}
