import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Rocket, Home, Search, RotateCcw } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useTheme } from '../contexts/ThemeContext';

const NotFoundPage: React.FC = () => {
  const { t } = useTranslation();
  const { theme } = useTheme();
  const navigate = useNavigate();
  const [countdown, setCountdown] = useState(10);

  useEffect(() => {
    if (countdown > 0) {
      const timer = setTimeout(() => setCountdown(countdown - 1), 1000);
      return () => clearTimeout(timer);
    } else {
      navigate('/');
    }
  }, [countdown, navigate]);

  const cardClass = theme === 'dark' 
    ? 'bg-gray-800/90 backdrop-blur-sm' 
    : 'bg-white/90 backdrop-blur-sm';

  return (
    <div className="relative min-h-screen overflow-hidden">
      {/* Animated stars background */}
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,_#1a1a1a_0%,_#000000_100%)]">
        <div className="stars absolute inset-0" />
      </div>

      {/* Floating astronaut */}
      <div className="absolute w-full h-full">
        <img
          src="https://images.unsplash.com/photo-1614728894747-a83421e2b9c9"
          alt="Floating Astronaut"
          className="absolute w-64 h-64 object-cover rounded-full animate-float"
          style={{
            top: '20%',
            right: '15%',
            animation: 'float 6s ease-in-out infinite'
          }}
        />
      </div>

      {/* Content */}
      <div className="relative z-10 min-h-screen flex items-center justify-center px-4">
        <div className={`${cardClass} max-w-2xl rounded-2xl p-8 text-center space-y-8`}>
          <div className="space-y-4">
            <h1 className="text-8xl font-bold bg-gradient-to-r from-blue-500 to-purple-600 text-transparent bg-clip-text animate-pulse">
              404
            </h1>
            <p className="text-2xl font-semibold">
              {t('notFound.title')}
            </p>
            <p className="text-gray-500 dark:text-gray-400">
              {t('notFound.description')}
            </p>
          </div>

          <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
            <button
              onClick={() => navigate('/')}
              className="flex items-center gap-2 px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white rounded-full transition-colors duration-200 group"
            >
              <Home className="w-5 h-5 group-hover:animate-bounce" />
              {t('notFound.home')}
            </button>
            <button
              onClick={() => navigate('/search')}
              className="flex items-center gap-2 px-6 py-3 bg-purple-600 hover:bg-purple-700 text-white rounded-full transition-colors duration-200 group"
            >
              <Search className="w-5 h-5 group-hover:animate-bounce" />
              {t('notFound.search')}
            </button>
            <button
              onClick={() => navigate(-1)}
              className="flex items-center gap-2 px-6 py-3 bg-gray-600 hover:bg-gray-700 text-white rounded-full transition-colors duration-200 group"
            >
              <RotateCcw className="w-5 h-5 group-hover:animate-spin" />
              {t('notFound.back')}
            </button>
          </div>

          <div className="pt-4">
            <p className="text-gray-500 dark:text-gray-400">
              {t('notFound.redirect')} {countdown} {t('notFound.seconds')}
            </p>
          </div>

          <Rocket 
            className="w-12 h-12 mx-auto text-blue-500 animate-rocket" 
            style={{ transform: 'rotate(45deg)' }}
          />
        </div>
      </div>
    </div>
  );
};

export default NotFoundPage;