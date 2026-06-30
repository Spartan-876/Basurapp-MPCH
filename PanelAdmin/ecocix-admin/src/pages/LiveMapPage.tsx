import { useEffect, useRef, useState, useCallback } from "react";
import { useQuery } from "@tanstack/react-query";
import Map, { Marker } from "react-map-gl/maplibre";
import type { MapRef } from "react-map-gl/maplibre";
import {
  Box, Typography, Card, CardContent, Chip, Avatar, IconButton,
  CircularProgress, ToggleButton, ToggleButtonGroup, Divider, alpha,
} from "@mui/material";
import MyLocationIcon from "@mui/icons-material/MyLocation";
import LocalShippingIcon from "@mui/icons-material/LocalShipping";
import LocationOnIcon from "@mui/icons-material/LocationOn";
import RefreshIcon from "@mui/icons-material/Refresh";
import TrendingUpIcon from "@mui/icons-material/TrendingUp";
import CloseIcon from "@mui/icons-material/Close";
import { adminService } from "../services/api";
import { API_URL } from "../config/axios";
import type { Camion } from "../types";
import ecocixLogo from "../assets/ecocix-logo.png";
import PageHeader from "../components/PageHeader";

const MAP_STYLE = "https://tiles.openfreemap.org/styles/liberty";
const CHICLAYO_CENTER = { lat: -6.7714, lng: -79.8409 };
const GEOJSON_URL = `${API_URL}/geojson/limites_chiclayo.geojson`;

