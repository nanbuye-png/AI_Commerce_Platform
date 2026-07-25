import { createBrowserRouter } from 'react-router-dom';
import MainLayout from '../layouts/MainLayout';
import AuthLayout from '../layouts/AuthLayout';
import ProtectedRoute from './ProtectedRoute';

// Pages (Sprint 1 - preserved)
import Login from '../pages/Login';
import Register from '../pages/Register';
import NotFound from '../pages/NotFound';

// Feature Pages (Sprint 2 - skeletons)
import HomePage from '../features/home/HomePage';
import ProductListPage from '../features/product/ProductListPage';
import ProductDetailPage from '../features/product/ProductDetailPage';
import OrderPage from '../features/order/OrderPage';
import ProfilePage from '../features/profile/ProfilePage';
import AIPage from '../features/ai/AIPage';

// Feature Pages (Sprint 3 - product commerce core)
import SearchPage from '../features/search/SearchPage';

// Feature Pages (Sprint 4 - shopping experience core)
import CartPage from '../features/cart/pages/CartPage';
import CheckoutPage from '../features/checkout/CheckoutPage';

const router = createBrowserRouter([
  // Auth routes (no layout wrapper needed, but using AuthLayout for consistency)
  {
    path: '/login',
    element: (
      <AuthLayout>
        <Login />
      </AuthLayout>
    ),
  },
  {
    path: '/register',
    element: (
      <AuthLayout>
        <Register />
      </AuthLayout>
    ),
  },

  // Main layout routes (public)
  {
    path: '/',
    element: <MainLayout />,
    children: [
      {
        index: true,
        element: <HomePage />,
      },
      {
        path: 'products',
        element: <ProductListPage />,
      },
      {
        path: 'products/:productId',
        element: <ProductDetailPage />,
      },
      {
        path: 'categories/:categoryId',
        element: <ProductListPage />,
      },
      {
        path: 'search',
        element: <SearchPage />,
      },
      {
        path: 'ai',
        element: <AIPage />,
      },

      // Protected routes (require login)
      {
        path: 'cart',
        element: (
          <ProtectedRoute>
            <CartPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'checkout',
        element: (
          <ProtectedRoute>
            <CheckoutPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'orders',
        element: (
          <ProtectedRoute>
            <OrderPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'profile',
        element: (
          <ProtectedRoute>
            <ProfilePage />
          </ProtectedRoute>
        ),
      },

      // 404 fallback
      {
        path: '*',
        element: <NotFound />,
      },
    ],
  },
]);

export default router;