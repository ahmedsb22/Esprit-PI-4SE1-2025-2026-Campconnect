import { CanActivateFn, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const roleGuard = (expectedRoles: string[]): CanActivateFn => {
  return (_route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
      router.navigate(['/frontoffice/login'], { queryParams: { returnUrl: state.url } });
      return false;
    }

    const hasRequiredRole = expectedRoles.some(role => authService.hasRole(role));

    if (hasRequiredRole) {
      return true;
    }

    // Redirect to home if unauthorized
    router.navigate(['/frontoffice/home']);
    return false;
  };
};
