package com.example.appauto

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit
import androidx.core.view.WindowCompat

class newsoftware : AppCompatActivity() {
    private lateinit var dbHelper: DBManager
    var data_until: String? = null
    var genNum: Int? = null
    var auth: String? = null
    // 下拉有效期对应的天数（成员变量，便于点击时读取）
    private var redeemableDays: Int = 365
    private var langDays: Int = 0
    private var railDays: Int = 0
    private var plotDays: Int = 0
    private var geophysDays: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_newsoftware)

        // 设置状态栏为深色，和 Toolbar 保持一致
        window.statusBarColor = Color.parseColor("#111827")
        // 使用浅色状态栏图标（白色），避免深色背景下看不清
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val data = findViewById<Button>(R.id.data)
        val new_code = findViewById<Button>(R.id.button_new)

        // 下拉有效期选择（UI）与隐藏 RadioGroup（逻辑）同步 —— 新增首项“本次不注册”
        val periodDropdown = findViewById<MaterialAutoCompleteTextView>(R.id.periodDropdown)
        val periodDropdownLang = findViewById<MaterialAutoCompleteTextView>(R.id.periodDropdownLang)
        val periodDropdownRail = findViewById<MaterialAutoCompleteTextView>(R.id.periodDropdownRail)
        val periodDropdownPlot = findViewById<MaterialAutoCompleteTextView>(R.id.periodDropdownPlot)
        val periodDropdownGeophys = findViewById<MaterialAutoCompleteTextView>(R.id.periodDropdownGeophys)

        val periods = listOf("本次不注册", "一天", "三天", "七天", "一个月", "两个月", "半年", "一年", "永久")
        val periodAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, periods)

        periodDropdown.setAdapter(periodAdapter)
        periodDropdownLang.setAdapter(periodAdapter)
        periodDropdownRail.setAdapter(periodAdapter)
        periodDropdownPlot.setAdapter(periodAdapter)
        periodDropdownGeophys.setAdapter(periodAdapter)

        // 4 个新增功能默认：本次不注册（对应值 0）
        periodDropdownLang.setText("本次不注册", false)
        periodDropdownRail.setText("本次不注册", false)
        periodDropdownPlot.setText("本次不注册", false)
        periodDropdownGeophys.setText("本次不注册", false)

        // 统一映射（永久沿用旧逻辑 73000 天）
        fun mapPeriodToDays(label: String): Int = when (label) {
            "本次不注册" -> 0
            "一天" -> 1
            "三天" -> 3
            "七天" -> 7
            "一个月" -> 30
            "两个月" -> 60
            "半年" -> 180
            "一年" -> 365
            "永久" -> 73000
            else -> 365
        }

        // 软件注册有效期下拉初始值
        val initialText = periodDropdown.text?.toString()?.ifBlank { null } ?: "一年"
        periodDropdown.setText(initialText, false)
        redeemableDays = mapPeriodToDays(initialText)

        // 软件注册有效期：仅根据下拉更新 redeemableDays
        periodDropdown.setOnItemClickListener { _, _, position, _ ->
            val selected = periods[position]
            redeemableDays = mapPeriodToDays(selected)
        }

        // 4 个新增功能有效期：记录值（本次不注册=0）

        periodDropdownLang.setOnItemClickListener { _, _, position, _ ->
            langDays = mapPeriodToDays(periods[position])
        }
        periodDropdownRail.setOnItemClickListener { _, _, position, _ ->
            railDays = mapPeriodToDays(periods[position])
        }
        periodDropdownPlot.setOnItemClickListener { _, _, position, _ ->
            plotDays = mapPeriodToDays(periods[position])
        }
        periodDropdownGeophys.setOnItemClickListener { _, _, position, _ ->
            geophysDays = mapPeriodToDays(periods[position])
        }

        dbHelper = DBManager(this)
        dbHelper.openDatabase()
        val db = dbHelper.database

        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(com.google.android.material.R.drawable.ic_arrow_back_black_24)
        toolbar.setNavigationOnClickListener{
            finish()
        }

        data.setOnClickListener {
            showDatePicker()
        }


        new_code.setOnClickListener {
            //获取access_token
            val sql = "select * from luowang where user=\"345\""
            val dataresult = db.rawQuery(sql, null)
            dataresult.moveToFirst()
            val access_token =
                dataresult.getString(dataresult.getColumnIndexOrThrow("accesstoken"))
            auth = "bearer $access_token"
            dataresult.close()

            val genNumText = findViewById<TextView>(R.id.number).text.toString().trim()
            val genNumValue = genNumText.toIntOrNull()
            if (genNumValue == null || genNumValue <= 0) {
                Toast.makeText(this, "请填写有效的注册码生成个数", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            genNum = genNumValue

            if (data_until == null){
                Toast.makeText(this, "请选择日期", Toast.LENGTH_SHORT).show()
            }
            else {
                val remark = findViewById<TextView>(R.id.remark).text.toString()
                run {
                    //请求新的软件注册码
                    val gson = GsonBuilder().serializeNulls().create()
                    val get_code = Retrofit
                        .Builder()
                        .baseUrl("https://cloud.sinognss.com/")
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build()

                    //构建请求参数中的列表元素
                    val FUNC_SOFTWARE = 1
                    val FUNC_LANG = 2
                    val FUNC_RAIL = 3
                    val FUNC_PLOT = 4
                    val FUNC_GEOPHYS = 5

                    val functions = listOf(
                        Function(id = FUNC_SOFTWARE, regDays = redeemableDays),
                        Function(id = FUNC_LANG, regDays = langDays),
                        Function(id = FUNC_RAIL, regDays = railDays),
                        Function(id = FUNC_PLOT, regDays = plotDays),
                        Function(id = FUNC_GEOPHYS, regDays = geophysDays)
                    )
                    //继续构建网络请求
                    val get_code_inter = get_code.create(new_software_create::class.java)
                    get_code_inter.new_software_create(new_software_code_get(
                        true,
                        null,
                        data_until!!,
                        functions,
                        genNum!!,
                        1,
                        false,
                        remark,
                        "暂无归属"
                    ), auth.toString()).enqueue(object : Callback<code_back> {
                        override fun onResponse(p0: Call<code_back>, p1: Response<code_back>) {
                            val result = p1.body()
                            if(result == null){
                                Toast.makeText(applicationContext,p1.message(), Toast.LENGTH_SHORT).show()
                            }
                            else{
                                Toast.makeText(applicationContext,result.message, Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }

                        override fun onFailure(p0: Call<code_back>, p1: Throwable) {
                            Toast.makeText(applicationContext,"创建注册码时连接失败", Toast.LENGTH_SHORT).show()
                        }

                    })
                }
            }
        }
    }
    private fun showDatePicker() {
        val calendarConstraintsBuilder = CalendarConstraints.Builder()
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val twoYearsFromToday = today + TimeUnit.DAYS.toMillis(365 * 2)

        calendarConstraintsBuilder.setValidator(DateValidatorPointForward.from(today)) // 设置日期验证器，只允许选择今天或之后的日期
        calendarConstraintsBuilder.setEnd(twoYearsFromToday) // 设置结束日期为两年后的今天
        calendarConstraintsBuilder.setOpenAt(today) // 设置初始打开时显示的日期为今天

        // 设置日期范围，只展示今天或之后的日期
        calendarConstraintsBuilder.setStart(today)

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTheme(R.style.ThemeOverlay_APPAuto_MaterialDatePicker)
            .setTitleText("选择日期")
            .setSelection(today)
            .setCalendarConstraints(calendarConstraintsBuilder.build())
            .build()

        datePicker.show(supportFragmentManager, "datePicker")

        datePicker.addOnPositiveButtonClickListener { selection ->
            // 将选择的日期转换为 Date 对象
            val selectedDate = Date(selection)
            // 创建 SimpleDateFormat 对象，指定日期格式
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            // 使用 SimpleDateFormat 格式化日期
            val formattedDate = dateFormat.format(selectedDate).toString()
            // 更新 UI 或执行其他操作
            data_until = "$formattedDate 23:59:59"
            findViewById<TextView>(R.id.textView_datauntil).text = data_until
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val button_luowang = item.itemId
        if (button_luowang==R.id.luowang){
            val intent = Intent(this, lwaccount::class.java)
            startActivity(intent)
        }
        return true
    }
}