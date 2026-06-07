import express from 'express';
import cors from 'cors';
import fs from 'fs';
import { fileURLToPath } from 'url';
import path from 'path';

const app = express();
const PORT = 3001;

app.use(cors());
app.use(express.json());

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

let rutaCamion = [];

try {
    const rutaArchivo = path.join(__dirname, 'ruta_simulacion.geojson');
    const dataRaw = fs.readFileSync(rutaArchivo, 'utf8');
    const geojsonData = JSON.parse(dataRaw);

    rutaCamion = geojsonData.features[0].geometry.coordinates;
} catch (error) {
    process.exit(1);
}

let indiceActual = 0;
let camionActivo = true;

setInterval(() => {
    if (camionActivo && rutaCamion.length > 0) {
        indiceActual = (indiceActual + 1) % rutaCamion.length;
    }
}, 5000);

app.get('/api/camion/ubicacion-actual', (req, res) => {
    if (rutaCamion.length === 0) {
        return res.status(500).json({ error: "No hay coordenadas" });
    }

    const punto = rutaCamion[indiceActual];

    res.json({
        idCamion: "CAM-CIX-001",
        placa: "BC-9876",
        activo: camionActivo,
        ultimaActualizacion: new Date().toISOString(),
        coordenadas: {
            latitud: punto[1],
            longitud: punto[0]
        }
    });
});

app.post('/api/camion/control', (req, res) => {
    const { activo, reiniciar } = req.body;
    if (activo !== undefined) camionActivo = activo;
    if (reiniciar) indiceActual = 0;
    res.json({ mensaje: "Estado actualizado", activo: camionActivo, posicionActual: indiceActual });
});

app.listen(PORT, () => {
    console.log(`Servidor corriendo en el puerto ${PORT}`);
    console.log(`URL http://localhost:${PORT}/api/camion/ubicacion-actual`);
});