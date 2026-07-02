# Runtime Dockerfile using pre-built static assets.
# To prevent high CPU/memory usage on resource-constrained EC2 hosts (e.g. t3a.nano),
# the Vue frontend compilation (npm run build) is executed on the GitHub Actions runner,
# and the generated 'dist/' directory is synchronized to the host.
FROM nginx:stable-alpine
COPY dist /usr/share/nginx/html

# Copy custom Nginx configuration for Vue Router support
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose Nginx port (using non-privileged port)
EXPOSE 8080

# Modify Nginx configuration to use /tmp/nginx.pid instead of /var/run/nginx.pid to allow non-root running
RUN sed -i 's|/var/run/nginx.pid|/tmp/nginx.pid|g' /etc/nginx/nginx.conf

USER nginx

CMD ["nginx", "-g", "daemon off;"]
