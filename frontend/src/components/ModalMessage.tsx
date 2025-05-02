import React, { useEffect } from 'react';
import { CheckCircle, XCircle, X } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useTheme } from '../contexts/ThemeContext';

interface ModalMessageProps {
  show: boolean;
  onClose: () => void;
  message: string;
  type?: 'success' | 'error';
}

const ModalMessage: React.FC<ModalMessageProps> = ({ 
  show, 
  onClose, 
  message, 
  type = 'success' 
}) => {
  const { t } = useTranslation();
  const { theme } = useTheme();

  useEffect(() => {
    if (show) {
      // Prevent background scrolling when modal is open
      document.body.style.overflow = 'hidden';
      return () => {
        document.body.style.overflow = 'unset';
      };
    }
  }, [show]);

  if (!show) return null;

  const bgColor = type === 'success' 
    ? 'bg-green-500 dark:bg-green-600' 
    : 'bg-red-500 dark:bg-red-600';

  const Icon = type === 'success' ? CheckCircle : XCircle;

  return (
    <div 
      className="fixed inset-0 flex items-center justify-center bg-black/50 dark:bg-black/70 z-50 animate-fadeIn"
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div 
        className={`w-full max-w-md rounded-lg shadow-xl ${theme === 'dark' ? 'dark' : ''} animate-slideIn`}
      >
        <div className={`${bgColor} p-6 rounded-lg text-white relative`}>
          <button
            onClick={onClose}
            className="absolute top-4 right-4 text-white/80 hover:text-white transition-colors"
            aria-label={t('common.close')}
          >
            <X className="w-5 h-5" />
          </button>

          <div className="flex items-start gap-4">
            <Icon className="w-6 h-6 mt-1" />
            <div className="flex-1">
              <h2 className="text-xl font-bold mb-2">
                {type === 'success' ? t('modal.success') : t('modal.error')}
              </h2>
              <p className="text-white/90">{message}</p>
            </div>
          </div>

          <div className="mt-6 flex justify-end">
            <button
              onClick={onClose}
              className="px-4 py-2 bg-white/10 hover:bg-white/20 text-white rounded-lg transition-colors duration-200"
            >
              {t('common.close')}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ModalMessage;