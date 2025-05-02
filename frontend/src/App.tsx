import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ThemeProvider } from './contexts/ThemeContext';
import { AuthProvider } from './contexts/AuthContext';
import PrivateRoute from './components/PrivateRoute';
import Navbar from './components/Navbar';
import HomeRouter from './components/HomeRouter';
import SearchPage from './pages/SearchPage';
import ExoplanetDetail from './pages/ExoplanetDetail';
import FavoritesPage from './pages/FavoritesPage';
import ProfilePage from './pages/ProfilPage';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import OtpVerificationPage from './pages/OtpVerificationPage';
import NotFoundPage from './pages/NotFoundPage';
import AdminPanel from './pages/AdminPanel';
import './i18n';

function App() {
  return (
    <Router>
      <AuthProvider>
        <ThemeProvider>
          <div className="min-h-screen bg-white dark:bg-gray-900 text-gray-900 dark:text-white">
            <Navbar />
            <main className="container mx-auto px-4 py-8">
              <Routes>
                <Route path="/" element={<HomeRouter />} />
                <Route path="/search" element={<SearchPage />} />
                <Route path="/exoplanet/:id" element={<ExoplanetDetail />} />
                <Route path="/favorites" element={
                  <PrivateRoute>
                    <FavoritesPage />
                  </PrivateRoute>
                } />
                <Route path="/profile" element={
                  <PrivateRoute>
                    <ProfilePage />
                  </PrivateRoute>
                } />
                <Route path="/admin" element={
                  <PrivateRoute>
                    <AdminPanel />
                  </PrivateRoute>
                } />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/signup" element={<SignupPage />} />
                <Route path="/verify-otp" element={<OtpVerificationPage />} />
                <Route path="*" element={<NotFoundPage />} />
              </Routes>
            </main>
          </div>
        </ThemeProvider>
      </AuthProvider>
    </Router>
  );
}

export default App;