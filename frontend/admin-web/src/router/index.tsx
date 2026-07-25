import { createBrowserRouter } from 'react-router-dom';
import AdminLayout from '../layouts/AdminLayout';
import DashboardPage from '../features/dashboard/DashboardPage';
import UserListPage from '../features/users/UserListPage';
import MerchantListPage from '../features/merchants/MerchantListPage';
import MerchantDetailPage from '../features/merchants/MerchantDetailPage';
import ProductReviewPage from '../features/products/ProductReviewPage';
import OrderListPage from '../features/orders/OrderListPage';
import AICenterPage from '../features/ai-center/AICenterPage';
import AuditLogPage from '../features/audit/AuditLogPage';
import Login from '../pages/Login';
import ProtectedRoute from './ProtectedRoute';
import RoleGuard from './RoleGuard';

const router = createBrowserRouter([
  {
    path: '/login',
    element: <Login />,
  },
  {
    path: '/',
    element: (
      <ProtectedRoute>
        <RoleGuard allowedRoles={['ADMIN', 'SUPER_ADMIN']}>
          <AdminLayout />
        </RoleGuard>
      </ProtectedRoute>
    ),
    children: [
      { index: true, element: <DashboardPage /> },
      { path: 'dashboard', element: <DashboardPage /> },
      { path: 'users', element: <UserListPage /> },
      { path: 'merchants', element: <MerchantListPage /> },
      { path: 'merchants/:id', element: <MerchantDetailPage /> },
      { path: 'products', element: <ProductReviewPage /> },
      { path: 'orders', element: <OrderListPage /> },
      { path: 'ai-center', element: <AICenterPage /> },
      { path: 'audit-log', element: <AuditLogPage /> },
      { path: '*', element: <DashboardPage /> },
    ],
  },
]);

export default router;