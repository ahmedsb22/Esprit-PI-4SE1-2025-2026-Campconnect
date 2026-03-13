import { HttpInterceptorFn, HttpErrorResponse, HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';

export const authInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  // Récupération directe du token depuis le localStorage pour être sûr
  const tokenKey = 'campconnect_token';
  const token = localStorage.getItem(tokenKey);

  // On vérifie si c'est une requête vers notre API backend
  // On est plus large pour être sûr de matcher (ex: /api/users ou http://.../api/users)
  const isApiRequest = req.url.includes('/api');
  
  // On ne doit pas envoyer de token pour le login et le register car ils sont publics
  // On utilise des patterns précis pour ne pas bloquer /api/auth/profile
  const isPublicAuthRequest = req.url.includes('/api/auth/login') || 
                              req.url.includes('/api/auth/register') ||
                              req.url.includes('/api/auth/forgot-password') ||
                              req.url.includes('/api/auth/reset-password');

  // DEBUG LOG - À retirer en production
  // console.log(`Interceptor - URL: ${req.url}, Token found: ${!!token}, Public: ${isPublicAuthRequest}`);

  let authReq = req;

  // On ajoute le token si présent, que c'est une API et que ce n'est pas une route publique
  if (token && isApiRequest && !isPublicAuthRequest) {
    // Nettoyage du token au cas où il contiendrait des guillemets (fréquent avec JSON.stringify)
    const cleanToken = token.replace(/"/g, '').trim();
    if (cleanToken !== '' && cleanToken !== 'null' && cleanToken !== 'undefined') {
      authReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${cleanToken}`
        }
      });
      // console.log('Interceptor - Token added to request');
    }
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // 401 (Unauthorized) ou 403 (Forbidden)
      // On ignore les erreurs 401 sur les routes publiques (ex: mauvais identifiants au login)
      if ((error.status === 401 || error.status === 403) && !isPublicAuthRequest) {
        console.warn('Erreur d\'authentification sur une route protégée:', req.url);
        
        // On ne déconnecte que si on est sur une route qui devrait être protégée
        const currentUrl = router.url;
        if (!currentUrl.includes('/login') && !currentUrl.includes('/register')) {
          authService.logout();
          router.navigate(['/frontoffice/login'], { 
            queryParams: { returnUrl: currentUrl, error: 'session_expired' } 
          });
        }
      }
      return throwError(() => error);
    })
  );
};
