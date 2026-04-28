# =======================
# STAGE 1: BUILD
# =======================
FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci --silent

COPY . .
RUN npm run build:prod && \
    date +%Y-%m-%d_%H:%M:%S > /app/dist/cloudops-dashboard/browser/build-version.txt

# =======================
# STAGE 2: SERVE
# =======================
# nginx:alpine is tiny, always available on Docker Hub, and works with
# standard nginx.conf paths — no Red Hat registry or auth required.
FROM nginx:alpine AS runtime

# Copy Angular build to nginx html root
COPY --from=builder /app/dist/cloudops-dashboard/browser /usr/share/nginx/html

# Overwrite the default nginx config with ours
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 8080

# wget via busybox, no curl needed
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget -q --spider http://localhost:8080 || exit 1

ENV BACKEND_URL="http://localhost:8080"

CMD ["/bin/sh", "-c", "sed -i \"s|BACKEND_URL_PLACEHOLDER|$BACKEND_URL|g\" /etc/nginx/nginx.conf && nginx -g 'daemon off;'"]
