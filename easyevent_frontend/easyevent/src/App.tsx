import { Box, CssBaseline, Toolbar } from '@mui/material'
import ResponsiveAppBar from './components/ResponsiveAppBar'
import Footer from './components/Footer'
import Home from './pages/Home'
import LoginPage from './pages/LoginPage'
import { Route, Routes } from 'react-router-dom'
import RegisterPage from './pages/RegisterPage'
import OAuthCallback from './pages/OAuthCallback'

function App() {
  return (
    <>
      <CssBaseline />
      <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', flex: 1 }}>
        <ResponsiveAppBar />
        <Toolbar />
        <Routes>
          <Route path='/' element={<Home />} />
          <Route path='/login' element={<LoginPage />} />
          <Route path='/register' element={<RegisterPage />} />
          <Route path="/oauth2/callback" element={<OAuthCallback />} />
        </Routes>
        <Footer />
      </Box>
    </>
  )
}

export default App
