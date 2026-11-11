package pl.ticwatch.atlasadb

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.flyfishxu.kadb.Kadb
import com.flyfishxu.kadb.cert.KadbCert
import com.flyfishxu.kadb.cert.KadbCertPolicy
import com.flyfishxu.kadb.cert.OkioFilePrivateKeyStore
import okio.Path.Companion.toPath
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val io = Executors.newSingleThreadExecutor()
    private lateinit var host: EditText
    private lateinit var pairPort: EditText
    private lateinit var pairCode: EditText
    private lateinit var connectPort: EditText
    private lateinit var command: EditText
    private lateinit var log: TextView
    private lateinit var status: TextView
    private var connected = false
    private val prefs by lazy { getSharedPreferences("endpoint", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(13, 17, 27)
        window.navigationBarColor = Color.rgb(13, 17, 27)
        val bg = Color.rgb(13, 17, 27)
        val panel = Color.rgb(24, 31, 45)
        val accent = Color.rgb(58, 171, 156)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(14))
            setBackgroundColor(bg)
        }
        val title = TextView(this).apply { text = "ATLAS  •  ADB CONSOLE"; textSize = 21f; setTextColor(Color.WHITE); setTypeface(null, Typeface.BOLD) }
        root.addView(title)
        status = TextView(this).apply { text = "● Rozłączony"; setTextColor(Color.LTGRAY); textSize = 14f; setPadding(0,dp(7),0,dp(12)) }
        root.addView(status)

        val inputScroll = ScrollView(this)
        val fields = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        inputScroll.addView(fields)
        root.addView(inputScroll, LinearLayout.LayoutParams(-1, 0, 2f))
        fields.addView(section("Połączenie Wi-Fi", accent))
        host = edit("Adres IP zegarka, np. 192.168.1.25", prefs.getString("host", "") ?: "")
        fields.addView(host)
        pairPort = edit("Port parowania (z ekranu kodu)", "", true)
        pairCode = edit("Kod parowania (6 cyfr)", "", true)
        connectPort = edit("Port połączenia ADB", prefs.getString("connectPort", "") ?: "", true)
        fields.addView(pairPort); fields.addView(pairCode); fields.addView(connectPort)
        val row1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row1.addView(button("Sparuj", accent) { pair() }, LinearLayout.LayoutParams(0, dp(46), 1f))
        row1.addView(button("Połącz", accent) { connect() }, LinearLayout.LayoutParams(0, dp(46), 1f))
        row1.addView(button("Rozłącz", panel) { connected = false; setStatus("● Rozłączony", false); append("Rozłączono w aplikacji.") }, LinearLayout.LayoutParams(0, dp(46), 1f))
        fields.addView(row1)
        fields.addView(section("Szybka diagnostyka", accent))
        val presets = listOf(
            "Model" to "getprop ro.product.model",
            "Android" to "getprop ro.build.version.release",
            "Bateria" to "dumpsys battery",
            "Temperatura" to "dumpsys thermalservice",
            "RAM" to "cat /proc/meminfo",
            "Dysk" to "df -h",
            "Ekran" to "wm size; wm density",
            "Aplikacje" to "pm list packages -3"
        )
        presets.chunked(2).forEach { pair ->
            val r = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            pair.forEach { (label, cmd) -> r.addView(button(label, panel) { command.setText(cmd); execute(cmd) }, LinearLayout.LayoutParams(0, dp(44), 1f)) }
            fields.addView(r)
        }
        fields.addView(section("Konsola • polecenia shell", accent))
        command = edit("np. dumpsys battery", "getprop ro.product.model")
        command.isSingleLine = false
        command.minLines = 2
        fields.addView(command)
        val row2 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row2.addView(button("▶ Wykonaj", accent) { execute(command.text.toString()) }, LinearLayout.LayoutParams(0, dp(48), 2f))
        row2.addView(button("Wyczyść", panel) { log.text = "" }, LinearLayout.LayoutParams(0, dp(48), 1f))
        row2.addView(button("Udostępnij", panel) { shareLog() }, LinearLayout.LayoutParams(0, dp(48), 1f))
        fields.addView(row2)
        root.addView(section("Wynik", accent))
        val resultScroll = ScrollView(this).apply { setBackgroundColor(panel) }
        log = TextView(this).apply {
            text = "Włącz debugowanie bezprzewodowe na zegarku.\nParowanie i połączenie mają osobne porty.\n"
            textSize = 12f; typeface = Typeface.MONOSPACE
            setTextColor(Color.rgb(200, 239, 223)); setTextIsSelectable(true)
            setPadding(dp(12),dp(12),dp(12),dp(12))
        }
        resultScroll.addView(log)
        root.addView(resultScroll, LinearLayout.LayoutParams(-1,0,3f))
        setContentView(root)
        io.execute {
            try {
                val store = OkioFilePrivateKeyStore(java.io.File(filesDir, "adb_private_key.pem").absolutePath.toPath())
                KadbCert.configure(store = store, policy = KadbCertPolicy())
                KadbCert.ensureReady()
                append("Klucz ADB gotowy — zapisywany lokalnie w pamięci aplikacji.")
            } catch (e: Exception) { append("Błąd inicjalizacji klucza: ${e.message}") }
        }
    }

    private fun pair() {
        val h = host.text.toString().trim()
        val p = pairPort.text.toString().toIntOrNull()
        val code = pairCode.text.toString().trim()
        if (h.isBlank() || (p == null || p !in 1..65535) || !code.matches(Regex("[0-9]{6}"))) {
            append("Podaj IP, poprawny port parowania i sześciocyfrowy kod."); return
        }
        runJob("Parowanie z $h:$p") {
            Kadb.pair(h, p!!, code)
            append("Parowanie zakończone. Wpisz teraz oddzielny port połączenia z ekranu debugowania bezprzewodowego.")
        }
    }

    private fun connect() {
        val h = host.text.toString().trim()
        val p = connectPort.text.toString().toIntOrNull()
        if (h.isBlank() || (p == null || p !in 1..65535)) { append("Podaj IP i poprawny PORT POŁĄCZENIA."); return }
        runJob("Łączenie z $h:$p") {
            Kadb.create(h, p!!).use { adb ->
                val result = adb.shell("getprop ro.product.model")
                append("Połączono: ${result.output.trim()}")
            }
            prefs.edit().putString("host",h).putString("connectPort",p.toString()).apply()
            connected = true
            setStatus("● Połączony z $h:$p", true)
        }
    }

    private fun execute(input: String) {
        val cmd = input.trim().removePrefix("adb shell ").removePrefix("shell ")
        if (cmd.isEmpty()) return
        if (!connected) { append("Najpierw połącz się z zegarkiem."); return }
        val h = host.text.toString().trim()
        val p = connectPort.text.toString().toIntOrNull()
        if ((p == null || p !in 1..65535)) { append("Nieprawidłowy port połączenia."); return }
        runJob("$ $cmd") {
            Kadb.create(h, p!!).use { adb ->
                val result = adb.shell(cmd)
                append(result.output.ifBlank { "(brak wyniku)" })
                append("Kod zakończenia: ${result.exitCode}")
            }
        }
    }

    private fun runJob(label: String, task: () -> Unit) {
        append(label)
        io.execute {
            try { task() }
            catch (e: Exception) { connected = false; setStatus("● Rozłączony / błąd", false); append("BŁĄD: ${e.javaClass.simpleName}: ${e.message}") }
        }
    }
    private fun setStatus(s: String, ok: Boolean) = runOnUiThread {
        status.text = s; status.setTextColor(if (ok) Color.rgb(90, 221, 172) else Color.LTGRAY)
    }
    private fun append(line: String) = runOnUiThread {
        val next = log.text.toString() + "\n" + line + "\n"
        log.text = if (next.length > 100000) next.takeLast(100000) else next
    }
    private fun shareLog() {
        val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"; putExtra(android.content.Intent.EXTRA_TEXT, log.text.toString())
        }
        startActivity(android.content.Intent.createChooser(send, "Udostępnij wynik ADB"))
    }
    private fun section(s: String, color: Int) = TextView(this).apply {
        text = s; setTextColor(color); textSize = 15f; setTypeface(null, Typeface.BOLD)
        setPadding(0, dp(12), 0, dp(7))
    }
    private fun edit(hintText: String, value: String, numeric: Boolean = false) = EditText(this).apply {
        hint = hintText; setText(value); setTextColor(Color.WHITE); setHintTextColor(Color.GRAY)
        textSize = 14f; isSingleLine = true
        inputType = if (numeric) InputType.TYPE_CLASS_NUMBER else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        backgroundTintList = android.content.res.ColorStateList.valueOf(Color.rgb(65, 93, 108))
        setPadding(dp(8), dp(6), dp(8), dp(6))
    }
    private fun button(text: String, color: Int, action: () -> Unit) = Button(this).apply {
        this.text = text; textSize = 11f; isAllCaps = false
        setTextColor(Color.WHITE); setBackgroundColor(color)
        setOnClickListener { action() }
    }
    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
    override fun onDestroy() { io.shutdownNow(); super.onDestroy() }
}
