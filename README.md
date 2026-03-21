Description
CampConnect est une application web complète de gestion de réservations de sites de camping, développée avec Spring Boot (backend) et Angular (frontend). Le projet inclut la gestion des utilisateurs, des réservations, des équipements, des contrats, des factures, ainsi qu'un système de monitoring et de déploiement avec Docker et Kubernetes.
Technologies Utilisées

#Backend
- **Spring Boot 3.1.6** - Framework Java
- **Spring Security** - Authentification et autorisation
- **JWT** - Tokens d'authentification
- **Spring Data JPA** - Accès aux données
- **MySQL 8.0** - Base de données
- **Maven** - Gestion des dépendances
- **Spring Boot Actuator** - Monitoring et métriques
- **Prometheus** - Collecte de métriques

# Frontend
- **Angular 17** - Framework TypeScript
- **Bootstrap** - Interface utilisateur
- **RxJS** - Programmation réactive
- **Angular Standalone** - Architecture sans modules

# DevOps
- **Docker** - Conteneurisation
- **Docker Compose** - Orchestration locale
- **Kubernetes (KubeADM)** - Orchestration de conteneurs
- **GitHub Actions** - CI/CD
- **Prometheus** - Monitoring
- **Grafana** - Visualisation des métriques
Structure du Projet
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
