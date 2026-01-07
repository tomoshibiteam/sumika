package com.sumika.ui.theme

import androidx.compose.ui.graphics.Color

// ========================================
// PREMIUM COLOR PALETTE
// ========================================

// Primary: Deep Purple to Fuchsia gradient
val Primary = Color(0xFF7C3AED)
val PrimaryLight = Color(0xFF9F67FF)
val PrimaryDark = Color(0xFF6D28D9)
val OnPrimary = Color.White

// Secondary: Pink to Orange gradient
val Secondary = Color(0xFFEC4899)
val SecondaryLight = Color(0xFFF472B6)
val SecondaryDark = Color(0xFFDB2777)
val OnSecondary = Color.White

// Accent: Cyan
val Accent = Color(0xFF06B6D4)
val AccentLight = Color(0xFF22D3EE)
val AccentDark = Color(0xFF0891B2)
val OnAccent = Color.White

// Background & Surface (Light Mode)
val BackgroundLight = Color(0xFFFAFAF9)
val SurfaceLight = Color.White
val SurfaceVariantLight = Color(0xFFF5F5F4)
val OnBackgroundLight = Color(0xFF27272A)
val OnSurfaceLight = Color(0xFF27272A)
val OnSurfaceVariantLight = Color(0xFF52525B)

// Background & Surface (Dark Mode)
val BackgroundDark = Color(0xFF18181B)
val SurfaceDark = Color(0xFF27272A)
val SurfaceVariantDark = Color(0xFF3F3F46)
val OnBackgroundDark = Color(0xFFFAFAFA)
val OnSurfaceDark = Color(0xFFFAFAFA)
val OnSurfaceVariantDark = Color(0xFFA1A1AA)

// Semantic Colors
val Success = Color(0xFF10B981)
val SuccessLight = Color(0xFF34D399)
val SuccessDark = Color(0xFF059669)

val Warning = Color(0xFFF59E0B)
val WarningLight = Color(0xFFFBBF24)
val WarningDark = Color(0xFFD97706)

val Error = Color(0xFFEF4444)
val ErrorLight = Color(0xFFF87171)
val ErrorDark = Color(0xFFDC2626)

// Gradient Colors
val GradientStart = Color(0xFF7C3AED)
val GradientMiddle = Color(0xFFEC4899)
val GradientEnd = Color(0xFFF97316)

val GradientStartLight = Color(0xFF9F67FF)
val GradientEndLight = Color(0xFFFCA5A5)

// Glass Morphism
val GlassSurface = Color(0x1AFFFFFF) // 10% white
val GlassHighlight = Color(0x33FFFFFF) // 20% white
val GlassBorder = Color(0x2EFFFFFF) // 18% white
val GlassShadow = Color(0x40000000) // 25% black

// Overlay
val Overlay = Color(0x80000000) // 50% black
val OverlayLight = Color(0x40000000) // 25% black

// Special Effects
val Shimmer = Color(0x80FFFFFF) // 50% white for shimmer effect
val Glow = Color(0xFFFFD700) // Gold glow

// Legacy compatibility
val GlassSurfaceDark = GlassSurface
val GradientAccent = Accent
val Heart = Error
