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
# STAGE 2: SERVE (Red Hat Hardened)
# =======================
FROM registry.access.redhat.com/ubi8/nginx-120:latest AS runtime

# UBI Nginx runs on 8080 by default to allow non-root execution
USER 1001

# Copy build artifacts to the default UBI nginx path
COPY --from=builder /app/dist/cloudops-dashboard/browser /usr/share/nginx/html

# Red Hat UBI nginx configuration path differs from Alpine
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD curl -f http://localhost:8080 || exit 1

# Provide default if not set
ENV BACKEND_URL="http://localhost:8080"

CMD ["/bin/sh", "-c", "sed -i \"s|BACKEND_URL_PLACEHOLDER|$BACKEND_URL|g\" /etc/nginx/nginx.conf && nginx -g 'daemon off;'"]
