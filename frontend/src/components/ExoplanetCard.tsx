import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Star } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useTheme } from '../contexts/ThemeContext';
import { useAuth } from '../contexts/AuthContext';
import { Exoplanet } from '../types/exoplanet';

interface ExoplanetCardProps {
  exoplanet: Exoplanet;
  isFavorite?: boolean;
  onToggleFavorite?: (id: number) => void;
}

const ExoplanetCard: React.FC<ExoplanetCardProps> = ({
  exoplanet,
  isFavorite = false,
  onToggleFavorite,
}) => {
  const { t } = useTranslation();
  const { theme } = useTheme();
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const cardClass = theme === 'dark' 
    ? 'bg-gray-800 hover:bg-gray-700' 
    : 'bg-white hover:bg-gray-50 border border-gray-200';

  const handleFavoriteClick = (e: React.MouseEvent) => {
    e.preventDefault();
    if (!isAuthenticated) {
      navigate('/login', { state: { from: `/exoplanet/${exoplanet.id}` } });
      return;
    }
    onToggleFavorite?.(exoplanet.id);
  };

  return (
    <div className={`rounded-lg overflow-hidden shadow-lg transition-all duration-300 ${cardClass}`}>
      <div className="relative h-48">
        <img
          src={exoplanet.imageUrl || 'https://images.unsplash.com/photo-1462331940025-496dfbfc7564'}
          alt={exoplanet.name}
          className="w-full h-full object-cover"
        />
        {onToggleFavorite && (
          <button
            onClick={handleFavoriteClick}
            className="absolute top-2 right-2 p-2 rounded-full bg-gray-900 bg-opacity-50 hover:bg-opacity-75 transition-all duration-200"
          >
            <Star
              className={`w-6 h-6 ${isFavorite ? 'text-yellow-400 fill-current' : 'text-white'}`}
            />
          </button>
        )}
      </div>
      
      <div className="p-4">
        <h3 className="text-xl font-bold mb-2">
          {exoplanet.name}
        </h3>
        <Link
          to={`/exoplanet/${exoplanet.id}`}
          className="mt-4 inline-block w-full bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors duration-200 text-center"
        >
          {t('exoplanet.learnMore')}
        </Link>
      </div>
    </div>
  );
};

export default ExoplanetCard;
