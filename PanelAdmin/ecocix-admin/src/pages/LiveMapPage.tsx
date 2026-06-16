import { useEffect, useRef, useState, useCallback } from "react";
import { useQuery } from "@tanstack/react-query";
import Map, { Marker } from "react-map-gl/maplibre";
import type { MapRef } from "react-map-gl/maplibre";
import {
    Box,
    Typography,
    Card,
    CardContent,
    Chip,
    Avatar,
    IconButton,
    useTheme,
    alpha,
    CircularProgress,
} from "@mui/material";
import MyLocationIcon from "@mui/icons-material/MyLocation";
import LocalShippingIcon from "@mui/icons-material/LocalShipping";
import LocationOnIcon from "@mui/icons-material/LocationOn";
import RefreshIcon from "@mui/icons-material/Refresh";
import { adminService } from "../services/api";
import { API_URL } from "../config/axios";
import type { Camion } from "../types";
import ecocixLogo from "../assets/ecocix-logo.png";

const MAP_STYLE = "https://tiles.openfreemap.org/styles/liberty";
const CHICLAYO_CENTER = { lat: -6.7714, lng: -79.8409 };
const GEOJSON_URL = `${API_URL}/geojson/limites_chiclayo.geojson`;

export default function LiveMapPage() {
    const theme = useTheme();
    const mapRef = useRef<MapRef>(null);
    const polygonAdded = useRef(false);
    const [viewState, setViewState] = useState({
        longitude: CHICLAYO_CENTER.lng,
        latitude: CHICLAYO_CENTER.lat,
        zoom: 14,
    });

    const {
        data: camiones = [],
        isLoading,
        refetch,
    } = useQuery({
        queryKey: ["camiones"],
        queryFn: adminService.getCamiones,
        refetchInterval: 8000,
    });

    const activeCamiones = camiones.filter((c: Camion) => c.activo);

    const handleRecenter = useCallback(() => {
        setViewState({
            longitude: CHICLAYO_CENTER.lng,
            latitude: CHICLAYO_CENTER.lat,
            zoom: 14,
        });
    }, []);

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

                    m.addSource("chiclayo-limites", {
                        type: "geojson",
                        data: geojson,
                    });

                    m.addLayer({
                        id: "chiclayo-fill",
                        type: "fill",
                        source: "chiclayo-limites",
                        paint: {
                            "fill-color": "#2E7D32",
                            "fill-opacity": 0.07,
                        },
                    });

                    m.addLayer({
                        id: "chiclayo-border",
                        type: "line",
                        source: "chiclayo-limites",
                        paint: {
                            "line-color": "#2E7D32",
                            "line-width": 2,
                            "line-dasharray": [4, 2],
                        },
                    });

                    polygonAdded.current = true;
                })
                .catch(() => {});
        };

        map.on("load", onLoaded);
        if (map.isStyleLoaded()) onLoaded();

        return () => {
            map.off("load", onLoaded);
        };
    }, []);

    return (
        <Box
            sx={{
                display: "flex",
                gap: 2,
                height: "calc(100vh - 128px)",
                minHeight: 500,
            }}
        >
            {/* Mapa */}
            <Card
                sx={{
                    flex: 1,
                    position: "relative",
                    overflow: "hidden",
                    display: "flex",
                    flexDirection: "column",
                }}
            >
                <Box sx={{ flex: 1, position: "relative" }}>
                    <Map
                        ref={mapRef}
                        {...viewState}
                        onMove={(evt) => setViewState(evt.viewState)}
                        style={{ width: "100%", height: "100%" }}
                        mapStyle={MAP_STYLE}
                        attributionControl={false}
                    >
                        {camiones.map((c: Camion) => {
                            if (!c.coordenadas) return null;
                            return (
                                <Marker
                                    key={c.idCamion}
                                    longitude={c.coordenadas.longitud}
                                    latitude={c.coordenadas.latitud}
                                    anchor="bottom"
                                >
                                    <Box
                                        sx={{
                                            display: "flex",
                                            flexDirection: "column",
                                            alignItems: "center",
                                            cursor: "pointer",
                                            filter: c.activo
                                                ? "none"
                                                : "grayscale(0.6) opacity(0.5)",
                                            transition: "filter 0.2s",
                                            "&:hover": { filter: "none" },
                                        }}
                                    >
                                        <Box
                                            component="img"
                                            src={ecocixLogo}
                                            alt={c.placa}
                                            sx={{
                                                width: 44,
                                                height: 52,
                                                objectFit: "contain",
                                                filter: "drop-shadow(0 2px 6px rgba(0,0,0,0.3))",
                                                transition: "transform 0.2s",
                                                "&:hover": {
                                                    transform: "scale(1.12)",
                                                },
                                            }}
                                        />
                                        <Typography
                                            sx={{
                                                mt: -1,
                                                bgcolor: "background.paper",
                                                px: 0.75,
                                                py: 0.15,
                                                borderRadius: 1,
                                                fontSize: 9,
                                                fontWeight: 700,
                                                fontFamily: "Inter, sans-serif",
                                                boxShadow:
                                                    "0 1px 4px rgba(0,0,0,0.15)",
                                                whiteSpace: "nowrap",
                                                lineHeight: 1.2,
                                                border: "1px solid rgba(0,0,0,0.06)",
                                                zIndex: 1,
                                            }}
                                        >
                                            {c.placa}
                                        </Typography>
                                    </Box>
                                </Marker>
                            );
                        })}
                    </Map>

                    {/* Floating controls */}
                    <Box
                        sx={{
                            position: "absolute",
                            top: 12,
                            left: 12,
                            display: "flex",
                            flexDirection: "column",
                            gap: 1,
                            zIndex: 3,
                        }}
                    >
                        <IconButton
                            onClick={() => refetch()}
                            sx={{
                                bgcolor: "background.paper",
                                boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
                                "&:hover": { bgcolor: "grey.50" },
                            }}
                        >
                            <RefreshIcon />
                        </IconButton>
                        <IconButton
                            onClick={handleRecenter}
                            sx={{
                                bgcolor: "background.paper",
                                boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
                                "&:hover": { bgcolor: "grey.50" },
                            }}
                        >
                            <MyLocationIcon />
                        </IconButton>
                    </Box>

                    {/* Info bar inferior */}
                    <Box
                        sx={{
                            position: "absolute",
                            bottom: 12,
                            left: 12,
                            right: 12,
                            bgcolor: alpha(
                                theme.palette.background.paper,
                                0.92,
                            ),
                            backdropFilter: "blur(8px)",
                            borderRadius: 2,
                            px: 2.5,
                            py: 1.5,
                            display: "flex",
                            alignItems: "center",
                            justifyContent: "space-between",
                            boxShadow: "0 4px 16px rgba(0,0,0,0.1)",
                            zIndex: 3,
                        }}
                    >
                        <Box>
                            <Typography
                                variant="subtitle2"
                                sx={{ fontWeight: 700, color: "success.main" }}
                            >
                                Flota activa
                            </Typography>
                            <Typography
                                variant="caption"
                                color="text.secondary"
                            >
                                {activeCamiones.length} de {camiones.length}{" "}
                                camiones en servicio
                            </Typography>
                        </Box>
                        <Chip
                            label="Actualización cada 8s"
                            size="small"
                            color="success"
                            variant="outlined"
                            sx={{ fontWeight: 600 }}
                        />
                    </Box>
                </Box>
            </Card>

            {/* Panel lateral */}
            <Card
                sx={{
                    width: { xs: "100%", md: 340 },
                    display: "flex",
                    flexDirection: "column",
                    overflow: "hidden",
                    flexShrink: 0,
                }}
            >
                <CardContent sx={{ pb: 1 }}>
                    <Typography
                        variant="subtitle1"
                        sx={{ fontWeight: 700, mb: 0.5 }}
                    >
                        Estado de la Flota
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                        Posiciones en tiempo real
                    </Typography>
                </CardContent>

                {isLoading ? (
                    <Box
                        sx={{
                            display: "flex",
                            justifyContent: "center",
                            py: 4,
                        }}
                    >
                        <CircularProgress size={32} />
                    </Box>
                ) : (
                    <Box sx={{ flex: 1, overflow: "auto", px: 2, pb: 2 }}>
                        {camiones.map((c: Camion) => (
                            <Card
                                key={c.idCamion}
                                sx={{
                                    mb: 1.5,
                                    opacity: c.activo ? 1 : 0.55,
                                    border: `1px solid ${c.activo ? theme.palette.grey[200] : theme.palette.grey[100]}`,
                                    boxShadow: "none",
                                    transition: "opacity 0.2s",
                                    cursor: "pointer",
                                    "&:hover": {
                                        borderColor: c.activo
                                            ? "success.main"
                                            : theme.palette.grey[200],
                                    },
                                }}
                                onClick={() => {
                                    if (c.coordenadas) {
                                        setViewState({
                                            longitude: c.coordenadas.longitud,
                                            latitude: c.coordenadas.latitud,
                                            zoom: 16,
                                        });
                                    }
                                }}
                            >
                                <CardContent
                                    sx={{
                                        py: 1.5,
                                        "&:last-child": { pb: 1.5 },
                                    }}
                                >
                                    <Box
                                        sx={{
                                            display: "flex",
                                            justifyContent: "space-between",
                                            alignItems: "center",
                                            mb: 1,
                                        }}
                                    >
                                        <Box
                                            sx={{
                                                display: "flex",
                                                alignItems: "center",
                                                gap: 1,
                                            }}
                                        >
                                            <Avatar
                                                sx={{
                                                    width: 36,
                                                    height: 36,
                                                    bgcolor: c.activo
                                                        ? alpha(
                                                              theme.palette
                                                                  .success.main,
                                                              0.1,
                                                          )
                                                        : "grey.100",
                                                    color: c.activo
                                                        ? "success.main"
                                                        : "grey.500",
                                                }}
                                            >
                                                <LocalShippingIcon fontSize="small" />
                                            </Avatar>
                                            <Box>
                                                <Typography
                                                    variant="body2"
                                                    sx={{
                                                        fontWeight: 700,
                                                        lineHeight: 1.2,
                                                    }}
                                                >
                                                    {c.idCamion}
                                                </Typography>
                                                <Typography
                                                    variant="caption"
                                                    color="text.secondary"
                                                >
                                                    {c.placa}
                                                </Typography>
                                            </Box>
                                        </Box>
                                        <Chip
                                            label={
                                                c.activo ? "Activo" : "Inactivo"
                                            }
                                            color={
                                                c.activo ? "success" : "error"
                                            }
                                            size="small"
                                            sx={{ fontWeight: 600 }}
                                        />
                                    </Box>
                                    {c.coordenadas && (
                                        <Box
                                            sx={{
                                                display: "flex",
                                                alignItems: "center",
                                                gap: 0.5,
                                                color: "text.secondary",
                                            }}
                                        >
                                            <LocationOnIcon
                                                sx={{ fontSize: 14 }}
                                            />
                                            <Typography
                                                variant="caption"
                                                sx={{ fontFamily: "monospace" }}
                                            >
                                                {c.coordenadas.latitud.toFixed(
                                                    4,
                                                )}
                                                ,{" "}
                                                {c.coordenadas.longitud.toFixed(
                                                    4,
                                                )}
                                            </Typography>
                                        </Box>
                                    )}
                                    <Typography
                                        variant="caption"
                                        color="text.secondary"
                                        sx={{ display: "block", mt: 0.5 }}
                                    >
                                        Última actualización:{" "}
                                        {new Date(
                                            c.ultimaActualizacion,
                                        ).toLocaleTimeString()}
                                    </Typography>
                                </CardContent>
                            </Card>
                        ))}
                    </Box>
                )}
            </Card>
        </Box>
    );
}
