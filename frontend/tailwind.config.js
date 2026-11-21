/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#17264a',
        forest: '#365ee8',
        mint: '#e6ecff',
        paper: '#f7f8fc',
        line: '#e5e9f2',
        muted: '#75809a',
      },
      fontFamily: { sans: ['DM Sans', 'sans-serif'], display: ['Manrope', 'sans-serif'] },
      boxShadow: { soft: '0 12px 38px rgba(23, 38, 74, .08)' },
    },
  },
  plugins: [],
}

