import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-contracts',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './contracts.component.html',
  styleUrls: ['./contracts.component.scss']
})
export class ContractsComponent implements OnInit, AfterViewInit {
  contracts: any[] = [];
  stats = {
    active: 0,
    pending: 0,
    expired: 0
  };

  contractForm = {
    bookingId: ''
  };

  private apiUrl = `${environment.apiUrl}/contracts`;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadContracts();
  }

  ngAfterViewInit() {
    setTimeout(() => {
      this.initDataTable();
      this.initBackofficeScripts();
    }, 100);
  }

  loadContracts() {
    this.http.get<any[]>(this.apiUrl).subscribe({
      next: (contracts) => {
        this.contracts = contracts.map(contract => ({
          id: contract.id,
          contractId: contract.contractNumber || `CNT-${contract.id}`,
          reservationNumber: contract.reservationNumber || '-',
          isSigned: contract.isSigned || false,
          status: (contract.status || 'DRAFT').toLowerCase(),
          content: contract.terms || ''
        }));
        this.refreshStats();
        this.refreshTable();
      },
      error: (err) => console.error('Failed to load contracts', err)
    });
  }

  viewContract(id: number) {
    const contract = this.contracts.find(c => c.id === id);
    if (contract) {
      alert(contract.content || 'No contract content available');
    }
  }

  downloadContract(id: number) {
    const contract = this.contracts.find(c => c.id === id);
    if (contract) {
      const blob = new Blob([contract.content || ''], { type: 'text/plain;charset=utf-8' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `${contract.contractId}.txt`;
      link.click();
      URL.revokeObjectURL(url);
    }
  }

  renewContract(id: number) {
    const contract = this.contracts.find(c => c.id === id);
    if (!contract) return;
    this.http.put(`${this.apiUrl}/${id}`, { ...contract, status: 'SIGNED' }).subscribe({
      next: () => this.loadContracts(),
      error: (err) => console.error('Failed to renew contract', err)
    });
  }

  createContract() {
    if (!this.contractForm.bookingId) {
      alert('Booking ID is required');
      return;
    }
    const bookingId = Number(this.contractForm.bookingId);
    if (Number.isNaN(bookingId)) {
      alert('Booking ID must be a number');
      return;
    }
    const payload = {
      contractNumber: `CNT-${Date.now()}`,
      terms: 'Standard camping contract terms.',
      status: 'DRAFT',
      reservation: { id: bookingId }
    };
    this.http.post(this.apiUrl, payload).subscribe({
      next: () => {
        this.contractForm.bookingId = '';
        this.loadContracts();
      },
      error: (err) => console.error('Failed to create contract', err)
    });
  }

  refreshStats() {
    this.stats.active = this.contracts.filter(c => c.status === 'signed').length;
    this.stats.pending = this.contracts.filter(c => c.status === 'pending_signature' || c.status === 'draft').length;
    this.stats.expired = this.contracts.filter(c => c.status === 'expired').length;
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
