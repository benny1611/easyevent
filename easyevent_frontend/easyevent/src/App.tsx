import { Box, CssBaseline, Toolbar } from '@mui/material'
import './App.css'
import ResponsiveAppBar from './components/ResponsiveAppBar'
import Hero from './components/Hero'

function App() {
  return (
    <>
      <CssBaseline />
      <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <ResponsiveAppBar />
      <Toolbar />
      <Hero sx={{ flexGrow: 1 }} />
    </Box>
    </>
  )
}

export default App
