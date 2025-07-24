# Stage 1: Build the app
FROM node:18 as build
WORKDIR /app

# Copy dependencies and install
COPY package*.json ./
RUN npm install --legacy-peer-deps

# Copy all source files and build
COPY . .
ENV CI=true
RUN npm run build && ls -al /app/dist

# Stage 2: Serve via NGINX
FROM nginx:alpine
ENV PORT=8080

# Remove default config and add custom one
RUN rm /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy dist folder from build stage
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]
