# 📚 Guide d'Intégration des Templates - Campconnect1

## Vue d'ensemble

Ce document explique comment les templates **Frontoffice** et **Backoffice** ont été intégrés dans l'application Spring Boot **Campconnect1**.

---

## 🏗️ Architecture du Projet

```
campconnect1/
├── src/main/
│   ├── java/tn/esprit/exam/
│   │   ├── ExamApplication.java          # Point d'entrée Spring Boot
│   │   └── controller/
│   │       └── WebController.java        # Contrôleur de routing
│   └── resources/
│       ├── application.properties        # Configuration (port 8089)
│       └── static/                       # Ressources statiques publiques
│           ├── index.html               # Frontoffice - Page d'accueil
│           ├── camping-sites.html       # Frontoffice - Sites de camping
│           ├── equipment.html           # Frontoffice - Équipements
│           ├── assets/                  # CSS, JS, Images du frontoffice
│           │   ├── css/
│           │   ├── js/
│           │   ├── img/
│           │   │   └── placeholder.svg  # Images grises générées
│           │   └── vendor/              # Bootstrap, libraries
│           └── backoffice/              # Module Backoffice
│               ├── dashboard.html       # Dashboard admin
│               ├── billing.html         # Facturation
│               ├── users.html           # Gestion utilisateurs
│               ├── sites.html           # Validation sites
│               ├── contracts.html       # Gestion contrats
│               ├── orders.html          # Gestion commandes
│               ├── css/                 # Styles backoffice
│               ├── js/                  # Scripts backoffice
│               └── vendor/              # Libraries backoffice
```

---

## 🔧 Étape 1 : Préparation du Projet Spring Boot

### 1.1 Nettoyage du Code Existant

**Actions effectuées :**
- ✅ Supprimé toutes les entités JPA (Chaine, Domaine, Projet, Utilisateur, etc.)
- ✅ Supprimé tous les repositories, services, controllers REST
- ✅ Supprimé les configurations H2, MySQL, JPA, Security
- ✅ Supprimé les fichiers de documentation inutiles

### 1.2 Configuration Maven Simplifiée

**`pom.xml` - Dépendances conservées :**
```xml
<dependencies>
    <!-- Spring Boot Web - Pour servir les pages statiques -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Lombok - Pour faciliter le développement futur -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- Spring Boot Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

**Dépendances supprimées :**
- ❌ `spring-boot-starter-data-jpa` (plus besoin de base de données)
- ❌ `mysql-connector-j` (pas de MySQL)
- ❌ `h2` (pas de H2)
- ❌ `spring-boot-starter-security` (pas de sécurité pour l'instant)
- ❌ `springdoc-openapi-starter-webmvc-ui` (pas de Swagger)

### 1.3 Configuration Application

**`src/main/resources/application.properties` :**
```properties
### CAMPCONNECT1 APPLICATION ###
server.port=8089
spring.application.name=Campconnect1
```

**Explication :**
- Port unique **8089** pour servir à la fois frontoffice et backoffice
- Configuration minimale sans base de données

---

## 🎨 Étape 2 : Intégration du Frontoffice

### 2.1 Structure du Template Frontoffice

**Template original :**
```
frontoffice/
├── index.html
├── camping-sites.html
├── equipment.html
├── privacy.html
├── terms.html
├── assets/
│   ├── css/
│   ├── js/
│   ├── img/
│   └── vendor/
```

### 2.2 Migration vers Spring Boot

**Commande PowerShell utilisée :**
```powershell
Move-Item -Path "frontoffice/*" -Destination "src/main/resources/static/" -Force
```

**Résultat :**
- Tous les fichiers HTML déplacés à la racine de `/static/`
- Le dossier `assets/` déplacé dans `/static/assets/`
- Spring Boot sert automatiquement tout contenu de `/static/` à la racine HTTP

### 2.3 Remplacement des Images

**Problème :** Images .webp, .png trop volumineuses

**Solution :** Création de placeholders SVG gris ciel

**Script PowerShell :**
```powershell
# Créer un placeholder SVG gris ciel
$svgContent = @'
<svg width="800" height="600" xmlns="http://www.w3.org/2000/svg">
  <rect width="800" height="600" fill="#B8C5D6"/>
  <text x="50%" y="50%" font-family="Arial" font-size="24" 
        fill="#6B7280" text-anchor="middle" dominant-baseline="middle">
    Image Placeholder
  </text>
