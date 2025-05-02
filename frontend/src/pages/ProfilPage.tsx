import React, { useState, useEffect } from 'react';
import { User, Mail, Star, Shield, Lock, X, Download, Key, Moon, Languages } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useTheme } from '../contexts/ThemeContext';
import { useAuth } from '../contexts/AuthContext';
import apiService from '../services/apiService';
import ModalMessage from '../components/ModalMessage';

const ProfilePage: React.FC = () => {
  const { t, i18n } = useTranslation();
  const { theme, toggleTheme } = useTheme();
  const { user, generateBackupCodes } = useAuth();

  const [favoritesCount, setFavoritesCount] = useState<number>(0);
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [darkMode, setDarkMode] = useState(theme === 'dark');
  const [language, setLanguage] = useState(i18n.language);
  const [editable, setEditable] = useState(false);
  const [showChangePassword, setShowChangePassword] = useState(false);
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  
  const [showBackupCodes, setShowBackupCodes] = useState(false);
  const [backupCodes, setBackupCodes] = useState<string[]>([]);
  const [generatingCodes, setGeneratingCodes] = useState(false);

  // Modal state
  const [modalMessage, setModalMessage] = useState('');
  const [modalType, setModalType] = useState<'success' | 'error'>('success');
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    if (user?.email) {
      apiService.getFavorites(user.email)
        .then((res) => setFavoritesCount(res.data.length))
        .catch(() => {
          setModalMessage(t('common.error'));
          setModalType('error');
          setShowModal(true);
        });

      apiService.getProfile(user.email)
        .then((res) => {
          setFirstName(res.data.firstName || '');
          setLastName(res.data.lastName || '');
          // Mettre à jour les préférences utilisateur
          setDarkMode(res.data.darkMode);
          setLanguage(res.data.language || 'fr');
        })
        .catch(() => {
          setModalMessage(t('common.error'));
          setModalType('error');
          setShowModal(true);
        });
    }
  }, [user, t]);

  // Synchroniser les préférences avec le context
  useEffect(() => {
    if (darkMode !== (theme === 'dark')) {
      toggleTheme();
    }
    
    if (language !== i18n.language) {
      i18n.changeLanguage(language);
    }
  }, [darkMode, language, theme, toggleTheme, i18n]);

  const cardClass = theme === 'dark' ? 'bg-gray-800' : 'bg-white border border-gray-200';
  const inputClass = theme === 'dark'
    ? 'bg-gray-700 border-gray-600 text-white focus:ring-blue-500'
    : 'bg-gray-50 border-gray-300 text-gray-900 focus:ring-blue-600';

  const handleSaveProfile = async () => {
    if (!user?.email) return;
    try {
      await apiService.updateProfile({ email: user.email, firstName, lastName });
      
      // Enregistrer les préférences de l'utilisateur
      await apiService.updatePreferences({ 
        email: user.email, 
        darkMode, 
        language 
      });
      
      setModalMessage(t('profile.updateSuccess'));
      setModalType('success');
      setEditable(false);
    } catch (err) {
      setModalMessage(t('common.error'));
      setModalType('error');
    } finally {
      setShowModal(true);
    }
  };

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user?.email) return;
    
    if (newPassword !== confirmPassword) {
      setModalMessage(t('common.passwordMismatch'));
      setModalType('error');
      setShowModal(true);
      return;
    }
    
    try {
      await apiService.changePassword({
        email: user.email,
        currentPassword,
        newPassword,
      });
      setModalMessage(t('auth.changePassword.success'));
      setModalType('success');
      setShowChangePassword(false);
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (err) {
      setModalMessage(t('auth.changePassword.error'));
      setModalType('error');
    } finally {
      setShowModal(true);
    }
  };
  
  const handleGenerateBackupCodes = async () => {
    if (!user?.email) return;
    
    try {
      setGeneratingCodes(true);
      // Nombre fixe de codes (5)
      const codes = await generateBackupCodes(user.email, 5);
      setBackupCodes(codes);
      setShowBackupCodes(true);
    } catch (err) {
      setModalMessage(t('common.error'));
      setModalType('error');
      setShowModal(true);
    } finally {
      setGeneratingCodes(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto px-4">
      {/* Header */}
      <div className={`${cardClass} rounded-lg shadow-lg overflow-hidden`}>
        <div className="h-32 bg-gradient-to-r from-blue-500 to-purple-600"></div>
        <div className="p-6">
          <div className="flex flex-col md:flex-row items-center gap-6">
            <div className="relative -mt-20">
              <div className="w-32 h-32 rounded-full bg-gray-300 border-4 border-white dark:border-gray-800 overflow-hidden">
                <img 
                  src={`https://ui-avatars.com/api/?name=${firstName}+${lastName}&background=random&size=128`} 
                  alt="Profile" 
                  className="w-full h-full object-cover" 
                />
              </div>
            </div>
            <div className="text-center md:text-left">
              <h1 className="text-2xl font-bold">{user?.email || 'John Doe'}</h1>
              <p className="text-gray-500">Space Enthusiast</p>
            </div>
          </div>
        </div>
      </div>

      {/* Personal Info */}
      <div className={`${cardClass} rounded-lg shadow-lg p-6 mt-6`}>
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-semibold flex items-center gap-2">
            <User className="text-blue-500" />
            {t('profile.personalInformation')}
          </h2>
          {!editable && (
            <button onClick={() => setEditable(true)} className="text-blue-500 hover:underline">
              {t('common.edit') || 'Modifier'}
            </button>
          )}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
          <div className="relative">
            <label className="block text-sm font-medium mb-1">{t('profile.firstName')}</label>
            <input
              type="text"
              value={firstName}
              readOnly={!editable}
              onChange={(e) => setFirstName(e.target.value)}
              className={`w-full px-4 py-2 rounded-lg border ${inputClass} ${!editable ? 'pr-10' : ''}`}
            />
            {!editable && <Lock size={16} className="absolute right-3 top-9 text-gray-400" />}
          </div>

          <div className="flex items-center gap-2">
            <Mail className="text-gray-400" />
            <span>{user?.email}</span>
          </div>

          <div className="relative">
            <label className="block text-sm font-medium mb-1">{t('profile.lastName')}</label>
            <input
              type="text"
              value={lastName}
              readOnly={!editable}
              onChange={(e) => setLastName(e.target.value)}
              className={`w-full px-4 py-2 rounded-lg border ${inputClass} ${!editable ? 'pr-10' : ''}`}
            />
            {!editable && <Lock size={16} className="absolute right-3 top-9 text-gray-400" />}
          </div>

          <div className="flex items-center gap-2">
            <Star className="text-yellow-500" />
            <span>{favoritesCount} {t('profile.favoriteExoplanets')}</span>
          </div>
        </div>

        {editable && (
          <div className="mt-6">
            <h3 className="text-lg font-medium mb-3">Préférences</h3>
            
            <div className="space-y-4">
              {/* Dark Mode Toggle */}
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Moon className="text-blue-500" />
                  <span>{t('theme.toggle')}</span>
                </div>
                
                <label className="relative inline-flex items-center cursor-pointer">
                  <input 
                    type="checkbox" 
                    checked={darkMode} 
                    onChange={() => setDarkMode(!darkMode)}
                    className="sr-only peer" 
                  />
                  <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 dark:peer-focus:ring-blue-800 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-blue-600"></div>
                </label>
              </div>
              
              {/* Language Selector */}
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Languages className="text-blue-500" />
                  <span>{t('language.select')}</span>
                </div>
                
                <select
                  value={language}
                  onChange={(e) => setLanguage(e.target.value)}
                  className={`px-3 py-2 rounded-lg border ${inputClass}`}
                >
                  <option value="en">{t('language.en')}</option>
                  <option value="fr">{t('language.fr')}</option>
                </select>
              </div>
            </div>
          </div>
        )}

        {editable && (
          <div className="text-right mt-6">
            <button
              onClick={() => setEditable(false)}
              className="px-4 py-2 border rounded mr-2 hover:bg-gray-100 dark:hover:bg-gray-700"
            >
              {t('common.cancel')}
            </button>
            <button
              onClick={handleSaveProfile}
              className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
            >
              {t('common.save')}
            </button>
          </div>
        )}
      </div>

      {/* Security Section */}
      <div className={`${cardClass} p-6 rounded-lg shadow-lg mt-6`}>
        <h2 className="text-xl font-semibold mb-4 flex items-center gap-2">
          <Shield className="text-blue-500" />
          {t('Account Security')}
        </h2>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <button
            onClick={() => setShowChangePassword(true)}
            className="flex items-center justify-center gap-2 bg-blue-600 text-white py-2 px-4 rounded hover:bg-blue-700"
          >
            <Key className="w-5 h-5" />
            {t('auth.changePassword.title')}
          </button>
          
          <button
            onClick={handleGenerateBackupCodes}
            disabled={generatingCodes}
            className="flex items-center justify-center gap-2 bg-green-600 text-white py-2 px-4 rounded hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <Download className="w-5 h-5" />
            {generatingCodes ? 'Generating...' : 'Generate Backup Codes'}
          </button>
        </div>
      </div>

      {/* Password Modal */}
      {showChangePassword && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className={`${cardClass} rounded-lg shadow-xl max-w-md w-full p-6`}>
            <div className="flex justify-between items-center mb-6">
              <h3 className="text-xl font-bold">{t('auth.changePassword.title')}</h3>
              <button onClick={() => setShowChangePassword(false)} className="text-gray-500 hover:text-gray-700">
                <X className="w-6 h-6" />
              </button>
            </div>
            <form onSubmit={handleChangePassword} className="space-y-4">
              <div>
                <label className="block text-sm font-medium mb-2">{t('auth.changePassword.currentPassword')}</label>
                <input
                  type="password"
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  className={`w-full px-4 py-2 rounded-lg border ${inputClass}`}
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-2">{t('auth.changePassword.newPassword')}</label>
                <input
                  type="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  className={`w-full px-4 py-2 rounded-lg border ${inputClass}`}
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-2">{t('auth.changePassword.confirmPassword')}</label>
                <input
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className={`w-full px-4 py-2 rounded-lg border ${inputClass}`}
                  required
                />
              </div>
              <div className="flex gap-4 mt-6">
                <button 
                  type="button" 
                  onClick={() => setShowChangePassword(false)} 
                  className="flex-1 px-4 py-2 border rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700"
                >
                  {t('auth.changePassword.cancel')}
                </button>
                <button 
                  type="submit" 
                  className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                >
                  {t('auth.changePassword.submit')}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
      
      {/* Backup Codes Modal */}
      {showBackupCodes && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className={`${cardClass} rounded-lg shadow-xl max-w-md w-full p-6`}>
            <div className="flex justify-between items-center mb-6">
              <h3 className="text-xl font-bold">Backup Codes</h3>
              <button onClick={() => setShowBackupCodes(false)} className="text-gray-500 hover:text-gray-700">
                <X className="w-6 h-6" />
              </button>
            </div>
            
            <div className="mb-6">
              <p className="text-sm text-gray-500 dark:text-gray-400 mb-4">
                Save these backup codes in a secure place. Each code can be used once if you can't access your OTP authenticator.
              </p>
              
              <div className="grid grid-cols-2 gap-2">
                {backupCodes.map((code, index) => (
                  <div key={index} className="p-2 bg-gray-100 dark:bg-gray-700 rounded border border-gray-200 dark:border-gray-600 text-center font-mono">
                    {code}
                  </div>
                ))}
              </div>
            </div>
            
            <div className="flex justify-end">
              <button
                onClick={() => setShowBackupCodes(false)}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Global Modal */}
      <ModalMessage
        show={showModal}
        onClose={() => setShowModal(false)}
        message={modalMessage}
        type={modalType}
      />
    </div>
  );
};

export default ProfilePage;