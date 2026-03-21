# CampConnect - Plateforme de Réservation de Camping

##  Description

CampConnect est une application web complète de gestion de réservations de sites de camping, développée avec Spring Boot (backend) et Angular (frontend). Le projet inclut la gestion des utilisateurs, des réservations, des équipements, des contrats, des factures, ainsi qu'un système de monitoring et de déploiement avec Docker et Kubernetes.

##  Technologies Utilisées

### Backend
- **Spring Boot 3.1.6** - Framework Java
- **Spring Security** - Authentification et autorisation
- **JWT** - Tokens d'authentification
- **Spring Data JPA** - Accès aux données
- **MySQL 8.0** - Base de données
- **Maven** - Gestion des dépendances
- **Spring Boot Actuator** - Monitoring et métriques
- **Prometheus** - Collecte de métriques

### Frontend
- **Angular 17** - Framework TypeScript
- **Bootstrap** - Interface utilisateur
- **RxJS** - Programmation réactive
- **Angular Standalone** - Architecture sans modules

### DevOps
- **Docker** - Conteneurisation
- **Docker Compose** - Orchestration locale
- **Kubernetes (KubeADM)** - Orchestration de conteneurs
- **GitHub Actions** - CI/CD
- **Prometheus** - Monitoring
- **Grafana** - Visualisation des métriques

##  Structure du Projet

```
campconnect1/
├── src/main/java/tn/esprit/exam/
│   ├── controller/      # Contrôleurs REST
│   ├── service/         # Services métier
│   ├── repository/      # Repositories JPA
│   ├── entity/          # Entités JPA
│   ├── dto/             # Data Transfer Objects
│   ├── security/        # Configuration Spring Security
│   └── exception/       # Gestion des exceptions
├── frontend/
│   ├── src/app/
│   │   ├── backoffice/  # Interface d'administration
│   │   ├── frontoffice/ # Interface publique
│   │   ├── services/    # Services Angular
│   │   └── core/        # Guards, interceptors
│   └── Dockerfile
├── k8s/                 # Configurations Kubernetes
├── monitoring/          # Configurations Prometheus/Grafana
├── .github/workflows/   # Pipelines CI/CD
└── docker-compose.yml   # Orchestration Docker

```

##  Démarrage Rapide

### Option 1: Docker Compose (Recommandé)

```bash
# Démarrer tous les services
docker-compose up -d --build

# Vérifier l'état
docker-compose ps

# Voir les logs
docker-compose logs -f backend
```

**Accès:**
- Frontend: http://localhost:4200
- Backend: http://localhost:8089
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin123)

### Option 2: Développement Local

#### Backend
```bash
# Compiler et lancer
mvn clean install
mvn spring-boot:run
```

#### Frontend
```bash
cd frontend
npm install
npm start
```

##  Documentation

Consultez les fichiers d'explication détaillés:

- **[EXPLICATION_CRUD_DTO.txt](EXPLICATION_CRUD_DTO.txt)** - CRUD complet et utilisation des DTO
- **[EXPLICATION_TESTS_UNITAIRES.txt](EXPLICATION_TESTS_UNITAIRES.txt)** - Tests backend et frontend
- **[EXPLICATION_DOCKER.txt](EXPLICATION_DOCKER.txt)** - Docker et Docker Compose
- **[EXPLICATION_KUBERNETES.txt](EXPLICATION_KUBERNETES.txt)** - Déploiement Kubernetes
- **[EXPLICATION_MONITORING.txt](EXPLICATION_MONITORING.txt)** - Prometheus et Grafana
- **[EXPLICATION_CI_CD.txt](EXPLICATION_CI_CD.txt)** - Pipelines CI/CD
- **[EXPLICATION_SPRING_SECURITY.txt](EXPLICATION_SPRING_SECURITY.txt)** - Sécurité complète
- **[GUIDE_DEMARRAGE_RAPIDE.txt](GUIDE_DEMARRAGE_RAPIDE.txt)** - Guide de démarrage

##  Sécurité

- **Spring Security** avec JWT
- **CORS** configuré pour Angular
- **Validation** des données (Jakarta Validation)
- **Gestion des rôles** (ADMIN, OWNER, CAMPER, PROVIDER)

**Note:** Pour la démonstration, toutes les routes API sont publiques. En production, activer les règles de sécurité dans `SecurityConfig.java`.

##  Tests

### Backend
```bash
mvn test
```

### Frontend
```bash
cd frontend
npm test
```

##  Docker

### Build des images
```bash
# Backend
docker build -f Dockerfile.backend -t campconnect-backend:latest .

# Frontend
cd frontend
docker build -t campconnect-frontend:latest .
```

##  Kubernetes

### Déploiement
```bash
# Créer les secrets
kubectl apply -f k8s/mysql-secret.yaml

# Déployer MySQL
kubectl apply -f k8s/mysql-deployment.yaml

# Déployer le backend
kubectl apply -f k8s/backend-deployment.yaml

# Déployer le frontend
kubectl apply -f k8s/frontend-deployment.yaml

# Déployer le monitoring
kubectl apply -f k8s/prometheus-deployment.yaml
kubectl apply -f k8s/grafana-deployment.yaml
```

##  Monitoring

- **Prometheus**: Collecte des métriques depuis Spring Boot Actuator
- **Grafana**: Visualisation des métriques avec dashboards

Métriques disponibles:
- Requêtes HTTP (count, duration)
- Mémoire JVM
- CPU
- Threads
- Connexions base de données

##  CI/CD

Pipelines GitHub Actions:
- **Backend CI/CD**: Build, tests, scan sécurité, build Docker, push
- **Frontend CI/CD**: Build, tests, build Docker, push

Déclenchement automatique sur push/PR vers `main` ou `develop`.

##  Rôles Utilisateurs

- **ADMIN**: Accès complet, gestion des utilisateurs
- **OWNER**: Gestion de ses sites de camping
- **CAMPER**: Réservation de sites
- **PROVIDER**: Location d'équipements

##  API Documentation

Swagger UI disponible à: http://localhost:8089/swagger-ui.html

## 🛠️ Développement

### Prérequis
- Java 17
- Maven 3.6+
- Node.js 20+
- MySQL 8.0
- Docker Desktop (optionnel)

### Configuration

Fichier: `src/main/resources/application.properties`

Variables d'environnement:
- `MAIL_USERNAME`: Email pour l'envoi de mails (optionnel)
- `MAIL_PASSWORD`: Mot de passe de l'email (optionnel)

## 📄 Licence

Ce projet est développé dans le cadre d'un examen académique.


---


