import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { UserPlus } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useTheme } from '../contexts/ThemeContext';
import { useAuth } from '../contexts/AuthContext';
import ModalMessage from '../components/ModalMessage';

const SignupPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [modal, setModal] = useState({ show: false, type: 'success', message: '' });

  const { t } = useTranslation();
  const { theme } = useTheme();
  const { signup } = useAuth();

  const showModal = (message: string, type: 'success' | 'error' = 'success') => {
    setModal({ show: true, type, message });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (password.length < 6) {
      showModal(t('auth.signup.passwordMinLength'), 'error');
      return;
    }
    if (password !== confirmPassword) {
      showModal(t('auth.signup.passwordMismatch'), 'error');
      return;
    }
    try {
      await signup(email, password);
      showModal(t('auth.signup.success'), 'success');
    } catch (error: any) {
      const backendMessage = typeof error?.response?.data === 'object' 
        ? error?.response?.data?.message 
        : String(error?.response?.data || t('common.error'));
      showModal(backendMessage, 'error');
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSubmit(e);
    }
  };

  const getPasswordStrength = (password: string) => {
    if (password.length === 0) return 0;
    if (password.length < 6) return 1;
    if (password.length < 8) return 2;
    return 3;
  };

  const passwordStrength = getPasswordStrength(password);
  const strengthColor = [
    'bg-gray-200 dark:bg-gray-700',
    'bg-red-500',
    'bg-yellow-500',
    'bg-green-500'
  ][passwordStrength];

  const formClass = theme === 'dark'
    ? 'bg-gray-800 text-white'
    : 'bg-white text-gray-900 border border-gray-200';

  const inputClass = theme === 'dark'
    ? 'bg-gray-700 border-gray-600 text-white focus:ring-blue-500'
    : 'bg-gray-50 border-gray-300 text-gray-900 focus:ring-blue-600';

  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center px-4">
      <div className="w-full max-w-md">
        <div className={`rounded-lg shadow-xl p-8 ${formClass}`}>
          <div className="flex items-center justify-center mb-8">
            <UserPlus className="w-12 h-12 text-blue-500" />
          </div>
          <h2 className="text-2xl font-bold text-center mb-8">{t('auth.signup.title')}</h2>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label htmlFor="email" className="block text-sm font-medium mb-2">
                {t('auth.signup.email')}
              </label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                onKeyDown={handleKeyDown}
                className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 ${inputClass}`}
                placeholder="you@example.com"
                required
              />
            </div>

            <div>
              <label htmlFor="password" className="block text-sm font-medium mb-2">
                {t('auth.signup.password')}
              </label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                onKeyDown={handleKeyDown}
                className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 ${inputClass}`}
                placeholder="••••••••"
                required
                minLength={6}
              />
              <div className="h-1 mt-2 rounded-full bg-gray-200 dark:bg-gray-700">
                <div
                  className={`h-full rounded-full transition-all duration-300 ${strengthColor}`}
                  style={{ width: `${(passwordStrength / 3) * 100}%` }}
                />
              </div>
              <p className="text-xs mt-1 text-gray-500 dark:text-gray-400">
                {t('auth.signup.passwordMinLength')}
              </p>
            </div>

            <div>
              <label htmlFor="confirmPassword" className="block text-sm font-medium mb-2">
                {t('auth.signup.confirmPassword')}
              </label>
              <input
                id="confirmPassword"
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                onKeyDown={handleKeyDown}
                className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 ${inputClass}`}
                placeholder="••••••••"
                required
              />
            </div>

            <button
              type="submit"
              className="w-full bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 transition duration-200"
            >
              {t('auth.signup.submit')}
            </button>
          </form>

          <p className={`mt-6 text-center ${theme === 'dark' ? 'text-gray-400' : 'text-gray-600'}`}>
            {t('auth.signup.haveAccount')}{' '}
            <Link to="/login" className="text-blue-500 hover:text-blue-400">
              {t('auth.signup.signIn')}
            </Link>
          </p>
        </div>
      </div>

      {/* Fenêtre modale */}
      <ModalMessage
        show={modal.show}
        onClose={() => setModal((prev) => ({ ...prev, show: false }))}
        message={modal.message}
        type={modal.type as 'success' | 'error'}
      />
    </div>
  );
};

export default SignupPage;
