export interface Exoplanet {
  id: number;
  name: string;
  distance: number;
  mass: number;
  radius: number;
  temperature: number;
  orbitalPeriodYear: number;
  orbitalPeriodDays: number;
  discoveryYear: number;
  imageUrl?: string;
}

export interface User {
  id: string;
  email: string;
  favorites: string[];
}
