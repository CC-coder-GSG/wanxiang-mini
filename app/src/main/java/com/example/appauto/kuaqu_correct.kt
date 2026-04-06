package com.example.appauto

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class kuaqu_correct : AppCompatActivity() {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://cloud.sinognss.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val accountOptions = listOf(
        "9" to "随区域",
        "0" to "SinoGNSS",
        "2" to "中国移动",
        "1" to "千寻",
        "0,2" to "SinoGNSS -> 中国移动",
        "0,2,1" to "SinoGNSS -> 中国移动 -> 千寻",
        "0,1" to "SinoGNSS -> 千寻",
        "0,1,2" to "SinoGNSS -> 千寻 -> 中国移动",
        "2,0,1" to "中国移动 -> SinoGNSS -> 千寻",
        "2,0" to "中国移动 -> SinoGNSS",
        "2,1,0" to "中国移动 -> 千寻 -> SinoGNSS",
        "2,1" to "中国移动 -> 千寻",
        "1,2,0" to "千寻 -> 中国移动 -> SinoGNSS",
        "1,2" to "千寻 -> 中国移动",
        "1,0,2" to "千寻 -> SinoGNSS -> 中国移动",
        "1,0" to "千寻 -> SinoGNSS"
    )

    private var sn: String? = null
    private var auth: String = ""
    private var deviceId: Int = 0
    private var currentDuration: Int = 0
    private var selectedAccountCode: String = "9"
    private var selectedAccountLabel: String = "随区域"
    private var ignoreSwitchCallback = false

    private lateinit var textCorrect: TextView
    private lateinit var switchCrossRegion: SwitchCompat
    private lateinit var spinner: Spinner
    private lateinit var buttonBaseStation: Button
    private lateinit var buttonDelete: Button
    private lateinit var textRenewHint: TextView
    private lateinit var numberPickerRenew: NumberPicker
    private lateinit var buttonRenew: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_kuaqu_correct)

        window.statusBarColor = Color.parseColor("#111827")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        textCorrect = findViewById(R.id.textView4)
        switchCrossRegion = findViewById(R.id.switch1)
        spinner = findViewById(R.id.spinner)
        buttonBaseStation = findViewById(R.id.button)
        buttonDelete = findViewById(R.id.button_delete)
        textRenewHint = findViewById(R.id.textRenewHint)
        numberPickerRenew = findViewById(R.id.numberPickerRenew)
        buttonRenew = findViewById(R.id.button_renew)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon?.setTint(Color.WHITE)
        toolbar.overflowIcon?.setTint(Color.WHITE)
        toolbar.setNavigationOnClickListener { finish() }

        deviceId = intent.getIntExtra("id", 0)
        auth = intent.getStringExtra("auth").orEmpty()

        setupSpinner()
        setupActions()
        fetchDetail()
    }

    private fun setupSpinner() {
        ArrayAdapter.createFromResource(
            this,
            R.array.spinner_basestation,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedAccountLabel = parent?.getItemAtPosition(position)?.toString() ?: "随区域"
                selectedAccountCode = accountOptions.firstOrNull { it.second == selectedAccountLabel }?.first ?: "9"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun setupActions() {
        switchCrossRegion.setOnClickListener {
            val targetChecked = switchCrossRegion.isChecked
            val actionText = if (targetChecked) "开启" else "关闭"
            val revertValue = !targetChecked
            confirmDialog(
                title = "确认",
                message = "确认${actionText}跨区？",
                onConfirm = { updateCrossRegion(targetChecked) },
                onCancel = {
                    ignoreSwitchCallback = true
                    switchCrossRegion.isChecked = revertValue
                    ignoreSwitchCallback = false
                }
            )
        }

        buttonBaseStation.setOnClickListener {
            val currentSn = sn
            if (currentSn.isNullOrBlank()) {
                Toast.makeText(this, "未获取到设备 SN", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            confirmDialog(title = "确认", message = "确认更改基站为 $selectedAccountLabel ？", onConfirm = {
                retrofit.create(basestation_change::class.java)
                    .basestation_change(currentSn, selectedAccountCode, auth)
                    .enqueue(object : Callback<direct_more_message> {
                        override fun onResponse(call: Call<direct_more_message>, response: Response<direct_more_message>) {
                            val body = response.body()
                            if (body != null) {
                                Toast.makeText(applicationContext, body.message, Toast.LENGTH_SHORT).show()
                                fetchDetail()
                            } else {
                                Toast.makeText(applicationContext, "请重新登录", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<direct_more_message>, t: Throwable) {
                            Toast.makeText(applicationContext, "修改基站时网络连接错误", Toast.LENGTH_SHORT).show()
                        }
                    })
            })
        }

        buttonDelete.setOnClickListener {
            val currentSn = sn
            if (currentSn.isNullOrBlank()) {
                Toast.makeText(this, "未获取到设备 SN", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            confirmDialog(title = "提示", message = "你确定要删除此设备吗？", onConfirm = {
                retrofit.create(equipment_delete::class.java)
                    .delete_equipment(currentSn, auth)
                    .enqueue(object : Callback<delete_equipment_callback> {
                        override fun onResponse(
                            call: Call<delete_equipment_callback>,
                            response: Response<delete_equipment_callback>
                        ) {
                            val message = response.body()?.message ?: "删除成功"
                            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                            finish()
                        }

                        override fun onFailure(call: Call<delete_equipment_callback>, t: Throwable) {
                            Toast.makeText(applicationContext, "删除设备时连接失败", Toast.LENGTH_SHORT).show()
                        }
                    })
            })
        }

        buttonRenew.setOnClickListener {
            val currentSn = sn
            if (currentSn.isNullOrBlank()) {
                Toast.makeText(this, "未获取到设备 SN", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (currentDuration <= 0) {
                Toast.makeText(this, "暂无可用配套时长", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val renewYears = numberPickerRenew.value
            confirmDialog(title = "确认", message = "确认使用 $renewYears 年配套时长为该设备续期？", onConfirm = {
                retrofit.create(renew_device_by_duration::class.java)
                    .renew_device(currentSn, renewYears, auth)
                    .enqueue(object : Callback<renew_device_callback> {
                        override fun onResponse(
                            call: Call<renew_device_callback>,
                            response: Response<renew_device_callback>
                        ) {
                            val body = response.body()
                            if (body != null) {
                                Toast.makeText(applicationContext, body.message.ifEmpty { "续期成功" }, Toast.LENGTH_SHORT).show()
                                fetchDetail()
                            } else {
                                Toast.makeText(applicationContext, "续期成功", Toast.LENGTH_SHORT).show()
                                fetchDetail()
                            }
                        }

                        override fun onFailure(call: Call<renew_device_callback>, t: Throwable) {
                            Toast.makeText(applicationContext, "续期时网络连接失败", Toast.LENGTH_SHORT).show()
                        }
                    })
            })
        }
    }

    private fun fetchDetail() {
        retrofit.create(idinfo::class.java)
            .get_idinfo(deviceId, auth)
            .enqueue(object : Callback<id_info> {
                override fun onResponse(call: Call<id_info>, response: Response<id_info>) {
                    val detail = response.body()?.data
                    if (detail == null) {
                        Toast.makeText(applicationContext, "请重新登录", Toast.LENGTH_SHORT).show()
                        return
                    }

                    val accountType = detail.accountType ?: "9"
                    val accountLabel = accountOptions.firstOrNull { it.first == accountType }?.second ?: accountType
                    val duration = detail.duration ?: 0
                    val status = detail.status ?: 0
                    val expireTime = detail.expireTime ?: 0L
                    val remainingTime = detail.remainingTime ?: 0
                    val online = detail.online == true
                    val span = detail.isSpan == true
                    val displaySn = detail.sn.orEmpty()

                    sn = displaySn
                    currentDuration = duration
                    selectedAccountCode = accountType
                    selectedAccountLabel = accountLabel

                    ignoreSwitchCallback = true
                    switchCrossRegion.visibility = View.VISIBLE
                    switchCrossRegion.isChecked = span
                    ignoreSwitchCallback = false

                    setSpinnerSelection(accountLabel)
                    bindRenewSection(duration)

                    textCorrect.text = buildString {
                        append("基站策略：").append(accountLabel)
                        append("\n设备类型：").append(detail.deviceType ?: "—")
                        append("\n经销商：").append(detail.salesName ?: "—")
                        append("\n创建人：").append(detail.creatorName ?: "—")
                        append("\n备注：").append(detail.remark ?: "—")
                        append("\n在线状态：").append(if (online) "在线" else "离线")
                        append("\n服务状态：").append(serviceStatusLabel(status))
                        if (expireTime > 0L && status != 1) {
                            append("\n过期时间：").append(formatMillis(expireTime))
                        }
                        append("\n剩余(天)：").append(if (remainingTime > 0) remainingTime else "—")
                        append("\n配套剩余(年)：").append(if (duration > 0) duration else "—")
                        append("\n是否跨区：").append(if (span) "是" else "否")
                        append("\nSN：").append(displaySn)
                    }
                }

                override fun onFailure(call: Call<id_info>, t: Throwable) {
                    Toast.makeText(applicationContext, "连接服务器异常", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateCrossRegion(enabled: Boolean) {
        retrofit.create(direct_more::class.java)
            .direct_more_change(deviceId, enabled, auth)
            .enqueue(object : Callback<direct_more_message> {
                override fun onResponse(call: Call<direct_more_message>, response: Response<direct_more_message>) {
                    val body = response.body()
                    if (body != null) {
                        Toast.makeText(applicationContext, body.message, Toast.LENGTH_SHORT).show()
                        fetchDetail()
                    } else {
                        Toast.makeText(applicationContext, "请重新登录", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<direct_more_message>, t: Throwable) {
                    Toast.makeText(applicationContext, "修改跨区时连接失败", Toast.LENGTH_SHORT).show()
                    ignoreSwitchCallback = true
                    switchCrossRegion.isChecked = !enabled
                    ignoreSwitchCallback = false
                }
            })
    }

    private fun bindRenewSection(duration: Int) {
        if (duration > 0) {
            textRenewHint.text = "使用剩余配套时长为设备续期，剩余 $duration 年可用"
            numberPickerRenew.visibility = View.VISIBLE
            numberPickerRenew.minValue = 1
            numberPickerRenew.maxValue = duration
            numberPickerRenew.value = 1
            buttonRenew.isEnabled = true
            buttonRenew.alpha = 1f
        } else {
            textRenewHint.text = "暂无可用配套时长"
            numberPickerRenew.visibility = View.GONE
            buttonRenew.isEnabled = false
            buttonRenew.alpha = 0.6f
        }
    }

    private fun setSpinnerSelection(label: String) {
        val adapter = spinner.adapter as? ArrayAdapter<String> ?: return
        val position = adapter.getPosition(label)
        if (position != Spinner.INVALID_POSITION) {
            spinner.setSelection(position, false)
        }
    }

    private fun serviceStatusLabel(status: Int): String {
        return when (status) {
            0 -> "服务中"
            1 -> "未激活"
            3 -> "已到期"
            9 -> "已禁用"
            21 -> "即将到期"
            else -> "未知"
        }
    }

    private fun formatMillis(value: Long): String {
        return try {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(value))
        } catch (_: Exception) {
            value.toString()
        }
    }

    private fun confirmDialog(
        title: String,
        message: String,
        onConfirm: () -> Unit,
        onCancel: () -> Unit = {}
    ) {
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("确认") { _, _ -> onConfirm() }
            .setNegativeButton("取消") { _, _ -> onCancel() }
            .create()
        dialog.show()
        styleDialog(dialog)
    }

    private fun styleDialog(dialog: AlertDialog) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
            isAllCaps = false
            setTextColor(Color.WHITE)
            backgroundTintList = ColorStateList.valueOf(Color.parseColor("#111827"))
            (this as? com.google.android.material.button.MaterialButton)?.cornerRadius =
                (12 * resources.displayMetrics.density).toInt()
        }

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
            isAllCaps = false
            setTextColor(Color.parseColor("#111827"))
            backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            (this as? com.google.android.material.button.MaterialButton)?.apply {
                strokeColor = ColorStateList.valueOf(Color.parseColor("#E5E7EB"))
                strokeWidth = (1 * resources.displayMetrics.density).toInt()
                cornerRadius = (12 * resources.displayMetrics.density).toInt()
                elevation = 2f * resources.displayMetrics.density
            }
        }
    }
}
