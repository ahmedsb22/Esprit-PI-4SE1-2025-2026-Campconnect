import { HttpInterceptorFn, HttpErrorResponse, HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';

export const authInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  // On utilise le service pour récupérer le token
  const token = authService.getToken();

  // On vérifie si c'est une requête vers notre API backend
  // Avec le proxy configuré, toutes les requêtes API commencent par /api
  const isApiRequest = req.url.includes('/api');
  
  // On ne doit pas envoyer de token pour les routes publiques
  const isPublicAuthRequest = req.url.includes('/api/auth/login') || 
                              req.url.includes('/api/auth/register') ||
                              req.url.includes('/api/auth/forgot-password') ||
                              req.url.includes('/api/auth/reset-password');

  let authReq = req;

  // On ajoute le token si présent, que c'est une API et que ce n'est pas une route publique
  if (token && isApiRequest && !isPublicAuthRequest) {
    // Nettoyage rigoureux du token
    let cleanToken = token.trim();
    if (cleanToken.startsWith('"') && cleanToken.endsWith('"')) {
      cleanToken = cleanToken.substring(1, cleanToken.length - 1);
    }
    cleanToken = cleanToken.trim();

    if (cleanToken && cleanToken !== 'null' && cleanToken !== 'undefined') {
      authReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${cleanToken}`
        }
      });
    }
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // 401 (Unauthorized) ou 403 (Forbidden)
      // On ignore les erreurs 401 sur les routes publiques (ex: mauvais identifiants au login)
      if ((error.status === 401 || error.status === 403) && !isPublicAuthRequest) {
        console.warn(`INTERCEPTOR: Erreur ${error.status} sur ${req.url}`);
        
        // Délai de 1s pour voir la ligne rouge dans l'onglet Réseau
        setTimeout(() => {
          const currentUrl = router.url;
          if (!currentUrl.includes('/login') && !currentUrl.includes('/register')) {
            if (error.status === 401) {
              authService.logout();
              router.navigate(['/frontoffice/login'], { 
                queryParams: { returnUrl: currentUrl, error: 'session_expired' } 
              });
            } else {
              router.navigate(['/frontoffice/home'], { queryParams: { error: 'forbidden' } });
            }
          }
        }, 1000);
      }
      return throwError(() => error);
    })
  );
};