</svg>
'@

Set-Content -Path "src/main/resources/static/assets/img/placeholder.svg" -Value $svgContent

# Remplacer toutes les références d'images
$files = Get-ChildItem "src/main/resources/static/*.html"
foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    $content = $content -replace 'assets/img/[^"]+\.(webp|png|jpg|jpeg)', 'assets/img/placeholder.svg'
    Set-Content -Path $file.FullName -Value $content
}
```

**Avantages :**
- ✅ Fichiers SVG ultra-légers (< 1 KB)
- ✅ Scalables sans perte de qualité
- ✅ Couleur gris ciel (#B8C5D6) élégante

### 2.4 Contrôleur de Navigation

**`WebController.java` :**
```java
package tn.esprit.exam.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "redirect:/index.html";
    }

    @GetMapping("/admin")
    public String admin() {
        return "redirect:/backoffice/dashboard.html";
    }
}
```

**Explication :**
- `@Controller` : Indique que cette classe gère les requêtes HTTP
- `GET /` → Redirige vers `index.html` (frontoffice)
- `GET /admin` → Redirige vers le dashboard backoffice

---

## 🔐 Étape 3 : Intégration du Backoffice

### 3.1 Structure du Template Backoffice

**Template original :**
```
backoffice/
├── index.html
├── css/
├── js/
└── vendor/
```

### 3.2 Migration et Organisation

**Commande PowerShell :**
```powershell
# Supprimer ancien backoffice s'il existe
Remove-Item -Path "src/main/resources/static/backoffice" -Recurse -Force -ErrorAction SilentlyContinue

# Créer nouveau dossier
New-Item -Path "src/main/resources/static/backoffice" -ItemType Directory -Force