export default function LiveMapPage() {
  const mapRef = useRef<MapRef>(null);
  const polygonAdded = useRef(false);
  const [viewState, setViewState] = useState({
    longitude: CHICLAYO_CENTER.lng,
    latitude: CHICLAYO_CENTER.lat,
    zoom: 14,
  });
  const [filter, setFilter] = useState<string>("todos");
  const [selectedTruck, setSelectedTruck] = useState<Camion | null>(null);

  const { data: camiones = [], isLoading, refetch } = useQuery({
    queryKey: ["camiones"],
    queryFn: adminService.getCamiones,
    refetchInterval: 8000,
  });

  const { data: stats } = useQuery({
    queryKey: ["statsReportes"],
    queryFn: adminService.getStatsReportes,
  });

  const activeCamiones = camiones.filter((c: Camion) => c.activo);
  const filteredCamiones = filter === "todos" ? camiones : camiones.filter((c: Camion) => {
    if (filter === "ruta") return c.activo;
    if (filter === "base") return !c.activo;
    return false;
  });

  const handleRecenter = useCallback(() => {
    setViewState({ longitude: CHICLAYO_CENTER.lng, latitude: CHICLAYO_CENTER.lat, zoom: 14 });
  }, []);

  const selectTruck = (c: Camion) => {
    setSelectedTruck(c);
    if (c.coordenadas) {
      setViewState({ longitude: c.coordenadas.longitud, latitude: c.coordenadas.latitud, zoom: 16 });
    }
  };

  useEffect(() => {
    const map = mapRef.current?.getMap();
    if (!map || polygonAdded.current) return;
    const onLoaded = () => {
      if (polygonAdded.current) return;
      fetch(GEOJSON_URL)
        .then((res) => res.json())
        .then((geojson) => {
          if (!mapRef.current) return;
          const m = mapRef.current.getMap();
          if (!m || m.getSource("chiclayo-limites")) return;
          m.addSource("chiclayo-limites", { type: "geojson", data: geojson });
          m.addLayer({ id: "chiclayo-fill", type: "fill", source: "chiclayo-limites", paint: { "fill-color": "#003527", "fill-opacity": 0.05 } });
          m.addLayer({ id: "chiclayo-border", type: "line", source: "chiclayo-limites", paint: { "line-color": "#003527", "line-width": 2, "line-dasharray": [4, 2] } });
          polygonAdded.current = true;
        })
        .catch(() => {});
    };
    map.on("load", onLoaded);
    if (map.isStyleLoaded()) onLoaded();
    return () => { map.off("load", onLoaded); };
  }, []);

  return (
    <Box>
      <PageHeader />
      <Box sx={{ mt: 3, display: "flex", flexDirection: "column", gap: 2, height: "calc(100vh - 160px)", minHeight: 500 }}>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h5" sx={{ fontWeight: 700, color: "#151c27", fontFamily: "Inter" }}>
          Centro Operativo
        </Typography>
        <Typography variant="body2" sx={{ color: "#404944", mt: 0.5, fontFamily: "Inter" }}>
          Monitoreo en tiempo real de la flota de recolección.
        </Typography>
      </Box>

      {/* Filter Tabs */}
      <ToggleButtonGroup value={filter} exclusive onChange={(_, v) => { if (v) setFilter(v); }} sx={{ alignSelf: "flex-start" }}>
        <ToggleButton value="todos">Todos ({camiones.length})</ToggleButton>
        <ToggleButton value="ruta">En Ruta ({activeCamiones.length})</ToggleButton>
        <ToggleButton value="base">En Base ({camiones.length - activeCamiones.length})</ToggleButton>
      </ToggleButtonGroup>

      {/* Stats Row */}
      <Box sx={{ display: "grid", gridTemplateColumns: "repeat(2, 1fr)", gap: 2 }}>
        <Card>
          <CardContent sx={{ display: "flex", alignItems: "center", gap: 2, p: "1rem !important" }}>
            <Avatar sx={{ bgcolor: alpha("#006c49", 0.1), color: "#006c49", width: 40, height: 40 }}>
              <TrendingUpIcon />
            </Avatar>
            <Box>
              <Typography sx={{ fontSize: 11, color: "#707974", fontWeight: 500, fontFamily: "Inter" }}>En Operación</Typography>
              <Typography sx={{ fontWeight: 700, fontSize: 20, color: "#151c27", fontFamily: "Inter" }}>
                {camiones.length > 0 ? Math.round((activeCamiones.length / camiones.length) * 100) : 0}%
              </Typography>
            </Box>
          </CardContent>
        </Card>
        <Card>
          <CardContent sx={{ display: "flex", alignItems: "center", gap: 2, p: "1rem !important" }}>
            <Avatar sx={{ bgcolor: alpha("#ED6C02", 0.1), color: "#ED6C02", width: 40, height: 40 }}>
              <LocationOnIcon />
            </Avatar>
            <Box>
              <Typography sx={{ fontSize: 11, color: "#707974", fontWeight: 500, fontFamily: "Inter" }}>Reportes Pendientes</Typography>
              <Typography sx={{ fontWeight: 700, fontSize: 20, color: "#151c27", fontFamily: "Inter" }}>
                {stats?.pendientes ?? 0}
              </Typography>
            </Box>
          </CardContent>
        </Card>
      </Box>

      {/* Main Content: Map + Side Panel */}
      <Box sx={{ display: "flex", gap: 2, flex: 1, minHeight: 0 }}>
        {/* Map */}
        <Card sx={{ flex: 1, position: "relative", overflow: "hidden", display: "flex", flexDirection: "column" }}>
          <Box sx={{ flex: 1, position: "relative" }}>
            <Map
              ref={mapRef}
              {...viewState}
              onMove={(evt) => setViewState(evt.viewState)}
              style={{ width: "100%", height: "100%" }}
              mapStyle={MAP_STYLE}
              attributionControl={false}
            >
              {filteredCamiones.map((c: Camion) => {
                if (!c.coordenadas) return null;
                return (
                  <Marker key={c.idCamion} longitude={c.coordenadas.longitud} latitude={c.coordenadas.latitud} anchor="bottom">
                    <Box
                      sx={{
                        display: "flex", flexDirection: "column", alignItems: "center", cursor: "pointer",
                        filter: c.activo ? "none" : "grayscale(0.6) opacity(0.5)", transition: "filter 0.2s",
                        "&:hover": { filter: "none" },
                      }}
                      onClick={() => selectTruck(c)}
                    >
                      <Box component="img" src={ecocixLogo} alt={c.placa} sx={{
                        width: 44, height: 52, objectFit: "contain",
                        filter: "drop-shadow(0 2px 6px rgba(0,0,0,0.3))", transition: "transform 0.2s",
                        "&:hover": { transform: "scale(1.12)" },
                      }} />
                      <Typography sx={{
                        mt: -1, bgcolor: "background.paper", px: 0.75, py: 0.15, borderRadius: 1,
                        fontSize: 9, fontWeight: 700, fontFamily: "Inter", boxShadow: "0 1px 4px rgba(0,0,0,0.15)",
                        whiteSpace: "nowrap", lineHeight: 1.2, border: "1px solid rgba(0,0,0,0.06)", zIndex: 1,
                      }}>
                        {c.placa}
                      </Typography>
                    </Box>
                  </Marker>
                );
              })}
            </Map>

            {/* Floating controls */}
            <Box sx={{ position: "absolute", top: 12, left: 12, display: "flex", flexDirection: "column", gap: 1, zIndex: 3 }}>
              <IconButton onClick={() => refetch()} sx={{ bgcolor: "background.paper", boxShadow: "0 2px 8px rgba(0,0,0,0.1)", "&:hover": { bgcolor: "grey.50" } }}>
                <RefreshIcon />
              </IconButton>
              <IconButton onClick={handleRecenter} sx={{ bgcolor: "background.paper", boxShadow: "0 2px 8px rgba(0,0,0,0.1)", "&:hover": { bgcolor: "grey.50" } }}>
                <MyLocationIcon />
              </IconButton>
            </Box>
          </Box>
        </Card>

        {/* Side Panel */}
        <Card sx={{ width: { xs: "100%", md: 320 }, display: "flex", flexDirection: "column", overflow: "hidden", flexShrink: 0 }}>
          {selectedTruck ? (
            /* Truck Detail Panel */
            <CardContent sx={{ p: "1.5rem !important", flex: 1, overflow: "auto" }}>
              <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", mb: 2 }}>
                <Box>
                  <Typography sx={{ fontWeight: 700, fontSize: 18, color: "#151c27", fontFamily: "Inter" }}>
                    {selectedTruck.placa}
                  </Typography>
                  <Typography sx={{ fontSize: 13, color: "#707974", fontFamily: "Inter" }}>
                    ID: {selectedTruck.idCamion}
                  </Typography>
                </Box>
                <IconButton size="small" onClick={() => setSelectedTruck(null)}>
                  <CloseIcon fontSize="small" />
                </IconButton>
              </Box>

              <Chip
                label={selectedTruck.activo ? "Activo (En Ruta)" : "Inactivo"}
                color={selectedTruck.activo ? "success" : "error"}
                sx={{ mb: 2, fontWeight: 600, fontSize: 12 }}
              />

              <Divider sx={{ my: 2, borderColor: "#f0f3ff" }} />

              <Box sx={{ display: "flex", flexDirection: "column", gap: 1.5 }}>
                <Box sx={{ display: "flex", alignItems: "center", gap: 0.5 }}>
                  <LocationOnIcon sx={{ fontSize: 14, color: "#707974" }} />
                  <Typography sx={{ fontSize: 12, color: "#707974", fontFamily: "Inter" }}>
                    {selectedTruck.coordenadas?.latitud.toFixed(4)}, {selectedTruck.coordenadas?.longitud.toFixed(4)}
                  </Typography>
                </Box>
                {selectedTruck.ultimaActualizacion && (
                  <Typography sx={{ fontSize: 11, color: "#707974", fontFamily: "Inter" }}>
                    Última actualización: {new Date(selectedTruck.ultimaActualizacion).toLocaleTimeString()}
                  </Typography>
                )}
              </Box>
            </CardContent>
          ) : (
            /* Fleet List */
            <>
              <CardContent sx={{ pb: 1 }}>
                <Typography variant="subtitle1" sx={{ fontWeight: 700, color: "#151c27", fontFamily: "Inter" }}>
                  Estado de la Flota
                </Typography>
                <Typography variant="body2" sx={{ color: "#707974", fontFamily: "Inter", fontSize: 13 }}>
                  Posiciones en tiempo real
                </Typography>
              </CardContent>
              {isLoading ? (
                <Box sx={{ display: "flex", justifyContent: "center", py: 4 }}><CircularProgress size={32} /></Box>
              ) : (
                <Box sx={{ flex: 1, overflow: "auto", px: 2, pb: 2 }}>
                  {filteredCamiones.map((c: Camion) => (
                    <Card
                      key={c.idCamion}
                      sx={{
                        mb: 1.5, opacity: c.activo ? 1 : 0.55, boxShadow: "none",
                        border: `1px solid ${c.activo ? "#e7eefe" : "#f0f3ff"}`,
                        cursor: "pointer", transition: "all 0.2s",
                        "&:hover": { borderColor: c.activo ? "#003527" : "#e7eefe" },
                      }}
                      onClick={() => selectTruck(c)}
                    >
                      <CardContent sx={{ py: 1.5, "&:last-child": { pb: 1.5 } }}>
                        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                          <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                            <Avatar sx={{
                              width: 36, height: 36,
                              bgcolor: c.activo ? alpha("#006c49", 0.1) : "#f0f3ff",
                              color: c.activo ? "#006c49" : "#707974",
                            }}>
                              <LocalShippingIcon fontSize="small" />
                            </Avatar>
                            <Box>
                              <Typography sx={{ fontSize: 13, fontWeight: 700, lineHeight: 1.2, fontFamily: "Inter" }}>{c.placa}</Typography>
                              <Typography sx={{ fontSize: 11, color: "#707974", fontFamily: "Inter" }}>{c.idCamion}</Typography>
                            </Box>
                          </Box>
                          <Chip
                            label={c.activo ? "En Ruta" : "En Base"}
                            color={c.activo ? "success" : "default"}
                            size="small"
                            sx={{ fontWeight: 600, fontSize: 11 }}
                          />
                        </Box>
                      </CardContent>
                    </Card>
                  ))}
                </Box>
              )}
            </>
          )}
        </Card>
      </Box>
    </Box>
    </Box>
  );
}
