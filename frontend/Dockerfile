FROM node:22-alpine AS build
ARG APP_NAME
ARG VITE_API_BASE_URL=
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
WORKDIR /app
COPY frontend/${APP_NAME}/package.json frontend/${APP_NAME}/package-lock.json ./
RUN npm ci
COPY frontend/shared /shared
COPY frontend/${APP_NAME}/ ./
RUN npm run build

FROM nginx:1.27-alpine
COPY infrastructure/nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
