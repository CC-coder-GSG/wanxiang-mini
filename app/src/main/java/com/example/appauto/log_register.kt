package com.example.appauto

import android.graphics.Color
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
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class log_register : AppCompatActivity() {
    private lateinit var dbHelper: DBManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_register)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        // 设置状态栏为深色，和 Toolbar 保持一致
        window.statusBarColor = Color.parseColor("#111827")
        // 使用浅色状态栏图标（白色），避免深色背景下看不清
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // 让返回箭头/三点菜单在深色背景上可见
        toolbar.navigationIcon?.setTint(Color.WHITE)
        toolbar.overflowIcon?.setTint(Color.WHITE)
        toolbar.setNavigationOnClickListener {
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