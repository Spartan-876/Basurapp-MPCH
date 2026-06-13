FROM node:20-alpine

# Instalar pnpm globalmente en el contenedor
RUN npm install -g pnpm

WORKDIR /app

# Copiar archivos de dependencias
COPY package.json pnpm-lock.yaml ./

# Instalar solo las dependencias necesarias para producción
RUN pnpm install --frozen-lockfile --prod

# Copiar los scripts de ejecución y los archivos geográficos de los camiones
COPY server.js ./
COPY ruta_camion.geojson ./
COPY ruta_camion2.geojson ./

EXPOSE 3000

CMD ["node", "server.js"]