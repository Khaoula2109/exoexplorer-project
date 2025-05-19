import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://api.exoexplorer.local/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor to add the JWT in the Authorization header
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Queries
interface LoginRequest {
  email: string;
  password: string;
}

interface SignupRequest {
  email: string;
  password: string;
}

interface OtpVerificationRequest {
  email: string;
  otp: string;
}

interface BackupCodeVerificationRequest {
  email: string;
  backupCode: string;
}

interface ToggleFavoriteRequest {
  email: string;
  exoplanetId: number;
}

interface ProfileUpdateRequest {
  email: string;
  firstName: string;
  lastName: string;
}

interface PasswordChangeRequest {
  email: string;
  currentPassword: string;
  newPassword: string;
}

interface UserPreferencesRequest {
  email: string;
  darkMode?: boolean;
  language?: string;
}

// API Services
export default {
  // ===== AUTHENTICATION SERVICES =====
  signup: (data: SignupRequest) =>
    apiClient.post('/auth/signup', data),

  login: (data: LoginRequest) =>
    apiClient.post('/auth/login', data),

  verifyOtp: (data: OtpVerificationRequest) =>
    apiClient.post('/auth/verify-otp', data),

  generateBackupCodes: (email: string, count: number = 5) =>
    apiClient.post('/auth/generate-backup-codes', { email, count }),

  verifyBackupCode: (data: BackupCodeVerificationRequest) =>
    apiClient.post('/auth/verify-backup-code', data),

  // ===== EXOPLANET SERVICES =====
  getExoplanets: () => 
    apiClient.get('/exoplanets'),

  getExoplanetSummaries: (params: {
    name?: string;
    minTemp?: string | number;
    maxTemp?: string | number;
    minDistance?: string | number;
    maxDistance?: string | number;
    minYear?: string | number;
    maxYear?: string | number;
    page?: number;
    size?: number;
  }) =>
    apiClient.get('/exoplanets/summary', { params }),

  getExoplanetById: (id: number) => 
    apiClient.get(`/exoplanets/${id}`),
    
  getExoplanetWithDetails: (id: number) => 
    apiClient.get(`/exoplanets/${id}/details`),

  getHabitableExoplanets: () => 
    apiClient.get('/exoplanets/habitable'),

  createExoplanet: (exoplanet: any) => 
    apiClient.post('/exoplanets', exoplanet),

  updateExoplanet: (id: number, exoplanet: any) => 
    apiClient.put(`/exoplanets/${id}`, exoplanet),

  deleteExoplanet: (id: number) => 
    apiClient.delete(`/exoplanets/${id}`),

  refreshExoplanets: () => 
    apiClient.post('/exoplanets/refresh'),

  // ===== USER SERVICES =====
  getFavorites: (email: string) => 
    apiClient.get(`/user/favorites?email=${email}`),

  toggleFavorite: (data: ToggleFavoriteRequest) =>
    apiClient.post('/user/toggle-favorite', data),

  getProfile: (email: string) =>
    apiClient.get('/user/profile', { params: { email } }),

  updateProfile: (data: ProfileUpdateRequest) =>
    apiClient.put('/user/update-profile', data),

  changePassword: (data: PasswordChangeRequest) => 
    apiClient.post('/user/change-password', data),
    
  updatePreferences: (data: UserPreferencesRequest) => 
    apiClient.put('/user/preferences', data),

  getBackupCodes: (email: string) => 
    apiClient.get('/user/backup-codes', { params: { email } }),

  // ===== ADMIN SERVICES =====
  insert500Exoplanets: () => 
    apiClient.post('/admin/data-loader/insert-500-exoplanets'),

  clearExoplanets: () => 
    apiClient.delete('/admin/data-loader/clear-exoplanets'),

  insertHabitableExoplanets: () => 
    apiClient.post('/admin/data-loader/insert-habitable-exoplanets'),

  // ===== TEST SERVICES =====
  resetUser: (email: string) => 
    apiClient.delete('/test/reset-user', { params: { email } }),

  resetDb: () => 
    apiClient.delete('/test/reset-db'),

  resetAll: () => 
    apiClient.delete('/test/reset-all'),
};