package com.example.appauto

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RegisterSystem : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_system)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val button_out = findViewById<Button>(R.id.button_out)
        val button_permanentRegCode = findViewById<Button>(R.id.button_permanentRegCode)
        val button_funtion = findViewById<Button>(R.id.button_funtion)

        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(com.google.android.material.R.drawable.ic_arrow_back_black_24)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        //取出intent传递的值
        //判断各个功能按钮的显示
        val SN = intent.getStringExtra("SN")
        val tempRegDeadline = intent.getStringExtra("tempRegDeadline")
        val auth = intent.getStringExtra("auth")
        val offHostNetTime = intent.getStringExtra("offHostNetTime")
        val offNtripTime = intent.getStringExtra("offNtripTime")
        val lastUpdateTime = intent.getStringExtra("lastUpdateTime")
        val isHostNet = intent.getBooleanExtra("isHostNet", false)
        val isNtrip = intent.getBooleanExtra("isNtrip", false)
        if (tempRegDeadline != null) {
            button_out.visibility = Button.GONE
        }
        //永久码下发按钮显示
        val permanentRegCodeHave = intent.getBooleanExtra("permanentRegCodeHave", false)
        if (permanentRegCodeHave) {
            button_permanentRegCode.visibility = Button.VISIBLE
        } else {
            button_permanentRegCode.visibility = Button.GONE
        }

        //出库按钮点击事件
        button_out.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            //设置对话框标题
            builder.setTitle("提示")
            //设置对话框消息
            builder.setMessage("你确定要出库吗？")
            //设置“确定”按钮
            builder.setPositiveButton("确定") { dialog, which ->
                val retrofit =
                    Retrofit.Builder().baseUrl("https://cloud.sinognss.com/").addConverterFactory(
                        GsonConverterFactory.create()
                    ).build()
                val retrofit_out = retrofit.create(out::class.java)
                if (SN == null) {
                    Toast.makeText(applicationContext, "SN异常，请检查或重试", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    retrofit_out.out(SN, auth.toString())
                        .enqueue(object : retrofit2.Callback<out_back> {
                            override fun onResponse(p0: Call<out_back>, p1: Response<out_back>) {
                                button_out.visibility = Button.GONE
                                Toast.makeText(
                                    applicationContext,
                                    p1.body()?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            override fun onFailure(p0: Call<out_back>, p1: Throwable) {
                                Toast.makeText(
                                    applicationContext,
                                    "出库时网络请求失败",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        })
                }
            }
            builder.setNegativeButton("取消") { dialog, which ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
        }
        //永久码下发按钮点击事件
        button_permanentRegCode.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            //设置对话框标题
            builder.setTitle("提示")
            //设置对话框消息
            builder.setMessage("你确定要下发永久码吗？")
            //设置“确定”按钮
            builder.setPositiveButton("确定") { dialog, which ->
                val retrofit = Retrofit.Builder().baseUrl("https://cloud.sinognss.com/")
                    .addConverterFactory(GsonConverterFactory.create()).build()
                val retrofit_permanent = retrofit.create(permanent_code::class.java)
                if (SN == null) {
                    Toast.makeText(applicationContext, "SN异常，请检查或重试", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    retrofit_permanent.permanent_code(SN, auth.toString())
                        .enqueue(object : retrofit2.Callback<permanent_code_back> {
                            override fun onResponse(
                                p0: Call<permanent_code_back>,
                                p1: Response<permanent_code_back>
                            ) {
                                if (p1.isSuccessful) {
                                    it.isEnabled = false
                                    it.setBackgroundColor(
                                        ContextCompat.getColor(
                                            this@RegisterSystem,
                                            android.R.color.darker_gray
                                        )
                                    ) // 改变按钮颜色
                                    button_permanentRegCode.text = "永久码已下发"
                                    Toast.makeText(
                                        applicationContext,
                                        p1.body()?.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        applicationContext,
                                        "永久码下发异常",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onFailure(p0: Call<permanent_code_back>, p1: Throwable) {
                                Toast.makeText(
                                    applicationContext,
                                    "永久码下发时网络连接失败",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                }
            }
            builder.setNegativeButton("取消") { dialog, which ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
        }

        //功能授权按钮点击事件
        button_funtion.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            // 设置对话框标题
            builder.setTitle("功能授权")

            // 创建一个包含两个开关的视图
            val view = layoutInflater.inflate(R.layout.dialog_function_authorization, null)
            val switch1 = view.findViewById<SwitchCompat>(R.id.switch1)
            val switch2 = view.findViewById<SwitchCompat>(R.id.switch2)
            if (isNtrip) {
                switch1.setChecked(true)
            }
            if (isHostNet) {
                switch2.setChecked(true)
            }
            builder.setView(view)

                // 设置“确定”按钮
                builder.setPositiveButton("确定") { dialog, which ->
                    val isSwitch1Checked = switch1.isChecked
                    val isSwitch2Checked = switch2.isChecked

                    val gson = GsonBuilder().serializeNulls().create()
                    val retrofit = Retrofit.Builder().baseUrl("https://cloud.sinognss.com/").addConverterFactory(GsonConverterFactory.create(gson)).build()
                    val retrofit_function = retrofit.create(function_given::class.java)

                    retrofit_function.function_given(function_information(
                        HostNetDeadlineTimeEditable = true,
                        NtripDeadlineTimeEditable = true,
                        companyId = "",
                        initHostNetRegDeadline = offHostNetTime,
                        initNtripRegDeadline = offNtripTime,
                        isHostNet = isSwitch2Checked,
                        isNtrip = isSwitch1Checked,
                        isSelectall = false,
                        keyword = SN,
                        lastUpdateTime = lastUpdateTime,
                        offHostNetTime = offHostNetTime,
                        offNtripTime = offNtripTime,
                        snList = listOf(SN.toString()),
                        trialStatus = null,
                        type = 2
                    ), auth.toString()).enqueue(object : Callback<function_given_back>{
                        override fun onResponse(
                            p0: Call<function_given_back>,
                            p1: Response<function_given_back>
                        ) {
                            if(p1.isSuccessful){
                                Toast.makeText(applicationContext, p1.body()?.message, Toast.LENGTH_SHORT).show()
                            }
                            else{ Toast.makeText(applicationContext, "修改失败，尝试重新登陆罗网或重新查询设备", Toast.LENGTH_SHORT).show()}
                        }

                        override fun onFailure(p0: Call<function_given_back>, p1: Throwable) {
                            Toast.makeText(
                                applicationContext,
                                "功能授权时网络连接失败",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    })
                }

                // 设置“取消”按钮
                builder.setNegativeButton("取消") { dialog, which ->
                    dialog.dismiss()
                }

                val dialog = builder.create()
                dialog.show()
            }
        }
    }