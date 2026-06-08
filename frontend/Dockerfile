# Multi-stage build for Vue 3 Frontend
# Stage 1: Build stage
FROM node:20-alpine AS builder
WORKDIR /app

# Copy dependency manifest files for layer caching
COPY package*.json ./
RUN npm ci

# Copy the rest of the application files and build
COPY . .
RUN npm run build

# Stage 2: Runtime stage using Nginx
FROM nginx:stable-alpine
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy custom Nginx configuration for Vue Router support
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose Nginx port
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
