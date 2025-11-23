package com.example.appauto

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.GridView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import java.sql.DriverManager
import java.sql.SQLException

class ListviewActivity : AppCompatActivity() {
    //定义套件
    private val data = listOf("status","series","demand","ERP","product","HardwareIdentification","mainboardERP","mainboardName","screen","mainboard","4G","radio","IMU","BT","WIFI","IMU_1","memory","mainboard_text","machine_text")
    private val data_name = listOf("状态","系列","需求来源","ERP","物品名称","硬件识别号","主板ERP号","主板物品名称","显示屏","主板","4G模块","电台","倾斜测量","BT","WIFI","惯导","内存","主板备注","整机备注")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_label)

        // 设置状态栏为深色，和 Toolbar 保持一致
        window.statusBarColor = Color.parseColor("#111827")
        // 使用浅色状态栏图标（白色），避免深色背景下看不清
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val button_ERP = findViewById<Button>(R.id.button_ERP)
        val button_care = findViewById<Button>(R.id.button_care)
        button_care.visibility = View.GONE
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(com.google.android.material.R.drawable.ic_arrow_back_black_24)
        toolbar.setNavigationOnClickListener{
            finish()
        }

        val edit_ERP = findViewById<EditText>(R.id.editText_ERP)
        val listview = findViewById<ExpandableListView>(R.id.listview)

        var result_list_1 = mutableListOf<String>()

        // 定义一个可变列表用于存储组数据
        var groupList = mutableListOf<String>()
        // 定义一个可变列表用于存储子项数据
        var childList = mutableListOf<List<String>>()

        button_ERP.setOnClickListener{
            // 开始查询，禁用按钮，修改文字和颜色
            button_ERP.isEnabled = false
            button_ERP.text = "查询中"
            button_ERP.alpha = 0.7f

            val runnable = Runnable {

                val handler = Handler(Looper.getMainLooper())

                val jdbcUrl = "jdbc:mysql://8.152.199.145:1000/handphone?useSSL=false"
                val username = "root"
                val password = "sn123456."

                try {
                    val ERP = edit_ERP.text.toString()
                    val sql = "select * from product_status where ERP like '%$ERP%' or status like '%$ERP%' or series like '%$ERP%' or demand like '%$ERP%' or product like '%$ERP%' or HardwareIdentification like '%$ERP%' or mainboardERP like '%$ERP%' or mainboardName like '%$ERP%' or screen like '%$ERP%' or mainboard like '%$ERP%' or 4G like '%$ERP%' or radio like '%$ERP%' or mainboard_text like '%$ERP%' or machine_text like '%$ERP%'"

                    val dbHelper = DriverManager.getConnection(jdbcUrl, username, password)

                    val stmt = dbHelper.createStatement()
                    val data_result = stmt.executeQuery(sql)

                    groupList.clear()
                    childList.clear()

                    var groupIndex = 1
                    while (data_result.next()) {
                        val status_info = mutableListOf<String>()

                        // 数组写入
                        for (i in 1..19) {
                            if (data_result.getString(i).isNullOrBlank()) {
                                status_info.add(" ")
                            } else {
                                status_info.add(data_result.getString(i))
                            }
                        }

                        val result = mutableListOf<String>()
                        for (i in 1..19) {
                            result.add(data_name[i - 1] + "：" + status_info[i - 1])
                        }

                        groupList.add("第 $groupIndex 组数据")
                        childList.add(result)
                        groupIndex++
                    }

                    data_result.close()
                    dbHelper.close()

                    if (groupList.isEmpty()) {
                        // 子线程调用 Toast
                        Looper.prepare()
                        handler.post {
                            // 查询结束，恢复按钮状态
                            button_ERP.isEnabled = true
                            button_ERP.text = "查询"
                            button_ERP.alpha = 1f
                        }

                        Toast.makeText(this, "未查询到", Toast.LENGTH_SHORT).show()
                        Looper.loop()
                    }
                } catch (Throwable: SQLException) {
                    Throwable.printStackTrace()
                }finally{
                    handler.post {
                        // 查询结束，恢复按钮状态
                        button_ERP.isEnabled = true
                        button_ERP.text = "查询"
                        button_ERP.alpha = 1f
                    }
                }

                handler.post {
                    // 使用 ExpandableListView 实现
                    val adapter = CustomExpandableListAdapter(this, groupList, childList, 4)
                    listview.setAdapter(adapter)
                }
            }

            val thread = Thread(runnable)
            thread.start()

            // 隐藏虚拟键盘
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}