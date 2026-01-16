import { Box, CssBaseline, Toolbar } from '@mui/material'
import ResponsiveAppBar from './components/ResponsiveAppBar'
import Hero from './components/Hero'
import HowItWorks from './components/HowItWorks'

function App() {
  return (
    <>
      <CssBaseline />
      <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
        <ResponsiveAppBar />
        <Toolbar />
        <Hero sx={{ flexGrow: 1 }} />
        <HowItWorks />
      </Box>
    </>
  )
}

export default App
