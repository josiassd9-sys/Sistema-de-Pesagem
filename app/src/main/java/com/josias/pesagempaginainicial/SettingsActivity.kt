package com.josias.pesagempaginainicial

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.josias.pesagempaginainicial.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplica o tema salvo antes de inflar o layout
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarSettings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val temas = AppTheme.values()

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            temas.map { it.prefName }
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerTema.adapter = adapter

        // Seleciona no Spinner o tema atualmente salvo
        val temaAtual = ThemePreferences.getCurrentTheme(this)
        val indexAtual = temas.indexOf(temaAtual).takeIf { it >= 0 } ?: 0
        binding.spinnerTema.setSelection(indexAtual)

        // Carrega overrides atuais nos EditTexts
        loadCurrentOverrides()

        binding.btnSalvarTema.setOnClickListener {
            val pos = binding.spinnerTema.selectedItemPosition
            val selecionado = temas[pos]

            // Salva tema base
            ThemePreferences.setCurrentTheme(this, selecionado)

            // Salva overrides de cor (HEX ou limpa se vazio)
            ThemeOverrides.setOverrideColor(
                this,
                ThemeOverrides.KEY_BACKGROUND,
                binding.etBackgroundColor.text?.toString()
            )
            ThemeOverrides.setOverrideColor(
                this,
                ThemeOverrides.KEY_FOREGROUND,
                binding.etForegroundColor.text?.toString()
            )
            ThemeOverrides.setOverrideColor(
                this,
                ThemeOverrides.KEY_CARD,
                binding.etCardColor.text?.toString()
            )
            ThemeOverrides.setOverrideColor(
                this,
                ThemeOverrides.KEY_PRIMARY,
                binding.etPrimaryColor.text?.toString()
            )
            ThemeOverrides.setOverrideColor(
                this,
                ThemeOverrides.KEY_ACCENT_PRICE,
                binding.etAccentPriceColor.text?.toString()
            )
            ThemeOverrides.setOverrideColor(
                this,
                ThemeOverrides.KEY_SETTINGS_BUTTON_BG,
                binding.etSettingsButtonBgColor.text?.toString()
            )

            Toast.makeText(
                this,
                "Tema '${selecionado.prefName}' salvo",
                Toast.LENGTH_SHORT
            ).show()

            // Fecha a tela de configurações e volta para a anterior
            finish()
        }
    }

    private fun loadCurrentOverrides() {
        binding.etBackgroundColor.setText(
            ThemeOverrides.getOverrideHex(this, ThemeOverrides.KEY_BACKGROUND) ?: ""
        )
        binding.etForegroundColor.setText(
            ThemeOverrides.getOverrideHex(this, ThemeOverrides.KEY_FOREGROUND) ?: ""
        )
        binding.etCardColor.setText(
            ThemeOverrides.getOverrideHex(this, ThemeOverrides.KEY_CARD) ?: ""
        )
        binding.etPrimaryColor.setText(
            ThemeOverrides.getOverrideHex(this, ThemeOverrides.KEY_PRIMARY) ?: ""
        )
        binding.etAccentPriceColor.setText(
            ThemeOverrides.getOverrideHex(this, ThemeOverrides.KEY_ACCENT_PRICE) ?: ""
        )
        binding.etSettingsButtonBgColor.setText(
            ThemeOverrides.getOverrideHex(this, ThemeOverrides.KEY_SETTINGS_BUTTON_BG) ?: ""
        )
    }
}