'use client';

import React, { createContext, useContext, useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { authAPI, setLoggingOut } from './api';

interface User {
  id: number;
  email: string;
  fullName: string;
  role: string;
  companyId: number;
}

interface AuthContextType {
  user: User | null;
  login: (email: string, password: string) => Promise<void>;
  signup: (data: any) => Promise<void>;
  logout: () => void;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Feature flag: Set to true once backend cookie support is implemented
const USE_COOKIES = true;

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    // Clear the logout flag if it exists (from a previous logout)
    setLoggingOut(false);

    if (USE_COOKIES) {
      // Cookie-based auth: verify session with backend
      verifySession();
    } else {
      // Temporary fallback: use localStorage until backend is ready
      loadFromLocalStorage();
    }
  }, []);

  const loadFromLocalStorage = () => {
    try {
      const savedUser = localStorage.getItem('user');
      const savedToken = localStorage.getItem('auth_token');

      if (savedUser && savedToken) {
        setUser(JSON.parse(savedUser));
      }
    } catch (error) {
      console.error('Error loading from localStorage:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const verifySession = async () => {
    try {
      const response = await authAPI.verifySession();
      if (response.data.success) {
        setUser(response.data.data);
      }
    } catch (error: any) {
      console.log('Session verification failed:', error.response?.status);
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  };

  const login = async (email: string, password: string) => {
    try {
      const response = await authAPI.login({ email, password });
      const { accessToken, userId, fullName, role, companyId } = response.data.data;

      const userData: User = {
        id: userId,
        email,
        fullName,
        role,
        companyId,
      };

      if (USE_COOKIES) {
        // Cookie-based: token stored in httpOnly cookie by backend
        setUser(userData);
      } else {
        // Temporary fallback: store in localStorage
        localStorage.setItem('auth_token', accessToken);
        localStorage.setItem('user', JSON.stringify(userData));
        setUser(userData);
      }

      router.push('/dashboard');
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Login failed');
    }
  };

  const signup = async (data: any) => {
    try {
      const response = await authAPI.signup(data);
      const { accessToken, userId, email, fullName, role, companyId } = response.data.data;

      const userData: User = {
        id: userId,
        email,
        fullName,
        role,
        companyId,
      };

      if (USE_COOKIES) {
        // Cookie-based: token stored in httpOnly cookie by backend
        setUser(userData);
      } else {
        // Temporary fallback: store in localStorage
        localStorage.setItem('auth_token', accessToken);
        localStorage.setItem('user', JSON.stringify(userData));
        setUser(userData);
      }

      router.push('/dashboard');
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Signup failed');
    }
  };

  const logout = async () => {
    // Set flag to prevent interceptor from redirecting (persists across page load)
    setLoggingOut(true);

    // Clear user state first
    setUser(null);

    // Clear localStorage if not using cookies
    if (!USE_COOKIES) {
      localStorage.removeItem('auth_token');
      localStorage.removeItem('user');
    }

    // Call backend to clear the httpOnly cookie
    if (USE_COOKIES) {
      try {
        await authAPI.logout();
      } catch (error) {
        // Ignore errors during logout
        console.log('Logout API call failed, but continuing with logout');
      }
    }

    // Redirect (flag will be cleared when landing page loads)
    window.location.href = '/';
  };

  return (
    <AuthContext.Provider value={{ user, login, signup, logout, isLoading }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
