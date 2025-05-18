import React, { createContext, useContext, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService from '../services/apiService';

interface User {
  id: string;
  email: string;
  firstName?: string;
  lastName?: string;
  isAdmin?: boolean;
}

interface AuthContextType {
  user: User | null;
  login: (email: string, password: string) => Promise<void>;
  signup: (email: string, password: string) => Promise<void>;
  verifyOtp: (email: string, otp: string) => Promise<void>;
  verifyBackupCode: (email: string, backupCode: string) => Promise<void>;
  generateBackupCodes: (email: string, count?: number) => Promise<string[]>;
  logout: () => void;
  isAuthenticated: boolean;
  isAdmin: boolean;
  setUser: React.Dispatch<React.SetStateAction<User | null>>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const savedUser = localStorage.getItem('user');
    return savedUser ? JSON.parse(savedUser) : null;
  });
  
  const navigate = useNavigate();

  useEffect(() => {
    if (user) localStorage.setItem('user', JSON.stringify(user));
    else localStorage.removeItem('user');
  }, [user]);

  const login = async (email: string, password: string) => {
    try {
      await apiService.login({ email, password });
      // Redirect to the verify-otp page
      navigate('/verify-otp', { state: { email } });
    } catch (error) {
      console.error('Erreur lors de la connexion:', error);
      throw error;
    }
  };

  const signup = async (email: string, password: string) => {
    try {
      await apiService.signup({ email, password });
      navigate('/verify-otp', { state: { email } });
    } catch (error) {
      console.error('Erreur lors de l\'inscription:', error);
      throw error;
    }
  };
  
  const verifyOtp = async (email: string, otp: string) => {
    try {
      const response = await apiService.verifyOtp({ email, otp });
      const token = response.data.token;
      const isAdmin = response.data.isAdmin || false;

      if (token) {
        localStorage.setItem('token', token);
        
        // Profile information
        const profileResponse = await apiService.getProfile(email);
        const profile = profileResponse.data;
        
        setUser({
          id: email, // Used email as default ID
          email,
          firstName: profile.firstName || '',
          lastName: profile.lastName || '',
          isAdmin
        });
        
        navigate('/');
        return;
      }
      throw new Error('Token non reçu');
    } catch (error) {
      console.error('Erreur lors de la vérification OTP:', error);
      throw error;
    }
  };
  
  const verifyBackupCode = async (email: string, backupCode: string) => {
    try {
      const response = await apiService.verifyBackupCode({ email, backupCode });
      const token = response.data.token;
      
      if (token) {
        localStorage.setItem('token', token);
        
        // Profile information
        const profileResponse = await apiService.getProfile(email);
        const profile = profileResponse.data;
        
        setUser({
          id: email,
          email,
          firstName: profile.firstName || '',
          lastName: profile.lastName || '',
          isAdmin: profile.isAdmin || false
        });
        
        navigate('/');
        return;
      }
      throw new Error('Token non reçu');
    } catch (error) {
      console.error('Erreur lors de la vérification du code de secours:', error);
      throw error;
    }
  };
  
  const generateBackupCodes = async (email: string, count: number = 5): Promise<string[]> => {
    try {
      const response = await apiService.generateBackupCodes(email, count);
      return response.data;
    } catch (error) {
      console.error('Erreur lors de la génération des codes de secours:', error);
      throw error;
    }
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('token');
    navigate('/');
  };

  return (
    <AuthContext.Provider value={{
      user,
      login,
      signup,
      verifyOtp,
      verifyBackupCode,
      generateBackupCodes,
      logout,
      isAuthenticated: !!user,
      isAdmin: user?.isAdmin || false,
      setUser,
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) throw new Error('useAuth doit être utilisé dans un AuthProvider');
  return context;
};