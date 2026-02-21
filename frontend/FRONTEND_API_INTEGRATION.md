# CampConnect Frontend - Documentation API Integration

## Vue d'ensemble

Le frontend Angular intègre complètement les APIs du backend Spring Boot pour la plateforme de gestion de camping CampConnect.

## Services disponibles

### 1. **AuthService** (`auth.service.ts`)
Gestion complète de l'authentification et du profil utilisateur.

#### Méthodes:
- `login(credentials: LoginRequest)`: Connexion utilisateur
- `register(data: RegisterRequest)`: Inscription d'un nouvel utilisateur
- `getCurrentUser()`: Récupération du profil connec
- `getUserById(id)`: Récupération profil par ID
- `updateProfile(id, user)`: Mise à jour du profil
- `changePassword(oldPassword, newPassword)`: Changement de mot de passe
- `logout()`: Déconnexion
- `getToken()`: Récupère le JWT token
- `getUser()`: Récupère les données utilisateur du localStorage
- `isAuthenticated()`: Vérifie l'authentification
- `isAdmin()`: Vérifie si admin
- `isProvider()`: Vérifie si propriétaire
- `isCamper()`: Vérifie si campeur

**URL Base**: `{apiUrl}/auth`

### 2. **SiteService** (`site.service.ts`)
Gestion des sites de camping avec CRUD complet.

#### Méthodes:
- `getAllSites()`: Tous les sites
- `getSiteById(id)`: Site par ID
- `getActiveSites()`: Sites actifs
- `getSitesByOwner(ownerId)`: Sites du propriétaire
- `searchSites(name, location, priceMin, priceMax)`: Recherche avancée
- `createSite(site, ownerId)`: Création (owner requis)
- `updateSite(id, site)`: Mise à jour
- `deleteSite(id)`: Suppression
- `addImage(siteId, imageUrl, description)`: Ajouter image
- `addAmenity(siteId, name, icon)`: Ajouter commodité

**URL Base**: `{apiUrl}/sites`

### 3. **EquipmentService** (`equipment.service.ts`)
Gestion des équipements avec recherche et inventaire.

#### Méthodes:
- `getAllEquipment()`: Tous les équipements
- `getEquipmentById(id)`: Équipement par ID
- `getEquipmentByCategory(category)`: Par catégorie
- `getAvailableEquipment()`: Équipements disponibles (stock > 0)
- `getEquipmentBySite(siteId)`: Équipements du site
- `searchEquipment(name, category, priceMin, priceMax)`: Recherche
- `createEquipment(equipment, siteId)`: Création (owner)
- `updateEquipment(id, equipment)`: Mise à jour
- `deleteEquipment(id)`: Suppression
- `updateStock(id, quantity)`: Mise à jour stock

**URL Base**: `{apiUrl}/equipment`

### 4. **BookingService** (`booking.service.ts`)
Gestion des réservations de camping.

#### Méthodes:
- `getAllBookings()`: Toutes les réservations
- `getBookingById(id)`: Réservation par ID
- `getMyBookings(userId)`: Mes réservations
- `getSiteBookings(siteId)`: Réservations site
- `getUpcomingBookings(userId)`: Réservations à venir
- `getPastBookings(userId)`: Réservations passées
- `checkAvailability(siteId, startDate, endDate)`: Vérifier disponibilité
- `calculatePrice(siteId, startDate, endDate, guests)`: Calcul prix
- `createBooking(camperId, siteId, startDate, endDate, guests)`: Créer réservation
- `updateBooking(id, booking)`: Mise à jour
- `cancelBooking(id)`: Annulation réservation

**URL Base**: `{apiUrl}/bookings`

### 5. **OrderService** (`order.service.ts`)
Gestion des commandes d'équipement.

#### Méthodes:
- `getAllOrders()`: Toutes les commandes
- `getOrderById(id)`: Commande par ID
- `getMyOrders(userId)`: Mes commandes
- `createOrder(userId, equipmentId, quantity)`: Créer commande
- `updateOrderStatus(id, status)`: Mise à jour statut
- `cancelOrder(id)`: Annulation commande

