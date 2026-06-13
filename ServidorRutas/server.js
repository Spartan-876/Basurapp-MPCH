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

function cargarRuta(archivo) {
    const rutaArchivo = path.join(__dirname, archivo);
    const dataRaw = fs.readFileSync(rutaArchivo, 'utf8');
    const geojsonData = JSON.parse(dataRaw);
    return geojsonData.features[0].geometry.coordinates;
}

const camiones = [
    {
        id: "CAM-CIX-001",
        placa: "BC-9876",
        archivo: "ruta_camion.geojson",
        ruta: [],
        indiceActual: 0,
        activo: true
    },
    {
        id: "CAM-CIX-002",
        placa: "AD-1234",
        archivo: "ruta_camion2.geojson",
        ruta: [],
        indiceActual: 0,
        activo: true
    }
];

for (const camion of camiones) {
    try {
        camion.ruta = cargarRuta(camion.archivo);
        console.log(`Ruta cargada: ${camion.id} (${camion.placa}) - ${camion.ruta.length} puntos`);
    } catch (error) {
        console.error(`Error cargando ruta para ${camion.id}: ${error.message}`);
    }
}

setInterval(() => {
    for (const camion of camiones) {
        if (camion.activo && camion.ruta.length > 0) {
            camion.indiceActual = (camion.indiceActual + 1) % camion.ruta.length;
        }
    }
}, 5000);

function formatoCamion(camion) {
    if (camion.ruta.length === 0) {
        return {
            idCamion: camion.id,
            placa: camion.placa,
            activo: false,
            ultimaActualizacion: new Date().toISOString(),
            coordenadas: null,
            mensaje: "Ruta no disponible"
        };
    }
    const punto = camion.ruta[camion.indiceActual];
    return {
        idCamion: camion.id,
        placa: camion.placa,
        activo: camion.activo,
        ultimaActualizacion: new Date().toISOString(),
        coordenadas: {
            latitud: punto[1],
            longitud: punto[0]
        }
    };
}

app.get('/api/camiones', (req, res) => {
    res.json(camiones.map(formatoCamion));
});

app.get('/api/camion/:id/ubicacion', (req, res) => {
    const camion = camiones.find(c => c.id === req.params.id);
    if (!camion) {
        return res.status(404).json({ error: "Camion no encontrado" });
    }
    res.json(formatoCamion(camion));
});

app.get('/api/camion/ubicacion-actual', (req, res) => {
    const camion = camiones.find(c => c.activo && c.ruta.length > 0) || camiones[0];
    res.json(formatoCamion(camion));
});

app.put('/api/camion/:id/estado', (req, res) => {
    const camion = camiones.find(c => c.id === req.params.id);
    if (!camion) {
        return res.status(404).json({ error: "Camion no encontrado" });
    }
    if (req.body.activo !== undefined) {
        camion.activo = req.body.activo;
    }
    res.json({ mensaje: "Estado actualizado", camion: formatoCamion(camion) });
});

app.post('/api/camion/control', (req, res) => {
    const { idCamion, activo, reiniciar } = req.body;

    if (idCamion) {
        const camion = camiones.find(c => c.id === idCamion);
        if (!camion) {
            return res.status(404).json({ error: "Camion no encontrado" });
        }
        if (activo !== undefined) camion.activo = activo;
        if (reiniciar) camion.indiceActual = 0;
        return res.json({ mensaje: "Estado actualizado", camion: formatoCamion(camion) });
    }

    for (const camion of camiones) {
        if (activo !== undefined) camion.activo = activo;
        if (reiniciar) camion.indiceActual = 0;
    }
    res.json({ mensaje: "Todos los camiones actualizados", camiones: camiones.map(formatoCamion) });
});

app.listen(PORT, () => {
    console.log(`Servidor corriendo en el puerto ${PORT}`);
    console.log(`Camiones activos: ${camiones.filter(c => c.ruta.length > 0).length}`);
    for (const camion of camiones) {
        console.log(`  - ${camion.id} (${camion.placa}): ${camion.ruta.length} puntos`);
    }
});
