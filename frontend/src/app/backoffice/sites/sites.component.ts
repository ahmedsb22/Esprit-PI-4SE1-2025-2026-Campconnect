import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-sites',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sites.component.html',
  styleUrls: ['./sites.component.scss']
})
export class SitesComponent implements OnInit, AfterViewInit {
  sites: any[] = [];

  stats = {
    pending: 0,
    approved: 0,
    rejected: 0
  };

  private apiUrl = `${environment.apiUrl}/sites`;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadSites();
  }

  ngAfterViewInit() {
    setTimeout(() => {
      this.initDataTable();
      this.initBackofficeScripts();
    }, 100);
  }

  loadSites() {
    this.http.get<any[]>(this.apiUrl).subscribe({
      next: (sites) => {
        this.sites = sites.map(site => ({
          id: site.id,
          name: site.name,
          description: site.description,
          pricePerNight: site.pricePerNight,
          owner: site.owner ? `${site.owner.firstName ?? ''} ${site.owner.lastName ?? ''}`.trim() : 'N/A',
          location: site.location,
          date: site.createdAt ?? '-',
          status: (site.status || 'PENDING').toLowerCase(),
          image: site.imageUrl || 'assets/img/placeholder.svg'
        }));
        this.refreshStats();
        this.refreshTable();
      },
      error: (err) => console.error('Failed to load sites', err)
    });
  }

  approveSite(id: number) {
    this.http.put(`${this.apiUrl}/${id}/approve`, {}).subscribe({
      next: () => this.loadSites(),
      error: (err) => console.error('Failed to approve site', err)
    });
  }

  rejectSite(id: number) {
    this.http.put(`${this.apiUrl}/${id}/reject`, {}).subscribe({
      next: () => this.loadSites(),
      error: (err) => console.error('Failed to reject site', err)
    });
  }

  viewSite(id: number) {
    this.http.get<any>(`${this.apiUrl}/${id}`).subscribe({
      next: (site) => {
        alert(`Site: ${site.name}\nLocation: ${site.location}\nPrice: ${site.pricePerNight} TND`);
      },
      error: (err) => console.error('Failed to fetch site', err)
    });
  }

  refreshStats() {
    this.stats.pending = this.sites.filter(site => site.status === 'pending').length;
    this.stats.approved = this.sites.filter(site => site.status === 'active').length;
    this.stats.rejected = this.sites.filter(site => site.status === 'inactive').length;
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

  initDataTable() {
    // DataTable will be initialized by the script in angular.json
    if ((window as any).$ && (window as any).$.fn.DataTable) {
      const table = document.querySelector('#dataTable') as any;
      if (table && !(window as any).$.fn.DataTable.isDataTable(table)) {
        (window as any).$(table).DataTable();
      }
    }
  }

  scrollToTop() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
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
