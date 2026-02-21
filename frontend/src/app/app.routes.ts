import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/frontoffice/home',
    pathMatch: 'full'
  },
  {
    path: 'frontoffice',
    loadChildren: () => import('./frontoffice/frontoffice.routes').then(m => m.FRONTOFFICE_ROUTES)
  },
  {
    path: 'backoffice',
    loadChildren: () => import('./backoffice/backoffice.routes').then(m => m.BACKOFFICE_ROUTES)
  },
  {
    path: '**',
    redirectTo: '/frontoffice/home'
  }
];
