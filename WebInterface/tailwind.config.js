/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        red: {
          50: '#fef2f2',
          100: '#fee2e2',
          200: '#fecaca',
          300: '#fca5a5',
          400: '#f87171',
          500: '#ef4444',
          600: '#dc2626',
          700: '#b91c1c',
          800: '#991b1b',
          900: '#7f1d1d',
          // Adjust the shade to end at #ff0322
          1000: '#ff0322'
        },
        yellow: {
          50: '#fffae6',
          100: '#fffae6',
          200: '#fff5cc',
          300: '#feefb3',
          400: '#feea99',
          500: '#fee580',
          600: '#fee066',
          700: '#fedb4d',
          800: '#fdd533',
          900: '#fdd01a',
          // Adjust the shade to end at #fdcb00
          1000: '#fdcb00'
        },
      },
    },
  },
  plugins: [],
}

