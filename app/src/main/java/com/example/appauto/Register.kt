package com.example.appauto

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.concurrent.TimeUnit

class Register : AppCompatActivity() {
    private lateinit var dbHelper: DBManager
    private var SN_ALL : String? = null
    private var tempRegDeadline_ALL : String? = null
    private var auth : String? = null
    private var permanentRegCodeHave = false
    private var offHostNetTime : String? = null
    private var offNtripTime : String? = null
    private var isHostNet : Boolean? = false
    private var isNtrip : Boolean? = false
    private var lastUpdateTime : String? = null
    private var trialDay : Int? = 0
    private var todayCanTrial : Boolean? = false
    private var tempRegCodeExpireTime : String? = null
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val text = findViewById<TextView>(R.id.editText_register)
        val text1 = findViewById<TextView>(R.id.textView)
        val text3 = findViewById<TextView>(R.id.textView3)
        val button_register_system = findViewById<Button>(R.id.button_register_system)
        val button_register_information = findViewById<Button>(R.id.button_register_information)
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        var entend_num = 30

        dbHelper = DBManager(this)
        dbHelper.openDatabase()
        val db = dbHelper.database

        button_register_system.visibility = Button.GONE

        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(com.google.android.material.R.drawable.ic_arrow_back_black_24)
        toolbar.setNavigationOnClickListener{
            finish()
        }
        val button_register = findViewById<Button>(R.id.button_register)
        // 初始化 Spinner
        val spinner = findViewById<Spinner>(R.id.spinner)
        // 获取整数数组资源
        val spinnerValues = listOf(30,1,2,3,4,5,6,7,15)
        // 创建 ArrayAdapter 并设置给 Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerValues)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        // 定制延长注册事件的天数
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // 在这里获取选中的整数
                entend_num = spinnerValues[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 当没有选中任何项时，这里不做任何处理
            }
        }
        button_register.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            //设置对话框标题
            builder.setTitle("提示")
            //设置对话框消息
            builder.setMessage("确定要生成注册码吗？")
            //设置“确定”按钮
            builder.setPositiveButton("确定") { dialog, which ->
                //定制sql语句
                val SN = text.text

                if (SN.isNullOrBlank()) {
                    Toast.makeText(this, "请输入SN号", Toast.LENGTH_SHORT).show()
                } else {
                    val gson = GsonBuilder().serializeNulls().create()
                    val retrofit = Retrofit.Builder().baseUrl("https://cloud.sinognss.com/")
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build()

                    //获取access_token
                    val sql = "select * from luowang where user=\"345\""
                    val dataresult = db.rawQuery(sql, null)
                    //SN信息插入数据库
                    //操作时间写入数据库
                    //系统时间距离截止日期超过 N 天
                    val nowtime_1_h = LocalDateTime.now()
                    val formatter_utc_h =
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    val nowtimeString = nowtime_1_h.format(formatter_utc_h)
                    val sql_operatetime =
                        "INSERT INTO historyregister (`SN`,operatetime) VALUES ('$SN','$nowtimeString')"
                    db.execSQL(sql_operatetime)

                    if (dataresult.moveToFirst()) {
                        val access_token =
                            dataresult.getString(dataresult.getColumnIndexOrThrow("accesstoken"))
                        auth = "bearer $access_token"
                        dataresult.close()
                        //db.close()
                        //dbHelper.closeDatabase()

                        val Registerinfo = retrofit.create(register_sendinto::class.java)
                        val orderItems = OrderItem(false, "create_time")
                        val list_orderItems = mutableListOf(orderItems)
                        val register_send1 =
                            register_send(SN.toString(), list_orderItems, "", null, 1, 10)

                        //text1.text = gsonRece

                        Registerinfo.register_info(register_send1, auth.toString())
                            .enqueue(object : Callback<registerinfo> {
                                @RequiresApi(Build.VERSION_CODES.O)
                                override fun onResponse(
                                    call: Call<registerinfo>,
                                    response: Response<registerinfo>
                                ) {
                                    if (response.isSuccessful) {
                                        val registerInfo = response.body()
                                        if (registerInfo != null) {
                                            val pageModel = registerInfo.data.pageModel
                                            if (pageModel != null) {
                                                val records = pageModel.records
                                                if (records.isNotEmpty()) {
                                                    val record_num = records.size
                                                    if (!(record_num > 1)) {
                                                        // 现在你可以遍历 records 列表并读取其中的值
                                                        for (record in records) {
                                                            // 读取 record 对象的属性
                                                            val sn = record.sn
                                                            val companyName = record.companyName
                                                            val createTime = record.createTime
                                                            val productionType =
                                                                record.productionType
                                                            val tempRegDeadline =
                                                                record.tempRegDeadline
                                                            if (tempRegDeadline != null) {
                                                                // 将 tempRegDeadline 字符串转换为日期格式
                                                                val dateFormat =
                                                                    SimpleDateFormat("yyyy-MM-dd")
                                                                val deadlineDate: Date? =
                                                                    dateFormat.parse(tempRegDeadline)
                                                                // 获取当前系统时间
                                                                val currentTime = Date()
                                                                // 确保 deadlineDate 不为 null，否则使用当前时间
                                                                val deadlineTime =
                                                                    deadlineDate?.getTime()
                                                                        ?: currentTime.getTime()
                                                                // 计算截止日期与当前日期的天数差
                                                                val diffInDays =
                                                                    TimeUnit.DAYS.convert(
                                                                        deadlineTime - currentTime.getTime(),
                                                                        TimeUnit.MILLISECONDS
                                                                    )
                                                                // 比较 deadlineDate 和 currentTime
                                                                if (diffInDays > entend_num + 1) {
                                                                    // 系统时间距离截止日期超过 N 天
                                                                    val nowtime_1 =
                                                                        LocalDateTime.now()
                                                                    val code_time =
                                                                        nowtime_1.plusDays(
                                                                            entend_num.toLong()
                                                                        )
                                                                    val formatter_utc =
                                                                        DateTimeFormatter.ofPattern(
                                                                            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
                                                                        )
                                                                    val String_codetime =
                                                                        code_time.format(
                                                                            formatter_utc
                                                                        )
                                                                    //获取注册码
                                                                    val retrofit_get_domestic_code =
                                                                        Retrofit.Builder()
                                                                            .baseUrl("https://cloud.sinognss.com/")
                                                                            .addConverterFactory(
                                                                                GsonConverterFactory.create(
                                                                                    gson
                                                                                )
                                                                            ).build()
                                                                    val domesticcode =
                                                                        retrofit_get_domestic_code.create(
                                                                            register_domestic::class.java
                                                                        )
                                                                    domesticcode.register_domestic_send(
                                                                        domestcode(
                                                                            sn,
                                                                            String_codetime,
                                                                            "DOMESTIC"
                                                                        ),
                                                                        auth.toString()
                                                                    ).enqueue(object :
                                                                        Callback<domesticinfo> {
                                                                        override fun onResponse(
                                                                            p0: Call<domesticinfo>,
                                                                            p1: Response<domesticinfo>
                                                                        ) {
                                                                            val domestic_message =
                                                                                p1.body()
                                                                            if (domestic_message != null) {
                                                                                Toast.makeText(
                                                                                    applicationContext,
                                                                                    domestic_message.message,
                                                                                    Toast.LENGTH_SHORT
                                                                                ).show()
                                                                                //注册成功时间信息写入数据库
                                                                                val sql_register =
                                                                                    "UPDATE historyregister SET registertime='$String_codetime' WHERE operatetime='$nowtimeString'"
                                                                                db.execSQL(
                                                                                    sql_register
                                                                                )

                                                                                //下发注册码之后获取信息
                                                                                val retrofit_getfinal_info =
                                                                                    Retrofit.Builder()
                                                                                        .baseUrl("https://cloud.sinognss.com/")
                                                                                        .addConverterFactory(
                                                                                            GsonConverterFactory.create(
                                                                                                gson
                                                                                            )
                                                                                        ).build()
                                                                                val finalinfo =
                                                                                    retrofit_getfinal_info.create(
                                                                                        finalreceiverinfo::class.java
                                                                                    )
                                                                                finalinfo.get_final_receiverinfo(
                                                                                    sn,
                                                                                    auth.toString()
                                                                                ).enqueue(object :
                                                                                    Callback<finalinfo> {
                                                                                    override fun onResponse(
                                                                                        p0: Call<finalinfo>,
                                                                                        p1: Response<finalinfo>
                                                                                    ) {
                                                                                        val info =
                                                                                            p1.body()
                                                                                        if (p1 != null) {
                                                                                            val campanyname =
                                                                                                info?.data?.devRegInfo?.companyName
                                                                                            val createtime =
                                                                                                info?.data?.devRegInfo?.createTime
                                                                                            val isHostNet =
                                                                                                info?.data?.devRegInfo?.isHostNet
                                                                                            val isNtrip =
                                                                                                info?.data?.devRegInfo?.isNtrip
                                                                                            val productionType_final =
                                                                                                info?.data?.devRegInfo?.productionType
                                                                                            val sn_final =
                                                                                                info?.data?.devRegInfo?.sn
                                                                                            val tempRegCodeExpireTime =
                                                                                                info?.data?.devRegInfo?.tempRegCodeExpireTime
                                                                                            val tempRegCodeHave =
                                                                                                info?.data?.devRegInfo?.tempRegCodeHave
                                                                                            val tempRegDeadline_final =
                                                                                                info?.data?.devRegInfo?.tempRegDeadline
                                                                                            text3.text =
                                                                                                "SN: $sn_final,\n 经销商: $campanyname,\n 创建时间: $createtime,\n 型号：$productionType_final,\n 注册截止时间：$tempRegDeadline_final,\n 注册码过期时间：$tempRegCodeExpireTime,\n 永久码：$tempRegCodeHave,\n 主机网络：$isHostNet,\n 手簿网络：$isNtrip"
                                                                                        }
                                                                                    }

                                                                                    override fun onFailure(
                                                                                        p0: Call<finalinfo>,
                                                                                        p1: Throwable
                                                                                    ) {
                                                                                        Toast.makeText(
                                                                                            applicationContext,
                                                                                            "获取信息时连接出现问题",
                                                                                            Toast.LENGTH_SHORT
                                                                                        ).show()
                                                                                    }

                                                                                })

                                                                            }
                                                                        }

                                                                        override fun onFailure(
                                                                            p0: Call<domesticinfo>,
                                                                            p1: Throwable
                                                                        ) {
                                                                            Toast.makeText(
                                                                                applicationContext,
                                                                                "注册码下发时连接异常",
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                        }

                                                                    })


                                                                } else {
                                                                    // 系统时间距离截止日期未超过 N 天
                                                                    val nowtime = LocalDate.now()
                                                                    val nowtime_1 =
                                                                        LocalDateTime.now()
                                                                    val extend_time =
                                                                        nowtime.plusDays((entend_num + 1).toLong())
                                                                    val code_time =
                                                                        nowtime_1.plusDays(
                                                                            entend_num.toLong()
                                                                        )
                                                                    // 定义日期格式
                                                                    val formatter =
                                                                        DateTimeFormatter.ofPattern(
                                                                            "yyyy-MM-dd"
                                                                        )
                                                                    val formatter_utc =
                                                                        DateTimeFormatter.ofPattern(
                                                                            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
                                                                        )
                                                                    // 将日期格式化为字符串
                                                                    val String_extendtime =
                                                                        extend_time.format(formatter)
                                                                    val String_codetime =
                                                                        code_time.format(
                                                                            formatter_utc
                                                                        )
                                                                    // 延长注册时间
                                                                    val retrofit_extend_time =
                                                                        Retrofit.Builder()
                                                                            .baseUrl("https://cloud.sinognss.com/")
                                                                            .addConverterFactory(
                                                                                GsonConverterFactory.create(
                                                                                    gson
                                                                                )
                                                                            ).build()
                                                                    val extend_register_time =
                                                                        retrofit_extend_time.create(
                                                                            register_time_extend_put::class.java
                                                                        )
                                                                    extend_register_time.register_time(
                                                                        register_time_extend(
                                                                            false,
                                                                            listOf(sn),
                                                                            String_extendtime
                                                                        ),
                                                                        auth.toString()
                                                                    ).enqueue(object :
                                                                        Callback<extend_time_notice> {
                                                                        override fun onResponse(
                                                                            call: Call<extend_time_notice>,
                                                                            response: Response<extend_time_notice>
                                                                        ) {
                                                                            if (response.isSuccessful) {
                                                                                val extend_message =
                                                                                    response.body()
                                                                                if (extend_message != null) {
                                                                                    //Toast.makeText(applicationContext, extend_message.message, Toast.LENGTH_SHORT).show()
                                                                                    //延长注册时间写入数据库
                                                                                    val sql_extendtime =
                                                                                        "UPDATE historyregister SET extendtime='$String_codetime' WHERE operatetime='$nowtimeString'"
                                                                                    db.execSQL(
                                                                                        sql_extendtime
                                                                                    )
                                                                                    //获取注册码
                                                                                    val retrofit_get_domestic_code =
                                                                                        Retrofit.Builder()
                                                                                            .baseUrl(
                                                                                                "https://cloud.sinognss.com/"
                                                                                            )
                                                                                            .addConverterFactory(
                                                                                                GsonConverterFactory.create(
                                                                                                    gson
                                                                                                )
                                                                                            )
                                                                                            .build()
                                                                                    val domesticcode =
                                                                                        retrofit_get_domestic_code.create(
                                                                                            register_domestic::class.java
                                                                                        )
                                                                                    domesticcode.register_domestic_send(
                                                                                        domestcode(
                                                                                            sn,
                                                                                            String_codetime,
                                                                                            "DOMESTIC"
                                                                                        ),
                                                                                        auth.toString()
                                                                                    )
                                                                                        .enqueue(
                                                                                            object :
                                                                                                Callback<domesticinfo> {
                                                                                                override fun onResponse(
                                                                                                    p0: Call<domesticinfo>,
                                                                                                    p1: Response<domesticinfo>
                                                                                                ) {
                                                                                                    val domestic_message =
                                                                                                        p1.body()
                                                                                                    if (domestic_message != null) {
                                                                                                        Toast.makeText(
                                                                                                            applicationContext,
                                                                                                            domestic_message.message,
                                                                                                            Toast.LENGTH_SHORT
                                                                                                        )
                                                                                                            .show()
                                                                                                        //注册成功时间信息写入数据库
                                                                                                        val sql_register =
                                                                                                            "UPDATE historyregister SET registertime='$String_codetime' WHERE operatetime='$nowtimeString'"
                                                                                                        db.execSQL(
                                                                                                            sql_register
                                                                                                        )
                                                                                                        //下发注册码之后获取信息
                                                                                                        val retrofit_getfinal_info =
                                                                                                            Retrofit.Builder()
                                                                                                                .baseUrl(
                                                                                                                    "https://cloud.sinognss.com/"
                                                                                                                )
                                                                                                                .addConverterFactory(
                                                                                                                    GsonConverterFactory.create(
                                                                                                                        gson
                                                                                                                    )
                                                                                                                )
                                                                                                                .build()
                                                                                                        val finalinfo =
                                                                                                            retrofit_getfinal_info.create(
                                                                                                                finalreceiverinfo::class.java
                                                                                                            )
                                                                                                        finalinfo.get_final_receiverinfo(
                                                                                                            sn,
                                                                                                            auth.toString()
                                                                                                        )
                                                                                                            .enqueue(
                                                                                                                object :
                                                                                                                    Callback<finalinfo> {
                                                                                                                    override fun onResponse(
                                                                                                                        p0: Call<finalinfo>,
                                                                                                                        p1: Response<finalinfo>
                                                                                                                    ) {
                                                                                                                        val info =
                                                                                                                            p1.body()
                                                                                                                        if (p1 != null) {
                                                                                                                            val campanyname =
                                                                                                                                info?.data?.devRegInfo?.companyName
                                                                                                                            val createtime =
                                                                                                                                info?.data?.devRegInfo?.createTime
                                                                                                                            val isHostNet =
                                                                                                                                info?.data?.devRegInfo?.isHostNet
                                                                                                                            val isNtrip =
                                                                                                                                info?.data?.devRegInfo?.isNtrip
                                                                                                                            val productionType_final =
                                                                                                                                info?.data?.devRegInfo?.productionType
                                                                                                                            val sn_final =
                                                                                                                                info?.data?.devRegInfo?.sn
                                                                                                                            val tempRegCodeExpireTime =
                                                                                                                                info?.data?.devRegInfo?.tempRegCodeExpireTime
                                                                                                                            val tempRegCodeHave =
                                                                                                                                info?.data?.devRegInfo?.tempRegCodeHave
                                                                                                                            val tempRegDeadline_final =
                                                                                                                                info?.data?.devRegInfo?.tempRegDeadline
                                                                                                                            text3.text =
                                                                                                                                "SN: $sn_final,\n 经销商: $campanyname,\n 创建时间: $createtime,\n 型号：$productionType_final,\n 注册截止时间：$tempRegDeadline_final,\n 注册码过期时间：$tempRegCodeExpireTime,\n 永久码：$tempRegCodeHave,\n 主机网络：$isHostNet,\n 手簿网络：$isNtrip"
                                                                                                                        }
                                                                                                                    }

                                                                                                                    override fun onFailure(
                                                                                                                        p0: Call<finalinfo>,
                                                                                                                        p1: Throwable
                                                                                                                    ) {
                                                                                                                        Toast.makeText(
                                                                                                                            applicationContext,
                                                                                                                            "获取信息时连接出现问题",
                                                                                                                            Toast.LENGTH_SHORT
                                                                                                                        )
                                                                                                                            .show()
                                                                                                                    }

                                                                                                                })
                                                                                                    }
                                                                                                }

                                                                                                override fun onFailure(
                                                                                                    p0: Call<domesticinfo>,
                                                                                                    p1: Throwable
                                                                                                ) {
                                                                                                    Toast.makeText(
                                                                                                        applicationContext,
                                                                                                        "注册码下发时连接异常",
                                                                                                        Toast.LENGTH_SHORT
                                                                                                    )
                                                                                                        .show()
                                                                                                }

                                                                                            })
                                                                                } else {
                                                                                    Toast.makeText(
                                                                                        applicationContext,
                                                                                        "延期异常",
                                                                                        Toast.LENGTH_SHORT
                                                                                    ).show()
                                                                                }
                                                                            }
                                                                        }

                                                                        override fun onFailure(
                                                                            p0: Call<extend_time_notice>,
                                                                            p1: Throwable
                                                                        ) {
                                                                            Toast.makeText(
                                                                                applicationContext,
                                                                                "注册延期功能连接失败",
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                        }
                                                                    })
                                                                }
                                                            } else {
                                                                Toast.makeText(
                                                                    applicationContext,
                                                                    "注册截至日期不存在，请先出库",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                        }

                                                    } else {
                                                        Toast.makeText(
                                                            applicationContext,
                                                            "检测到多个设备，为避免误操作，请检查SN号",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        text3.text = ""
                                                    }
                                                } else {
                                                    Toast.makeText(
                                                        applicationContext,
                                                        "未查询到该SN号",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    text3.text = ""
                                                }
                                            } else {
                                                Toast.makeText(
                                                    applicationContext,
                                                    "PageModel is null",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        } else {
                                            Toast.makeText(
                                                applicationContext,
                                                "Response body is null",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        Toast.makeText(
                                            applicationContext,
                                            "Response not successful: ${response.code()}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                override fun onFailure(p0: Call<registerinfo>, p1: Throwable) {
                                    p1.printStackTrace()
                                    Toast.makeText(
                                        applicationContext,
                                        "连接失败",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            })
                    } else {
                        Toast.makeText(applicationContext, "请先登录罗网", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                // 隐藏虚拟键盘
                inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
            }
            builder.setNegativeButton("取消"){ dialog, which ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
        }
        //注册信息按钮点击事件
        button_register_information.setOnClickListener {
            dbHelper = DBManager(this)
            dbHelper.openDatabase()
            val db = dbHelper.database
            //获取access_token
            val sql = "select * from luowang where user=\"345\""
            val dataresult = db.rawQuery(sql, null)
            dataresult.moveToFirst()
            val access_token =
                dataresult.getString(dataresult.getColumnIndexOrThrow("accesstoken"))
            auth = "bearer $access_token"
            dataresult.close()

            val gson = GsonBuilder().serializeNulls().create()
            val retrofit = Retrofit.Builder().baseUrl("https://cloud.sinognss.com/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            val Registerinfo = retrofit.create(register_sendinto::class.java)
            //参数准备
            val SN = text.text
            val orderItems = OrderItem(false, "create_time")
            val list_orderItems = mutableListOf(orderItems)
            //网络请求
            val register_send1 = register_send(SN.toString(), list_orderItems, "", null, 1, 10)
            Registerinfo.register_info(register_send1,auth.toString()).enqueue(object : Callback<registerinfo>{
                override fun onResponse(p0: Call<registerinfo>, p1: Response<registerinfo>) {
                    if(p1.isSuccessful){
                        val registerInfo = p1.body()
                        val pageModel = registerInfo?.data?.pageModel
                        val records = pageModel?.records
                        if (records != null) {
                            if (records.isNotEmpty()){
                                val record_num = records.size
                                if (!(record_num > 1)){
                                    button_register_system.visibility = Button.VISIBLE
                                    for (record in records) {
                                        // 读取 record 对象的属性
                                        val sn = record.sn
                                        val companyName = record.companyName
                                        val createTime = record.createTime
                                        isHostNet = record.isHostNet
                                        isNtrip = record.isNtrip
                                        lastUpdateTime = record.lastUpdateTime
                                        val lastUserName = record.lastUserName
                                        offHostNetTime = record.offHostNetTime
                                        offNtripTime = record.offNtripTime
                                        val permanentRegCode = record.permanentRegCode
                                        permanentRegCodeHave = record.permanentRegCodeHave
                                        var permanentRegCodeHave_word = "无"
                                        if(permanentRegCodeHave == true){permanentRegCodeHave_word = "有"}
                                        val productionType = record.productionType
                                        val remark = record.remark
                                        val tempRegCode = record.tempRegCode
                                        tempRegCodeExpireTime = record.tempRegCodeExpireTime
                                        val tempRegDeadline = record.tempRegDeadline
                                        val tempRegCodeHave = record.tempRegCodeHave
                                        val tempRegDeadline_final = record.tempRegDeadline
                                        val tempRegMaxDuration = record.tempRegMaxDuration
                                        todayCanTrial = record.todayCanTrial
                                        val trialCode = record.trialCode
                                        val trialCodeHave = record.trialCodeHave
                                        trialDay = record.trialDay
                                        val trialStatus = record.trialStatus
                                        var trialStatus_word = "试用结束"
                                        if(trialStatus == "TRIALING"){trialStatus_word = "试用中"}
                                        else{trialStatus_word = "试用结束"}
                                        text3.text = "SN: $sn\n经销商: $companyName\n创建时间: $createTime\n型号：$productionType\n注册截止时间：$tempRegDeadline\n永久码：$permanentRegCodeHave_word\n备注：$remark\n临时码到期时间：$tempRegCodeExpireTime\n注册截至时间：$tempRegDeadline_final\n是否可试用：$todayCanTrial\n剩余试用天数：$trialDay\n试用状态：$trialStatus_word"
                                        SN_ALL = sn
                                        tempRegDeadline_ALL = tempRegDeadline
                                        button_register_system.visibility = Button.VISIBLE
                                    }
                                }else{
                                    Toast.makeText(applicationContext, "检测到多个设备，为避免误操作，请检查SN号", Toast.LENGTH_SHORT).show()
                                    text3.text = ""
                                }
                            }else{
                                Toast.makeText(applicationContext, "未查询到该SN号", Toast.LENGTH_SHORT).show()
                                text3.text = ""
                            }
                        }

                    }else {
                        Toast.makeText(applicationContext, "Response not successful: ${p1.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(p0: Call<registerinfo>, p1: Throwable) {
                    Toast.makeText(applicationContext, "获取信息时连接出现问题", Toast.LENGTH_SHORT).show()
                }

            })
            // 隐藏虚拟键盘
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
        //注册系统按钮点击事件
        button_register_system.setOnClickListener {
            val intent = Intent(this, RegisterSystem::class.java)
            intent.putExtra("SN", SN_ALL) // 将 id 值添加到 Intent 中
            intent.putExtra("tempRegDeadline",tempRegDeadline_ALL)
            intent.putExtra("auth", auth)
            intent.putExtra("permanentRegCodeHave", permanentRegCodeHave)
            intent.putExtra("offHostNetTime", offHostNetTime)
            intent.putExtra("offNtripTime", offNtripTime)
            intent.putExtra("isHostNet", isHostNet)
            intent.putExtra("isNtrip", isNtrip)
            intent.putExtra("lastUpdateTime", lastUpdateTime)
            intent.putExtra("trialDay", trialDay)
            intent.putExtra("todayCanTrial", todayCanTrial)
            intent.putExtra("tempRegCodeExpireTime", tempRegCodeExpireTime)
            startActivity(intent)
        }

        //文本框监听事件
        text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                text3.text = ""
                button_register_system.visibility = Button.GONE
            }
        })
        }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.log, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val button_luowang = item.itemId
        if (button_luowang==R.id.luowang){
            val intent = Intent(this, lwaccount::class.java)
            startActivity(intent)
        }
        val button_log = item.itemId
        if (button_log==R.id.log){
            val intent = Intent(this, log_register::class.java)
            startActivity(intent)
        }
        return true
    }
    override fun onDestroy() {
        super.onDestroy()
        dbHelper.closeDatabase()
    }
}