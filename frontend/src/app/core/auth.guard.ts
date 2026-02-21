import { CanActivateFn } from '@angular/router';

// No authentication required — all routes are public
export const AuthGuard: CanActivateFn = () => true;
