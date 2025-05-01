import React from 'react';
import { useTranslation } from 'react-i18next';
import { Languages } from 'lucide-react';
import { useTheme } from '../contexts/ThemeContext';

const LanguageSelector: React.FC = () => {
  const { t, i18n } = useTranslation();
  const { theme } = useTheme();

  const toggleLanguage = () => {
    const newLang = i18n.language === 'en' ? 'fr' : 'en';
    i18n.changeLanguage(newLang);
    localStorage.setItem('language', newLang);
  };

  const buttonClass = theme === 'dark'
    ? 'text-gray-300 hover:text-white'
    : 'text-gray-700 hover:text-gray-900';

  return (
    <button
      onClick={toggleLanguage}
      className={`p-2 rounded-lg flex items-center gap-2 ${buttonClass}`}
      aria-label={t('language.select') || 'Select language'}
    >
      <Languages className="w-5 h-5" />
      <span className="text-sm font-medium">
        {i18n.language === 'en' ? t('language.en') : t('language.fr')}
      </span>
    </button>
  );
};

export default LanguageSelector;
