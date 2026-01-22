import { Box, CssBaseline, Toolbar } from '@mui/material'
import ResponsiveAppBar from './components/ResponsiveAppBar'
import Footer from './components/Footer'
import Home from './pages/Home'
import LoginPage from './pages/LoginPage'
import { Route, Routes } from 'react-router-dom'

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
        </Routes>
        <Footer />
      </Box>
    </>
  )
}

export default App
