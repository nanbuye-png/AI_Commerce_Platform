import React from 'react';
import { Navigate } from 'react-router-dom';
import useAuthStore from '../stores/authStore';

interface RoleGuardProps {
  children: React.ReactNode;
  allowedRoles: string[];
}

const RoleGuard: React.FC<RoleGuardProps> = ({ children, allowedRoles }) => {
  const { userInfo, isAuthenticated } = useAuthStore();

  if (!isAuthenticated || !userInfo) {
    return <Navigate to="/login" replace />;
  }

  if (!allowedRoles.includes(userInfo.role)) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

export default RoleGuard;