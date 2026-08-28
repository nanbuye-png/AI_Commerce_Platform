import React, { useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import useAuthStore from '../stores/authStore';
import { getToken } from '../utils/token';

interface ProtectedRouteProps {
  children: React.ReactNode;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { isAuthenticated, initialized, init } = useAuthStore();

  useEffect(() => {
    init();
  }, [init]);

  const token = getToken();
  if (!initialized) return null;

  if (!token && !isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
