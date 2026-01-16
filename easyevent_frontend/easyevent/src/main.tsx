import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.tsx'
import { I18nProvider } from './i18n/i18nContext.tsx'
import { CssBaseline, ThemeProvider } from '@mui/material'
import { theme } from './themes/navbar_theme.ts'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ThemeProvider theme={theme}>
      <I18nProvider>
        <CssBaseline />
        <App />
      </I18nProvider>
    </ThemeProvider>
  </StrictMode>,
)
