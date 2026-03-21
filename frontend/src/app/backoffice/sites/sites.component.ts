import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-sites',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './sites.component.html',
  styleUrls: ['./sites.component.scss']
})
export class SitesComponent implements OnInit, AfterViewInit {
  sites: any[] = [];
  editingSite: any = null;

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
        this.sites = sites.map(site => {
          // Déterminer le statut à partir de isActive et isVerified
          let status = 'pending';
          if (site.isVerified === true && site.isActive === true) {
            status = 'approved';
          } else if (site.isVerified === false || site.isActive === false) {
            status = 'rejected';
          }
          
          return {
            id: site.id,
            name: site.name,
            description: site.description,
            pricePerNight: site.pricePerNight,
            owner: site.owner ? `${site.owner.firstName ?? ''} ${site.owner.lastName ?? ''}`.trim() : (site.ownerName || 'N/A'),
            location: site.location,
            date: site.createdAt ?? '-',
            status: status,
            image: site.imageUrl || 'assets/img/placeholder.svg',
            isActive: site.isActive,
            isVerified: site.isVerified
          };
        });
        this.refreshStats();
        this.refreshTable();
      },
      error: (err) => {
        console.error('Failed to load sites', err);
        alert('Erreur lors du chargement des sites: ' + (err.error?.message || err.message));
      }
    });
  }

  approveSite(id: number) {
    if (!confirm('Approuver ce site de camping ?')) return;
    
    this.http.put(`${this.apiUrl}/${id}/approve`, {}).subscribe({
      next: () => {
        alert('Site approuvé avec succès !');
        this.loadSites();
      },
      error: (err) => {
        console.error('Failed to approve site', err);
        alert('Erreur lors de l\'approbation: ' + (err.error?.message || err.message));
      }
    });
  }

  rejectSite(id: number) {
    if (!confirm('Rejeter ce site de camping ?')) return;
    
    this.http.put(`${this.apiUrl}/${id}/reject`, {}).subscribe({
      next: () => {
        alert('Site rejeté.');
        this.loadSites();
      },
      error: (err) => {
        console.error('Failed to reject site', err);
        alert('Erreur lors du rejet: ' + (err.error?.message || err.message));
      }
    });
  }

  viewSite(id: number) {
    this.http.get<any>(`${this.apiUrl}/${id}`).subscribe({
      next: (site) => {
        const status = site.isVerified && site.isActive ? 'Approuvé' : 
                      (!site.isVerified || !site.isActive) ? 'Rejeté' : 'En attente';
        const details = `Site: ${site.name || 'N/A'}\n` +
                       `Localisation: ${site.location || 'N/A'}\n` +
                       `Prix par nuit: ${site.pricePerNight || 0} TND\n` +
                       `Capacité: ${site.capacity || 'N/A'} personnes\n` +
                       `Catégorie: ${site.category || 'N/A'}\n` +
                       `Statut: ${status}\n` +
                       `Description: ${site.description || 'Aucune description'}`;
        alert(details);
      },
      error: (err) => {
        console.error('Failed to fetch site', err);
        alert('Erreur lors du chargement des détails: ' + (err.error?.message || err.message));
      }
    });
  }

  deleteSite(id: number) {
    if (!confirm('Êtes-vous sûr de vouloir supprimer ce site ?')) return;
    
    this.http.delete(`${this.apiUrl}/${id}`).subscribe({
      next: () => {
        alert('Site supprimé avec succès.');
        this.loadSites();
      },
      error: (err) => {
        console.error('Failed to delete site', err);
        alert('Erreur lors de la suppression.');
      }
    });
  }

  editSite(site: any) {
    this.editingSite = { ...site };
    // Show modal
    const modalElement = document.getElementById('editSiteModal');
    if (modalElement && (window as any).bootstrap) {
      const modal = new (window as any).bootstrap.Modal(modalElement);
      modal.show();
    }
  }

  updateSite() {
    if (!this.editingSite.name || !this.editingSite.location || !this.editingSite.pricePerNight) {
      alert('Veuillez remplir tous les champs obligatoires.');
      return;
    }
    
    this.http.put(`${this.apiUrl}/${this.editingSite.id}`, this.editingSite).subscribe({
      next: () => {
        // Hide modal
        const modalElement = document.getElementById('editSiteModal');
        if (modalElement && (window as any).bootstrap) {
          const modal = (window as any).bootstrap.Modal.getInstance(modalElement);
          if (modal) modal.hide();
        }
        this.editingSite = null;
        alert('Site mis à jour avec succès !');
        this.loadSites();
      },
      error: (err) => {
        console.error('Failed to update site', err);
        alert('Erreur lors de la mise à jour.');
      }
    });
  }

  refreshStats() {
    this.stats.pending = this.sites.filter(site => site.status === 'pending').length;
    this.stats.approved = this.sites.filter(site => site.status === 'approved').length;
    this.stats.rejected = this.sites.filter(site => site.status === 'rejected').length;
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
