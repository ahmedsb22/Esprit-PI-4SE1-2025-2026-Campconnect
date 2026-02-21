import { Routes } from '@angular/router';

export const FRONTOFFICE_ROUTES: Routes = [
  {
    path: '',
    redirectTo: 'home',
    pathMatch: 'full'
  },
  {
    path: 'home',
    loadComponent: () => import('./home/home.component').then(m => m.HomeComponent)
  },
  {
    path: 'camping-sites',
    loadComponent: () => import('./camping-sites/camping-sites.component').then(m => m.CampingSitesComponent)
  },
  {
    path: 'equipment',
    loadComponent: () => import('./equipment/equipment.component').then(m => m.EquipmentComponent)
  },
  {
    path: 'login',
    loadComponent: () => import('./login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./register/register.component').then(m => m.RegisterComponent)
  }
];
