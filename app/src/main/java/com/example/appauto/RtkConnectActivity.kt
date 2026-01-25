package com.example.appauto

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rtk.RtkManager
import java.util.Locale
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private fun rtkStatusText(anyState: Any): String {
    // Prefer numeric status codes (stable). Fall back to toString() only if we can't extract a code.
    fun extractCode(v: Any): Int? {
        return when (v) {
            is Int -> v
            is Number -> v.toInt()
            else -> {
                // Try common field/property names via reflection (no kotlin-reflect dependency)
                val candidates = listOf("code", "status", "value")
                for (name in candidates) {
                    try {
                        val f = v.javaClass.getDeclaredField(name)
                        f.isAccessible = true
                        val fv = f.get(v)
                        when (fv) {
                            is Int -> return fv
                            is Number -> return fv.toInt()
                        }
                    } catch (_: Throwable) {
                    }
                }
                // Last resort: parse an int from toString()
                Regex("-?\\d+").find(v.toString())?.value?.toIntOrNull()
            }
        }
    }

    val code = extractCode(anyState)
    if (code == null) {
        return anyState.toString()
    }

    // SDK doc status code mapping
    return when (code) {
        // GNSS / GGA
        -101 -> "📡 搜星中（-101）"
        -102 -> "⚠️ 板卡未输出GGA（-102）"

        // NTRIP / RTCM
        200 -> "✅ NTRIP连接成功（200）"
        201 -> "⛔ NTRIP已断开（201）"
        -201 -> "❌ 账号或密码错误（-201）"
        -202 -> "⏱️ NTRIP连接超时（-202）"
        -203 -> "⏱️ NTRIP通讯超时（-203）"
        -204 -> "🌐 无法连接至服务器（-204）"
        -205 -> "❌ 挂载点不存在（-205）"
        -206 -> "⚠️ NTRIP参数为空（-206）"

        // Permission
        -305 -> "🚫 没有串口读取权限（-305）"

        else -> {
            if (code < 0) {
                String.format(Locale.getDefault(), "❌ 连接异常（%d）", code)
            } else {
                String.format(Locale.getDefault(), "ℹ️ 状态码：%d", code)
            }
        }
    }
}

private fun rtkStatusTextForUi(anyState: Any): String = rtkStatusText(anyState)

private const val RTK_PREFS = "rtk_connect_prefs"
private const val KEY_HOST = "host"
private const val KEY_PORT = "port"
private const val KEY_USER = "user"
private const val KEY_PWD = "pwd"
private const val KEY_MOUNT = "mount"
private const val KEY_COMMID = "commidType" // 0 internal, 1 bluetooth
private const val KEY_BT_NAME = "btName"
private const val KEY_BT_MAC = "btMac"

class RtkConnectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rtk_connect)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val tvStatus = findViewById<TextView>(R.id.tv_status)
        val tvLog = findViewById<TextView>(R.id.tv_log)
        val tvGga = findViewById<TextView>(R.id.tv_gga)
        val btnClearLog = findViewById<MaterialButton>(R.id.btn_clear_log)

        val tvGgaTime = findViewById<TextView>(R.id.tv_gga_time)
        val tvGgaLatLon = findViewById<TextView>(R.id.tv_gga_latlon)
        val tvGgaFix = findViewById<TextView>(R.id.tv_gga_fix)
        val tvGgaSat = findViewById<TextView>(R.id.tv_gga_sat)
        val tvGgaHdop = findViewById<TextView>(R.id.tv_gga_hdop)
        val tvGgaAlt = findViewById<TextView>(R.id.tv_gga_alt)

        val btnConnect = findViewById<MaterialButton>(R.id.btn_connect)
        val btnDisconnect = findViewById<MaterialButton>(R.id.btn_disconnect)

        val etHost = findViewById<TextInputEditText>(R.id.et_host)
        val etPort = findViewById<TextInputEditText>(R.id.et_port)
        val etUser = findViewById<TextInputEditText>(R.id.et_user)
        val etPwd = findViewById<TextInputEditText>(R.id.et_pwd)
        val etMount = findViewById<TextInputEditText>(R.id.et_mount)

        val rgComm = findViewById<RadioGroup>(R.id.rg_comm)
        val rbInternal = findViewById<MaterialRadioButton>(R.id.rb_internal)
        val rbBluetooth = findViewById<MaterialRadioButton>(R.id.rb_bluetooth)

        val btnPickBt = findViewById<MaterialButton>(R.id.btn_pick_bt)
        val tilBtName = findViewById<TextInputLayout>(R.id.til_bt_name)
        val tilBtMac = findViewById<TextInputLayout>(R.id.til_bt_mac)
        val etBtName = findViewById<TextInputEditText>(R.id.et_bt_name)
        val etBtMac = findViewById<TextInputEditText>(R.id.et_bt_mac)

        val prefs = getSharedPreferences(RTK_PREFS, MODE_PRIVATE)

        // Restore inputs
        etHost.setText(prefs.getString(KEY_HOST, "").orEmpty())
        val savedPort = prefs.getInt(KEY_PORT, 2101)
        etPort.setText(String.format(Locale.getDefault(), "%d", savedPort))
        etUser.setText(prefs.getString(KEY_USER, "").orEmpty())
        etPwd.setText(prefs.getString(KEY_PWD, "").orEmpty())
        etMount.setText(prefs.getString(KEY_MOUNT, "").orEmpty())

        val savedComm = prefs.getInt(KEY_COMMID, 0)
        if (savedComm == 1) rbBluetooth.isChecked = true else rbInternal.isChecked = true

        etBtName.setText(prefs.getString(KEY_BT_NAME, "").orEmpty())
        etBtMac.setText(prefs.getString(KEY_BT_MAC, "").orEmpty())

        fun updateBtUiVisible() {
            val isBt = rbBluetooth.isChecked
            tilBtName.visibility = if (isBt) View.VISIBLE else View.GONE
            tilBtMac.visibility = if (isBt) View.VISIBLE else View.GONE
            btnPickBt.visibility = if (isBt) View.VISIBLE else View.GONE
        }
        updateBtUiVisible()

        rgComm.setOnCheckedChangeListener { _, _ ->
            updateBtUiVisible()
        }

        val inputFields = listOf(etHost, etPort, etUser, etPwd, etMount, etBtName, etBtMac)

        fun applyUiState(anyState: Any) {
            val raw = anyState.toString()
            val code = Regex("-?\\d+").find(raw)?.value?.toIntOrNull()

            val isConnecting = raw.contains("Connecting", ignoreCase = true)

            // Prefer numeric status codes; keep a best-effort string fallback for non-numeric states.
            val isConnected = (code == 200) || raw.contains("Connected", ignoreCase = true)
            val isDisconnected = (code == 201) || raw.contains("Disconnected", ignoreCase = true)
            val isIdle = raw.contains("Idle", ignoreCase = true)

            // Allow editing when idle OR disconnected OR any negative error code.
            // Note: don't repeat `isDisconnected` because it would be redundant if `isIdle` already includes it.
            val allowEdit = isIdle || isDisconnected || (code != null && code < 0)

            inputFields.forEach { it.isEnabled = allowEdit }
            for (i in 0 until rgComm.childCount) {
                rgComm.getChildAt(i).isEnabled = allowEdit
            }
            btnPickBt.isEnabled = allowEdit

            btnConnect.isEnabled = !isConnecting && !isConnected
            btnDisconnect.isEnabled = !allowEdit
            btnConnect.text = if (isConnecting) "连接中…" else "连接"
        }

        applyUiState("Idle")

        val tsFmt = DateTimeFormatter.ofPattern("HH:mm:ss.SSS", Locale.getDefault())

        fun appendLog(line: String) {
            val ts = LocalTime.now().format(tsFmt)
            val out = "[$ts] $line"

            // Trim to avoid TextView growing unbounded
            if (tvLog.text.length > 12000) {
                tvLog.text = tvLog.text.takeLast(6000)
            }
            tvLog.append(out)
            tvLog.append("\n")
        }

        fun ggaFixText(q: Int): String = when (q) {
            0 -> "无效"
            1 -> "单点"
            2 -> "DGPS"
            4 -> "RTK固定"
            5 -> "RTK浮点"
            6 -> "估算"
            else -> "未知($q)"
        }

        fun parseUtc(hhmmss: String): String {
            if (hhmmss.length < 6) return "-"
            val h = hhmmss.substring(0, 2)
            val m = hhmmss.substring(2, 4)
            val s = hhmmss.substring(4)
            return "$h:$m:$s"
        }

        fun nmeaToDegree(v: String, hemi: String): Double? {
            if (v.isBlank()) return null
            val dot = v.indexOf('.')
            if (dot < 0) return null
            // lat: ddmm.mmmm, lon: dddmm.mmmm -> infer by digits before dot
            val degLen = if (dot > 4) 3 else 2
            if (v.length < degLen) return null
            val deg = v.substring(0, degLen).toDoubleOrNull() ?: return null
            val min = v.substring(degLen).toDoubleOrNull() ?: return null
            var out = deg + (min / 60.0)
            val h = hemi.uppercase(Locale.getDefault())
            if (h == "S" || h == "W") out = -out
            return out
        }

        data class GgaInfo(
            val utc: String,
            val lat: Double?,
            val lon: Double?,
            val fixQ: Int,
            val sats: Int?,
            val hdop: Double?,
            val alt: Double?,
            val altUnit: String
        )

        fun parseGga(line: String): GgaInfo? {
            val core = line.substringBefore('*')
            val parts = core.split(',')
            if (parts.size < 11) return null
            val utcRaw = parts.getOrNull(1).orEmpty()
            val latRaw = parts.getOrNull(2).orEmpty()
            val latH = parts.getOrNull(3).orEmpty()
            val lonRaw = parts.getOrNull(4).orEmpty()
            val lonH = parts.getOrNull(5).orEmpty()
            val fixQ = parts.getOrNull(6)?.toIntOrNull() ?: -1
            val sats = parts.getOrNull(7)?.toIntOrNull()
            val hdop = parts.getOrNull(8)?.toDoubleOrNull()
            val alt = parts.getOrNull(9)?.toDoubleOrNull()
            val altUnit = parts.getOrNull(10).orEmpty().ifBlank { "M" }

            return GgaInfo(
                utc = parseUtc(utcRaw),
                lat = nmeaToDegree(latRaw, latH),
                lon = nmeaToDegree(lonRaw, lonH),
                fixQ = fixQ,
                sats = sats,
                hdop = hdop,
                alt = alt,
                altUnit = altUnit
            )
        }

        // Device picker page
        val pickBtLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
            if (res.resultCode == RESULT_OK) {
                val data = res.data
                val name = data?.getStringExtra(BtDeviceSelectActivity.EXTRA_BT_NAME).orEmpty()
                val mac = data?.getStringExtra(BtDeviceSelectActivity.EXTRA_BT_MAC).orEmpty()
                if (mac.isNotBlank()) {
                    etBtName.setText(name)
                    etBtMac.setText(mac)
                }
            }
        }

        btnPickBt.setOnClickListener {
            rbBluetooth.isChecked = true
            updateBtUiVisible()
            pickBtLauncher.launch(Intent(this, BtDeviceSelectActivity::class.java))
        }

        btnConnect.setOnClickListener {
            val host = etHost.text?.toString().orEmpty().trim()
            val port = etPort.text?.toString()?.trim()?.toIntOrNull() ?: 0
            val user = etUser.text?.toString().orEmpty().trim()
            val pwd = etPwd.text?.toString().orEmpty()
            val mount = etMount.text?.toString().orEmpty().trim()

            val commIdType = if (rbBluetooth.isChecked) 1 else 0
            val btName = etBtName.text?.toString().orEmpty().trim()
            val btMac = etBtMac.text?.toString().orEmpty().trim()

            // Save params
            prefs.edit()
                .putString(KEY_HOST, host)
                .putInt(KEY_PORT, port)
                .putString(KEY_USER, user)
                .putString(KEY_PWD, pwd)
                .putString(KEY_MOUNT, mount)
                .putInt(KEY_COMMID, commIdType)
                .putString(KEY_BT_NAME, btName)
                .putString(KEY_BT_MAC, btMac)
                .apply()

            if (host.isBlank() || port <= 0 || mount.isBlank()) {
                tvStatus.text = "状态：⚠️ 请填写 Host / Port / MountPoint"
                return@setOnClickListener
            }
            if (commIdType == 1 && btMac.isBlank()) {
                tvStatus.text = "状态：⚠️ 请选择蓝牙设备（需要MAC）"
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                RtkManager.connect(
                    context = this@RtkConnectActivity,
                    host = host,
                    port = port,
                    user = user,
                    pwd = pwd,
                    mount = mount,
                    commIdType = commIdType,
                    btName = btName,
                    btMac = btMac
                )
            }
        }

        btnDisconnect.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                RtkManager.disconnect()
            }
        }

        btnClearLog.setOnClickListener {
            tvLog.text = ""
            tvGga.text = "(等待 GPGGA/GNGGA …)"
            tvGgaTime.text = "UTC：-"
            tvGgaLatLon.text = "坐标：-"
            tvGgaFix.text = "定位：-"
            tvGgaSat.text = "卫星：-"
            tvGgaHdop.text = "HDOP：-"
            tvGgaAlt.text = "高程：-"
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    RtkManager.state.collect { st ->
                        tvStatus.text = "状态：${rtkStatusTextForUi(st)}"
                        applyUiState(st)
                    }
                }
                launch {
                    RtkManager.nmea.collect { msg ->
                        val line = msg.trim()

                        if (line.startsWith("\$GPGGA") || line.startsWith("\$GNGGA")) {
                            tvGga.text = line

                            val info = parseGga(line)
                            if (info != null) {
                                tvGgaTime.text = "UTC：${info.utc}"

                                val latStr = info.lat?.let { String.format(Locale.getDefault(), "%.8f", it) } ?: "-"
                                val lonStr = info.lon?.let { String.format(Locale.getDefault(), "%.8f", it) } ?: "-"
                                tvGgaLatLon.text = "坐标：$latStr, $lonStr"

                                tvGgaFix.text = "定位：${ggaFixText(info.fixQ)}"
                                tvGgaSat.text = "卫星：${info.sats?.toString() ?: "-"}"
                                tvGgaHdop.text = "HDOP：${info.hdop?.let { String.format(Locale.getDefault(), "%.2f", it) } ?: "-"}"
                                tvGgaAlt.text = "高程：${info.alt?.let { String.format(Locale.getDefault(), "%.3f", it) } ?: "-"} ${info.altUnit}"
                            }
                        }

                        // Append all NMEA lines to log with timestamp.
                        if (line.isNotBlank()) {
                            appendLog(line)
                        }
                    }
                }
            }
        }
    }
}