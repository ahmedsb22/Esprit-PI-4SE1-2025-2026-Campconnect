import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss']
})
export class UsersComponent implements OnInit, AfterViewInit {
  users: any[] = [];
  filteredUsers: any[] = [];

  filterRole = '';
  filterStatus = '';

  newUser = {
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    role: 'CAMPER'
  };

  private apiUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadUsers();
  }

  ngAfterViewInit() {
    setTimeout(() => {
      this.initDataTable();
      this.initBackofficeScripts();
    }, 100);
  }

  loadUsers() {
    this.http.get<any[]>(this.apiUrl).subscribe({
      next: (users) => {
        this.users = users.map(user => ({
          id: user.id,
          name: `${user.firstName ?? ''} ${user.lastName ?? ''}`.trim() || user.email,
          email: user.email,
          role: (user.roles && user.roles.length > 0) ? user.roles[0].toLowerCase() : 'user',
          status: user.active !== false,
          lastLogin: user.updatedAt ?? user.createdAt ?? '-',
          avatar: user.profileImageUrl || 'assets/backoffice-vendor/img/undraw_profile.svg'
        }));
        this.filteredUsers = [...this.users];
        this.refreshTable();
      },
      error: (err) => {
        console.error('Failed to load users', err);
      }
    });
  }

  toggleUserStatus(user: any) {
    const active = !user.status;
    this.http.put(`${this.apiUrl}/${user.id}/status`, null, { params: { active } as any }).subscribe({
      next: () => {
        user.status = active;
      },
      error: (err) => console.error('Failed to update status', err)
    });
  }

  createUser() {
    if (!this.newUser.firstName || !this.newUser.lastName || !this.newUser.email || !this.newUser.password) {
      alert('Please fill all fields');
      return;
    }
    if (this.newUser.password.length < 8) {
      alert('Password must be at least 8 characters');
      return;
    }
    const payload = {
      email: this.newUser.email,
      password: this.newUser.password,
      firstName: this.newUser.firstName,
      lastName: this.newUser.lastName,
      roles: [this.newUser.role],
      active: true
    };
    this.http.post<any>(this.apiUrl, payload).subscribe({
      next: () => {
        this.newUser = { firstName: '', lastName: '', email: '', password: '', role: 'CAMPER' };
        this.loadUsers();
      },
      error: (err) => {
        console.error('Failed to create user', err);
        alert(err.error?.message || 'Failed to create user');
      }
    });
  }

  deleteUser(id: number) {
    const user = this.users.find(u => u.id === id);
    if (user && confirm(`Delete user ${user.name}?`)) {
      this.http.delete(`${this.apiUrl}/${id}`).subscribe({
        next: () => {
          this.loadUsers();
        },
        error: (err) => console.error('Failed to delete user', err)
      });
    }
  }

  applyFilters() {
    this.filteredUsers = this.users.filter(user => {
      const roleMatch = this.filterRole ? user.role === this.filterRole : true;
      const statusMatch = this.filterStatus ? (this.filterStatus === 'active' ? user.status : !user.status) : true;
      return roleMatch && statusMatch;
    });
    this.refreshTable();
  }

  resetFilters() {
    this.filterRole = '';
    this.filterStatus = '';
    this.filteredUsers = [...this.users];
    this.refreshTable();
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
