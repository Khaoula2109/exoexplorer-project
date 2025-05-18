import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useTheme } from '../contexts/ThemeContext';
import { useAuth } from '../contexts/AuthContext';
import ModalMessage from '../components/ModalMessage';
import { KeyRound, Shield, ArrowRight } from 'lucide-react';

const OtpVerificationPage: React.FC = () => {
  const [otp, setOtp] = useState('');
  const [backupCode, setBackupCode] = useState('');
  const [useBackupCode, setUseBackupCode] = useState(false);
  const [inputFocused, setInputFocused] = useState(false);
  
  const { t } = useTranslation();
  const { theme } = useTheme();
  const navigate = useNavigate();
  const location = useLocation();
  const email = (location.state as any)?.email;
  const { verifyOtp, verifyBackupCode } = useAuth();

  const [modal, setModal] = useState({
    show: false,
    message: '',
    type: 'success' as 'success' | 'error',
  });

  const cardClass = theme === 'dark'
    ? 'bg-gray-800 text-white'
    : 'bg-white text-gray-900 border border-gray-200';

  const inputClass = theme === 'dark'
    ? 'bg-gray-700 border-gray-600 text-white focus:ring-blue-500'
    : 'bg-gray-50 border-gray-300 text-gray-900 focus:ring-blue-600';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!email) {
      setModal({
        show: true,
        message: t('common.error'),
        type: 'error',
      });
      return;
    }

    try {
      if (useBackupCode) {
        await verifyBackupCode(email, backupCode);
      } else {
        await verifyOtp(email, otp);
      }
    } catch (error) {
      setModal({
        show: true,
        message: useBackupCode 
          ? t('Backup code verification failed') 
          : t('auth.verifyOtp.error'),
        type: 'error',
      });
    }
  };

  const toggleUseBackupCode = () => {
    setUseBackupCode(prev => !prev);
    setOtp('');
    setBackupCode('');
  };

  if (!email) {
    navigate('/login');
    return null;
  }

  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center px-4">
      <div className="w-full max-w-md">
        <div className={`rounded-lg shadow-xl p-8 ${cardClass}`}>
          <div className="flex items-center justify-center mb-8">
            {useBackupCode ? (
              <Shield className="w-12 h-12 text-blue-500" />
            ) : (
              <KeyRound className="w-12 h-12 text-blue-500" />
            )}
          </div>
          
          <h2 className="text-2xl font-bold text-center mb-8">
            {useBackupCode ? t('Use Backup Code') : t('auth.verifyOtp.title')}
          </h2>
          
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label className="block text-sm font-medium mb-2">
                {useBackupCode ? t('Backup Code') : t('OTP Code')}
              </label>
              
              {useBackupCode ? (
                <input
                  type="text"
                  value={backupCode}
                  onChange={(e) => setBackupCode(e.target.value)}
                  onFocus={() => setInputFocused(true)}
                  onBlur={() => setInputFocused(false)}
                  className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 ${inputClass} ${inputFocused ? 'border-blue-500' : ''}`}
                  placeholder="XXXXXXXX"
                  required
                />
              ) : (
                <input
                  type="text"
                  value={otp}
                  onChange={(e) => setOtp(e.target.value)}
                  onFocus={() => setInputFocused(true)}
                  onBlur={() => setInputFocused(false)}
                  className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 ${inputClass} ${inputFocused ? 'border-blue-500' : ''}`}
                  placeholder="123456"
                  maxLength={6}
                  required
                />
              )}
              
              <p className="mt-2 text-sm text-gray-500 dark:text-gray-400">
                {useBackupCode
                  ? t('Enter the backup code you received during setup')
                  : t('Enter the 6-digit OTP code sent to your email')}
              </p>
            </div>

            <button
              type="submit"
              className="w-full bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 transition duration-200 flex items-center justify-center gap-2"
            >
              {t('auth.verifyOtp.submit')}
              <ArrowRight className="w-4 h-4" />
            </button>
          </form>
          
          <div className="mt-6 text-center">
            <button 
              onClick={toggleUseBackupCode}
              className="text-blue-500 hover:text-blue-400 text-sm font-medium"
            >
              {useBackupCode
                ? t('Return to OTP verification')
                : t('Use a backup code instead')}
            </button>
          </div>
        </div>
      </div>

      {/* Message Modal */}
      <ModalMessage
        show={modal.show}
        message={modal.message}
        type={modal.type}
        onClose={() => setModal({ ...modal, show: false })}
      />
    </div>
  );
};

export default OtpVerificationPage;