**URL Base**: `{apiUrl}/orders`

### 6. **ContractService** (`contract.service.ts`)
Gestion des contrats de réservation avec audit.

#### Méthodes:
- `getAllContracts()`: Tous les contrats
- `getContractById(id)`: Contrat par ID
- `getContractByBooking(bookingId)`: Contrat réservation
- `getContractsByOwner(ownerId)`: Contrats propriétaire
- `getContractsByUser(userId)`: Contrats utilisateur
- `getAuditLog(id)`: Historique audit
- `generateContract(bookingId)`: Générer contrat
- `signContract(id, userId, signature, ipAddress)`: Signer contrat
- `updateContractStatus(id, status)`: Mise à jour statut

**URL Base**: `{apiUrl}/contracts`

### 7. **InvoiceService** (`invoice.service.ts`)
Gestion des factures et paiements.

#### Méthodes:
- `getAllInvoices()`: Toutes les factures
- `getInvoiceById(id)`: Facture par ID
- `getInvoiceByNumber(invoiceNumber)`: Par numéro
- `getOverdueInvoices()`: Factures en retard
- `generateInvoiceForBooking(bookingId)`: Générer pour réservation
- `generateInvoiceForOrder(orderId)`: Générer pour commande
- `recordPayment(invoiceId, amount, method, transactionId)`: Enregistrer paiement
- `updateInvoiceStatus(id, status)`: Mise à jour statut

**URL Base**: `{apiUrl}/invoices`

### 8. **AdminService** (`admin.service.ts`)
Administration des utilisateurs.

#### Méthodes:
- `getAllUsers()`: Tous les utilisateurs
- `getUserById(id)`: Utilisateur par ID
- `createUser(user)`: Créer utilisateur
- `updateUser(id, user)`: Mise à jour
- `updateUserStatus(id, active)`: Activer/Désactiver
- `deleteUser(id)`: Supprimer utilisateur

**URL Base**: `{apiUrl}/admin/users`

### 9. **AnalyticsService** (`analytics.service.ts`)
Statistiques et analytiques.

#### Méthodes:
- `getDashboardStats()`: Stats tableau de bord
- `getBookingStats()`: Stats réservations
- `getRevenueStats()`: Stats revenus
- `getEquipmentStats()`: Stats équipements
- `getUserStats()`: Stats utilisateurs

**URL Base**: `{apiUrl}/analytics`

## Configuration HTTP

### Environment
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8089/api'
};
```

### Interceptor (JWT Authentication)
L'intercepteur `authInterceptor` ajoute automatiquement le token JWT dans tous les headers:
```
Authorization: Bearer {token}
```

### CORS Configuration
Le backend accepte les requêtes depuis:
- `http://localhost:4200` (Angular dev server)
- `http://localhost:4201`
- `http://127.0.0.1:4200`
- `http://localhost:3000` / `3001` (autres ports de développement)

## Gestion des erreurs

Tous les services implémentent une gestion d'erreur uniforme:
```typescript
.pipe(
  catchError(error => this.handleError('methodName', error))
)
```

Les erreurs sont loggées dans la console et retournées comme `throwError`.

## Stockage Local (localStorage)

- `campconnect_token`: JWT Token d'authentification
- `campconnect_user`: Données utilisateur (JSON stringifié)

## Types/Interfaces Disponibles

### CampingSite
```typescript
{
  id?: number;
  name: string;
  description?: string;
  location?: string;
  latitude?: number;
  longitude?: number;
  totalPlots?: number;
  availablePlots?: number;
  pricePerNight?: number;
  owner?: any;
  rating?: number;
  status?: string;
  images?: Array<{ id, url, description }>;
  amenities?: Array<{ id, name, icon }>;
  createdAt?: string;
  updatedAt?: string;
}
```

### Equipment
```typescript
{
  id?: number;
  name: string;
  description?: string;
  category?: string;
  price?: number;
  stock?: number;
  sold?: number;
  images?: string[];
  site?: any;
  createdAt?: string;
  updatedAt?: string;
}
```

