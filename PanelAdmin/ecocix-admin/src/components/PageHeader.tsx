import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Box, Toolbar, IconButton, Typography, Avatar, Badge, Tooltip,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import NotificationsNoneIcon from '@mui/icons-material/NotificationsNone';
import LogoutIcon from '@mui/icons-material/Logout';

interface PageHeaderProps {
  onMenuClick?: () => void;
  showMenuButton?: boolean;
}

export default function PageHeader({ onMenuClick, showMenuButton = false }: PageHeaderProps) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <Box sx={{
      position: 'sticky', top: 0, zIndex: 1100,
      bgcolor: '#FFFFFF', borderBottom: '1px solid #e7eefe',
    }}>
      <Toolbar sx={{ minHeight: '56px !important' }}>
        {showMenuButton && (
          <IconButton edge="start" onClick={onMenuClick} sx={{ mr: 1, color: '#404944' }}>
            <MenuIcon />
          </IconButton>
        )}

        <Box sx={{ flexGrow: 1 }} />

        <IconButton onClick={() => navigate('/reportes')} sx={{ color: '#404944' }}>
          <Badge badgeContent={3} color="error" variant="dot">
            <NotificationsNoneIcon />
          </Badge>
        </IconButton>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, ml: 1.5 }}>
          <Avatar sx={{ width: 32, height: 32, bgcolor: '#064E3B', color: '#FFFFFF', fontSize: 13, fontWeight: 600 }}>
            {(user?.nombre || user?.email || 'A').charAt(0).toUpperCase()}
          </Avatar>
          <Box sx={{ display: { xs: 'none', sm: 'block' } }}>
            <Typography sx={{ fontSize: 13, fontWeight: 600, color: '#151c27', lineHeight: 1.2, fontFamily: 'Inter' }}>
              {user?.nombre || 'Administrador'}
            </Typography>
            <Typography sx={{ fontSize: 11, color: '#707974', lineHeight: 1.2, fontFamily: 'Inter' }}>
              {user?.email}
            </Typography>
          </Box>
        </Box>

        <Tooltip title="Cerrar Sesión">
          <IconButton onClick={logout} sx={{ ml: 1.5, color: '#404944', '&:hover': { color: '#ba1a1a' } }}>
            <LogoutIcon />
          </IconButton>
        </Tooltip>
      </Toolbar>
    </Box>
  );
}
