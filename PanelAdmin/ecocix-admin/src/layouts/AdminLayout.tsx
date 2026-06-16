import { useState } from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Drawer, AppBar, Toolbar, IconButton, Typography, Box,
  List, ListItemButton, ListItemIcon, ListItemText, Button, Divider, Avatar, useTheme, alpha,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import DashboardIcon from '@mui/icons-material/Dashboard';
import LocalShippingIcon from '@mui/icons-material/LocalShipping';
import DescriptionIcon from '@mui/icons-material/Description';
import PeopleIcon from '@mui/icons-material/People';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import NotificationsIcon from '@mui/icons-material/Notifications';
import MapIcon from '@mui/icons-material/Map';
import LogoutIcon from '@mui/icons-material/Logout';
import ecocixLogo from '../assets/ecocix-logo.png';

const DRAWER_WIDTH = 240;

const navItems = [
  { to: '/', label: 'Dashboard', icon: <DashboardIcon /> },
  { to: '/camiones', label: 'Camiones', icon: <LocalShippingIcon /> },
  { to: '/mapa', label: 'Mapa', icon: <MapIcon /> },
  { to: '/reportes', label: 'Reportes', icon: <DescriptionIcon /> },
  { to: '/ciudadanos', label: 'Ciudadanos', icon: <PeopleIcon /> },
  { to: '/admins', label: 'Admins', icon: <AdminPanelSettingsIcon /> },
  { to: '/alertas', label: 'Alertas', icon: <NotificationsIcon /> },
];

export default function AdminLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const theme = useTheme();
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const drawer = (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, px: 2.5, py: 2.5 }}>
        <Avatar
          sx={{
            width: 40,
            height: 40,
          }}
        >
          <Box component="img" src={ecocixLogo} sx={{ width: 40, height: 40, borderRadius: '50%' }} />
        </Avatar>
        <Box>
          <Typography variant="subtitle1" sx={{ fontWeight: 700, color: 'primary.contrastText', lineHeight: 1.2 }}>
            EcoCix
          </Typography>
          <Typography variant="caption" sx={{ color: alpha(theme.palette.primary.contrastText, 0.6), fontSize: 11 }}>
            Panel Admin
          </Typography>
        </Box>
      </Box>
      <Divider sx={{ borderColor: alpha(theme.palette.primary.contrastText, 0.08) }} />
      <List sx={{ px: 1.5, pt: 1.5 }}>
        {navItems.map((item) => (
          <NavLink key={item.to} to={item.to} end={item.to === '/'} style={{ textDecoration: 'none', color: 'inherit' }}>
            {({ isActive }) => (
              <ListItemButton
                selected={isActive}
                sx={{
                  mb: 0.5,
                  color: isActive ? theme.palette.success.main : alpha(theme.palette.primary.contrastText, 0.7),
                  borderLeft: isActive ? `3px solid ${theme.palette.success.main}` : '3px solid transparent',
                  '&.Mui-selected': {
                    bgcolor: alpha(theme.palette.success.main, 0.15),
                    color: theme.palette.success.main,
                    '& .MuiListItemIcon-root': { color: theme.palette.success.main },
                  },
                  '&:hover': {
                    bgcolor: alpha(theme.palette.success.main, 0.08),
                  },
                }}
              >
                <ListItemIcon sx={{ color: 'inherit', minWidth: 36 }}>{item.icon}</ListItemIcon>
                <ListItemText
                  primary={item.label}
                  slotProps={{ primary: { sx: { fontSize: 14, fontWeight: isActive ? 600 : 400 } } }}
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
      <AppBar
        position="fixed"
        elevation={0}
        sx={{
          bgcolor: alpha(theme.palette.background.paper, 0.85),
          color: 'text.primary',
          backdropFilter: 'blur(12px)',
          borderBottom: `1px solid ${theme.palette.grey[200]}`,
        }}
      >
        <Toolbar>
          <IconButton edge="start" onClick={() => setMobileOpen(!mobileOpen)} sx={{ mr: 2, display: { md: 'none' } }}>
            <MenuIcon />
          </IconButton>
          <Box sx={{ flexGrow: 1 }} />
          <Typography variant="body2" color="text.secondary" sx={{ mr: 2 }}>
            {user?.nombre || user?.email}
          </Typography>
          <Button startIcon={<LogoutIcon />} onClick={handleLogout} color="inherit" size="small" sx={{ color: 'text.secondary' }}>
            Salir
          </Button>
        </Toolbar>
      </AppBar>

      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={() => setMobileOpen(false)}
        sx={{
          display: { xs: 'block', md: 'none' },
          '& .MuiDrawer-paper': {
            width: DRAWER_WIDTH,
            bgcolor: 'primary.main',
            border: 'none',
            borderTopRightRadius: 16,
            borderBottomRightRadius: 16,
            boxShadow: '4px 0 24px rgba(0,0,0,0.2)',
          },
        }}
      >
        {drawer}
      </Drawer>

      <Drawer
        variant="permanent"
        sx={{
          display: { xs: 'none', md: 'block' },
          '& .MuiDrawer-paper': {
            width: DRAWER_WIDTH,
            bgcolor: 'primary.main',
            border: 'none',
            borderTopRightRadius: 16,
            borderBottomRightRadius: 16,
            boxShadow: '2px 0 16px rgba(0,0,0,0.1)',
          },
        }}
      >
        {drawer}
      </Drawer>

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          ml: { md: `${DRAWER_WIDTH}px` },
          bgcolor: 'grey.50',
          minHeight: '100vh',
        }}
      >
        <Toolbar />
        <Box sx={{ p: { xs: 2, md: 3 } }}>
          <Outlet />
        </Box>
      </Box>
    </Box>
  );
}
