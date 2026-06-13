import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import api from '../config/axios';
import type { LoginResponse, Admin } from '../types';

interface AuthContextType {
  user: Admin | null;
  token: string | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<Admin | null>(null);
  const [token, setToken] = useState<string | null>(localStorage.getItem('admin_token'));

  useEffect(() => {
    if (token) {
      const stored = localStorage.getItem('admin_user');
      if (stored) {
        setUser(JSON.parse(stored));
      } else {
        api.get('/api/admin/auth/listar')
          .then((res) => {
            const admins = res.data;
            const admin = admins.find((a: Admin) => a.email === JSON.parse(atob(token.split('.')[1])).sub);
            if (admin) {
              setUser(admin);
              localStorage.setItem('admin_user', JSON.stringify(admin));
            }
          })
          .catch(() => { logout(); });
      }
    }
  }, [token]);

  const login = async (email: string, password: string) => {
    const res = await api.post<LoginResponse>('/api/admin/auth/login', { email, password });
    localStorage.setItem('admin_token', res.data.token);
    setToken(res.data.token);
    setUser({ id: 0, nombre: res.data.nombre, email: res.data.email, activo: true, fechaCreacion: '' });
    localStorage.setItem('admin_user', JSON.stringify({ id: 0, nombre: res.data.nombre, email: res.data.email, activo: true, fechaCreacion: '' }));
  };

  const logout = () => {
    localStorage.removeItem('admin_token');
    localStorage.removeItem('admin_user');
    setUser(null);
    setToken(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, login, logout, isAuthenticated: !!token }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
