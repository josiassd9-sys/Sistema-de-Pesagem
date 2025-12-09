package com.josias.pesagempaginainicial

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.josias.pesagempaginainicial.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val PREFS_NAME = "pesagem_prefs"
    private val KEY_SCALE_HOST = "key_scale_host"
    private val KEY_SCALE_PORT = "key_scale_port"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val host = prefs.getString(KEY_SCALE_HOST, "192.168.0.100")
        val port = prefs.getInt(KEY_SCALE_PORT, 12345)

        binding.etScaleHost.setText(host)
        binding.etScalePort.setText(port.toString())

        binding.btnSaveScaleConfig.setOnClickListener {
            val h = binding.etScaleHost.text?.toString() ?: ""
            val p = binding.etScalePort.text?.toString()?.toIntOrNull() ?: 0
            if (h.isBlank() || p <= 0) {
                Toast.makeText(this, "Host ou porta inválidos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            prefs.edit().putString(KEY_SCALE_HOST, h).putInt(KEY_SCALE_PORT, p).apply()
            Toast.makeText(this, "Configurações salvas", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnCancelScaleConfig.setOnClickListener {
            finish()
        }
    }
}
