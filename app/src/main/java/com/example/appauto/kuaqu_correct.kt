package com.example.appauto

import android.content.res.ColorStateList
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class kuaqu_correct : AppCompatActivity() {
    private var SN : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_kuaqu_correct)

        // 设置状态栏为深色，和 Toolbar 保持一致
        window.statusBarColor = Color.parseColor("#111827")
        // 使用浅色状态栏图标（白色），避免深色背景下看不清
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val text_correct = findViewById<TextView>(R.id.textView4)
        val switch = findViewById<SwitchCompat>(R.id.switch1)
        val spinner = findViewById<Spinner>(R.id.spinner)
        val basestation = findViewById<Button>(R.id.button)
        val button_delete = findViewById<Button>(R.id.button_delete)
        var basestation_name : String? = null
        var baseSelected : String? = null
        var base_selected_item : String? = null

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                baseSelected = parent?.getItemAtPosition(position).toString()
                base_selected_item = parent?.getItemAtPosition(position).toString()
                when (baseSelected) {
                    "随区域" -> baseSelected = "9"
                    "SinoGNSS" -> baseSelected = "0"
                    "中国移动" -> baseSelected = "2"
                    "千寻" -> baseSelected = "1"
                    "SinoGNSS -> 中国移动" -> baseSelected = "0,2"
                    "SinoGNSS -> 中国移动 -> 千寻" -> baseSelected = "0,2,1"
                    "SinoGNSS -> 千寻" -> baseSelected = "0,1"
                    "SinoGNSS -> 千寻 -> 中国移动" -> baseSelected = "0,1,2"
                    "中国移动 -> SinoGNSS -> 千寻" -> baseSelected = "2,0,1"
                    "中国移动 -> SinoGNSS" -> baseSelected = "2,0"
                    "中国移动 -> 千寻 -> SinoGNSS" -> baseSelected = "2,1,0"
                    "中国移动 -> 千寻" -> baseSelected = "2,1"
                    "千寻 -> 中国移动 -> SinoGNSS" -> baseSelected = "1,2,0"
                    "千寻 -> 中国移动" -> baseSelected = "1,2"
                    "千寻 -> SinoGNSS -> 中国移动" -> baseSelected = "1,0,2"
                    "千寻 -> SinoGNSS" -> baseSelected = "1,0"
                    else -> baseSelected = baseSelected
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(applicationContext, "没有元素被选中", Toast.LENGTH_SHORT).show()
            }
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // 让返回箭头/三点菜单在深色背景上可见
        toolbar.navigationIcon?.setTint(Color.WHITE)
        toolbar.overflowIcon?.setTint(Color.WHITE)
        toolbar.setNavigationOnClickListener {
            finish()
        }
                //获取到id值
                val id = intent.getIntExtra("id", 0)
                val auth = intent.getStringExtra("auth")

                val retofit_correct =
                    Retrofit.Builder().baseUrl("https://cloud.sinognss.com/").addConverterFactory(
                        GsonConverterFactory.create()
                    ).build()
                val retrofit_correct_info = retofit_correct.create(idinfo::class.java)
                retrofit_correct_info.get_idinfo(id, auth.toString())
                    .enqueue(object : Callback<id_info> {
                        override fun onResponse(p0: Call<id_info>, p1: Response<id_info>) {
                            val luowanginfo = p1.body()
                            if (luowanginfo != null) {
                                val basestation = luowanginfo.data.accountType
                                val deviceType = luowanginfo.data.deviceType
                                val salename = luowanginfo.data.salesName
                                val span = luowanginfo.data.isSpan
                                SN = luowanginfo.data.sn
                                basestation_name = basestation
                                when (basestation) {
                                    "9" -> basestation_name = "随区域"
                                    "0" -> basestation_name = "SinoGNSS"
                                    "2" -> basestation_name = "中国移动"
                                    "1" -> basestation_name = "千寻"
                                    "0,2" -> basestation_name = "SinoGNSS -> 中国移动"
                                    "0,2,1" -> basestation_name = "SinoGNSS -> 中国移动 -> 千寻"
                                    "0,1" -> basestation_name = "SinoGNSS -> 千寻"
                                    "0,1,2" -> basestation_name = "SinoGNSS -> 千寻 -> 中国移动"
                                    "2,0,1" -> basestation_name = "中国移动 -> SinoGNSS -> 千寻"
                                    "2,0" -> basestation_name = "中国移动 -> SinoGNSS"
                                    "2,1,0" -> basestation_name = "中国移动 -> 千寻 -> SinoGNSS"
                                    "2,1" -> basestation_name = "中国移动 -> 千寻"
                                    "1,2,0" -> basestation_name = "千寻 -> 中国移动 -> SinoGNSS"
                                    "1,2" -> basestation_name = "千寻 -> 中国移动"
                                    "1,0,2" -> basestation_name = "千寻 -> SinoGNSS -> 中国移动"
                                    "1,0" -> basestation_name = "千寻 -> SinoGNSS"
                                    else -> basestation_name = basestation
                                }
                                text_correct.text =
                                    " 基站策略：$basestation_name\n 设备类型：$deviceType\n 经销商：$salename\n 是否跨区：${span} \n SN：$SN"
                                if (span) {
                                    switch.isChecked = true
                                    switch.visibility = SwitchCompat.VISIBLE
                                } else {
                                    switch.isChecked = false
                                    switch.visibility = SwitchCompat.VISIBLE
                                }
                            }
                        }

                        override fun onFailure(p0: Call<id_info>, p1: Throwable) {
                            Toast.makeText(applicationContext, "连接服务器异常", Toast.LENGTH_SHORT)
                                .show()
                        }

                    })

                ArrayAdapter.createFromResource(
                    this,
                    R.array.spinner_basestation,
                    android.R.layout.simple_spinner_item
                ).also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                }

                // 根据已知值设置 Spinner 的默认选中项
                val position =
                    (spinner.adapter as ArrayAdapter<*>).getPosition(basestation_name as Nothing?)
                if (position != Spinner.INVALID_POSITION) {
                    spinner.setSelection(position)
                }

                switch.setOnClickListener {
                    if (switch.isChecked == true) {
                        //弹出提示框
                        val dialog = MaterialAlertDialogBuilder(this)
                            .setTitle("确认")
                            .setMessage("确认开启跨区？")
                            .setPositiveButton("确认") { dialog, which ->
                                val retrofit_direct =
                                    Retrofit.Builder().baseUrl("https://cloud.sinognss.com/")
                                        .addConverterFactory(GsonConverterFactory.create()).build()
                                val retrofit_direct_info =
                                    retrofit_direct.create(direct_more::class.java)
                                retrofit_direct_info.direct_more_change(id, true, auth.toString())
                                    .enqueue(object : Callback<direct_more_message> {
                                        override fun onResponse(
                                            p0: Call<direct_more_message>,
                                            p1: Response<direct_more_message>
                                        ) {
                                            val message = p1.body()
                                            if (message != null) {
                                                Toast.makeText(
                                                    applicationContext,
                                                    message.message,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                recreate()
                                            }
                                        }

                                        override fun onFailure(
                                            p0: Call<direct_more_message>,
                                            p1: Throwable
                                        ) {
                                            Toast.makeText(
                                                applicationContext,
                                                "修改跨区时连接失败",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    })
                            }
                            .setNegativeButton("取消") { dialog, which ->
                                // 用户点击取消按钮后的操作
                                // 回退点击 switch 的操作
                                switch.isChecked = false
                            }
                            .create()

                        dialog.show()
                        styleDialog(dialog)

                    } else if (switch.isChecked == false) {
                        //弹出提示框
                        val dialog = MaterialAlertDialogBuilder(this)
                            .setTitle("确认")
                            .setMessage("确认关闭跨区？")
                            .setPositiveButton("确认") { dialog, which ->
                                val retrofit_direct =
                                    Retrofit.Builder().baseUrl("https://cloud.sinognss.com/")
                                        .addConverterFactory(GsonConverterFactory.create()).build()
                                val retrofit_direct_info =
                                    retrofit_direct.create(direct_more::class.java)
                                retrofit_direct_info.direct_more_change(id, false, auth.toString())
                                    .enqueue(object : Callback<direct_more_message> {
                                        override fun onResponse(
                                            p0: Call<direct_more_message>,
                                            p1: Response<direct_more_message>
                                        ) {
                                            val message = p1.body()
                                            if (message != null) {
                                                Toast.makeText(
                                                    applicationContext,
                                                    message.message,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                recreate()
                                            }
                                        }

                                        override fun onFailure(
                                            p0: Call<direct_more_message>,
                                            p1: Throwable
                                        ) {
                                            Toast.makeText(
                                                applicationContext,
                                                "修改跨区时连接失败",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                    })
                            }
                            .setNegativeButton("取消") { dialog, which ->
                                // 用户点击取消按钮后的操作
                                // 回退点击 switch 的操作
                                switch.isChecked = true
                            }
                            .create()

                        dialog.show()
                        styleDialog(dialog)
                    }
                }

                basestation.setOnClickListener {
                    val dialog = MaterialAlertDialogBuilder(this)
                        .setTitle("确认")
                        .setMessage("确认更改基站为$base_selected_item？")
                        .setPositiveButton("确认") { dialog, which ->
                            val retrofit_base =
                                Retrofit.Builder().baseUrl("https://cloud.sinognss.com/")
                                    .addConverterFactory(GsonConverterFactory.create()).build()
                            val retrofit_base_info =
                                retrofit_base.create(basestation_change::class.java)
                            retrofit_base_info.basestation_change(
                                SN.toString(),
                                baseSelected.toString(),
                                auth.toString()
                            ).enqueue(object : Callback<direct_more_message> {
                                override fun onResponse(
                                    p0: Call<direct_more_message>, p1: Response<direct_more_message>
                                ) {
                                    val message = p1.body()
                                    if (message != null) {
                                        println(p1.toString())
                                        Toast.makeText(
                                            applicationContext,
                                            message.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        recreate()
                                    }
                                    else{
                                        Toast.makeText(
                                            applicationContext,
                                            "请重新登录",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        recreate()
                                    }
                                }

                                override fun onFailure(
                                    p0: Call<direct_more_message>, p1: Throwable
                                ) {
                                    Toast.makeText(
                                        applicationContext,
                                        "修改基站时网络连接错误",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            })
                        }
                        .setNegativeButton("取消") { dialog, which -> }
                        .create()

                    dialog.show()
                    styleDialog(dialog)
                }
                button_delete.setOnClickListener {
                    val builder = MaterialAlertDialogBuilder(this)
                    //设置对话框标题
                    builder.setTitle("提示")
                    //设置对话框消息
                    builder.setMessage("你确定要删除此设备吗？")
                    //设置“确定”按钮
                    builder.setPositiveButton("确定"){ dialog, which ->
                        val retrofit_delete = Retrofit.Builder().baseUrl("https://cloud.sinognss.com/").addConverterFactory(GsonConverterFactory.create()).build()
                        val delete_interface = retrofit_delete.create(equipment_delete::class.java)
                        delete_interface.delete_equipment(SN.toString(), auth.toString()).enqueue(object : Callback<delete_equipment_callback>{
                            override fun onResponse(
                                p0: Call<delete_equipment_callback>,
                                p1: Response<delete_equipment_callback>
                            ) {
                                Toast.makeText(
                                    applicationContext,
                                    p1.body()!!.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }

                            override fun onFailure(
                                p0: Call<delete_equipment_callback>,
                                p1: Throwable
                            ) {
                                Toast.makeText(
                                    applicationContext,
                                    "删除设备时连接失败",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                    }
                    builder.setNegativeButton("取消"){ dialog, which ->
                        dialog.dismiss()
                    }
                    val dialog = builder.create()
                    dialog.show()
                    styleDialog(dialog)
                }
            }
        }


    private fun styleDialog(dialog: AlertDialog) {
        // 确定按钮：深色主按钮
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
            isAllCaps = false
            setTextColor(Color.WHITE)
            backgroundTintList = ColorStateList.valueOf(Color.parseColor("#111827"))
            (this as? com.google.android.material.button.MaterialButton)?.cornerRadius =
                (12 * resources.displayMetrics.density).toInt()
        }

        // 取消按钮：白色卡片次按钮
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