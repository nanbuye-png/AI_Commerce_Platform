import { createBrowserRouter } from 'react-router-dom';
import MerchantLayout from '../layouts/MerchantLayout';
import DashboardPage from '../features/dashboard/DashboardPage';
import ProductListPage from '../features/products/ProductListPage';
import ProductCreatePage from '../features/products/ProductCreatePage';
import ProductEditPage from '../features/products/ProductEditPage';
import OrderListPage from '../features/orders/OrderListPage';
import OrderDetailPage from '../features/orders/OrderDetailPage';
import RefundListPage from '../features/refunds/RefundListPage';
import RefundDetailPage from '../features/refunds/RefundDetailPage';
import ReturnListPage from '../features/returns/ReturnListPage';
import ReturnDetailPage from '../features/returns/ReturnDetailPage';
import AIAssistantPage from '../features/ai-assistant/AIAssistantPage';
import Login from '../pages/Login';
import Register from '../pages/Register';
import ProtectedRoute from './ProtectedRoute';
import RoleGuard from './RoleGuard';

const router = createBrowserRouter([
  {
    path: '/login',
    element: <Login />,
  },
  {
    path: '/register',
    element: <Register />,
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
      { path: 'orders/:orderNo', element: <OrderDetailPage /> },
      { path: 'refunds', element: <RefundListPage /> },
      { path: 'refunds/:id', element: <RefundDetailPage /> },
      { path: 'returns', element: <ReturnListPage /> },
      { path: 'returns/:id', element: <ReturnDetailPage /> },
      { path: 'ai-assistant', element: <AIAssistantPage /> },
      { path: '*', element: <DashboardPage /> },
    ],
  },
]);

export default router;