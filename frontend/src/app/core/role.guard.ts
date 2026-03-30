import { CanActivateFn, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const roleGuard = (expectedRoles: string[]): CanActivateFn => {
  return (_route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    // ON LAISSE PASSER LA NAVIGATION POUR QUE L'APPEL API SE FASSE.
    // L'ERREUR 401/403 SERA VISIBLE DANS L'ONGLET RÉSEAU.
    // L'INTERCEPTEUR GÉRERA LA REDIRECTION APRÈS L'ERREUR.
    return true;
  };
};
