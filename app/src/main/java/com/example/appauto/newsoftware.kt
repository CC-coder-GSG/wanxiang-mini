package com.example.appauto

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class newsoftware : AppCompatActivity() {
    private lateinit var dbHelper: DBManager
    var data_until: String? = null
    var genNum: Int? = null
    var redeemableDays: Int = 365
    var auth: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_newsoftware)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val data = findViewById<Button>(R.id.data)
        val radio = findViewById<RadioGroup>(R.id.radioGroup)
        val new_code = findViewById<Button>(R.id.button_new)
        val internation = findViewById<SwitchCompat>(R.id.switch2)

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

        radio.setOnCheckedChangeListener{ group, checkedId ->
            when(checkedId){
                R.id.radioButton1 -> {
                    redeemableDays = 1
                }
                R.id.radioButton3 -> {
                    redeemableDays = 3
                }
                R.id.radioButton7 -> {
                    redeemableDays = 7
                }
                R.id.radioButton30 -> {
                    redeemableDays = 30
                }
                R.id.radioButton60 -> {
                    redeemableDays = 60
                }
                R.id.radioButton180 -> {
                    redeemableDays = 180
                }
                R.id.radioButton365 -> {
                    redeemableDays = 365
                }
                R.id.radioButton_forever -> {
                    redeemableDays = 73000
                }
            }
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

            genNum = findViewById<TextView>(R.id.number).text.toString().toInt()

            //国际码判断
            var international = false
            if(internation.isChecked){international = true}

            if (data_until == null){
                Toast.makeText(this, "请选择日期", Toast.LENGTH_SHORT).show()
            }
            else {
                val remark = findViewById<TextView>(R.id.remark).text.toString()
                if (genNum == null) {
                    Toast.makeText(this, "请填写注册码生成个数", Toast.LENGTH_SHORT).show()
                } else {
                    //请求新的软件注册码
                    val gson = GsonBuilder().serializeNulls().create()
                    val get_code = Retrofit
                        .Builder()
                        .baseUrl("https://cloud.sinognss.com/")
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build()
                    val get_code_inter = get_code.create(software_code_interface::class.java)
                    get_code_inter.software_code_fx(software_code(true, null,
                        data_until!!, genNum!!, international, 1, false, redeemableDays, remark, "暂无归属"), auth.toString()).enqueue(object : Callback<code_back> {
                        override fun onResponse(p0: Call<code_back>, p1: Response<code_back>) {
                            val result = p1.body()
                            if(result == null){
                                Toast.makeText(applicationContext,"请重新登录", Toast.LENGTH_SHORT).show()
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