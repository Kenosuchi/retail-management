FROM node:22.23.2-alpine3.24 AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginxinc/nginx-unprivileged:1.31.6-alpine3.24 AS runtime

COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=build \
    --chown=nginx:nginx \
    /app/dist/frontend/browser/ \
    /usr/share/nginx/html/

EXPOSE 8080
