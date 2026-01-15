import { Box, Button, Container, Stack, SvgIcon, Typography } from '@mui/material'
import { useI18n } from '../i18n/i18nContext';
import LogoIcon from '../assets/react.svg?react'

interface HeroProps {
  sx?: React.ComponentProps<typeof Box>['sx'];
}

export default function Hero({ sx }: HeroProps) {
    const {translation} = useI18n();
  return (
    <Box
      component="section"
      sx={{
        width: '100%',
        display: 'flex',
        alignItems: 'center',
        textAlign: 'center',
        background: (theme) =>
            theme.palette.mode === 'dark'
            ? 'linear-gradient(180deg, rgba(0,0,0,0.7), rgba(0,0,0,0.9))'
            : 'linear-gradient(180deg, #f5f7fa, #e4ebf5)',
        ...sx,
      }}
    >
      <Container maxWidth="md">
        <SvgIcon
          component={LogoIcon}
          inheritViewBox
          sx={{
            fontSize: 96,
            color: 'primary.main',
            mb: 2,
          }}
        />
        <Stack spacing={4}>
          {/* App name */}
          <Typography
            variant="h2"
            fontWeight={700}
            letterSpacing="-0.02em"
          >
            {translation.appName}
          </Typography>

          {/* Value proposition */}
          <Typography
            variant="h5"
            color="text.secondary"
          >
            {translation.hero.mantra}
          </Typography>

          {/* Actions */}
          <Stack
            direction={{ xs: 'column', sm: 'row' }}
            spacing={2}
            justifyContent="center"
          >
            <Button
              variant="contained"
              size="large"
            >
              {translation.hero.create}
            </Button>

            <Button
              variant="outlined"
              size="large"
            >
              {translation.hero.join}
            </Button>
          </Stack>
        </Stack>
      </Container>
    </Box>
  )
}
