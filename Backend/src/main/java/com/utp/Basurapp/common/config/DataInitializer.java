package com.utp.Basurapp.common.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utp.Basurapp.common.model.Distrito;
import com.utp.Basurapp.common.repository.DistritoRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String CHICLAYO_UBIGEO = "140101";
    private static final String CHICLAYO_NOMBRE = "Chiclayo";
    private static final String DEPARTAMENTO = "Lambayeque";
    private static final String PROVINCIA = "Chiclayo";

    private final DistritoRepository distritoRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public DataInitializer(DistritoRepository distritoRepository) {
        this.distritoRepository = distritoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (distritoRepository.findByUbigeo(CHICLAYO_UBIGEO).isPresent()) {
            log.info("Distrito {} ya esta inicializado. Saltando.", CHICLAYO_UBIGEO);
            return;
        }

        log.info("Inicializando geometria del distrito de Chiclayo desde GeoJSON...");

        try (InputStream is = new ClassPathResource("geojson/limites_chiclayo.geojson").getInputStream()) {
            JsonNode root = objectMapper.readTree(is);
            JsonNode features = root.get("features");
            if (features == null || !features.isArray() || features.isEmpty()) {
                log.warn("GeoJSON sin features. No se inicializa el distrito.");
                return;
            }

            JsonNode feature = features.get(0);
            JsonNode props = feature.get("properties");
            String ubigeo = props != null && props.hasNonNull("UBIGEO")
                    ? props.get("UBIGEO").asText()
                    : CHICLAYO_UBIGEO;
            String nombre = props != null && props.hasNonNull("DISTRITO")
                    ? props.get("DISTRITO").asText()
                    : CHICLAYO_NOMBRE;
            String departamento = props != null && props.hasNonNull("DEPARTAMEN")
                    ? props.get("DEPARTAMEN").asText()
                    : DEPARTAMENTO;
            String provincia = props != null && props.hasNonNull("PROVINCIA")
                    ? props.get("PROVINCIA").asText()
                    : PROVINCIA;

            JsonNode geometry = feature.get("geometry");
            String type = geometry.get("type").asText();
            JsonNode coordinates = geometry.get("coordinates");

            List<Polygon> polygons = new ArrayList<>();

            if ("MultiPolygon".equals(type)) {
                for (JsonNode polygonCoords : coordinates) {
                    polygons.add(buildPolygon(polygonCoords));
                }
            } else if ("Polygon".equals(type)) {
                polygons.add(buildPolygon(coordinates));
            } else {
                log.warn("Tipo de geometria no soportado: {}", type);
                return;
            }

            org.locationtech.jts.geom.MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(
                    polygons.toArray(new Polygon[0]));
            multiPolygon.setSRID(4326);

            Distrito distrito = new Distrito();
            distrito.setUbigeo(ubigeo);
            distrito.setNombre(nombre);
            distrito.setDepartamento(departamento);
            distrito.setProvincia(provincia);
            distrito.setGeometria(multiPolygon);

            distritoRepository.save(distrito);
            log.info("Distrito {} ({}) inicializado con {} poligonos, {} vertices en el primero.",
                    nombre, ubigeo, polygons.size(),
                    polygons.get(0).getExteriorRing().getNumPoints());
        } catch (Exception e) {
            log.error("Error al inicializar distrito de Chiclayo", e);
            throw e;
        }
    }

    private Polygon buildPolygon(JsonNode polygonCoords) {
        JsonNode ringsArray = polygonCoords;
        LinearRing shell = buildRing(ringsArray.get(0));

        LinearRing[] holes = null;
        if (ringsArray.size() > 1) {
            holes = new LinearRing[ringsArray.size() - 1];
            for (int i = 1; i < ringsArray.size(); i++) {
                holes[i - 1] = buildRing(ringsArray.get(i));
            }
        }

        return geometryFactory.createPolygon(shell, holes);
    }

    private LinearRing buildRing(JsonNode ringCoords) {
        Coordinate[] coords = new Coordinate[ringCoords.size()];
        for (int i = 0; i < ringCoords.size(); i++) {
            JsonNode point = ringCoords.get(i);
            coords[i] = new Coordinate(point.get(0).asDouble(), point.get(1).asDouble());
        }
        if (coords.length > 0 && !coords[0].equals(coords[coords.length - 1])) {
            Coordinate[] closed = new Coordinate[coords.length + 1];
            System.arraycopy(coords, 0, closed, 0, coords.length);
            closed[coords.length] = coords[0];
            coords = closed;
        }
        return geometryFactory.createLinearRing(coords);
    }

}
