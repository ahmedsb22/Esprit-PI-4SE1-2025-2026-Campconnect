# Flux d'Authentification & Guide de Test - CampConnect

Ce document explique le fonctionnement de la sécurité JWT (Spring Security 6.x) et comment tester les différents flux.

---

## 🔐 1. Flux Spring Security & JWT

### A. Inscription & Connexion
1. **Client (Angular)** : Envoie les identifiants au backend (`/api/auth/register` ou `/api/auth/login`).
2. **Backend (Spring)** : 
   - Vérifie les identifiants via `AuthenticationManager` et `CustomUserDetailsService`.
   - Si valide, `JwtService` génère un token JWT signé avec une clé secrète.
   - Retourne une `AuthResponse` contenant le token et les infos utilisateur (email, nom, rôles).
3. **Client (Angular)** : Stocke le token dans le `localStorage`.

### B. Requêtes Protégées
1. **Interceptor (Angular)** : Pour chaque requête sortante, le `authInterceptor` ajoute le header `Authorization: Bearer <token>`.
2. **Filter (Spring)** : Le `JwtAuthFilter` intercepte la requête :
   - Extrait le token du header.
   - Valide le token (expiration, signature).
   - Extrait l'email et charge l'utilisateur dans le `SecurityContextHolder`.
3. **Controller (Spring)** : Vérifie les autorisations (`@PreAuthorize` ou `requestMatchers`).

---

## 🧪 2. Comment Tester le Backend (Swagger / Postman)

### Accès à Swagger
- URL : `http://localhost:8089/swagger-ui.html`
- Vous y trouverez tous les endpoints `/api/auth/**`.

### Étapes de test :
1. **Register** : Utilisez `/api/auth/register` avec un JSON :
   ```json
   {
     "email": "test@example.com",
     "password": "password123",
     "firstName": "Test",
     "lastName": "User",
     "roles": ["CAMPER"]
   }
   ```
2. **Login** : Utilisez `/api/auth/login`. Récupérez le `token` dans la réponse.
3. **Tester un endpoint protégé** :
   - Dans Swagger, cliquez sur **Authorize** en haut à droite.
   - Saisissez votre token (format : `Bearer <votre_token>`).
   - Essayez d'accéder à `/api/auth/profile`.

---

## 🌐 3. Comment Tester dans le Navigateur (Angular)

### 🛡️ Identifiants de test (Mot de passe : `admin123` pour tous)
| Rôle | Email | Mot de passe |
| :--- | :--- | :--- |
| **Administrateur** | `admin@campconnect.tn` | `admin123` |
| **Camper (Client)** | `mohamed@campconnect.tn` | `admin123` |
| **Owner (Propriétaire)** | `youssef@campconnect.tn` | `admin123` |

---

## 🌐 3. Comment Tester dans le Navigateur (Angular)

### Flux de test recommandé :
1.  **Nettoyage** : Avant de commencer, videz le cache ou ouvrez une fenêtre de **navigation privée** pour éviter les vieux tokens.
2.  **Inscription (Register)** : 
    - Allez sur `http://localhost:4200/frontoffice/register`.
    - Remplissez le formulaire. Cela doit fonctionner sans erreur 401 car la route est publique.
3.  **Connexion (Login)** :
    - Allez sur `http://localhost:4200/frontoffice/login`.
    - Utilisez `admin@campconnect.tn` / `admin123`.
4.  **Accès Protégé** :
    - Une fois connecté en admin, allez sur `http://localhost:4200/backoffice/users`.
    - La liste des utilisateurs doit s'afficher car votre token est maintenant valide et injecté.
5.  **Protection des routes** : 
    - Déconnectez-vous.
    - Tentez d'accéder à `http://localhost:4200/backoffice/dashboard` -> Redirection automatique vers le login.
   - Saisissez un email existant.
   - Vérifiez la console du backend (ou votre boîte mail si configurée) pour voir le lien généré.
   - Utilisez le lien (ex: `/frontoffice/reset-password?token=...`) pour changer le mot de passe.
4. **Profil** : Une fois connecté, allez sur `/frontoffice/profile` pour modifier vos informations.

---

## ⚠️ Points d'attention
- **Clé Secrète** : Définie dans `application.properties` (`app.jwt.secret`). Elle doit rester privée.
- **Expiration** : Le token expire après 24h par défaut.
- **CORS** : Le backend autorise uniquement `http://localhost:4200`.
