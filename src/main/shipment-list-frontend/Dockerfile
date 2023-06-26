FROM node:16

ARG BACKEND_API_HOST
ENV BACKEND_API_HOST $BACKEND_API_HOST

WORKDIR /usr/src/app

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

# Make port 3000 available for the app
EXPOSE 3000

# Start the app
CMD ["npm", "start"]