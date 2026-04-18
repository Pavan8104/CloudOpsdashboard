# =======================
# STAGE 1: BUILD
# =======================
FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci --silent

COPY . .
RUN npm run build -- --configuration production

# =======================
# STAGE 2: SERVE
# =======================
FROM nginx:alpine AS runtime

COPY --from=builder /app/dist/cloudops-dashboard/browser /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:80 || exit 1

CMD ["nginx", "-g", "daemon off;"]