### Booking
```typescript
{
  id?: number;
  bookingNumber?: string;
  camper?: any;
  site?: any;
  startDate: string;
  endDate: string;
  guests: number;
  totalPrice?: number;
  status?: string;
  createdAt?: string;
  updatedAt?: string;
}
```

### User
```typescript
{
  id?: number;
  firstName: string;
  lastName: string;
  email: string;
  password?: string;
  roles?: string[];
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}
```

## Utilisation des Services

### Exemple 1: Récupérer tous les sites

```typescript
import { SiteService } from './services/site.service';
import { inject } from '@angular/core';

export class SitesComponent {
  private siteService = inject(SiteService);
  sites: any[] = [];

  ngOnInit() {
    this.siteService.getAllSites().subscribe({
      next: (data) => this.sites = data,
      error: (err) => console.error('Erreur:', err.message)
    });
  }
}
```

### Exemple 2: Créer une réservation

```typescript
import { BookingService } from './services/booking.service';

export class BookingComponent {
  private bookingService = inject(BookingService);

  createReservation() {
    this.bookingService.createBooking(
      campId, 
      siteId, 
      '2024-06-01', 
      '2024-06-07', 
      4
    ).subscribe({
      next: (booking) => console.log('Réservation créée:', booking),
      error: (err) => console.error('Erreur:', err.message)
    });
  }
}
```

### Exemple 3: Authentification

```typescript
import { AuthService, LoginRequest } from './services/auth.service';

export class LoginComponent {
  private authService = inject(AuthService);

  login(email: string, password: string) {
    const credentials: LoginRequest = { email, password };
    this.authService.login(credentials).subscribe({
      next: (response) => {
        console.log('Connecté:', response.user);
        // Naviguer vers dashboard
      },
      error: (err) => console.error('Erreur de connexion:', err.message)
    });
  }
}
```

## Architecture

```
frontend/src/app/
├── services/
│   ├── auth.service.ts ✓
│   ├── site.service.ts ✓
│   ├── equipment.service.ts ✓
│   ├── booking.service.ts ✓
│   ├── order.service.ts ✓
│   ├── contract.service.ts ✓
│   ├── invoice.service.ts ✓
│   ├── admin.service.ts ✓
│   └── analytics.service.ts ✓
├── core/
│   ├── auth.interceptor.ts ✓
│   └── auth.guard.ts ✓
├── backoffice/ (Admin dashboard)
├── frontoffice/ (Public site)
└── app.routes.ts
```

## Status de l'Intégration

✅ **Complété**:
- Services d'authentification
- Services CRUD pour sites
- Services CRUD pour équipements  
- Services CRUD pour réservations
- Services CRUD pour commandes
- Services gestion contrats
- Services gestion factures/paiements
- Services administration utilisateurs
- Services d'analytiques
- HTTP Interceptor JWT
- Gestion d'erreurs uniforme
- CORS correctement configuré

## Notes Importantes

1. **JWT Token**: Ajouté automatiquement par l'interceptor
2. **CRUD Complet**: Tous les services supportent Create/Read/Update/Delete
3. **Types TypeScript**: Interfaces définies pour la sécurité des types
4. **Gestion d'Erreurs**: Errors loggées et retournées avec messages
5. **Backend URL**: Configurable via `environment.ts`
6. **Stateless**: Utilise JWT au lieu de sessions (SessionCreationPolicy.STATELESS)

## Tests CRUD Recommandés

```bash
# Tester sites
curl http://localhost:8089/api/sites
curl -X POST http://localhost:8089/api/sites -H "Content-Type: application/json" -d "{...}"

# Tester équipements
curl http://localhost:8089/api/equipment

# Tester authentification
curl -X POST http://localhost:8089/api/auth/login -H "Content-Type: application/json" -d "{\"email\":\"...\",\"password\":\"...\"}"

# Tester réservations
curl http://localhost:8089/api/bookings
```

## Endpoints Disponibles

### Authentification
- `POST /api/auth/login` - Connexion
- `POST /api/auth/register` - Inscription
- `GET /api/auth/profile` - Profil connecté
- `GET /api/auth/user/{id}` - Utilisateur par ID
- `PUT /api/auth/profile/{id}` - Mise à jour profil
- `POST /api/auth/change-password` - Changement mot de passe

