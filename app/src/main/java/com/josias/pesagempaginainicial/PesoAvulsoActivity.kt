import android.R
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.josias.pesagempaginainicial.data.AppDatabase
import com.josias.pesagempaginainicial.data.Pesagem
import com.josias.pesagempaginainicial.databinding.ActivityPesoAvulsoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Date
import java.util.Locale

class PesoAvulsoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPesoAvulsoBinding
    private lateinit var mainHandler: Handler
    private lateinit var prefs: SharedPreferences
    private lateinit var db: AppDatabase

    private var currentWeight: Double = 0.0
    private var tareWeight: Double = 0.0

    private var scaleThread: Thread? = null

    @Volatile
    private var scaleRunning = false

    @Volatile
    private var scaleSocket: Socket? = null

    companion object {
        private const val TAG = "PesoAvulsoActivity"
        private const val PREFS_NAME = "peso_avulso_prefs"
        private const val KEY_TARE = "tare_weight"
        private const val KEY_DESCONTO = "desconto_value"
        private const val KEY_CLIENTE = "cliente"
        private const val KEY_MOTORISTA = "motorista"
        private const val KEY_PLACA = "placa"
        private const val KEY_PESO_INICIAL = "peso_inicial"
        private const val KEY_SCALE_HOST = "scale_host"
        private const val KEY_SCALE_PORT = "scale_port"
        private const val DEFAULT_SCALE_HOST = "192.168.1.100"
        private const val DEFAULT_SCALE_PORT = 1234
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPesoAvulsoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainHandler = Handler(Looper.getMainLooper())
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        db = AppDatabase.getInstance(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupClickListeners()
        loadState()
        recalcAll(currentWeight)
    }

    override fun onResume() {
        super.onResume()
        startScaleListener()
    }

    override fun onPause() {
        super.onPause()
        stopScaleListener()
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.btnSalvar.setOnClickListener { validateAndSave() }
        binding.btnExcluir.setOnClickListener { clearFields() }

        binding.btnUpload.setOnClickListener {
            tareWeight = currentWeight
            highlightMode(isTare = true)
            Toast.makeText(
                this,
                "Tara aplicada: ${formatNumber(tareWeight)} kg",
                Toast.LENGTH_SHORT
            ).show()
            recalcAll(currentWeight)
        }

        binding.btnDownload.setOnClickListener {
            tareWeight = 0.0
            highlightMode(isTare = false)
            Toast.makeText(this, "Tara zerada", Toast.LENGTH_SHORT).show()
            recalcAll(currentWeight)
        }
    }

    private fun startScaleListener() {
        if (scaleRunning) return
        scaleRunning = true
        scaleThread = Thread {
            while (scaleRunning) {
                val host = prefs.getString(KEY_SCALE_HOST, DEFAULT_SCALE_HOST) ?: DEFAULT_SCALE_HOST
                val port = prefs.getInt(KEY_SCALE_PORT, DEFAULT_SCALE_PORT)
                var socket: Socket? = null
                try {
                    socket = Socket()
                    socket.connect(InetSocketAddress(host, port), 3000)
                    socket.soTimeout = 5000
                    scaleSocket = socket
                    Log.i(TAG, "Conectado à balança em $host:$port")
                    mainHandler.post {
                        binding.tvConnectionStatus.text = "Conectado"
                        binding.tvConnectionStatus.setTextColor(Color.parseColor("#2E7D32"))
                    }

                    val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                    while (scaleRunning) {
                        val line = reader.readLine() ?: break

                        mainHandler.post {
                            val prev = binding.tvScaleLog.text?.toString() ?: ""
                            val nowLine = "${LocalTime.now()}: $line\n"
                            binding.tvScaleLog.text = (prev + nowLine).takeLast(4000)
                        }

                        val weight = tryParseWeightFromLine(line)
                        if (weight != null) {
                            mainHandler.post {
                                binding.tvPesoValue.text = formatNumber(weight)
                                currentWeight = weight
                                recalcAll(weight)
                            }
                        }
                    }

                } catch (e: Exception) {
                    Log.w(TAG, "Erro na conexão com a balança: ${e.localizedMessage}")
                    mainHandler.post {
                        binding.tvConnectionStatus.text = "Desconectado"
                        binding.tvConnectionStatus.setTextColor(Color.parseColor("#D32F2F"))
                    }
                    try {
                        Thread.sleep(2000)
                    } catch (_: InterruptedException) {
                    }
                } finally {
                    try {
                        socket?.close()
                    } catch (e: Exception) {
                    }
                    if (scaleSocket == socket) scaleSocket = null
                }
            }
        }
        scaleThread?.start()
    }

    private fun stopScaleListener() {
        scaleRunning = false
        try {
            scaleSocket?.close()
        } catch (e: Exception) {
        }
        scaleThread?.interrupt()
        scaleThread = null
    }

    private fun tryParseWeightFromLine(line: String): Double? {
        // Normaliza vírgulas para ponto, remove caracteres não imprimíveis e trim
        val cleaned = line.replace(',', '.').replace(Regex("[^\u0020-\u007E\n\r]"), "").trim()

        // Regex robusto para capturar o primeiro número decimal (ex: 0070.00, .5, -12.34)
        val regex = Regex("[-+]?(?:\\d+\\.\\d+|\\d+|\\.\\d+)")
        val match = regex.find(cleaned)

        return match?.value?.toDoubleOrNull()
    }

    private fun buildPayload(): String {
        recalcAll(currentWeight)

        val cliente = binding.etCliente.text?.toString()?.trim() ?: ""
        val motorista = binding.etMotorista.text?.toString()?.trim() ?: ""
        val placa = binding.etPlaca.text?.toString()?.trim() ?: ""
        val pesoInicial = parseDouble(binding.etPesoInicial.text?.toString() ?: "0")
        val desconto = parseDouble(binding.etDesconto.text?.toString() ?: "0")
        val subtotal = parseDouble(binding.tvSubtotalValue.text.toString().replace(" kg", ""))
        val pesoLiquidoTotal =
            parseDouble(binding.tvPesoLiquidoTotal.text.toString().replace(" kg", ""))

        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val now = fmt.format(Date())

        return JSONObject().apply {
            put("cliente", cliente)
            put("motorista", motorista)
            put("placa", placa)
            put("peso_atual", formatNumber(currentWeight))
            put("tara", formatNumber(tareWeight))
            put("peso_inicial", formatNumber(pesoInicial))
            put("desconto", formatNumber(desconto))
            put("subtotal", formatNumber(subtotal))
            put("peso_liquido_total", formatNumber(pesoLiquidoTotal))
            put("timestamp", now)
        }.toString(2)
    }

    private fun validateAndSave() {
        val cliente = binding.etCliente.text?.toString()?.trim() ?: ""
        if (cliente.isEmpty()) {
            binding.tilCliente.error = "Informe o cliente"
            return
        } else {
            binding.tilCliente.error = null
        }

        val motorista = binding.etMotorista.text?.toString()?.trim() ?: ""
        val placa = binding.etPlaca.text?.toString()?.trim() ?: ""
        val pesoInicialStr = binding.etPesoInicial.text?.toString()?.trim() ?: "0"
        val descontoStr = binding.etDesconto.text?.toString()?.trim() ?: "0"

        val pesoInicial = parseDouble(pesoInicialStr)
        val desconto = parseDouble(descontoStr)
        val pesoLiquido = (currentWeight - tareWeight).coerceAtLeast(0.0)
        val subtotal = (pesoLiquido - desconto).coerceAtLeast(0.0)
        val pesoLiquidoTotal = subtotal

        prefs.edit().apply {
            putFloat(KEY_TARE, tareWeight.toFloat())
            putFloat(KEY_DESCONTO, desconto.toFloat())
            putString(KEY_CLIENTE, cliente)
            putString(KEY_MOTORISTA, motorista)
            putString(KEY_PLACA, placa)
            putString(KEY_PESO_INICIAL, formatNumber(pesoInicial))
            apply()
        }

        CoroutineScope(Dispatchers.IO).launch {
            val p = Pesagem(
                cliente = cliente,
                motorista = motorista,
                placa = placa,
                pesoAtual = currentWeight,
                tara = tareWeight,
                valor = pesoLiquidoTotal,
                desconto = desconto,
                subtotal = subtotal,
                pesoLiquidoTotal = pesoLiquidoTotal,
                timestamp = System.currentTimeMillis()
            )
            db.pesagemDao().insert(p)
            Log.d(TAG, "Pesagem inserida no histórico")
        }

        Log.d(TAG, "Payload salvo: ${buildPayload()}")
        Toast.makeText(this, "Salvo localmente", Toast.LENGTH_SHORT).show()
    }

    private fun clearFields() {
        binding.etCliente.setText("")
        binding.etMotorista.setText("")
        binding.etPlaca.setText("")
        binding.etPesoInicial.setText("")
        binding.etDesconto.setText("")
        tareWeight = 0.0
        highlightMode(isTare = false)
        recalcAll(currentWeight)
        prefs.edit().clear().apply()
    }

    private fun loadState() {
        tareWeight = prefs.getFloat(KEY_TARE, 0f).toDouble()
        val desconto = prefs.getFloat(KEY_DESCONTO, 0f).toDouble()
        binding.etDesconto.setText(formatNumber(desconto))
        binding.etCliente.setText(prefs.getString(KEY_CLIENTE, ""))
        binding.etMotorista.setText(prefs.getString(KEY_MOTORISTA, ""))
        binding.etPlaca.setText(prefs.getString(KEY_PLACA, ""))
        binding.etPesoInicial.setText(prefs.getString(KEY_PESO_INICIAL, ""))
        highlightMode(isTare = tareWeight > 0.0)
    }

    private fun highlightMode(isTare: Boolean) {
        val activeColor = Color.parseColor("#0B57A4")
        val normalColor = Color.parseColor("#0B243C")
        binding.tvLabelTara.setTextColor(if (isTare) activeColor else normalColor)
        binding.tvLabelBruto.setTextColor(if (!isTare) activeColor else normalColor)
    }

    private fun recalcAll(currentWeightValue: Double) {
        val desconto = parseDouble(binding.etDesconto.text.toString())
        val net = (currentWeightValue - tareWeight).coerceAtLeast(0.0)
        val subtotal = (net - desconto).coerceAtLeast(0.0)

        binding.tvSubtotalValue.text = "${formatNumber(subtotal)} kg"
        binding.tvPesoLiquidoTotal.text = "${formatNumber(subtotal)} kg"

        Log.d(
            TAG,
            "recalcAll: current=$currentWeightValue, tare=$tareWeight, desconto=$desconto, subtotal=$subtotal"
        )
    }

    private fun parseDouble(raw: String): Double {
        val cleaned = raw.replace("[^0-9,.-]".toRegex(), "").replace(',', '.')
        return cleaned.toDoubleOrNull() ?: 0.0
    }

    private fun formatNumber(value: Double): String {
        return String.format(Locale.US, "%.2f", value)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}