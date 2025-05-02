import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useTheme } from '../contexts/ThemeContext';
import ExoplanetCard from '../components/ExoplanetCard';
import apiService from '../services/apiService';
import { Exoplanet } from '../types/exoplanet';
import { useAuth } from '../contexts/AuthContext';

const FavoritesPage: React.FC = () => {
  const { t } = useTranslation();
  const { theme } = useTheme();
  const { user } = useAuth();
  const [favorites, setFavorites] = useState<Exoplanet[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!user) {
      setFavorites([]);
      setLoading(false);
      return;
    }
    apiService.getFavorites(user.email)
      .then((response) => {
        const data = response.data as any[];
        const mapped = data.map((exo) => ({
          ...exo,
          imageUrl: exo.imageExo,
        }));
        setFavorites(mapped);
        setLoading(false);
      })
      .catch((err) => {
        setError(err.message);
        setLoading(false);
      });
  }, [user]);

  const cardClass = theme === 'dark'
    ? 'bg-gray-800'
    : 'bg-white border border-gray-200';

  return (
    <div className="max-w-6xl mx-auto px-4">
      <div className={`${cardClass} p-6 rounded-lg mb-8`}>
        <h1 className="text-3xl font-bold mb-2">{t('nav.favorites')}</h1>
        <p className="text-gray-400">Your collection of fascinating exoplanets</p>
      </div>

      {loading && <p>{t('common.loading')}</p>}
      {error && <p className="text-red-500">{error}</p>}

      {!loading && !error && (
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
          {favorites.map((exo) => (
            <ExoplanetCard key={exo.id} exoplanet={exo} isFavorite={true} onToggleFavorite={() => {}} />
          ))}
        </div>
      )}
    </div>
  );
};

export default FavoritesPage;
