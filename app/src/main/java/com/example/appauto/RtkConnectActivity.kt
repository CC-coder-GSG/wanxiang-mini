package com.example.appauto

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rtk.RtkManager
import rtk.RtkState
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class RtkConnectActivity : AppCompatActivity() {

    // UI 控件
    private lateinit var tvStatus: TextView
    private lateinit var tvLog: TextView
    private lateinit var tilMount: TextInputLayout

    private lateinit var etHost: TextInputEditText
    private lateinit var etPort: TextInputEditText
    private lateinit var etUser: TextInputEditText
    private lateinit var etPwd: TextInputEditText
    private lateinit var etMount: TextInputEditText
    private lateinit var etBtName: TextInputEditText
    private lateinit var etBtMac: TextInputEditText

    private lateinit var btnConnect: MaterialButton
    private lateinit var btnDisconnect: MaterialButton
    private lateinit var btnPickBt: MaterialButton

    private lateinit var tvGgaRaw: TextView
    private lateinit var tvGgaTime: TextView
    private lateinit var tvGgaLatLon: TextView
    private lateinit var tvGgaFix: TextView
    private lateinit var tvGgaSat: TextView
    private lateinit var tvGgaHdop: TextView
    private lateinit var tvGgaAlt: TextView

    private val RTK_PREFS = "rtk_connect_prefs"
    private val tsFmt = DateTimeFormatter.ofPattern("HH:mm:ss.SSS", Locale.getDefault())

    // --- 双状态显示变量 ---
    private var btStatusStr = "未连接"
    private var ntripStatusStr = "未启动"
    private var isBtConnected = false
    private var isNtripOk = false // 差分是否有数据

    // 健康监测
    private var lastGgaInfo: GgaInfo? = null
    private var lastDataTime: Long = 0
    private var isMonitoring = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rtk_connect)

        initViews()
        restoreParams()
        setupListeners()
        observeRtkData()

        // 初始化显示
        updateStatusDisplay()
    }

    private fun initViews() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        tvStatus = findViewById(R.id.tv_status)
        tvLog = findViewById(R.id.tv_log)

        tvGgaRaw = findViewById(R.id.tv_gga)
        tvGgaTime = findViewById(R.id.tv_gga_time)
        tvGgaLatLon = findViewById(R.id.tv_gga_latlon)
        tvGgaFix = findViewById(R.id.tv_gga_fix)
        tvGgaSat = findViewById(R.id.tv_gga_sat)
        tvGgaHdop = findViewById(R.id.tv_gga_hdop)
        tvGgaAlt = findViewById(R.id.tv_gga_alt)

        etHost = findViewById(R.id.et_host)
        etPort = findViewById(R.id.et_port)
        etUser = findViewById(R.id.et_user)
        etPwd = findViewById(R.id.et_pwd)
        tilMount = findViewById(R.id.til_mount)
        etMount = findViewById(R.id.et_mount)
        etBtName = findViewById(R.id.et_bt_name)
        etBtMac = findViewById(R.id.et_bt_mac)

        btnConnect = findViewById(R.id.btn_connect)
        btnDisconnect = findViewById(R.id.btn_disconnect)
        btnPickBt = findViewById(R.id.btn_pick_bt)

        findViewById<MaterialButton>(R.id.btn_clear_log).setOnClickListener {
            tvLog.text = ""
            tvGgaRaw.text = "(等待数据...)"
            resetCardUi()
        }
    }

    private fun resetCardUi() {
        tvGgaTime.text = "UTC时间：-"
        tvGgaLatLon.text = "经纬度：-"
        tvGgaFix.text = "解状态：-"
        tvGgaSat.text = "卫星数：-"
        tvGgaHdop.text = "HDOP：-"
        tvGgaAlt.text = "海拔：-"
    }

    private fun setupListeners() {
        val pickBtLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
            if (res.resultCode == RESULT_OK) {
                val name = res.data?.getStringExtra("EXTRA_BT_NAME").orEmpty()
                val mac = res.data?.getStringExtra("EXTRA_BT_MAC").orEmpty()
                if (mac.isNotBlank()) {
                    etBtName.setText(name)
                    etBtMac.setText(mac)
                }
            }
        }

        btnPickBt.setOnClickListener {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT), 101)
                    return@setOnClickListener
                }
            }
            pickBtLauncher.launch(Intent(this, BtDeviceSelectActivity::class.java))
        }

        // 🟢【已修复】这里改为调用 NtripUtils (您之前创建的文件)
        tilMount.setEndIconOnClickListener {
            val host = etHost.text?.toString()?.trim().orEmpty()
            val port = etPort.text?.toString()?.trim()?.toIntOrNull() ?: 2101
            if (host.isBlank()) {
                Toast.makeText(this, "请先填写 IP 地址", Toast.LENGTH_SHORT).show()
                return@setEndIconOnClickListener
            }
            val loadingDialog = AlertDialog.Builder(this)
                .setMessage("正在获取源列表...")
                .setCancelable(false)
                .create()
            loadingDialog.show()

            lifecycleScope.launch {
                try {
                    // 修正点：NtripHelper -> NtripUtils
                    val list = NtripUtils.getNtripSourceTable(host, port)
                    loadingDialog.dismiss()
                    if (list.isEmpty()) {
                        Toast.makeText(this@RtkConnectActivity, "未获取到挂载点", Toast.LENGTH_SHORT).show()
                    } else {
                        showMountPointSelector(list)
                    }
                } catch (e: Exception) {
                    loadingDialog.dismiss()
                    Toast.makeText(this@RtkConnectActivity, getChineseError(e), Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnConnect.setOnClickListener {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "请先授予蓝牙权限", Toast.LENGTH_SHORT).show()
                    requestPermissions(arrayOf(android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT), 101)
                    return@setOnClickListener
                }
            }

            val host = etHost.text?.toString().orEmpty().trim()
            val port = etPort.text?.toString()?.trim()?.toIntOrNull() ?: 0
            val user = etUser.text?.toString().orEmpty().trim()
            val pwd = etPwd.text?.toString().orEmpty()
            val mount = etMount.text?.toString().orEmpty().trim()
            val btName = etBtName.text?.toString().orEmpty().trim()
            val btMac = etBtMac.text?.toString().orEmpty().trim()

            if (host.isBlank() || mount.isBlank() || btMac.isBlank()) {
                Toast.makeText(this, "⚠️ 参数不完整", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveParams(host, port, user, pwd, mount, btName, btMac)

            lifecycleScope.launch(Dispatchers.IO) {
                RtkManager.connect(lifecycleScope,this@RtkConnectActivity, host, port, user, pwd, mount, btName, btMac)
            }
        }

        btnDisconnect.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                RtkManager.disconnect()
            }
        }
    }

    private fun updateStatusDisplay() {
        val text = "蓝牙: $btStatusStr  |  差分: $ntripStatusStr"
        tvStatus.text = text

        if (!isBtConnected) {
            tvStatus.setTextColor(getColor(android.R.color.black))
        } else {
            if (isNtripOk) {
                tvStatus.setTextColor(getColor(android.R.color.holo_green_dark))
            } else {
                tvStatus.setTextColor(getColor(android.R.color.holo_orange_dark))
            }
        }
    }

    private fun observeRtkData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    RtkManager.state.collect { st ->
                        applyUiState(st)

                        when(st) {
                            is RtkState.Idle -> {
                                btStatusStr = "未连接"
                                ntripStatusStr = "未启动"
                                isBtConnected = false
                                isNtripOk = false
                                isMonitoring = false
                            }
                            is RtkState.Connecting -> {
                                btStatusStr = "连接中..."
                                isBtConnected = false
                            }
                            is RtkState.Connected -> {
                                btStatusStr = "✅已连接"
                                isBtConnected = true
                                ntripStatusStr = "等待数据..."
                                isNtripOk = false
                                if (!isMonitoring) checkDataStreamHealth()
                            }
                            is RtkState.Error -> {
                                if (st.code <= -100 || (st.code in -10..0)) {
                                    ntripStatusStr = "❌${st.msg}"
                                    isNtripOk = false
                                } else {
                                    btStatusStr = "❌失败"
                                    isBtConnected = false
                                }
                            }
                        }
                        updateStatusDisplay()
                    }
                }

                launch {
                    RtkManager.serverTraffic.collect { bytes ->
                        lastDataTime = System.currentTimeMillis()
                        if (!isNtripOk) {
                            isNtripOk = true
                        }
                        ntripStatusStr = "✅服务正常"
                        updateStatusDisplay()
                    }
                }

                launch {
                    RtkManager.nmea.collect { fullMsg ->
                        val line = cleanNmeaMessage(fullMsg)
                        if (line.contains("GGA")) {
                            tvGgaRaw.text = line
                            val ggaInfo = parseGga(line)
                            if (ggaInfo != null) {
                                lastGgaInfo = ggaInfo
                                updateCardUi(ggaInfo)
                            }
                        }
                        if (line.contains("GGA") || line.contains("RMC")) {
                            appendLog(line)
                        }
                    }
                }
            }
        }
    }

    private fun checkDataStreamHealth() {
        isMonitoring = true
        lastDataTime = System.currentTimeMillis()

        lifecycleScope.launch {
            while (isBtConnected && isMonitoring) {
                kotlinx.coroutines.delay(2000)
                val silence = System.currentTimeMillis() - lastDataTime
                if (silence > 10000) {
                    isNtripOk = false
                    ntripStatusStr = "⚠️无数据(请检查挂载点)"
                    updateStatusDisplay()
                }
            }
        }
    }

    private fun getChineseError(e: Exception): String {
        return when (e) {
            is UnknownHostException -> "无法找到服务器 (IP地址错误)"
            is ConnectException -> "连接被拒绝 (端口可能错了)"
            is SocketTimeoutException -> "连接超时 (网络不通)"
            else -> "获取失败: ${e.message}"
        }
    }

    private fun updateCardUi(info: GgaInfo) {
        tvGgaTime.text = "UTC时间：${info.utc}"
        tvGgaLatLon.text = "经纬度：${info.lat}, ${info.lon}"

        tvGgaFix.text = "解状态：${info.fixStatus}"
        if (info.fixStatus.contains("固定")) {
            tvGgaFix.setTextColor(getColor(android.R.color.holo_green_dark))
        } else if (info.fixStatus.contains("浮点")) {
            tvGgaFix.setTextColor(getColor(android.R.color.holo_orange_dark))
        } else {
            tvGgaFix.setTextColor(getColor(android.R.color.black))
        }

        tvGgaSat.text = "卫星数：${info.satCount}"
        tvGgaHdop.text = "HDOP：${info.hdop}"
        tvGgaAlt.text = "海拔：${info.alt} M"
    }

    data class GgaInfo(
        val utc: String, val lat: String, val lon: String,
        val fixStatus: String, val satCount: String, val hdop: String, val alt: String
    )

    private fun parseGga(nmea: String): GgaInfo? {
        try {
            val start = nmea.indexOf("$")
            if (start == -1) return null
            val raw = nmea.substring(start)
            val parts = raw.split(",")
            if (parts.size < 10) return null

            val timeRaw = parts[1]
            val timeStr = if (timeRaw.length >= 6) "${timeRaw.substring(0,2)}:${timeRaw.substring(2,4)}:${timeRaw.substring(4,6)}" else timeRaw

            val latRaw = parts[2]
            val latDir = parts[3]
            val latVal = if (latRaw.isNotBlank()) "%.6f %s".format(nmeaToDegree(latRaw), latDir) else "-"

            val lonRaw = parts[4]
            val lonDir = parts[5]
            val lonVal = if (lonRaw.isNotBlank()) "%.6f %s".format(nmeaToDegree(lonRaw), lonDir) else "-"

            val fixQ = parts[6]
            val fixStr = when(fixQ) {
                "0" -> "无效解"
                "1" -> "单点定位"
                "2" -> "DGPS"
                "4" -> "RTK 固定解 🔥"
                "5" -> "RTK 浮点解 ⚠️"
                else -> "未知 ($fixQ)"
            }

            return GgaInfo(timeStr, latVal, lonVal, fixStr, parts[7], parts[8], parts[9])
        } catch (e: Exception) {
            return null
        }
    }

    private fun nmeaToDegree(raw: String): Double {
        return try {
            val pointIndex = raw.indexOf('.')
            if (pointIndex == -1) return 0.0
            val degreeEndIndex = pointIndex - 2
            val degrees = raw.substring(0, degreeEndIndex).toDouble()
            val minutes = raw.substring(degreeEndIndex).toDouble()
            degrees + minutes / 60.0
        } catch (e: Exception) { 0.0 }
    }

    private fun cleanNmeaMessage(msg: String): String {
        val key = "originalMessage="
        val index = msg.indexOf(key)
        if (index != -1) {
            var clean = msg.substring(index + key.length)
            if (clean.endsWith(")")) clean = clean.substring(0, clean.length - 1)
            if (clean.endsWith("}")) clean = clean.substring(0, clean.length - 1)
            return clean.trim()
        }
        return msg.trim()
    }

    private fun applyUiState(st: RtkState) {
        val isIdle = st is RtkState.Idle
        val isConnected = st is RtkState.Connected
        etHost.isEnabled = isIdle
        etPort.isEnabled = isIdle
        etUser.isEnabled = isIdle
        etPwd.isEnabled = isIdle
        etMount.isEnabled = isIdle
        btnPickBt.isEnabled = isIdle
        btnConnect.isEnabled = isIdle
        btnDisconnect.isEnabled = isConnected || st is RtkState.Error
    }

    private fun appendLog(line: String) {
        if (line.isBlank()) return
        val ts = LocalTime.now().format(tsFmt)
        tvLog.append("[$ts] $line\n")
        if (tvLog.text.length > 3000) tvLog.text = tvLog.text.takeLast(1500)
    }

    private fun saveParams(h: String, p: Int, u: String, pw: String, m: String, bn: String, ma: String) {
        getSharedPreferences(RTK_PREFS, MODE_PRIVATE).edit()
            .putString("host", h).putInt("port", p).putString("user", u)
            .putString("pwd", pw).putString("mount", m).putString("btName", bn)
            .putString("btMac", ma).apply()
    }

    private fun restoreParams() {
        val prefs = getSharedPreferences(RTK_PREFS, MODE_PRIVATE)
        etHost.setText(prefs.getString("host", ""))
        etPort.setText(prefs.getInt("port", 2101).toString())
        etUser.setText(prefs.getString("user", ""))
        etPwd.setText(prefs.getString("pwd", ""))
        etMount.setText(prefs.getString("mount", "AUTO"))
        etBtName.setText(prefs.getString("btName", ""))
        etBtMac.setText(prefs.getString("btMac", ""))
    }

    private fun showMountPointSelector(list: List<MountPointBean>) {
        val items = list.map { "${it.name}   [${it.format}]\n${it.navSystem}" }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("选择挂载点 (${list.size}个)")
            .setItems(items) { _, which ->
                val selected = list[which]
                etMount.setText(selected.name)
                Toast.makeText(this, "已选择: ${selected.name}", Toast.LENGTH_SHORT).show()
            }
            .setPositiveButton("取消", null)
            .show()
    }
}