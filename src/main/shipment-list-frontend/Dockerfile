FROM node:16-alpine

ARG BACKEND_API_HOST
ENV BACKEND_API_HOST $BACKEND_API_HOST

WORKDIR ./

COPY src/main/shipment-list-frontend/package*.json ./
RUN npm install

COPY src/main/shipment-list-frontend ./
RUN npm run build

# Make port 3000 available for the app
EXPOSE 3000

# Start the app
CMD ["npm", "start"]