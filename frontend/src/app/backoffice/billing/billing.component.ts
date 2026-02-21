import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-billing',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './billing.component.html',
  styleUrls: ['./billing.component.scss']
})
export class BillingComponent implements OnInit, AfterViewInit {
  invoices: any[] = [];

  stats = {
    totalRevenue: 0,
    paid: 0,
    pending: 0,
    cancelled: 0
  };

  invoiceForm = {
    bookingId: '',
    orderId: ''
  };

  private apiUrl = `${environment.apiUrl}/invoices`;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadInvoices();
  }

  ngAfterViewInit() {
    setTimeout(() => {
      this.initDataTable();
      this.initBackofficeScripts();
    }, 100);
  }

  loadInvoices() {
    this.http.get<any[]>(this.apiUrl).subscribe({
      next: (invoices) => {
        this.invoices = invoices.map(invoice => ({
          id: invoice.id,
          invoiceId: invoice.invoiceNumber || `INV-${invoice.id}`,
          client: invoice.camperName || 'N/A',
          clientEmail: invoice.camperEmail || '',
          siteName: invoice.siteName || '',
          date: invoice.issuedAt || '-',
          amount: invoice.totalAmount || 0,
          status: (invoice.status || 'DRAFT').toLowerCase()
        }));
        this.refreshStats();
        this.refreshTable();
      },
      error: (err) => console.error('Failed to load invoices', err)
    });
  }

  viewInvoice(invoiceId: string) {
    alert(`Invoice: ${invoiceId}`);
  }

  downloadInvoice(invoiceId: string) {
    const invoice = this.invoices.find(i => i.invoiceId === invoiceId);
    if (invoice) {
      const content = `Invoice ${invoice.invoiceId}\nAmount: ${invoice.amount} TND\nStatus: ${invoice.status}`;
      const blob = new Blob([content], { type: 'text/plain;charset=utf-8' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `${invoice.invoiceId}.txt`;
      link.click();
      URL.revokeObjectURL(url);
    }
  }

  deleteInvoice(invoiceId: string) {
    const id = this.findInvoiceId(invoiceId);
    if (!id) return;
    if (confirm(`Delete invoice ${invoiceId}?`)) {
      this.http.delete(`${this.apiUrl}/${id}`).subscribe({
        next: () => this.loadInvoices(),
        error: (err) => console.error('Failed to delete invoice', err)
      });
    }
  }

  createInvoice() {
    if (!this.invoiceForm.bookingId && !this.invoiceForm.orderId) {
      alert('Provide booking ID or order ID');
      return;
    }
    const bookingId = this.invoiceForm.bookingId ? Number(this.invoiceForm.bookingId) : null;
    const orderId = this.invoiceForm.orderId ? Number(this.invoiceForm.orderId) : null;
    if (bookingId !== null && Number.isNaN(bookingId)) { alert('Booking ID must be a number'); return; }
    if (orderId !== null && Number.isNaN(orderId)) { alert('Order ID must be a number'); return; }
    const payload: any = {
      invoiceNumber: `INV-${Date.now()}`,
      status: 'DRAFT',
      totalAmount: 0
    };
    if (bookingId) payload.reservation = { id: bookingId };
    if (orderId) payload.equipmentOrder = { id: orderId };
    this.http.post(this.apiUrl, payload).subscribe({
      next: () => {
        this.invoiceForm = { bookingId: '', orderId: '' };
        this.loadInvoices();
      },
      error: (err) => console.error('Failed to create invoice', err)
    });
  }

  applyFilters() {
    this.refreshTable();
  }

  findInvoiceId(invoiceNumber: string): number | null {
    const invoice = this.invoices.find(i => i.invoiceId === invoiceNumber);
    return invoice ? invoice.id : null;
  }

  refreshStats() {
    this.stats.totalRevenue = this.invoices.reduce((sum, inv) => sum + (inv.amount || 0), 0);
    this.stats.paid = this.invoices.filter(inv => inv.status === 'paid').reduce((sum, inv) => sum + (inv.amount || 0), 0);
    this.stats.pending = this.invoices.filter(inv => inv.status === 'sent').reduce((sum, inv) => sum + (inv.amount || 0), 0);
    this.stats.cancelled = this.invoices.filter(inv => inv.status === 'cancelled').reduce((sum, inv) => sum + (inv.amount || 0), 0);
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
        (window as any).$(table).DataTable();
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