### Sites
- `GET /api/sites` - Tous les sites
- `GET /api/sites/{id}` - Site par ID
- `GET /api/sites/active` - Sites actifs
- `GET /api/sites/owner/{ownerId}` - Sites du propriétaire
- `GET /api/sites/search` - Recherche sites
- `POST /api/sites` - Créer site
- `PUT /api/sites/{id}` - Mettre à jour
- `DELETE /api/sites/{id}` - Supprimer
- `POST /api/sites/{id}/images` - Ajouter image
- `POST /api/sites/{id}/amenities` - Ajouter commodité

### Équipements
- `GET /api/equipment` - Tous les équipements
- `GET /api/equipment/{id}` - Équipement par ID
- `GET /api/equipment/available` - Disponibles
- `GET /api/equipment/category/{category}` - Par catégorie
- `GET /api/equipment/site/{siteId}` - Du site
- `GET /api/equipment/search` - Recherche
- `POST /api/equipment` - Créer
- `PUT /api/equipment/{id}` - Mettre à jour
- `DELETE /api/equipment/{id}` - Supprimer
- `PUT /api/equipment/{id}/stock` - Mettre à jour stock

### Réservations
- `GET /api/bookings` - Toutes
- `GET /api/bookings/{id}` - Par ID
- `GET /api/bookings/my-bookings/{userId}` - Mes réservations
- `GET /api/bookings/site/{siteId}` - Du site
- `GET /api/bookings/upcoming/{userId}` - À venir
- `GET /api/bookings/past/{userId}` - Passées
- `GET /api/bookings/check-availability` - Vérifier disponibilité
- `GET /api/bookings/calculate-price` - Calculer prix
- `POST /api/bookings` - Créer
- `PUT /api/bookings/{id}` - Mettre à jour
- `DELETE /api/bookings/{id}` - Annuler

### Commandes
- `GET /api/orders` - Toutes
- `GET /api/orders/{id}` - Par ID
- `GET /api/orders/my-orders/{userId}` - Mes commandes
- `POST /api/orders` - Créer
- `PUT /api/orders/{id}/status` - Mettre à jour statut
- `DELETE /api/orders/{id}` - Annuler

### Contrats
- `GET /api/contracts` - Tous
- `GET /api/contracts/{id}` - Par ID
- `GET /api/contracts/booking/{bookingId}` - De la réservation
- `GET /api/contracts/owner/{ownerId}` - Du propriétaire
- `GET /api/contracts/user/{userId}` - De l'utilisateur
- `GET /api/contracts/{id}/audit` - Audit
- `POST /api/contracts/generate` - Générer
- `POST /api/contracts/{id}/sign` - Signer
- `PUT /api/contracts/{id}/status` - Mettre à jour statut

### Factures
- `GET /api/invoices` - Toutes
- `GET /api/invoices/{id}` - Par ID
- `GET /api/invoices/number/{invoiceNumber}` - Par numéro
- `GET /api/invoices/overdue` - En retard
- `POST /api/invoices/booking/{bookingId}` - Générer pour réservation
- `POST /api/invoices/order/{orderId}` - Générer pour commande
- `POST /api/invoices/{id}/payment` - Enregistrer paiement
- `PUT /api/invoices/{id}/status` - Mettre à jour statut

### Admin
- `GET /api/admin/users` - Tous les utilisateurs
- `GET /api/admin/users/{id}` - Utilisateur par ID
- `POST /api/admin/users` - Créer utilisateur
- `PUT /api/admin/users/{id}` - Mettre à jour
- `PUT /api/admin/users/{id}/status` - Changer statut
- `DELETE /api/admin/users/{id}` - Supprimer

### Analytics
- `GET /api/analytics/dashboard` - Stats tableau de bord
- `GET /api/analytics/bookings` - Stats réservations
- `GET /api/analytics/revenue` - Stats revenus
- `GET /api/analytics/equipment` - Stats équipements
- `GET /api/analytics/users` - Stats utilisateurs
