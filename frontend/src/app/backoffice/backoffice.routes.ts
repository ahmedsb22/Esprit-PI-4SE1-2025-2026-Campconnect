import { Routes } from '@angular/router';

export const BACKOFFICE_ROUTES: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'sites',
    loadComponent: () => import('./sites/sites.component').then(m => m.SitesComponent)
  },
  {
    path: 'orders',
    loadComponent: () => import('./orders/orders.component').then(m => m.OrdersComponent)
  },
  {
    path: 'users',
    loadComponent: () => import('./users/users.component').then(m => m.UsersComponent)
  },
  {
    path: 'billing',
    loadComponent: () => import('./billing/billing.component').then(m => m.BillingComponent)
  },
  {
    path: 'contracts',
    loadComponent: () => import('./contracts/contracts.component').then(m => m.ContractsComponent)
  },
  {
    path: 'bookings',
    loadComponent: () => import('./bookings/bookings.component').then(m => m.BookingsComponent)
  }
];
