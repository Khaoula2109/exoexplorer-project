import { Exoplanet } from '../types/exoplanet';

export function generateExoplanetDescription(exo: Exoplanet, language: string): string {
  const radius = exo.radius !== null && exo.radius !== undefined ? `${exo.radius} R⊕` : (language === 'fr' ? 'inconnue' : 'unknown');
  const mass = exo.mass !== null && exo.mass !== undefined ? `${exo.mass} M⊕` : (language === 'fr' ? 'inconnue' : 'unknown');
  const distance = exo.distance !== null && exo.distance !== undefined ? `${exo.distance} ${language === 'fr' ? 'années-lumière' : 'light-years'}` : (language === 'fr' ? 'inconnue' : 'unknown');
  const temp = exo.temperature !== null && exo.temperature !== undefined ? `${exo.temperature} K` : (language === 'fr' ? 'inconnue' : 'unknown');
  const orbitDays = exo.orbitalPeriodDays ? `${exo.orbitalPeriodDays} ${language === 'fr' ? 'jours' : 'days'}` : '';
  const orbitYears = exo.orbitalPeriodYear ? ` (${language === 'fr' ? 'soit' : 'or'} ${exo.orbitalPeriodYear.toFixed(2)} ${language === 'fr' ? 'ans' : 'years'})` : '';

  if (language === 'fr') {
    return `L'exoplanète ${exo.name} est située à environ ${distance}. Elle possède un rayon de ${radius}, une masse estimée à ${mass}, et une température moyenne de ${temp}.${orbitDays ? ' Son orbite dure environ ' + orbitDays + orbitYears + '.' : ''}`;
  } else {
    return `Exoplanet ${exo.name} is located approximately ${distance}. It has a radius of ${radius}, an estimated mass of ${mass}, and an average temperature of ${temp}.${orbitDays ? ' Its orbit lasts about ' + orbitDays + orbitYears + '.' : ''}`;
  }
}
