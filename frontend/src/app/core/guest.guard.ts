import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return true;
  }

  // If already authenticated, redirect based on role
  const role = authService.getRole();
  if (role === 'ADMIN') {
    router.navigate(['/backoffice/dashboard']);
  } else if (role === 'OWNER') {
    router.navigate(['/backoffice/sites']);
  } else {
    router.navigate(['/frontoffice/home']);
  }
  
  return false;
};
