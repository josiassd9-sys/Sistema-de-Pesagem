package com.josias.pesagempaginainicial

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.josias.pesagempaginainicial.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var lastTheme: AppTheme? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplica tema salvo
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Guarda o tema que foi aplicado nesta criação
        lastTheme = ThemePreferences.getCurrentTheme(this)

        binding.btnAbrirPesagem.setOnClickListener {
            startActivity(Intent(this, PesoAvulsoActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val current = ThemePreferences.getCurrentTheme(this)
        if (lastTheme != null && current != lastTheme) {
            recreate()
        }
    }
}