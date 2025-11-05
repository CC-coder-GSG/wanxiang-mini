package com.example.appauto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class log_register : AppCompatActivity() {
    private lateinit var dbHelper: DBManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_register)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(com.google.android.material.R.drawable.ic_arrow_back_black_24)
        toolbar.setNavigationOnClickListener{
            finish()
        }
        dbHelper = DBManager(this)
        dbHelper.openDatabase()
        val listview = findViewById<ListView>(R.id.listview)
        val db = dbHelper.database
        val sql = "select SN, extendtime, registertime, operatetime as _id from historyregister"
        val cursor = db.rawQuery(sql, null)

        // 添加 HeaderView
        val headerView = LayoutInflater.from(this).inflate(R.layout.list_header_layout, listview, false)
        listview.addHeaderView(headerView)

        // 创建适配器
        val adapter = object : SimpleCursorAdapter(
            this,
            R.layout.list_item_layout,
            cursor,
            arrayOf("SN", "extendtime", "registertime", "_id"), // 替换为你数据库中的列名
            intArrayOf(R.id.SN, R.id.extendtime, R.id.registertime, R.id.operatetime),
            0
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                // 在这里可以对每一行的视图进行进一步的自定义
                // 例如，设置字体大小、颜色等
                return view
            }
        }

        // 设置适配器
        listview.adapter = adapter
    }

    // 覆盖 onDestroy() 方法
    override fun onDestroy() {
        super.onDestroy()
        // 在这里添加你想要在 Activity 销毁时执行的操作
        // 例如，关闭数据库连接
        dbHelper.closeDatabase()
    }
}