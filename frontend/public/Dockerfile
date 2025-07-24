# Use Nginx to serve static content
FROM nginx:alpine

# Remove default Nginx web files
RUN rm -rf /usr/share/nginx/html/*

# Copy your static files to Nginx's web directory
COPY . /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
#dummy line to test cloud build
