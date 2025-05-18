import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useTheme } from '../contexts/ThemeContext';
import { useAuth } from '../contexts/AuthContext';
import apiService from '../services/apiService';
import { generateExoplanetDescription } from '../utils/generateDescription';
import ModalMessage from '../components/ModalMessage';
import { Star, Globe, Thermometer, Ruler, Scale, Calendar, Zap } from 'lucide-react';

const ExoplanetDetail: React.FC = () => {
  const { id } = useParams();
  const { t, i18n } = useTranslation();
  const { theme } = useTheme();
  const { user } = useAuth();

  const [exoplanet, setExoplanet] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isFavorite, setIsFavorite] = useState(false);
  const [tabSelected, setTabSelected] = useState('overview');

  // Modal management
  const [modalMessage, setModalMessage] = useState<string | null>(null);
  const [modalType, setModalType] = useState<'success' | 'error'>('success');

  const showModal = (message: string, type: 'success' | 'error' = 'success') => {
    setModalMessage(message);
    setModalType(type);
  };

  useEffect(() => {
    if (!id) return;
    (async () => {
      try {
        const response = await apiService.getExoplanetWithDetails(Number(id));
        const exo = response.data;
        
        if (exo) {
          setExoplanet({ 
            ...exo, 
            imageUrl: exo.imageExo,
            potentiallyHabitable: exo.potentiallyHabitable || false,
            earthSizeComparison: exo.earthSizeComparison || '',
            earthMassComparison: exo.earthMassComparison || '',
            travelTimeYears: exo.travelTimeYears || null
          });
        }

        if (user?.email) {
          const favorites = await apiService.getFavorites(user.email);
          const found = favorites.data.find((f: any) => f.id === Number(id));
          setIsFavorite(!!found);
        }

        setLoading(false);
      } catch (err: any) {
        console.error("Error fetching exoplanet details :", err);
        setError(err.message);
        setModalMessage(t('common.error'));
        setModalType('error');
        setLoading(false);
      }
    })();
  }, [id, user?.email, t]);

  const handleToggleFavorite = async () => {
    if (!user) {
      showModal(t('common.loginRequired'), 'error');
      return;
    }
    try {
      await apiService.toggleFavorite({ email: user.email, exoplanetId: Number(id) });
      setIsFavorite(prev => !prev);
      showModal(
        isFavorite ? t('exoplanet.removedFromFavorites') : t('exoplanet.addedToFavorites'),
        'success'
      );
    } catch (err: any) {
      showModal(t('common.error'), 'error');
    }
  };

  if (loading) return <div className="min-h-screen flex items-center justify-center">{t('common.loading')}</div>;
  if (error) return <div className="text-red-500">{error}</div>;
  if (!exoplanet) return <div>{t('common.error')}</div>;

  const cardClass = theme === 'dark' ? 'bg-gray-800' : 'bg-white border border-gray-200';
  const tabClass = 'px-4 py-2 rounded-t-lg cursor-pointer transition-colors';
  const activeTabClass = theme === 'dark' 
    ? 'bg-gray-700 text-white font-medium' 
    : 'bg-white text-gray-900 border-b-2 border-blue-500 font-medium';
  const inactiveTabClass = theme === 'dark'
    ? 'bg-gray-800 text-gray-400 hover:bg-gray-700'
    : 'bg-gray-100 text-gray-600 hover:bg-gray-200';

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <div className="grid md:grid-cols-2 gap-8">
        <div>
          <img
            src={exoplanet.imageUrl || 'https://images.unsplash.com/photo-1614732414444-096e5f1122d5?auto=format&fit=crop&q=80'}
            alt={exoplanet.name}
            className="w-full h-[400px] object-cover rounded-lg shadow-lg"
          />

          {/* Habitability badge */}
          {exoplanet.potentiallyHabitable && (
            <div className="mt-4">
              <span className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-green-100 text-green-800 dark:bg-green-800 dark:text-green-100">
                <Globe className="w-4 h-4 mr-1" />
                {t('exoplanet.habitability')}
              </span>
            </div>
          )}
        </div>

        <div className={`${cardClass} p-6 rounded-lg shadow-lg`}>
          <h1 className="text-4xl font-bold mb-4">{exoplanet.name}</h1>
          
          <div className="space-y-3 mb-6">
            <div className="flex items-center gap-2">
              <Globe className="w-5 h-5 text-blue-500" />
              <span className="font-medium">{t('exoplanet.distance')}:</span> 
              <span>{exoplanet.distance} {t('exoplanet.lightYears')}</span>
            </div>
            
            <div className="flex items-center gap-2">
              <Scale className="w-5 h-5 text-blue-500" />
              <span className="font-medium">{t('exoplanet.mass')}:</span> 
              <span>{exoplanet.mass} {t('exoplanet.earthMasses')}</span>
              {exoplanet.earthMassComparison && (
                <span className="text-sm text-gray-500 dark:text-gray-400">
                  ({exoplanet.earthMassComparison})
                </span>
              )}
            </div>
            
            <div className="flex items-center gap-2">
              <Ruler className="w-5 h-5 text-blue-500" />
              <span className="font-medium">{t('exoplanet.radius')}:</span> 
              <span>{exoplanet.radius} {t('exoplanet.earthRadii')}</span>
              {exoplanet.earthSizeComparison && (
                <span className="text-sm text-gray-500 dark:text-gray-400">
                  ({exoplanet.earthSizeComparison})
                </span>
              )}
            </div>
            
            <div className="flex items-center gap-2">
              <Thermometer className="w-5 h-5 text-blue-500" />
              <span className="font-medium">{t('exoplanet.temperature')}:</span> 
              <span>{exoplanet.temperature} {t('exoplanet.kelvin')}</span>
            </div>
            
            <div className="flex items-center gap-2">
              <Calendar className="w-5 h-5 text-blue-500" />
              <span className="font-medium">{t('exoplanet.discoveryYear')}:</span> 
              <span>{exoplanet.yearDiscovered}</span>
            </div>

            {exoplanet.travelTimeYears && (
              <div className="flex items-center gap-2">
                <Zap className="w-5 h-5 text-blue-500" />
                <span className="font-medium">Travel Time:</span>
                <span>{exoplanet.travelTimeYears.toFixed(1)} years at light speed</span>
              </div>
            )}
          </div>

          <button
            onClick={handleToggleFavorite}
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded transition-colors"
          >
            <Star className={isFavorite ? "w-5 h-5 fill-current text-yellow-300" : "w-5 h-5"} />
            {isFavorite ? t('exoplanet.removeFromFavorites') : t('exoplanet.addToFavorites')}
          </button>
        </div>
      </div>

      {/* Tabs for different content sections */}
      <div className="mt-8">
        <div className="flex space-x-1 border-b border-gray-200 dark:border-gray-700">
          <button 
            className={`${tabClass} ${tabSelected === 'overview' ? activeTabClass : inactiveTabClass}`}
            onClick={() => setTabSelected('overview')}
          >
            Overview
          </button>
          <button 
            className={`${tabClass} ${tabSelected === 'habitability' ? activeTabClass : inactiveTabClass}`}
            onClick={() => setTabSelected('habitability')}
          >
            Habitability
          </button>
          <button 
            className={`${tabClass} ${tabSelected === 'orbit' ? activeTabClass : inactiveTabClass}`}
            onClick={() => setTabSelected('orbit')}
          >
            Orbit
          </button>
        </div>

        <div className={`${cardClass} p-6 rounded-b-lg rounded-tr-lg shadow-lg mt-1`}>
          {tabSelected === 'overview' && (
            <div>
              <h2 className="text-2xl font-bold mb-4">{t('exoplanet.descriptionTitle')}</h2>
              <p className={`${theme === 'dark' ? 'text-gray-300' : 'text-gray-700'}`}>
                {generateExoplanetDescription({
                  id: exoplanet.id,
                  name: exoplanet.name,
                  distance: exoplanet.distance,
                  radius: exoplanet.radius,
                  mass: exoplanet.mass,
                  temperature: exoplanet.temperature,
                  orbitalPeriodDays: exoplanet.orbitalPeriodDays,
                  orbitalPeriodYear: exoplanet.orbitalPeriodYear,
                  discoveryYear: exoplanet.yearDiscovered,
                  imageUrl: exoplanet.imageUrl
                }, i18n.language)}
              </p>
            </div>
          )}
          
          {tabSelected === 'habitability' && (
            <div>
              <h2 className="text-2xl font-bold mb-4">{t('exoplanet.habitability')}</h2>
              
              {exoplanet.potentiallyHabitable ? (
                <div className="space-y-6">
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div className={`p-4 rounded-lg ${theme === 'dark' ? 'bg-gray-700' : 'bg-blue-50'}`}>
                      <h3 className="font-bold text-lg mb-2 flex items-center gap-2">
                        <Thermometer className="text-blue-500" />
                        {t('exoplanet.temperature_info')}
                      </h3>
                      <p className={theme === 'dark' ? 'text-gray-300' : 'text-gray-700'}>
                        {t('exoplanet.temp_desc')}
                      </p>
                    </div>
                    
                    <div className={`p-4 rounded-lg ${theme === 'dark' ? 'bg-gray-700' : 'bg-blue-50'}`}>
                      <h3 className="font-bold text-lg mb-2 flex items-center gap-2">
                        <Globe className="text-blue-500" />
                        {t('exoplanet.atmosphere_info')}
                      </h3>
                      <p className={theme === 'dark' ? 'text-gray-300' : 'text-gray-700'}>
                        {t('exoplanet.atm_desc')}
                      </p>
                    </div>
                    
                    <div className={`p-4 rounded-lg ${theme === 'dark' ? 'bg-gray-700' : 'bg-blue-50'}`}>
                      <h3 className="font-bold text-lg mb-2 flex items-center gap-2">
                        <Ruler className="text-blue-500" />
                        {t('exoplanet.surface_info')}
                      </h3>
                      <p className={theme === 'dark' ? 'text-gray-300' : 'text-gray-700'}>
                        {t('exoplanet.surface_desc')}
                      </p>
                    </div>
                  </div>
                  
                  <div className="p-4 rounded-lg bg-green-50 dark:bg-green-900/30 border border-green-200 dark:border-green-800">
                    <p className="text-green-800 dark:text-green-200">
                      Cette exoplanète présente des caractéristiques compatibles avec la présence potentielle d'eau liquide à sa surface, 
                      un élément crucial pour l'habitabilité selon nos connaissances actuelles. Sa température estimée se situe dans la 
                      plage habitable, ce qui en fait un candidat intéressant pour de futures études.
                    </p>
                  </div>
                </div>
              ) : (
                <div className="p-4 rounded-lg bg-red-50 dark:bg-red-900/30 border border-red-200 dark:border-red-800">
                  <p className="text-red-800 dark:text-red-200">
                    Cette exoplanète n'est pas considérée comme potentiellement habitable selon nos critères actuels. 
                    Sa température de {exoplanet.temperature}K se situe {exoplanet.temperature < 180 ? 'en dessous' : 'au-dessus'} de 
                    la plage habitable (180K-310K) pour l'eau liquide.
                  </p>
                </div>
              )}
            </div>
          )}
          
          {tabSelected === 'orbit' && (
            <div>
              <h2 className="text-2xl font-bold mb-4">Caractéristiques orbitales</h2>
              
              <div className="space-y-4">
                {exoplanet.orbitalPeriodDays && (
                  <div className="flex items-center gap-2">
                    <span className="font-medium">Période orbitale:</span>
                    <span>{exoplanet.orbitalPeriodDays.toFixed(1)} jours</span>
                    {exoplanet.orbitalPeriodYear && (
                      <span className="text-gray-500">
                        ({exoplanet.orbitalPeriodYear.toFixed(2)} années terrestres)
                      </span>
                    )}
                  </div>
                )}
                
                {exoplanet.semiMajorAxis && (
                  <div className="flex items-center gap-2">
                    <span className="font-medium">Demi-grand axe:</span>
                    <span>{exoplanet.semiMajorAxis.toFixed(2)} UA</span>
                  </div>
                )}
                
                {exoplanet.eccentricity && (
                  <div className="flex items-center gap-2">
                    <span className="font-medium">Excentricité:</span>
                    <span>{exoplanet.eccentricity.toFixed(3)}</span>
                  </div>
                )}
                
                <div className="mt-6 p-4 rounded-lg bg-blue-50 dark:bg-blue-900/30 border border-blue-200 dark:border-blue-800">
                  <p className="text-blue-800 dark:text-blue-200">
                    {exoplanet.orbitalPeriodDays && exoplanet.orbitalPeriodDays < 100
                      ? `Cette exoplanète effectue une orbite complète de son étoile en seulement ${exoplanet.orbitalPeriodDays.toFixed(1)} jours, 
                        ce qui est beaucoup plus rapide que les 365 jours de la Terre.`
                      : exoplanet.orbitalPeriodDays && exoplanet.orbitalPeriodDays > 500
                        ? `L'année sur cette exoplanète dure ${exoplanet.orbitalPeriodDays.toFixed(1)} jours, soit bien plus longue qu'une année terrestre.`
                        : `La durée d'une année sur cette exoplanète est relativement similaire à celle de la Terre.`}
                  </p>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Message Modal */}
      <ModalMessage 
        show={!!modalMessage}
        message={modalMessage || ''}
        type={modalType}
        onClose={() => setModalMessage(null)}
      />
    </div>
  );
};

export default ExoplanetDetail;