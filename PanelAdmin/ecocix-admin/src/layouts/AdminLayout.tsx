import { useState } from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Drawer, AppBar, Toolbar, IconButton, Typography, Box,
  List, ListItemButton, ListItemIcon, ListItemText, Button, Divider, Avatar,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import DashboardIcon from '@mui/icons-material/Dashboard';
import LocalShippingIcon from '@mui/icons-material/LocalShipping';
import DescriptionIcon from '@mui/icons-material/Description';
import PeopleIcon from '@mui/icons-material/People';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import NotificationsIcon from '@mui/icons-material/Notifications';
import LogoutIcon from '@mui/icons-material/Logout';
import GrassIcon from '@mui/icons-material/Grass';

const DRAWER_WIDTH = 240;

const navItems = [
  { to: '/', label: 'Dashboard', icon: <DashboardIcon /> },
  { to: '/camiones', label: 'Camiones', icon: <LocalShippingIcon /> },
  { to: '/reportes', label: 'Reportes', icon: <DescriptionIcon /> },
  { to: '/ciudadanos', label: 'Ciudadanos', icon: <PeopleIcon /> },
  { to: '/admins', label: 'Admins', icon: <AdminPanelSettingsIcon /> },
  { to: '/alertas', label: 'Alertas', icon: <NotificationsIcon /> },
];

export default function AdminLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const drawer = (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, px: 2, py: 2.5 }}>
        <Avatar sx={{ bgcolor: 'white', color: 'primary.main', width: 36, height: 36 }}>
          <GrassIcon />
        </Avatar>
        <Typography variant="subtitle1" sx={{ fontWeight: 700, color: 'white' }}>
          EcoCix Admin
        </Typography>
      </Box>
      <Divider sx={{ borderColor: 'rgba(255,255,255,0.12)' }} />
      <List sx={{ px: 1, pt: 1 }}>
        {navItems.map((item) => (
          <NavLink key={item.to} to={item.to} end={item.to === '/'} style={{ textDecoration: 'none', color: 'inherit' }}>
            {({ isActive }) => (
              <ListItemButton
                selected={isActive}
                sx={{
                  borderRadius: 1, mb: 0.5, color: 'rgba(255,255,255,0.8)',
                  '&.Mui-selected': { bgcolor: 'rgba(255,255,255,0.18)', color: 'white' },
                  '&:hover': { bgcolor: 'rgba(255,255,255,0.1)' },
                }}
              >
                <ListItemIcon sx={{ color: 'inherit', minWidth: 40 }}>{item.icon}</ListItemIcon>
                <ListItemText primary={item.label} />
              </ListItemButton>
            )}
          </NavLink>
        ))}
      </List>
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      <AppBar position="fixed" elevation={0} sx={{ bgcolor: 'white', color: 'text.primary', borderBottom: '1px solid #e0e0e0' }}>
        <Toolbar>
          <IconButton edge="start" onClick={() => setMobileOpen(!mobileOpen)} sx={{ mr: 2 }}>
            <MenuIcon />
          </IconButton>
          <Box sx={{ flexGrow: 1 }} />
          <Typography variant="body2" color="text.secondary" sx={{ mr: 2 }}>
            {user?.nombre || user?.email}
          </Typography>
          <Button startIcon={<LogoutIcon />} onClick={handleLogout} color="inherit" size="small">
            Salir
          </Button>
        </Toolbar>
      </AppBar>

      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={() => setMobileOpen(false)}
        sx={{ display: { xs: 'block', md: 'none' }, '& .MuiDrawer-paper': { width: DRAWER_WIDTH, bgcolor: '#0d631b' } }}
      >
        {drawer}
      </Drawer>

      <Drawer
        variant="permanent"
        sx={{
          display: { xs: 'none', md: 'block' },
          '& .MuiDrawer-paper': { width: DRAWER_WIDTH, bgcolor: '#0d631b', border: 'none' },
        }}
      >
        {drawer}
      </Drawer>

      <Box component="main" sx={{ flexGrow: 1, ml: { md: `${DRAWER_WIDTH}px` } }}>
        <Toolbar />
        <Box sx={{ p: 3, maxWidth: 1200, mx: 'auto' }}>
          <Outlet />
        </Box>
      </Box>
    </Box>
  );
}
