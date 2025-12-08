package com.josias.pesagempaginainicial

enum class AppTheme(val prefName: String, val styleRes: Int) {
    PADRAO("Padrão (Escuro)", R.style.AppTheme_Padrao),
    NEVE("Neve (Claro)", R.style.AppTheme_Neve),
    CYBERPUNK("Cyberpunk Neon", R.style.AppTheme_CyberpunkNeon),
    MENTA_FRESCA("Menta Fresca", R.style.AppTheme_MentaFresca),
    OCEANO_PROFUNDO("Oceano Profundo", R.style.AppTheme_OceanoProfundo),

    CINZA_CLARO("Cinza Claro", R.style.AppTheme_CinzaClaro),
    CINZA_MEDIO("Cinza Médio", R.style.AppTheme_CinzaMedio),
    CINZA_ESCURO("Cinza Escuro", R.style.AppTheme_CinzaEscuro);

    companion object {
        fun fromPrefName(name: String?): AppTheme =
            values().find { it.prefName == name } ?: PADRAO
    }
}