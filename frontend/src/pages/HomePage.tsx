import React from 'react';
import { Plane as Planet, Search, Star, Users } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useTheme } from '../contexts/ThemeContext';
import ExoplanetCard from '../components/ExoplanetCard';
import { useExoplanets } from '../hooks/useExoplanets';

const HomePage: React.FC = () => {
  const { t } = useTranslation();
  const { theme } = useTheme();
  const { exoplanets, loading, error } = useExoplanets();

  const cardClass = theme === 'dark' 
    ? 'bg-gray-800/90 hover:bg-gray-700/90 backdrop-blur-sm'
    : 'bg-white/90 hover:bg-gray-50/90 backdrop-blur-sm border border-gray-200';

  const latestDiscoveries = exoplanets.slice(0, 3);

  return (
    <div className="relative">
      {}
      <div 
        className="absolute inset-0 h-[100vh] bg-cover bg-center bg-no-repeat"
        style={{
          backgroundImage: 'url("https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?auto=format&fit=crop&q=80")',
          backgroundAttachment: 'fixed'
        }}
      >
        <div className="absolute inset-0 bg-black/50"></div>
      </div>

      <div className="relative space-y-12 pt-8">
        <section className="min-h-[80vh] flex flex-col items-center justify-center text-center py-16 px-4">
          <h1 className="text-5xl md:text-6xl lg:text-7xl font-bold mb-6 text-white">
            {t('home.title')}
          </h1>
          <p className="text-xl md:text-2xl max-w-2xl mx-auto text-gray-200">
            {t('home.subtitle')}
          </p>
          <Link
            to="/search"
            className="mt-8 px-8 py-4 bg-blue-600 hover:bg-blue-700 text-white rounded-full text-lg font-semibold transition-colors duration-200"
          >
            {t('home.searchTitle')}
          </Link>
        </section>

        <div className="container mx-auto px-4">
          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-8 py-16">
            <Link
              to="/search"
              className={`p-6 rounded-lg text-center transition-colors ${cardClass}`}
            >
              <Search className="w-12 h-12 mx-auto mb-4 text-blue-400" />
              <h2 className="text-xl font-semibold mb-2">
                {t('home.searchTitle')}
              </h2>
              <p className={`${theme === 'dark' ? 'text-gray-300' : 'text-gray-600'}`}>
                {t('home.searchDesc')}
              </p>
            </Link>

            <div className={`relative p-6 rounded-lg text-center ${cardClass}`}>
  <Planet className="w-12 h-12 mx-auto mb-4 text-green-400" />
  <h2 className="text-xl font-semibold mb-2">
    {t('home.modelsTitle')}
  </h2>
  <p className={`${theme === 'dark' ? 'text-gray-300' : 'text-gray-600'}`}>
    {t('home.modelsDesc')}
  </p>

  {}
  <span className="absolute top-4 right-4 bg-yellow-400 text-black text-xs font-bold px-2 py-1 rounded-full shadow">
    {t('common.comingSoon')}
  </span>
</div>


            <Link
              to="/favorites"
              className={`p-6 rounded-lg text-center transition-colors ${cardClass}`}
            >
              <Star className="w-12 h-12 mx-auto mb-4 text-yellow-400" />
              <h2 className="text-xl font-semibold mb-2">
                {t('home.favoritesTitle')}
              </h2>
              <p className={`${theme === 'dark' ? 'text-gray-300' : 'text-gray-600'}`}>
                {t('home.favoritesDesc')}
              </p>
            </Link>

            <Link
              to="/profile"
              className={`p-6 rounded-lg text-center transition-colors ${cardClass}`}
            >
              <Users className="w-12 h-12 mx-auto mb-4 text-purple-400" />
              <h2 className="text-xl font-semibold mb-2">
                {t('home.communityTitle')}
              </h2>
              <p className={`${theme === 'dark' ? 'text-gray-300' : 'text-gray-600'}`}>
                {t('home.communityDesc')}
              </p>
            </Link>
          </div>

          <section className="py-16">
            <h2 className="text-3xl font-bold text-center mb-8">
              {t('home.latestTitle')}
            </h2>

            {loading && <p className="text-center">{t('common.loading')}</p>}
            {error && <p className="text-center text-red-500">{error}</p>}

            {!loading && !error && (
              <div className="grid md:grid-cols-3 gap-8">
                {latestDiscoveries.map((exoplanet) => (
                  <ExoplanetCard
                    key={exoplanet.id}
                    exoplanet={exoplanet}
                    onToggleFavorite={() => {}}
                  />
                ))}
              </div>
            )}
          </section>
        </div>
      </div>
    </div>
  );
};

export default HomePage;
