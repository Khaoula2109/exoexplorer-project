import React from 'react';
import { useAuth } from '../contexts/AuthContext';
import HomePage from '../pages/HomePage';
import AdminPanel from '../pages/AdminPanel';

const HomeRouter: React.FC = () => {
  const { isAdmin } = useAuth();
  
  return isAdmin ? <AdminPanel /> : <HomePage />;
};

export default HomeRouter;