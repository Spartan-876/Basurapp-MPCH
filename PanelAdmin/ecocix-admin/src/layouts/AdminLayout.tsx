import { useState } from 'react';
import { Outlet, NavLink } from 'react-router-dom';
import {
  Drawer, Typography, Box,
  List, ListItemButton, ListItemIcon, ListItemText, Avatar,
} from '@mui/material';
import DashboardIcon from '@mui/icons-material/Dashboard';
import MapIcon from '@mui/icons-material/Map';
import AssessmentIcon from '@mui/icons-material/Assessment';
import DirectionsCarIcon from '@mui/icons-material/DirectionsCar';
import PeopleIcon from '@mui/icons-material/People';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import NotificationsActiveIcon from '@mui/icons-material/NotificationsActive';
import SettingsIcon from '@mui/icons-material/Settings';
import ecocixLogo from '../assets/ecocix-logo.png';

const DRAWER_WIDTH = 260;

const navItems = [
  { to: '/', label: 'Panel', icon: <DashboardIcon /> },
  { to: '/mapa', label: 'Mapa en Vivo', icon: <MapIcon /> },
  { to: '/camiones', label: 'Camiones', icon: <DirectionsCarIcon /> },
  { to: '/reportes', label: 'Reportes', icon: <AssessmentIcon /> },
  { to: '/ciudadanos', label: 'Ciudadanos', icon: <PeopleIcon /> },
  { to: '/admins', label: 'Administradores', icon: <AdminPanelSettingsIcon /> },
  { to: '/alertas', label: 'Alertas', icon: <NotificationsActiveIcon /> },
  { to: '/configuracion', label: 'Configuración', icon: <SettingsIcon /> },
];

export default function AdminLayout() {
  const [mobileOpen, setMobileOpen] = useState(false);

  const drawer = (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <Box sx={{ px: 3, py: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <Avatar sx={{ width: 40, height: 40, bgcolor: 'transparent' }}>
            <Box component="img" src={ecocixLogo} sx={{ width: 40, height: 40 }} />
          </Avatar>
          <Box>
            <Typography sx={{ fontWeight: 700, color: '#FFFFFF', fontSize: 16, lineHeight: 1.2, fontFamily: 'Inter' }}>
              EcoCix Admin
            </Typography>
            <Typography sx={{ fontSize: 11, color: 'rgba(255,255,255,0.55)', fontFamily: 'Inter', lineHeight: 1.2 }}>
              Gestión de Residuos
            </Typography>
          </Box>
        </Box>
      </Box>

      <List sx={{ px: 1.5, flex: 1 }}>
        {navItems.map((item) => (
          <NavLink key={item.to} to={item.to} end={item.to === '/'} style={{ textDecoration: 'none', color: 'inherit' }}>
            {({ isActive }) => (
              <ListItemButton
                selected={isActive}
                sx={{
                  mb: 0.5,
                  minHeight: 44,
                  color: isActive ? '#FFFFFF' : 'rgba(255,255,255,0.6)',
                  bgcolor: isActive ? 'rgba(255,255,255,0.12)' : 'transparent',
                  borderLeft: isActive ? '3px solid #FFFFFF' : '3px solid transparent',
                  borderRadius: '0 8px 8px 0',
                  '&.Mui-selected': {
                    bgcolor: 'rgba(255,255,255,0.12)',
                    color: '#FFFFFF',
                    '& .MuiListItemIcon-root': { color: '#FFFFFF' },
                  },
                  '&:hover': {
                    bgcolor: 'rgba(255,255,255,0.08)',
                  },
                }}
              >
                <ListItemIcon sx={{ color: 'inherit', minWidth: 36 }}>{item.icon}</ListItemIcon>
                <ListItemText
                  primary={item.label}
                  slotProps={{ primary: { sx: { fontSize: 14, fontWeight: isActive ? 600 : 400, fontFamily: 'Inter' } } }}
                />
              </ListItemButton>
            )}
          </NavLink>
        ))}
      </List>
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      {/* Sidebar — desktop */}
      <Drawer
        variant="permanent"
        sx={{
          display: { xs: 'none', md: 'block' },
          width: DRAWER_WIDTH,
          flexShrink: 0,
          '& .MuiDrawer-paper': {
            width: DRAWER_WIDTH,
            bgcolor: '#064E3B',
            border: 'none',
            boxSizing: 'border-box',
          },
        }}
      >
        {drawer}
      </Drawer>

      {/* Sidebar — mobile */}
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={() => setMobileOpen(false)}
        sx={{
          display: { xs: 'block', md: 'none' },
          '& .MuiDrawer-paper': {
            width: DRAWER_WIDTH,
            bgcolor: '#064E3B',
            border: 'none',
          },
        }}
      >
        {drawer}
      </Drawer>

      {/* Page content */}
      <Box component="main" sx={{ flexGrow: 1, bgcolor: '#f9f9ff', minHeight: '100vh', px: { xs: 2, md: 3 }, pb: { xs: 2, md: 3 } }}>
        <Outlet />
      </Box>
    </Box>
  );
}
