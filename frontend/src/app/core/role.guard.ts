import { CanActivateFn, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const roleGuard = (expectedRoles: string[]): CanActivateFn => {
  return (_route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    // Si l'utilisateur n'est pas authentifié, on redirige vers le login
    if (!authService.isAuthenticated()) {
      console.warn('GUARD: User not authenticated, redirecting to login');
      router.navigate(['/frontoffice/login'], { queryParams: { returnUrl: state.url } });
      return false;
    }

    // On vérifie s'il a au moins un des rôles requis
    const hasRequiredRole = expectedRoles.some(role => authService.hasRole(role));
    
    if (!hasRequiredRole) {
      console.warn(`GUARD: User doesn't have required roles: ${expectedRoles}`);
      router.navigate(['/frontoffice/home'], { queryParams: { error: 'forbidden' } });
      return false;
    }

    return true;
  };
};
