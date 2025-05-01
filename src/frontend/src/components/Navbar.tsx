import React from 'react';
import { Link } from 'react-router-dom';
import {
  Plane as Planet,
  Search,
  User,
  Star,
  LogIn,
  LogOut,
  Shield
} from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../contexts/AuthContext';
import ThemeToggle from './ThemeToggle';
import LanguageSelector from './LanguageSelector';

const Navbar: React.FC = () => {
  const { t } = useTranslation();
  const { isAuthenticated, isAdmin, logout } = useAuth();
  
  return (
    <nav className="bg-gray-100 dark:bg-gray-900 text-gray-900 dark:text-white p-4 shadow-md">
      <div className="container mx-auto flex items-center justify-between">
        <Link to="/" className="flex items-center space-x-2 text-xl font-bold">
          <Planet className="w-8 h-8" />
          <span>ExoExplorer</span>
        </Link>
        
        <div className="flex items-center space-x-6">
          {/* Affichage conditionnel basé sur isAdmin et isAuthenticated */}
          {isAdmin ? (
            <>
              {/* Liens pour les administrateurs uniquement */}
              <Link to="/admin" className="flex items-center space-x-1 hover:text-blue-600 dark:hover:text-blue-400">
                <Shield className="w-5 h-5" />
                <span>Admin</span>
              </Link>
              
              <button
                onClick={logout}
                className="flex items-center space-x-1 hover:text-blue-600 dark:hover:text-blue-400"
              >
                <LogOut className="w-5 h-5" />
                <span>{t('nav.logout')}</span>
              </button>
            </>
          ) : (
            <>
              {/* Liens pour les utilisateurs non-admin */}
              <Link to="/search" className="flex items-center space-x-1 hover:text-blue-600 dark:hover:text-blue-400">
                <Search className="w-5 h-5" />
                <span>{t('nav.search')}</span>
              </Link>
              
              {isAuthenticated && (
                <>
                  <Link to="/favorites" className="flex items-center space-x-1 hover:text-blue-600 dark:hover:text-blue-400">
                    <Star className="w-5 h-5" />
                    <span>{t('nav.favorites')}</span>
                  </Link>
                  
                  <Link to="/profile" className="flex items-center space-x-1 hover:text-blue-600 dark:hover:text-blue-400">
                    <User className="w-5 h-5" />
                    <span>{t('nav.profile')}</span>
                  </Link>
                  
                  <button
                    onClick={logout}
                    className="flex items-center space-x-1 hover:text-blue-600 dark:hover:text-blue-400"
                  >
                    <LogOut className="w-5 h-5" />
                    <span>{t('nav.logout')}</span>
                  </button>
                </>
              )}
              
              {!isAuthenticated && (
                <Link to="/login" className="flex items-center space-x-1 hover:text-blue-600 dark:hover:text-blue-400">
                  <LogIn className="w-5 h-5" />
                  <span>{t('nav.login')}</span>
                </Link>
              )}
            </>
          )}
          
          <ThemeToggle />
          <LanguageSelector />
        </div>
      </div>
    </nav>
  );
};

export default Navbar;