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
import com.google.android.material.button.MaterialButton
import java.sql.DriverManager
import java.sql.SQLException

class Taojian_List : AppCompatActivity() {
    //定义套件
    //private val data = listOf("status","series","demand","ERP","product","HardwareIdentification","mainboardERP","mainboardName","screen","mainboard","4G","radio","IMU","BT","WIFI","IMU_1","memory","mainboard_text","machine_text")
    private val data_name = listOf("套件名称","接收机","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","状态")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.listview_layout)
        // 设置状态栏为深色，和 Toolbar 保持一致
        window.statusBarColor = Color.parseColor("#111827")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val button_ERP = findViewById<MaterialButton>(R.id.button_ERP)
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // 让返回箭头/三点菜单在深色背景上可见
        toolbar.navigationIcon?.setTint(Color.WHITE)
        toolbar.overflowIcon?.setTint(Color.WHITE)
        toolbar.setNavigationOnClickListener {
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
                    val sql = "select * from taojian where name like '%$ERP%' or ERP1 like '%$ERP%' or ERP2 like '%$ERP%' or ERP3 like '%$ERP%' or ERP4 like '%$ERP%' or ERP5 like '%$ERP%' or ERP6 like '%$ERP%' or ERP7 like '%$ERP%' or ERP8 like '%$ERP%' or ERP9 like '%$ERP%' or ERP10 like '%$ERP%' or ERP11 like '%$ERP%' or ERP12 like '%$ERP%' or ERP13 like '%$ERP%' or ERP14 like '%$ERP%' or ERP15 like '%$ERP%' or ERP16 like '%$ERP%' or status like '%$ERP%'"

                    val dbHelper = DriverManager.getConnection(jdbcUrl, username, password)

                    val stmt = dbHelper.createStatement()
                    val data_result = stmt.executeQuery(sql)

                    groupList.clear()
                    childList.clear()

                    var groupIndex = 1
                    while (data_result.next()) {
                        val status_info = mutableListOf<String>()

                        // 数组写入
                        for (i in 1..18) {
                            if (data_result.getString(i).isNullOrBlank()) {
                                status_info.add(" ")
                            } else {
                                status_info.add(data_result.getString(i))
                            }
                        }

                        val result = mutableListOf<String>()
                        for (i in 1..18) {
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
                        // 关闭数据库及其生成的数据集
                        data_result.close()
                        dbHelper.close()
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
                    val adapter = CustomExpandableListAdapter(this, groupList, childList, 0)
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