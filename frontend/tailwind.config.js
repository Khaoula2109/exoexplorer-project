/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      animation: {
        fadeIn: 'fadeIn 0.2s ease-out',
        slideIn: 'slideIn 0.3s ease-out',
        float: 'float 6s ease-in-out infinite',
        rocket: 'rocket 2s ease-in-out infinite',
        twinkle: 'twinkle 5s ease-in-out infinite',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideIn: {
          '0%': { 
            transform: 'translateY(-20px)',
            opacity: '0',
          },
          '100%': { 
            transform: 'translateY(0)',
            opacity: '1',
          },
        },
        float: {
          '0%, 100%': { transform: 'translateY(0)' },
          '50%': { transform: 'translateY(-20px)' },
        },
        rocket: {
          '0%': { transform: 'translateY(0) rotate(45deg)' },
          '50%': { transform: 'translateY(-10px) rotate(45deg)' },
          '100%': { transform: 'translateY(0) rotate(45deg)' },
        },
        twinkle: {
          '0%, 100%': { opacity: '0.5' },
          '50%': { opacity: '1' },
        },
      },
    },
  },
  plugins: [],
};