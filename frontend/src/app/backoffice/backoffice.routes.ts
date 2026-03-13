import { Routes } from '@angular/router';
import { roleGuard } from '../core/role.guard';

export const BACKOFFICE_ROUTES: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: 'dashboard',
    canActivate: [roleGuard(['ADMIN', 'OWNER'])],
    loadComponent: () => import('./dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'sites',
    canActivate: [roleGuard(['ADMIN', 'OWNER'])],
    loadComponent: () => import('./sites/sites.component').then(m => m.SitesComponent)
  },
  {
    path: 'orders',
    canActivate: [roleGuard(['ADMIN', 'OWNER'])],
    loadComponent: () => import('./orders/orders.component').then(m => m.OrdersComponent)
  },
  {
    path: 'users',
    canActivate: [roleGuard(['ADMIN'])],
    loadComponent: () => import('./users/users.component').then(m => m.UsersComponent)
  },
  {
    path: 'billing',
    canActivate: [roleGuard(['ADMIN', 'OWNER'])],
    loadComponent: () => import('./billing/billing.component').then(m => m.BillingComponent)
  },
  {
    path: 'contracts',
    canActivate: [roleGuard(['ADMIN', 'OWNER'])],
    loadComponent: () => import('./contracts/contracts.component').then(m => m.ContractsComponent)
  },
  {
    path: 'bookings',
    canActivate: [roleGuard(['ADMIN', 'OWNER'])],
    loadComponent: () => import('./bookings/bookings.component').then(m => m.BookingsComponent)
  }
];
