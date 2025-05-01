import { useState, useEffect } from 'react';
import apiService from '../services/apiService';
import { Exoplanet } from '../types/exoplanet';

interface UseExoplanetsResult {
  exoplanets: Exoplanet[];
  totalPages: number;
  loading: boolean;
  error: string | null;
  page: number;
  size: number;
  setPage: React.Dispatch<React.SetStateAction<number>>;
  setSize: React.Dispatch<React.SetStateAction<number>>;
  fetchWithFilters: (filters: {
    name?: string;
    minTemp?: string;
    maxTemp?: string;
    minDistance?: string;
    maxDistance?: string;
    minYear?: string;
    maxYear?: string;
  }) => void;
}

export function useExoplanets(): UseExoplanetsResult {
  const [exoplanets, setExoplanets] = useState<Exoplanet[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);

  const fetchWithFilters = async (filters: {
    name?: string;
    minTemp?: string;
    maxTemp?: string;
    minDistance?: string;
    maxDistance?: string;
    minYear?: string;
    maxYear?: string;
  }) => {
    setLoading(true);
    try {
      const response = await apiService.getExoplanetSummaries({
        ...filters,
        page,
        size,
      });
      const content = response.data.content || [];
      const mapped: Exoplanet[] = content.map((exo: any) => ({
        id: exo.id,
        name: exo.name,
        imageUrl: exo.imageExo,
      }));
      setExoplanets(mapped);
      setTotalPages(response.data.totalPages || 0);
      setError(null);
    } catch (err: any) {
      setError(err.message || 'Erreur lors du chargement des exoplanètes');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWithFilters({});
  }, [page, size]);

  return {
    exoplanets,
    totalPages,
    loading,
    error,
    page,
    size,
    setPage,
    setSize,
    fetchWithFilters,
  };
}
