import { Box, CssBaseline, Toolbar } from '@mui/material'
import ResponsiveAppBar from './components/ResponsiveAppBar'
import Footer from './components/Footer'
import Home from './components/Home/Home'

function App() {
  return (
    <>
      <CssBaseline />
      <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
        <ResponsiveAppBar />
        <Toolbar />
        <Home />
        <Footer />
      </Box>
    </>
  )
}

export default App
