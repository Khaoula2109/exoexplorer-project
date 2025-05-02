import React, { useState } from 'react';
import { Shield, Database, RefreshCw, Trash2, Globe, AlertTriangle } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { useTheme } from '../contexts/ThemeContext';
import { useAuth } from '../contexts/AuthContext';
import apiService from '../services/apiService';
import ModalMessage from '../components/ModalMessage';

const AdminPanel: React.FC = () => {
  const { t } = useTranslation();
  const { theme } = useTheme();
  const { isAdmin } = useAuth();
  const navigate = useNavigate();
  
  const [loading, setLoading] = useState<Record<string, boolean>>({});
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [confirmAction, setConfirmAction] = useState<() => Promise<void>>(() => Promise.resolve());
  const [confirmMessage, setConfirmMessage] = useState('');
  
  // Modal state
  const [modal, setModal] = useState({
    show: false,
    message: '',
    type: 'success' as 'success' | 'error',
  });

  React.useEffect(() => {
    if (!isAdmin) {
      navigate('/');
    }
  }, [isAdmin, navigate]);

  const cardClass = theme === 'dark' ? 'bg-gray-800' : 'bg-white border border-gray-200';
  const buttonBaseClass = "flex items-center justify-center gap-2 px-4 py-3 rounded-lg font-medium transition-colors";
  const buttonPrimaryClass = `${buttonBaseClass} bg-blue-600 hover:bg-blue-700 text-white`;
  const buttonDangerClass = `${buttonBaseClass} bg-red-600 hover:bg-red-700 text-white`;
  const buttonSuccessClass = `${buttonBaseClass} bg-green-600 hover:bg-green-700 text-white`;
  
  const handleAction = async (
    key: string, 
    action: () => Promise<any>, 
    successMessage: string,
    requireConfirm = false,
    confirmMsg = ''
  ) => {
    if (requireConfirm) {
      setConfirmAction(() => async () => {
        try {
          setLoading(prev => ({ ...prev, [key]: true }));
          await action();
          setModal({
            show: true,
            message: successMessage,
            type: 'success',
          });
        } catch (error) {
          setModal({
            show: true,
            message: t('common.error'),
            type: 'error',
          });
        } finally {
          setLoading(prev => ({ ...prev, [key]: false }));
          setShowConfirmModal(false);
        }
      });
      setConfirmMessage(confirmMsg);
      setShowConfirmModal(true);
      return;
    }
    
    try {
      setLoading(prev => ({ ...prev, [key]: true }));
      await action();
      setModal({
        show: true,
        message: successMessage,
        type: 'success',
      });
    } catch (error) {
      setModal({
        show: true,
        message: t('common.error'),
        type: 'error',
      });
    } finally {
      setLoading(prev => ({ ...prev, [key]: false }));
    }
  };

  if (!isAdmin) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-8">
        <div className={`${cardClass} p-6 rounded-lg text-center`}>
          <h2 className="text-xl font-bold mb-4">Accès non autorisé</h2>
          <p>Vous devez être administrateur pour accéder à cette page.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="flex items-center mb-6 gap-3">
        <Shield className="w-8 h-8 text-blue-500" />
        <h1 className="text-3xl font-bold">Panneau d'administration</h1>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
        {/* Carte de gestion des données */}
        <div className={`${cardClass} p-6 rounded-lg shadow-lg`}>
          <h2 className="text-xl font-semibold mb-4 flex items-center gap-2">
            <Database className="text-blue-500" />
            Gestion des données
          </h2>
          
          <div className="space-y-4">
            <button
              onClick={() => handleAction(
                'refresh',
                () => apiService.refreshExoplanets(),
                'Données des exoplanètes mises à jour avec succès'
              )}
              disabled={loading['refresh']}
              className={buttonPrimaryClass}
            >
              <RefreshCw className={`w-5 h-5 ${loading['refresh'] ? 'animate-spin' : ''}`} />
              {loading['refresh'] ? 'Actualisation en cours...' : 'Rafraîchir les données des exoplanètes'}
            </button>
            
            <button
              onClick={() => handleAction(
                'insert500',
                () => apiService.insert500Exoplanets(),
                '500 exoplanètes test insérées avec succès'
              )}
              disabled={loading['insert500']}
              className={buttonSuccessClass}
            >
              <Database className="w-5 h-5" />
              {loading['insert500'] ? 'Insertion en cours...' : 'Insérer 500 exoplanètes test'}
            </button>
            
            <button
              onClick={() => handleAction(
                'insertHabitable',
                () => apiService.insertHabitableExoplanets(),
                'Exoplanètes habitables insérées avec succès'
              )}
              disabled={loading['insertHabitable']}
              className={buttonSuccessClass}
            >
              <Globe className="w-5 h-5" />
              {loading['insertHabitable'] ? 'Insertion en cours...' : 'Insérer des exoplanètes habitables'}
            </button>
            
            <button
              onClick={() => handleAction(
                'clearExoplanets',
                () => apiService.clearExoplanets(),
                'Toutes les exoplanètes ont été supprimées',
                true,
                'Êtes-vous sûr de vouloir supprimer toutes les exoplanètes ? Cette action est irréversible.'
              )}
              disabled={loading['clearExoplanets']}
              className={buttonDangerClass}
            >
              <Trash2 className="w-5 h-5" />
              {loading['clearExoplanets'] ? 'Suppression en cours...' : 'Supprimer toutes les exoplanètes'}
            </button>
          </div>
        </div>
        
        {/* Carte d'environnement de test */}
        <div className={`${cardClass} p-6 rounded-lg shadow-lg`}>
          <h2 className="text-xl font-semibold mb-4 flex items-center gap-2">
            <AlertTriangle className="text-amber-500" />
            Environnement de test
          </h2>
          
          <div className="space-y-4">
            <button
              onClick={() => handleAction(
                'resetDb',
                () => apiService.resetDb(),
                'Base de données réinitialisée avec une exoplanète test',
                true,
                'Êtes-vous sûr de vouloir réinitialiser la base de données ? Cette action est irréversible.'
              )}
              disabled={loading['resetDb']}
              className={buttonDangerClass}
            >
              <Database className="w-5 h-5" />
              {loading['resetDb'] ? 'Réinitialisation en cours...' : 'Réinitialiser BDD + ajouter exoplanète test'}
            </button>
            
            <button
              onClick={() => handleAction(
                'resetAll',
                () => apiService.resetAll(),
                'BDD complètement réinitialisée',
                true,
                'Êtes-vous sûr de vouloir réinitialiser complètement la base de données, y compris les utilisateurs ? Cette action est irréversible.'
              )}
              disabled={loading['resetAll']}
              className={buttonDangerClass}
            >
              <Trash2 className="w-5 h-5" />
              {loading['resetAll'] ? 'Réinitialisation en cours...' : 'Réinitialiser complètement la BDD'}
            </button>
          </div>
          
          <div className="mt-6 p-4 bg-amber-50 dark:bg-amber-900/30 border border-amber-200 dark:border-amber-800 rounded-lg">
            <p className="text-amber-700 dark:text-amber-200 text-sm">
              Attention : Les actions de cette section sont destinées uniquement à l'environnement de test.
              Ne les utilisez pas en production car elles peuvent causer des pertes de données irréversibles.
            </p>
          </div>
        </div>
      </div>
      
      {/* Modal de confirmation */}
      {showConfirmModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className={`${cardClass} rounded-lg shadow-xl max-w-md w-full p-6`}>
            <h3 className="text-xl font-bold mb-4">Confirmation requise</h3>
            <p className="mb-6">{confirmMessage}</p>
            
            <div className="flex justify-end gap-4">
              <button 
                onClick={() => setShowConfirmModal(false)}
                className="px-4 py-2 border rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700"
              >
                Annuler
              </button>
              <button
                onClick={() => confirmAction()}
                className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
              >
                Confirmer
              </button>
            </div>
          </div>
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

export default AdminPanel;