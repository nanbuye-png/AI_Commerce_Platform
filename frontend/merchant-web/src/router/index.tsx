import { createBrowserRouter } from 'react-router-dom';
import MerchantLayout from '../layouts/MerchantLayout';
import DashboardPage from '../features/dashboard/DashboardPage';
import ProductListPage from '../features/products/ProductListPage';
import ProductCreatePage from '../features/products/ProductCreatePage';
import ProductEditPage from '../features/products/ProductEditPage';
import OrderListPage from '../features/orders/OrderListPage';
import AIAssistantPage from '../features/ai-assistant/AIAssistantPage';
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
        <RoleGuard allowedRoles={['MERCHANT']}>
          <MerchantLayout />
        </RoleGuard>
      </ProtectedRoute>
    ),
    children: [
      { index: true, element: <DashboardPage /> },
      { path: 'dashboard', element: <DashboardPage /> },
      { path: 'products', element: <ProductListPage /> },
      { path: 'products/create', element: <ProductCreatePage /> },
      { path: 'products/:id/edit', element: <ProductEditPage /> },
      { path: 'orders', element: <OrderListPage /> },
      { path: 'ai-assistant', element: <AIAssistantPage /> },
      { path: '*', element: <DashboardPage /> },
    ],
  },
]);

export default router;