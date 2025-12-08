package com.josias.pesagempaginainicial

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import android.content.Intent
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.josias.pesagempaginainicial.databinding.ActivityPesoAvulsoBinding

class PesoAvulsoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPesoAvulsoBinding
    private val TAG = "PesoAvulsoActivity"
    private var lastTheme: AppTheme? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplica o tema salvo antes de inflar o layout
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityPesoAvulsoBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Guarda o tema aplicado nesta criação
            lastTheme = ThemePreferences.getCurrentTheme(this)

            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            })

            setupClickListeners()
            applyDynamicOverrides()

            Log.d(TAG, "onCreate concluído")
        } catch (e: Exception) {
            Log.e(TAG, "Erro em onCreate", e)
            Toast.makeText(
                this,
                "Erro ao iniciar a tela: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val current = ThemePreferences.getCurrentTheme(this)
        if (lastTheme != null && current != lastTheme) {
            recreate()
        } else {
            // Mesmo tema base, mas overrides podem ter mudado
            applyDynamicOverrides()
        }
    }

    private fun applyDynamicOverrides() {
        // Fundo principal (background)
        val background = ThemeOverrides.resolveColor(
            this,
            ThemeOverrides.KEY_BACKGROUND,
            R.attr.colorSurface
        )
        binding.coordinatorRoot.setBackgroundColor(background)

        // Texto principal (foreground) - usado em alguns textos gerais
        val foreground = ThemeOverrides.resolveColor(
            this,
            ThemeOverrides.KEY_FOREGROUND,
            R.attr.colorOnSurface
        )
        binding.tvPesoLabel.setTextColor(foreground)
        // tvSubtotal foi dividido em label + value no layout; aplica cor a ambos
        binding.tvSubtotalLabel.setTextColor(foreground)
        binding.tvSubtotalValue.setTextColor(foreground)

        // Fundo dos cards (card)
        val cardBg = ThemeOverrides.resolveColor(
            this,
            ThemeOverrides.KEY_CARD,
            R.attr.colorSurfaceVariant
        )
        binding.cardPeso.setCardBackgroundColor(cardBg)
        binding.cardCacamba1.setCardBackgroundColor(cardBg)

        // Texto dos cards (cardForeground)
        val cardFg = ThemeOverrides.resolveColor(
            this,
            ThemeOverrides.KEY_CARD_FOREGROUND,
            R.attr.colorOnSurface
        )
        binding.tvCacamba1.setTextColor(cardFg)

        // Cor primária (toolbar, alguns destaques)
        val primary = ThemeOverrides.resolveColor(
            this,
            ThemeOverrides.KEY_PRIMARY,
            R.attr.colorPrimary
        )
        binding.toolbar.setBackgroundColor(primary)

        // Cor de destaque para preço / valores
        val accentPrice = ThemeOverrides.resolveColor(
            this,
            ThemeOverrides.KEY_ACCENT_PRICE,
            R.attr.colorCustomAccentPrice
        )
        binding.tvPesoValue.setTextColor(accentPrice)
        binding.tvPesoLiquidoTotal.setTextColor(accentPrice)

        // Fundo do botão de configurações
        val settingsBg = ThemeOverrides.resolveColor(
            this,
            ThemeOverrides.KEY_SETTINGS_BUTTON_BG,
            R.attr.colorCustomSettingsButtonBg
        )
        binding.btnConfig.setBackgroundColor(settingsBg)
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.btnAdicionarCacamba.setOnClickListener {
            Toast.makeText(this, "Adicionar nova caçamba", Toast.LENGTH_SHORT).show()
        }

        binding.btnConfig.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnExcluir.setOnClickListener {
            Toast.makeText(this, "Excluir", Toast.LENGTH_SHORT).show()
        }

        binding.btnSalvar.setOnClickListener {
            Toast.makeText(this, "Salvar", Toast.LENGTH_SHORT).show()
        }

        binding.btnEnviar.setOnClickListener {
            Toast.makeText(this, "Enviar", Toast.LENGTH_SHORT).show()
        }

        binding.btnImprimir.setOnClickListener {
            Toast.makeText(this, "Imprimir", Toast.LENGTH_SHORT).show()
        }

        binding.btnAddCacamba1.setOnClickListener {
            Toast.makeText(this, "Adicionar à caçamba 1", Toast.LENGTH_SHORT).show()
        }

        binding.btnUpload.setOnClickListener {
            Toast.makeText(this, "Upload", Toast.LENGTH_SHORT).show()
        }

        binding.btnDownload.setOnClickListener {
            Toast.makeText(this, "Download", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
# Android / Gradle
.gradle/
**/build/
build/
/local.properties
/.idea/
/captures/
.externalNativeBuild/
.cxx/
*.iml

# Kotlin / Java
*.class

# OS
.DS_Store
Thumbs.db

# Android Studio
*.apk
*.ap_
// Add more as needed