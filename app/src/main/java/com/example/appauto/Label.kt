package com.example.appauto

import android.app.appsearch.GetSchemaResponse
import android.content.Context
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import java.sql.Connection
import java.sql.DriverManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.sql.SQLException
import kotlin.concurrent.thread

class Label : AppCompatActivity() {

    private val data = listOf("ERP","name","数传","惯导","WiFi","存储","屏幕","摄像头","激光","铭牌标贴","外箱标贴","型批编号","备注")
    private val data_name = listOf("ERP","名称","数传","惯导","WiFi","存储","屏幕","摄像头","激光","铭牌标贴","外箱标贴","型批编号","备注")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_label)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val button_care = findViewById<Button>(R.id.button_care)
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(com.google.android.material.R.drawable.ic_arrow_back_black_24)
        toolbar.setNavigationOnClickListener{
            finish()
        }

        button_care.setOnClickListener{
            val intent = Intent(this, CareThing::class.java)
            startActivity(intent)
        }

        val button_ERP = findViewById<Button>(R.id.button_ERP)
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
            button_ERP.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray))

            val runnable = Runnable {

                val handler = Handler(Looper.getMainLooper())

                val jdbcUrl = "jdbc:mysql://8.152.199.145:1000/handphone?useSSL=false"
                val username = "root"
                val password = "sn123456."

                try {
                    val ERP = edit_ERP.text.toString()
                    val sql = "select * from label where ERP like '%$ERP%' or name like '%$ERP%' or `铭牌标贴` like '%$ERP%' or `外箱标贴` like '%$ERP%' or `型批编号` like '%$ERP%' or `备注` like '%$ERP%'"

                    val dbHelper = DriverManager.getConnection(jdbcUrl, username, password)

                    val stmt = dbHelper.createStatement()
                    val data_result = stmt.executeQuery(sql)

                    groupList.clear()
                    childList.clear()

                    var groupIndex = 1
                    while (data_result.next()) {
                        val status_info = mutableListOf<String>()

                        // 数组写入
                        for (i in 1..13) {
                            if (data_result.getString(i).isNullOrBlank()) {
                                status_info.add(" ")
                            } else {
                                status_info.add(data_result.getString(i))
                            }
                        }

                        val result = mutableListOf<String>()
                        for (i in 1..13) {
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
                            button_ERP.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500))
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
                        button_ERP.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500))
                    }
                }

                handler.post {
                    // 使用 ExpandableListView 实现
                    val adapter = CustomExpandableListAdapter(this, groupList, childList, 1)
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