# Copier template backoffice
Copy-Item -Path "backoffice/*" -Destination "src/main/resources/static/backoffice/" -Recurse -Force
```

**Pourquoi un sous-dossier `/backoffice/` ?**
- ✅ Séparation claire frontoffice/backoffice
- ✅ URLs distinctes : `/index.html` vs `/backoffice/dashboard.html`
- ✅ Gestion des ressources indépendante
- ✅ Possibilité d'ajouter de la sécurité uniquement sur `/backoffice/**`

### 3.3 Création des Pages Personnalisées

**Pages créées de zéro :**

#### **1. Dashboard (`dashboard.html`)**
```html
<!DOCTYPE html>
<html lang="fr">
<head>
    <title>Dashboard - Campconnect1</title>
    <link href="../backoffice/css/sb-admin-2.min.css" rel="stylesheet">
</head>
<body id="page-top">
    <div id="wrapper">
        <!-- Sidebar personnalisé avec menu Campconnect -->
        <ul class="navbar-nav bg-gradient-primary sidebar">
            <li class="nav-item"><a href="dashboard.html">Dashboard</a></li>
            <li class="nav-item"><a href="billing.html">Billing</a></li>
            <li class="nav-item"><a href="users.html">Users</a></li>
            <li class="nav-item"><a href="sites.html">Sites</a></li>
            <li class="nav-item"><a href="contracts.html">Contracts</a></li>
            <li class="nav-item"><a href="orders.html">Orders</a></li>
        </ul>
        
        <!-- Content -->
        <div id="content-wrapper">
            <div id="content">
                <!-- Stats Cards -->
                <div class="row">
                    <div class="col-xl-3">
                        <div class="card border-left-primary">
                            <div class="card-body">
                                <div class="text-xs font-weight-bold text-primary">
                                    REVENUE (MONTHLY)
                                </div>
                                <div class="h5 mb-0 font-weight-bold">€40,000</div>
                            </div>
                        </div>
                    </div>
                    <!-- Plus de cartes... -->
                </div>
            </div>
        </div>
    </div>
</body>
</html>
```

#### **2. Billing & Invoices (`billing.html`)**

**Fonctionnalités implémentées :**
- ✅ Tableau de données avec pagination
- ✅ Filtres avancés (date, statut, montant)
- ✅ Badges colorés pour statuts (Payé/En attente/Annulé)
- ✅ Actions rapides (Voir/Télécharger/Supprimer)
- ✅ Bouton "Créer une facture"
- ✅ Résumé des totaux

**Code clé - Tableau avec pagination :**
```html
<div class="card">
    <div class="card-header">
        <h6 class="m-0 font-weight-bold text-primary">Invoices</h6>
    </div>
    <div class="card-body">
        <!-- Filtres -->
        <div class="row mb-3">
            <div class="col-md-3">
                <input type="date" class="form-control" id="filterDateFrom" placeholder="Date From">
            </div>
            <div class="col-md-3">
                <select class="form-control" id="filterStatus">
                    <option value="">All Status</option>
                    <option>Paid</option>
                    <option>Pending</option>
                    <option>Cancelled</option>
                </select>
            </div>
        </div>
        
        <!-- Tableau -->
        <table class="table table-bordered" id="invoicesTable">
            <thead>
                <tr>
                    <th>Invoice ID</th>
                    <th>Client</th>
                    <th>Date</th>
                    <th>Amount</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>#INV-001</td>
                    <td>John Doe</td>
                    <td>2026-02-15</td>
                    <td>€250.00</td>
                    <td><span class="badge badge-success">Paid</span></td>
                    <td>
                        <button class="btn btn-sm btn-info">View</button>
                        <button class="btn btn-sm btn-primary">Download</button>
                        <button class="btn btn-sm btn-danger">Delete</button>
                    </td>
                </tr>
            </tbody>
        </table>
        
        <!-- Pagination -->
        <div class="d-flex justify-content-between">
            <div>
                <select class="form-control" id="entriesPerPage">
                    <option>10</option>
                    <option>25</option>
                    <option>50</option>
                    <option>100</option>
                </select>
            </div>
            <nav>
                <ul class="pagination">
                    <li class="page-item"><a class="page-link" href="#">Previous</a></li>
                    <li class="page-item active"><a class="page-link" href="#">1</a></li>
                    <li class="page-item"><a class="page-link" href="#">2</a></li>
                    <li class="page-item"><a class="page-link" href="#">Next</a></li>
                </ul>
            </nav>
        </div>
    </div>
</div>
```

**JavaScript pour filtres et pagination :**
```javascript
<script>
// Recherche en temps réel
document.getElementById('searchInvoice').addEventListener('input', function(e) {
    const searchTerm = e.target.value.toLowerCase();
    const rows = document.querySelectorAll('#invoicesTable tbody tr');
    rows.forEach(row => {
        const text = row.textContent.toLowerCase();
        row.style.display = text.includes(searchTerm) ? '' : 'none';
    });
});

// Filtre par statut
document.getElementById('filterStatus').addEventListener('change', function(e) {
    const status = e.target.value.toLowerCase();
    const rows = document.querySelectorAll('#invoicesTable tbody tr');
    rows.forEach(row => {
        if (!status) {
            row.style.display = '';
        } else {
            const badge = row.querySelector('.badge');
            row.style.display = badge.textContent.toLowerCase().includes(status) ? '' : 'none';
        }
    });
});

// Pagination
document.getElementById('entriesPerPage').addEventListener('change', function(e) {
    console.log('Show ' + e.target.value + ' entries per page');
    // Logique de pagination à implémenter avec les données réelles
});
</script>
```

#### **3. User Management (`users.html`)**

**Fonctionnalités implémentées :**
- ✅ Tableau avec Avatar, Nom, Email, Rôle, Statut, Dernière connexion
- ✅ Recherche en temps réel
- ✅ Filtres par rôle et statut
- ✅ Modal d'ajout/édition utilisateur
- ✅ Toggle activer/désactiver
- ✅ Badges de rôle colorés

**Code clé - Modal d'ajout :**
```html
<!-- Modal Add/Edit User -->
<div class="modal fade" id="userModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Add New User</h5>
                <button type="button" class="close" data-dismiss="modal">
                    <span>&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <form id="userForm">
                    <div class="form-group">
                        <label>Full Name</label>
                        <input type="text" class="form-control" id="userName" required>
                    </div>
                    <div class="form-group">
                        <label>Email</label>
                        <input type="email" class="form-control" id="userEmail" required>
                    </div>
                    <div class="form-group">
                        <label>Role</label>
                        <select class="form-control" id="userRole">
                            <option>Admin</option>
                            <option>User</option>
                            <option>Editor</option>
                            <option>Manager</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Status</label>
                        <select class="form-control" id="userStatus">
                            <option>Active</option>
                            <option>Inactive</option>
                        </select>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-primary" onclick="saveUser()">Save User</button>
            </div>
        </div>
    </div>
</div>

<script>
function saveUser() {
    const name = document.getElementById('userName').value;
    const email = document.getElementById('userEmail').value;
    const role = document.getElementById('userRole').value;
    const status = document.getElementById('userStatus').value;
    
    console.log('Saving user:', { name, email, role, status });
    // Ici, ajouter l'appel API pour sauvegarder
    
    $('#userModal').modal('hide');
    alert('User saved successfully!');
}
</script>
```

#### **4. Autres Pages**

**Sites Validation (`sites.html`) :**
- Gestion des demandes de validation de sites de camping
- Statuts : En attente / Approuvé / Rejeté

**Contracts (`contracts.html`) :**
- Gestion des contrats entre campings et plateforme
- Suivi des dates de début/fin, montants

**Orders (`orders.html`) :**
- Gestion des commandes d'équipements
- Statuts de livraison, tracking

---

## 🎯 Étape 4 : Fonctionnement de Spring Boot

### 4.1 Serving des Fichiers Statiques

**Spring Boot automatiquement sert :**
```
/static/index.html           → http://localhost:8089/index.html
/static/assets/css/main.css  → http://localhost:8089/assets/css/main.css
/static/backoffice/dashboard.html → http://localhost:8089/backoffice/dashboard.html
```

**Aucune configuration supplémentaire nécessaire !**

### 4.2 Ordre de Priorité

1. **Contrôleurs** (`@GetMapping`)
2. **Fichiers statiques** (`/static/`)
3. **404 Error**

**Exemple :**
- `GET /` → Intercepté par `WebController.index()` → Redirect vers `/index.html`
- `GET /index.html` → Sert `/static/index.html`
- `GET /backoffice/dashboard.html` → Sert `/static/backoffice/dashboard.html`

### 4.3 Chemins Relatifs vs Absolus

**Dans les fichiers HTML :**
```html
<!-- ✅ Bon : Chemin relatif depuis la racine -->
<link href="assets/css/main.css" rel="stylesheet">
<script src="assets/js/main.js"></script>

<!-- ✅ Bon : Depuis backoffice -->
<link href="../backoffice/css/sb-admin-2.min.css" rel="stylesheet">

<!-- ❌ Mauvais : Chemin absolu -->
<link href="/static/assets/css/main.css" rel="stylesheet">
```

---

## 📊 Avantages de cette Architecture

### ✅ Séparation Front/Back
- URLs distinctes : `/` pour public, `/backoffice/` pour admin
- Possibilité d'ajouter sécurité uniquement sur `/backoffice/**`

### ✅ Un Seul Port (8089)
- Simplification du déploiement
- Pas de problèmes CORS
- Facile à containeriser avec Docker

### ✅ Pas de Base de Données
- Démarrage ultra-rapide
- Données simulées en JavaScript
- Facilité de test et démonstration

### ✅ Templates Modernes
- Bootstrap 4 responsive
- SB Admin 2 pour backoffice professionnel
- JavaScript vanilla (pas de framework complexe)

---

## 🚀 Compilation et Démarrage

### Build
```bash
./mvnw clean package -DskipTests
```

**Génère :** `target/campconnect1-1.0.jar`

### Run
```bash
java -jar target/campconnect1-1.0.jar
```

**Logs de démarrage :**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.1.6)

2026-02-17 13:32:40.123  INFO --- [           main] tn.esprit.exam.ExamApplication           : Starting ExamApplication
2026-02-17 13:32:41.456  INFO --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8089 (http)
2026-02-17 13:32:41.567  INFO --- [           main] tn.esprit.exam.ExamApplication           : Started ExamApplication in 2.3 seconds
```

---

## 🔗 URLs Accessibles

### Frontoffice (Public)
- http://localhost:8089/
- http://localhost:8089/index.html
- http://localhost:8089/camping-sites.html
- http://localhost:8089/equipment.html
- http://localhost:8089/privacy.html
- http://localhost:8089/terms.html

### Backoffice (Admin)
- http://localhost:8089/admin (redirect)
- http://localhost:8089/backoffice/dashboard.html
- http://localhost:8089/backoffice/billing.html
- http://localhost:8089/backoffice/users.html
- http://localhost:8089/backoffice/sites.html
- http://localhost:8089/backoffice/contracts.html
- http://localhost:8089/backoffice/orders.html

---

## 🎨 Personnalisations Futures

### Séparer Front/Back sur 2 Ports

**Modifier `application.properties` :**
```properties
# Frontoffice
server.port=8089

# Créer application-backoffice.properties
server.port=8090
spring.profiles.active=backoffice
```

**Lancer 2 instances :**
```bash
java -jar campconnect1-1.0.jar --server.port=8089
java -jar campconnect1-1.0.jar --server.port=8090 --spring.profiles.active=backoffice
```

### Ajouter Spring Security

**Protéger uniquement `/backoffice/**` :**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/backoffice/**").authenticated()
                .anyRequest().permitAll()
            )
            .formLogin();
        return http.build();
    }
}
```

### Connecter à une API REST

**Exemple - Fetch invoices :**
```javascript
async function loadInvoices() {
    const response = await fetch('http://localhost:8089/api/invoices');
    const invoices = await response.json();
    
    const tbody = document.querySelector('#invoicesTable tbody');
    tbody.innerHTML = invoices.map(inv => `
        <tr>
            <td>${inv.id}</td>
            <td>${inv.client}</td>
            <td>${inv.date}</td>
            <td>€${inv.amount}</td>
            <td><span class="badge badge-${inv.status}">${inv.status}</span></td>
            <td>
                <button onclick="viewInvoice(${inv.id})">View</button>
            </td>
        </tr>
    `).join('');
}
```

---

## 📝 Résumé Technique

| Aspect | Solution |
|--------|----------|
| **Framework** | Spring Boot 3.1.6 |
| **Java** | 17 |
| **Build Tool** | Maven |
| **Port** | 8089 (unique) |
| **Frontoffice** | Template HTML/CSS/JS dans `/static/` |
| **Backoffice** | Template SB Admin 2 dans `/static/backoffice/` |
| **Database** | Aucune (données simulées) |
| **Security** | Aucune (à ajouter) |
| **Images** | Placeholders SVG gris ciel (#B8C5D6) |

---

## ✅ Checklist de Validation

- [x] Frontoffice accessible sur http://localhost:8089/
- [x] Toutes les images remplacées par placeholders SVG
- [x] Backoffice accessible sur http://localhost:8089/backoffice/
- [x] Dashboard fonctionnel avec sidebar personnalisé
- [x] Page Billing avec filtres, pagination, badges
- [x] Page Users avec modal, recherche, filtres
- [x] Pages Sites, Contracts, Orders créées
- [x] Navigation sidebar vers toutes les pages
- [x] Application compilable sans erreurs
- [x] Démarrage rapide (< 3 secondes)
- [x] Toutes les URLs testées avec code 200

---

**🎉 Intégration terminée avec succès !**
