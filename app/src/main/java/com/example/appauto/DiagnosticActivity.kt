package com.example.appauto

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class DiagnosticActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "account_id"
        const val EXTRA_NAME = "account_name"
    }

    private var accountId = 0
    private var accountName = ""
    private var auth: String? = null

    private val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val sdfDetail = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())

    private val gson by lazy { GsonBuilder().serializeNulls().create() }
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://cloud.sinognss.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    private val api by lazy { retrofit.create(DiagnosticService::class.java) }

    // 时间范围
    private var startCal: Calendar = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, -24) }
    private var endCal: Calendar = Calendar.getInstance()

    // 分页
    private var warnPage = 1
    private var warnTotalPages = 1
    private val warnSize = 20

    // UI refs
    private lateinit var progressBar: ProgressBar
    private lateinit var cardError: MaterialCardView
    private lateinit var tvError: TextView
    private lateinit var cardBasic: MaterialCardView
    private lateinit var cardUsage: MaterialCardView
    private lateinit var cardTimeline: MaterialCardView
    private lateinit var cardStateTable: MaterialCardView
    private lateinit var cardWarn: MaterialCardView

    private lateinit var tvBasicName: TextView
    private lateinit var tvBasicStatus: TextView
    private lateinit var tvBasicExpire: TextView
    private lateinit var tvLatestOnline: TextView
    private lateinit var tvLatestState: TextView
    private lateinit var tvLatestType: TextView
    private lateinit var tvLatestMount: TextView
    private lateinit var tvLatestLogin: TextView
    private lateinit var tvLatestLogout: TextView
    private lateinit var tvLatestGga: TextView
    private lateinit var tvLatestBroadcast: TextView

    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView

    private lateinit var tvStatLogin: TextView
    private lateinit var tvStatGga: TextView
    private lateinit var tvStatInvalidGga: TextView
    private lateinit var tvStatAvgSat: TextView
    private lateinit var tvStatAvgDelay: TextView
    private lateinit var tvStatFixed: TextView
    private lateinit var tvStatFloated: TextView

    private lateinit var flTimeline: FrameLayout
    private lateinit var llStateTable: LinearLayout
    private lateinit var llWarnList: LinearLayout
    private lateinit var tvWarnEmpty: TextView
    private lateinit var llWarnPager: LinearLayout
    private lateinit var tvWarnPage: TextView
    private lateinit var btnWarnPrev: MaterialButton
    private lateinit var btnWarnNext: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_diagnostic)

        window.statusBarColor = Color.parseColor("#111827")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        accountId = intent.getIntExtra(EXTRA_ID, 0)
        accountName = intent.getStringExtra(EXTRA_NAME) ?: ""

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "诊断工具 · $accountName"
        toolbar.navigationIcon = androidx.core.content.ContextCompat.getDrawable(
            this, com.google.android.material.R.drawable.ic_arrow_back_black_24
        )
        toolbar.navigationIcon?.setTint(Color.WHITE)
        toolbar.setNavigationOnClickListener { finish() }

        bindViews()
        setupDatePickers()
        setupRangeButtons()
        setupWarnPager()
        loadAuth()
        loadDiag()
        applyDateRange(-24 * 3600 * 1000L) // 默认最近24h
        loadUsageAndWarn()
    }

    private fun loadAuth() {
        try {
            val dbHelper = DBManager(this)
            dbHelper.openDatabase()
            val db = dbHelper.database
            val c = db.rawQuery("select * from luowang where user=\"345\"", null)
            if (c.moveToFirst()) {
                val token = c.getString(c.getColumnIndexOrThrow("accesstoken"))
                if (!token.isNullOrBlank()) auth = "bearer $token"
            }
            c.close()
            db.close()
            dbHelper.closeDatabase()
        } catch (e: Exception) {
            Log.e("DiagnosticActivity", "loadAuth failed", e)
        }
    }

    private fun bindViews() {
        progressBar = findViewById(R.id.progress_bar)
        cardError = findViewById(R.id.card_error)
        tvError = findViewById(R.id.tv_error)
        cardBasic = findViewById(R.id.card_basic)
        cardUsage = findViewById(R.id.card_usage)
        cardTimeline = findViewById(R.id.card_timeline)
        cardStateTable = findViewById(R.id.card_state_table)
        cardWarn = findViewById(R.id.card_warn)

        tvBasicName = findViewById(R.id.tv_basic_name)
        tvBasicStatus = findViewById(R.id.tv_basic_status)
        tvBasicExpire = findViewById(R.id.tv_basic_expire)
        tvLatestOnline = findViewById(R.id.tv_latest_online)
        tvLatestState = findViewById(R.id.tv_latest_state)
        tvLatestType = findViewById(R.id.tv_latest_type)
        tvLatestMount = findViewById(R.id.tv_latest_mount)
        tvLatestLogin = findViewById(R.id.tv_latest_login)
        tvLatestLogout = findViewById(R.id.tv_latest_logout)
        tvLatestGga = findViewById(R.id.tv_latest_gga)
        tvLatestBroadcast = findViewById(R.id.tv_latest_broadcast)

        tvStartDate = findViewById(R.id.tv_start_date)
        tvEndDate = findViewById(R.id.tv_end_date)

        tvStatLogin = findViewById(R.id.tv_stat_login)
        tvStatGga = findViewById(R.id.tv_stat_gga)
        tvStatInvalidGga = findViewById(R.id.tv_stat_invalid_gga)
        tvStatAvgSat = findViewById(R.id.tv_stat_avg_sat)
        tvStatAvgDelay = findViewById(R.id.tv_stat_avg_delay)
        tvStatFixed = findViewById(R.id.tv_stat_fixed)
        tvStatFloated = findViewById(R.id.tv_stat_floated)

        flTimeline = findViewById(R.id.fl_timeline)
        llStateTable = findViewById(R.id.ll_state_table)
        llWarnList = findViewById(R.id.ll_warn_list)
        tvWarnEmpty = findViewById(R.id.tv_warn_empty)
        llWarnPager = findViewById(R.id.ll_warn_pager)
        tvWarnPage = findViewById(R.id.tv_warn_page)
        btnWarnPrev = findViewById(R.id.btn_warn_prev)
        btnWarnNext = findViewById(R.id.btn_warn_next)

        updateDateLabels()
    }

    private fun setupDatePickers() {
        tvStartDate.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                startCal.set(y, m, d, 0, 0, 0)
                updateDateLabels()
            }, startCal.get(Calendar.YEAR), startCal.get(Calendar.MONTH), startCal.get(Calendar.DAY_OF_MONTH)).show()
        }
        tvEndDate.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                endCal.set(y, m, d, 23, 59, 59)
                updateDateLabels()
            }, endCal.get(Calendar.YEAR), endCal.get(Calendar.MONTH), endCal.get(Calendar.DAY_OF_MONTH)).show()
        }
        findViewById<MaterialButton>(R.id.btn_query).setOnClickListener {
            warnPage = 1
            loadUsageAndWarn()
        }
    }

    private fun setupRangeButtons() {
        findViewById<MaterialButton>(R.id.btn_range_today).setOnClickListener {
            val now = Calendar.getInstance()
            startCal = Calendar.getInstance().apply { set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), 0, 0, 0) }
            endCal = Calendar.getInstance().apply { set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), 23, 59, 59) }
            updateDateLabels()
            warnPage = 1
            loadUsageAndWarn()
        }
        findViewById<MaterialButton>(R.id.btn_range_24h).setOnClickListener {
            applyDateRange(-24 * 3600 * 1000L)
            warnPage = 1
            loadUsageAndWarn()
        }
        findViewById<MaterialButton>(R.id.btn_range_7d).setOnClickListener {
            applyDateRange(-7 * 24 * 3600 * 1000L)
            warnPage = 1
            loadUsageAndWarn()
        }
    }

    private fun applyDateRange(offsetMs: Long) {
        endCal = Calendar.getInstance()
        startCal = Calendar.getInstance().apply { timeInMillis = endCal.timeInMillis + offsetMs }
        updateDateLabels()
    }

    private fun updateDateLabels() {
        tvStartDate.text = sdfDate.format(startCal.time)
        tvEndDate.text = sdfDate.format(endCal.time)
    }

    private fun setupWarnPager() {
        btnWarnPrev.setOnClickListener {
            if (warnPage > 1) { warnPage--; loadWarn() }
        }
        btnWarnNext.setOnClickListener {
            if (warnPage < warnTotalPages) { warnPage++; loadWarn() }
        }
    }

    // ── 加载基本诊断信息 ──────────────────────────────────────

    private fun loadDiag() {
        val a = auth ?: return
        progressBar.visibility = View.VISIBLE
        api.getDiag(DiagRequest(accountId, accountName), a).enqueue(object : Callback<DiagResponse> {
            override fun onResponse(call: Call<DiagResponse>, response: Response<DiagResponse>) {
                progressBar.visibility = View.GONE
                val body = response.body()
                if (!response.isSuccessful || body == null) { showError("加载基本信息失败"); return }
                if (body.code != 0) { showError(body.message); return }
                bindDiag(body.data)
            }
            override fun onFailure(call: Call<DiagResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                showError("网络错误：${t.message}")
            }
        })
    }

    private fun bindDiag(data: DiagData?) {
        cardBasic.visibility = View.VISIBLE
        val basic = data?.basic
        val latest = data?.latest

        tvBasicName.text = basic?.name ?: accountName
        tvBasicStatus.apply {
            val (txt, clr) = when (basic?.status) {
                0 -> Pair("服务中", Color.parseColor("#059669"))
                1 -> Pair("未激活", Color.parseColor("#D97706"))
                2 -> Pair("已过期", Color.parseColor("#DC2626"))
                else -> Pair("—", Color.parseColor("#6B7280"))
            }
            text = txt; setTextColor(clr)
        }
        tvBasicExpire.text = basic?.expiredate?.let { formatMs(it) } ?: "—"

        tvLatestOnline.apply {
            val online = latest?.online
            text = if (online == null) "—" else if (online) "在线" else "离线"
            setTextColor(if (online == true) Color.parseColor("#059669") else Color.parseColor("#6B7280"))
        }
        tvLatestState.apply {
            val (txt, clr) = posStateInfo(latest?.state)
            text = txt; setTextColor(clr)
        }
        tvLatestType.text = when (latest?.type) { 0 -> "Ntrip"; 1 -> "Tcp"; else -> "—" }
        tvLatestMount.text = latest?.mountPoint?.takeIf { it.isNotBlank() } ?: "—"
        tvLatestLogin.text = latest?.lastloadtime?.let { formatMs(it) } ?: "—"
        tvLatestLogout.text = latest?.lastunloadtime?.let { formatMs(it) } ?: "—"
        tvLatestGga.text = latest?.ggaTime?.let { formatMs(it) } ?: "—"
        tvLatestBroadcast.text = latest?.boradCastTime?.let { formatMs(it) } ?: "—"
    }

    // ── 加载使用详情 + 告警记录 ──────────────────────────────

    private fun loadUsageAndWarn() {
        val a = auth ?: return
        val st = startCal.timeInMillis
        val et = endCal.timeInMillis
        progressBar.visibility = View.VISIBLE

        // 使用详情
        api.getUsageDetail(UsageDetailRequest(accountId, accountName, st, et), a).enqueue(object : Callback<UsageDetailResponse> {
            override fun onResponse(call: Call<UsageDetailResponse>, response: Response<UsageDetailResponse>) {
                val body = response.body()
                if (response.isSuccessful && body != null && body.code == 0) {
                    bindUsage(body.data)
                }
                progressBar.visibility = View.GONE
            }
            override fun onFailure(call: Call<UsageDetailResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Log.e("DiagnosticActivity", "usageDetail failed", t)
            }
        })

        // 告警记录
        loadWarn()
    }

    private fun loadWarn() {
        val a = auth ?: return
        val st = startCal.timeInMillis
        val et = endCal.timeInMillis

        api.getWarnPage(WarnPageRequest(accountId, accountName, st, et, warnPage, warnSize), a).enqueue(object : Callback<WarnPageResponse> {
            override fun onResponse(call: Call<WarnPageResponse>, response: Response<WarnPageResponse>) {
                val body = response.body()
                if (response.isSuccessful && body != null && body.code == 0) {
                    bindWarn(body.data)
                }
            }
            override fun onFailure(call: Call<WarnPageResponse>, t: Throwable) {
                Log.e("DiagnosticActivity", "warnPage failed", t)
            }
        })
    }

    private fun bindUsage(data: UsageDetailData?) {
        val info = data?.useInfo
        if (info != null) {
            cardUsage.visibility = View.VISIBLE
            tvStatLogin.text = info.logIn?.toString() ?: "—"
            tvStatGga.text = info.ggaCount?.toString() ?: "—"
            tvStatInvalidGga.text = info.invalidGga?.toString() ?: "—"
            tvStatAvgSat.text = info.avgSatNum?.let { String.format("%.1f", it) } ?: "—"
            tvStatAvgDelay.text = info.avgDelay?.let { String.format("%.1f", it) } ?: "—"
            tvStatFixed.text = info.fixed ?: "—"
            tvStatFloated.text = info.floated ?: "—"
        } else {
            cardUsage.visibility = View.GONE
        }

        // 时间轴
        val segments = data?.statusList
        if (!segments.isNullOrEmpty()) {
            cardTimeline.visibility = View.VISIBLE
            buildTimeline(segments)
        } else {
            cardTimeline.visibility = View.GONE
        }

        // GGA 表格
        val states = data?.stateList
        if (!states.isNullOrEmpty()) {
            cardStateTable.visibility = View.VISIBLE
            buildStateTable(states)
        } else {
            cardStateTable.visibility = View.GONE
        }
    }

    private fun buildTimeline(segments: List<StatusSegment>) {
        flTimeline.removeAllViews()
        val totalStart = segments.minOfOrNull { it.startTime ?: 0L } ?: return
        val totalEnd = segments.maxOfOrNull { it.endTime ?: 0L } ?: return
        val totalDuration = (totalEnd - totalStart).toFloat().coerceAtLeast(1f)

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }

        segments.forEach { seg ->
            val start = seg.startTime ?: totalStart
            val end = seg.endTime ?: totalEnd
            val weight = ((end - start).toFloat() / totalDuration).coerceAtLeast(0.001f)
            val view = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weight)
                setBackgroundColor(if (seg.online == true) Color.parseColor("#10B981") else Color.parseColor("#D1D5DB"))
            }
            bar.addView(view)
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            background = androidx.core.content.ContextCompat.getDrawable(this@DiagnosticActivity, R.drawable.bg_date_picker)
            clipToOutline = true
        }
        container.addView(bar)
        flTimeline.addView(container)
    }

    private fun buildStateTable(states: List<StatePoint>) {
        llStateTable.removeAllViews()
        val colWidths = intArrayOf(dp(150), dp(70), dp(60), dp(90))
        val headers = arrayOf("时间", "GGA状态", "卫星数", "延迟(ms)")

        // 表头
        val headerRow = tableRow(true)
        headers.forEachIndexed { i, h ->
            headerRow.addView(tableCell(h, colWidths[i], true))
        }
        headerRow.setBackgroundColor(Color.parseColor("#F3F4F6"))
        llStateTable.addView(headerRow)

        // 数据行（最多100条）
        states.take(100).forEachIndexed { idx, s ->
            val row = tableRow(false)
            row.addView(tableCell(s.createTime?.let { formatMsShort(it) } ?: "—", colWidths[0], false))
            val (stateTxt, stateClr) = posStateInfo(s.state)
            val stateCell = tableCell(stateTxt, colWidths[1], false)
            stateCell.setTextColor(stateClr)
            row.addView(stateCell)
            row.addView(tableCell(s.satNum?.toString() ?: "—", colWidths[2], false))
            row.addView(tableCell(s.delay?.let { String.format("%.0f", it) } ?: "—", colWidths[3], false))
            if (idx % 2 == 1) row.setBackgroundColor(Color.parseColor("#F9FAFB"))
            llStateTable.addView(row)
        }
    }

    private fun tableRow(isHeader: Boolean): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            val dp6 = dp(6)
            setPadding(0, dp6, 0, dp6)
        }
    }

    private fun tableCell(text: String, widthDp: Int, isHeader: Boolean): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 12f
            setTextColor(if (isHeader) Color.parseColor("#6B7280") else Color.parseColor("#111827"))
            if (isHeader) typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(widthDp, LinearLayout.LayoutParams.WRAP_CONTENT)
            setPadding(dp(4), 0, dp(4), 0)
        }
    }

    private fun bindWarn(data: WarnPageData?) {
        llWarnList.removeAllViews()
        val records = data?.records
        val total = data?.total ?: 0
        warnTotalPages = data?.pages ?: 1

        if (records.isNullOrEmpty()) {
            tvWarnEmpty.visibility = View.VISIBLE
            llWarnPager.visibility = View.GONE
        } else {
            tvWarnEmpty.visibility = View.GONE
            records.forEach { record -> llWarnList.addView(buildWarnRow(record)) }

            val totalPages = data?.pages ?: 1
            if (totalPages > 1) {
                llWarnPager.visibility = View.VISIBLE
                tvWarnPage.text = "$warnPage / $totalPages"
                btnWarnPrev.isEnabled = warnPage > 1
                btnWarnNext.isEnabled = warnPage < totalPages
            } else {
                llWarnPager.visibility = View.GONE
            }
        }
    }

    private fun buildWarnRow(record: WarnRecord): View {
        val eventName = record.event ?: record.content ?: "未知事件"
        val isOnline = eventName.contains("上线")
        val isOffline = eventName.contains("下线")
        val isWarning = eventName.contains("认证") || eventName.contains("踢") || eventName.contains("GGA") || eventName.contains("无效")

        val iconColor = when {
            isOnline -> Color.parseColor("#059669")
            isOffline -> Color.parseColor("#6B7280")
            isWarning -> Color.parseColor("#D97706")
            else -> Color.parseColor("#2563EB")
        }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            val dp8 = dp(8)
            val dp12 = dp(12)
            setPadding(0, dp8, 0, dp8)
            gravity = Gravity.TOP
        }

        // 色块
        val dot = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(8), dp(8)).apply {
                setMargins(0, dp(4), dp(10), 0)
                gravity = Gravity.TOP
            }
            setBackgroundColor(iconColor)
        }
        row.addView(dot)

        // 文字内容
        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        // 事件名 + 时间（同一行）
        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val tvEvent = TextView(this).apply {
            text = eventName
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(iconColor)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        topRow.addView(tvEvent)

        val tvTime = TextView(this).apply {
            text = record.createTime?.let { formatMsShort(it) } ?: ""
            textSize = 11f
            setTextColor(Color.parseColor("#9CA3AF"))
        }
        topRow.addView(tvTime)
        contentLayout.addView(topRow)

        // IP:port
        val ipStr = buildString {
            record.ip?.let { append(it) }
            record.port?.let { append(":$it") }
        }
        if (ipStr.isNotBlank()) {
            contentLayout.addView(TextView(this).apply {
                text = ipStr
                textSize = 12f
                setTextColor(Color.parseColor("#6B7280"))
            })
        }

        // 挂载点
        record.mountPoint?.takeIf { it.isNotBlank() }?.let {
            contentLayout.addView(TextView(this).apply {
                text = "挂载点: $it"
                textSize = 12f
                setTextColor(Color.parseColor("#6B7280"))
            })
        }

        row.addView(contentLayout)

        // 底部分割线（除最后一条）
        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        wrapper.addView(row)
        wrapper.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
            setBackgroundColor(Color.parseColor("#F3F4F6"))
        })
        return wrapper
    }

    // ── 工具方法 ──────────────────────────────────────────────

    private fun showError(msg: String) {
        cardError.visibility = View.VISIBLE
        tvError.text = msg
    }

    private fun formatMs(ms: Long): String = sdfDetail.format(Date(ms))
    private fun formatMsShort(ms: Long): String = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(ms))

    private fun posStateInfo(state: Int?): Pair<String, Int> = when (state) {
        0 -> Pair("未定位", Color.parseColor("#6B7280"))
        1 -> Pair("单点", Color.parseColor("#6B7280"))
        2 -> Pair("伪距差分", Color.parseColor("#D97706"))
        3 -> Pair("PPS", Color.parseColor("#2563EB"))
        4 -> Pair("固定解", Color.parseColor("#059669"))
        5 -> Pair("浮点解", Color.parseColor("#2563EB"))
        6 -> Pair("惯性导航", Color.parseColor("#7C3AED"))
        null -> Pair("—", Color.parseColor("#6B7280"))
        else -> Pair("状态$state", Color.parseColor("#6B7280"))
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
