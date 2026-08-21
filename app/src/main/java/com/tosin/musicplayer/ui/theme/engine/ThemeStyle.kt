package com.tosin.musicplayer.ui.theme.engine

/**
 * High-level design paradigms supported by the Universal Theme Engine.
 */
enum class ThemeStyle(
    val displayName: String,
    val description: String
) {
    MATERIAL_EXPRESSIVE(
        displayName = "Material Expressive",
        description = "Modern Google Material 3 with expressive curves, tonal surfaces, and dynamic color."
    ),
    CYBERPUNK(
        displayName = "Cyberpunk Neon",
        description = "High-contrast dark synthwave aesthetic with multi-layer neon glows, scanlines, and sharp accents."
    ),
    NEUMORPHISM(
        displayName = "Neumorphism",
        description = "Soft UI aesthetic utilizing dual directional light and shadow projections for extruded tactile surfaces."
    ),
    CLAYMORPHISM(
        displayName = "Claymorphism",
        description = "Playful, inflated 3D clay surfaces with pillowy drop shadows and inner directional rim lighting."
    ),
    RETRO_MONO(
        displayName = "Retro Mono",
        description = "Brutalist retro aesthetic featuring hard pixel-offset drop shadows, crisp solid borders, and mono typography."
    ),
    BRUTALISM(
        displayName = "Brutalism",
        description = "Raw, unpolished anti-design with thick black borders, zero corners, high-contrast colors, and bold oversized typography."
    )
}
