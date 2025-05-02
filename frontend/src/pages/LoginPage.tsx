import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

import { LogIn } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useTheme } from '../contexts/ThemeContext';
import { useAuth } from '../contexts/AuthContext';
import ModalMessage from '../components/ModalMessage'; // 🧩 Modale

const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const { t } = useTranslation();
  const { theme } = useTheme();
  const { login } = useAuth();
  const navigate = useNavigate();
  const [modal, setModal] = useState({
    show: false,
    message: '',
    type: 'success' as 'success' | 'error',
  });


  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await login(email, password);
      navigate('/verify-otp', { state: { email } });
    } catch (error) {
      setModal({
        show: true,
        message: t('auth.login.invalid'),
        type: 'error',
      });
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSubmit(e);
    }
  };

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
            <LogIn className="w-12 h-12 text-blue-500" />
          </div>
          <h2 className="text-2xl font-bold text-center mb-8">{t('auth.login.title')}</h2>
          
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label htmlFor="email" className="block text-sm font-medium mb-2">
                {t('auth.login.email')}
              </label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 ${inputClass}`}
                placeholder="you@example.com"
                required
              />
            </div>
            
            <div>
              <label htmlFor="password" className="block text-sm font-medium mb-2">
                {t('auth.login.password')}
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
              />
            </div>

            <button
              type="submit"
              className="w-full bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 transition duration-200"
            >
              {t('auth.login.submit')}
            </button>
          </form>

          <p className={`mt-6 text-center ${theme === 'dark' ? 'text-gray-400' : 'text-gray-600'}`}>
            {t('auth.login.noAccount')}{' '}
            <Link to="/signup" className="text-blue-500 hover:text-blue-400">
              {t('auth.login.signUp')}
            </Link>
          </p>
        </div>
      </div>
      {/* ✅ Modale message */}
      <ModalMessage
        show={modal.show}
        message={modal.message}
        type={modal.type}
        onClose={() => setModal({ ...modal, show: false })}
      />
    </div>
  );
};

export default LoginPage;

