import React, { useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import useAuthStore from '../stores/authStore';
import { getToken } from '../utils/token';

interface ProtectedRouteProps {
  children: React.ReactNode;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { isAuthenticated, init } = useAuthStore();

  useEffect(() => {
    init();
  }, [init]);

  // 检查 token 是否存在（从 localStorage 或 store）
  const token = getToken();
  if (!token && !isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
