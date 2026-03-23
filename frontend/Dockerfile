# Dockerfile pour le Frontend Angular
# Stage 1: Build
FROM node:20-alpine AS build

WORKDIR /app

# Copier les fichiers de configuration
COPY package*.json ./

# Installer les dépendances
RUN npm ci

# Copier le code source
COPY . .

# Build l'application Angular
RUN npm run build -- --configuration production

# Stage 2: Serve avec Nginx
FROM nginx:alpine

# Copier les fichiers buildés (Angular 17+ peut mettre dans dist/project/browser)
# On copie tout le contenu du dossier de build pour être sûr
COPY --from=build /app/dist/campconnect-frontend /usr/share/nginx/html

# Si Angular a créé un sous-dossier browser, on déplace son contenu à la racine
RUN if [ -d "/usr/share/nginx/html/browser" ]; then \
        cp -r /usr/share/nginx/html/browser/* /usr/share/nginx/html/ && \
        rm -rf /usr/share/nginx/html/browser; \
    fi

# Copier la configuration Nginx personnalisée
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Exposer le port 80
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost/ || exit 1

# Démarrer Nginx
CMD ["nginx", "-g", "daemon off;"]
