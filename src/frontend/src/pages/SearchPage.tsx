import React, { useState, useEffect } from 'react';
import { Search as SearchIcon, X, ChevronLeft, ChevronRight, Sliders } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useTheme } from '../contexts/ThemeContext';
import ExoplanetCard from '../components/ExoplanetCard';
import apiService from '../services/apiService';
import { useAuth } from '../contexts/AuthContext';
import ModalMessage from '../components/ModalMessage';

const SearchPage: React.FC = () => {
  const { t } = useTranslation();
  const { theme } = useTheme();
  const { user } = useAuth();

  const [searchTerm, setSearchTerm] = useState('');
  const [showFilters, setShowFilters] = useState(false);
  const [exoplanets, setExoplanets] = useState([]);
  const [favorites, setFavorites] = useState<number[]>([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Modal
  const [modal, setModal] = useState({
    show: false,
    message: '',
    type: 'success' as 'success' | 'error',
  });

  // Filtres
  const [minTemp, setMinTemp] = useState('');
  const [maxTemp, setMaxTemp] = useState('');
  const [minDistance, setMinDistance] = useState('');
  const [maxDistance, setMaxDistance] = useState('');
  const [minYear, setMinYear] = useState('');
  const [maxYear, setMaxYear] = useState('');

  const cardClass = theme === 'dark' ? 'bg-gray-800' : 'bg-white border border-gray-200';
  const inputClass = theme === 'dark'
    ? 'bg-gray-700 border-gray-600 text-white placeholder-gray-400'
    : 'bg-gray-50 border-gray-300 text-gray-900 placeholder-gray-500';

  // Charger les favoris de l'utilisateur si connecté
  useEffect(() => {
    if (user?.email) {
      apiService.getFavorites(user.email)
        .then((response) => {
          const favIds = response.data.map((fav: any) => fav.id);
          setFavorites(favIds);
        })
        .catch((err) => {
          console.error("Error fetching favorites:", err);
        });
    }
  }, [user]);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await apiService.getExoplanetSummaries({
          name: searchTerm,
          minTemp: minTemp || undefined,
          maxTemp: maxTemp || undefined,
          minDistance: minDistance || undefined,
          maxDistance: maxDistance || undefined,
          minYear: minYear || undefined,
          maxYear: maxYear || undefined,
          page,
          size,
        });
        
        const exoData = response.data.content || [];
        
        const formattedExoplanets = exoData.map((exo: any) => ({
          id: exo.id,
          name: exo.name,
          imageUrl: exo.imageExo
        }));
        
        setExoplanets(formattedExoplanets);
        setTotalPages(response.data.totalPages || 0);
      } catch (err: any) {
        console.error("Error fetching exoplanets:", err);
        setError(err.message || t('search.error'));
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [searchTerm, minTemp, maxTemp, minDistance, maxDistance, minYear, maxYear, page, size, t]);

  const resetFilters = () => {
    setMinTemp('');
    setMaxTemp('');
    setMinDistance('');
    setMaxDistance('');
    setMinYear('');
    setMaxYear('');
  };

  const handleToggleFavorite = async (exoplanetId: number) => {
    if (!user?.email) {
      setModal({
        show: true,
        message: t('common.loginRequired'),
        type: 'error'
      });
      return;
    }

    try {
      await apiService.toggleFavorite({ email: user.email, exoplanetId });
      
      if (favorites.includes(exoplanetId)) {
        // Retirer des favoris
        setFavorites(favorites.filter(id => id !== exoplanetId));
        setModal({
          show: true,
          message: t('exoplanet.removedFromFavorites'),
          type: 'success'
        });
      } else {
        // Ajouter aux favoris
        setFavorites([...favorites, exoplanetId]);
        setModal({
          show: true,
          message: t('exoplanet.addedToFavorites'),
          type: 'success'
        });
      }
    } catch (err) {
      setModal({
        show: true,
        message: t('common.error'),
        type: 'error'
      });
    }
  };

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-6">{t('search.title')}</h1>

      <div className={`${cardClass} p-6 rounded-lg mb-8`}>
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1 relative">
            <SearchIcon className="absolute left-3 top-3 text-gray-400" />
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => { setSearchTerm(e.target.value); setPage(0); }}
              placeholder={t('search.placeholder') || 'Search exoplanets...'}
              className={`w-full pl-10 pr-4 py-2 rounded-lg border ${inputClass}`}
            />
          </div>

          <button
            onClick={() => setShowFilters(!showFilters)}
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
          >
            {showFilters ? <X className="w-5 h-5" /> : <Sliders className="w-5 h-5" />}
            {showFilters ? t('common.close') : t('search.filter')}
          </button>
        </div>

        {showFilters && (
          <div className="mt-6">
            <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
              <div>
                <label className="block text-sm font-medium mb-1">Température Min (K)</label>
                <input 
                  type="number" 
                  placeholder="0" 
                  value={minTemp} 
                  onChange={(e) => setMinTemp(e.target.value)} 
                  className={`p-2 rounded border ${inputClass} w-full`} 
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-1">Température Max (K)</label>
                <input 
                  type="number" 
                  placeholder="1000" 
                  value={maxTemp} 
                  onChange={(e) => setMaxTemp(e.target.value)} 
                  className={`p-2 rounded border ${inputClass} w-full`} 
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-1">Distance Min (ly)</label>
                <input 
                  type="number" 
                  placeholder="0" 
                  value={minDistance} 
                  onChange={(e) => setMinDistance(e.target.value)} 
                  className={`p-2 rounded border ${inputClass} w-full`} 
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-1">Distance Max (ly)</label>
                <input 
                  type="number" 
                  placeholder="1000" 
                  value={maxDistance} 
                  onChange={(e) => setMaxDistance(e.target.value)} 
                  className={`p-2 rounded border ${inputClass} w-full`} 
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-1">Année découverte min</label>
                <input 
                  type="number" 
                  placeholder="1995" 
                  value={minYear} 
                  onChange={(e) => setMinYear(e.target.value)} 
                  className={`p-2 rounded border ${inputClass} w-full`} 
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-1">Année découverte max</label>
                <input 
                  type="number" 
                  placeholder="2023" 
                  value={maxYear} 
                  onChange={(e) => setMaxYear(e.target.value)} 
                  className={`p-2 rounded border ${inputClass} w-full`} 
                />
              </div>
            </div>

            <div className="flex justify-end mt-4 gap-2">
              <button 
                onClick={resetFilters}
                className="px-4 py-2 border rounded hover:bg-gray-100 dark:hover:bg-gray-700"
              >
                {t('common.reset')}
              </button>
              
              <button 
                onClick={() => setPage(0)} 
                className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
              >
                {t('common.apply')}
              </button>
            </div>
          </div>
        )}
      </div>

      <div className="flex justify-between items-center mb-4">
        <div>
          {!loading && !error && exoplanets.length > 0 && (
            <p className="text-sm text-gray-500 dark:text-gray-400">
              {`Affichage ${page * size + 1}-${Math.min((page + 1) * size, page * size + exoplanets.length)} sur ${totalPages * size} résultats`}
            </p>
          )}
        </div>
        
        <div className="flex items-center gap-2">
          <span className="text-sm text-gray-600 dark:text-gray-300">
            {t('Résultats par page')}:
          </span>
          <select 
            value={size} 
            onChange={(e) => { setSize(Number(e.target.value)); setPage(0); }} 
            className={`border p-1 rounded ${inputClass}`}
          >
            <option value={6}>6</option>
            <option value={9}>9</option>
            <option value={12}>12</option>
            <option value={24}>24</option>
          </select>
        </div>
      </div>

      {loading && (
        <div className="flex justify-center items-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
        </div>
      )}
      
      {error && (
        <div className="text-center py-8">
          <p className="text-red-500 text-lg mb-4">{error}</p>
          <button 
            onClick={() => setPage(0)} 
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
          >
            {t('common.retry')}
          </button>
        </div>
      )}

      {!loading && !error && exoplanets.length === 0 && (
        <div className="text-center py-12">
          <p className="text-gray-500 text-lg mb-2">{t('search.noResults')}</p>
          <p className="text-gray-400 text-sm">
            {t('Essayez de modifier vos critères de recherche')}
          </p>
        </div>
      )}

      <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
        {exoplanets.map((exo: any) => (
          <ExoplanetCard 
            key={exo.id} 
            exoplanet={exo} 
            isFavorite={favorites.includes(exo.id)}
            onToggleFavorite={handleToggleFavorite}
          />
        ))}
      </div>

      {totalPages > 1 && (
        <div className="flex justify-center mt-8 gap-2">
          <button
            disabled={page <= 0}
            onClick={() => setPage((p) => p - 1)}
            className="flex items-center px-3 py-2 border rounded-lg disabled:opacity-50 hover:bg-gray-100 dark:hover:bg-gray-700 disabled:hover:bg-transparent disabled:dark:hover:bg-transparent"
          >
            <ChevronLeft className="w-5 h-5 mr-1" />
            {t('Précédent')}
          </button>
          
          <div className="flex items-center px-4">
            <span>{t('Page')} {page + 1} / {totalPages}</span>
          </div>
          
          <button
            disabled={page >= totalPages - 1}
            onClick={() => setPage((p) => p + 1)}
            className="flex items-center px-3 py-2 border rounded-lg disabled:opacity-50 hover:bg-gray-100 dark:hover:bg-gray-700 disabled:hover:bg-transparent disabled:dark:hover:bg-transparent"
          >
            {t('Suivant')}
            <ChevronRight className="w-5 h-5 ml-1" />
          </button>
        </div>
      )}

      {/* Modal Message */}
      <ModalMessage
        show={modal.show}
        message={modal.message}
        type={modal.type}
        onClose={() => setModal({ ...modal, show: false })}
      />
    </div>
  );
};

export default SearchPage;