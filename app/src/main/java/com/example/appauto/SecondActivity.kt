package com.example.appauto

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appauto.MyDatabaseHelper
import java.sql.DriverManager
import java.sql.SQLException

class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.second_layout)

        val toolbar1 = findViewById<Toolbar>(R.id.toolbar1)
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        setSupportActionBar(toolbar1)
        toolbar1.setNavigationIcon(com.google.android.material.R.drawable.ic_arrow_back_black_24)
        toolbar1.setNavigationOnClickListener{
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.button2)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //获取按钮和输入框的id
        val button2 = findViewById<Button>(R.id.button2)
        val button3 = findViewById<View>(R.id.button3)
        val button4 = findViewById<Button>(R.id.button4)
        val editText1 = findViewById<EditText>(R.id.editText_1)
        val textview1 = findViewById<TextView>(R.id.textView1)
        val textview2 = findViewById<TextView>(R.id.textView2)
        //数组集合
        val database_name = arrayOf("CangZhou","JiaDing","nmp","R5","R50","R50_Pro")
        var MSISDN = ""
        var PLACE = ""
        var textview1_text = ""
        var textview2_text = ""

        //button2按钮点击事件
        button2.setOnClickListener {
                val runnable = Runnable {
                    //数据库复制
                    //val dbHelper = DBManager(this)
                    //打开数据库
                    val handler = Handler(Looper.getMainLooper())

                    val jdbcUrl = "jdbc:mysql://8.152.199.145:1000/handphone?useSSL=false&useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=UTC"
                    val username = "user"
                    val password = "sn123456."
                    try {

                        val dbHelper = DriverManager.getConnection(jdbcUrl, username, password)

                        val db = dbHelper.createStatement()
                        //val data_result = stmt.executeQuery(sql)
                        //dbHelper.openDatabase()
                        //val db = dbHelper.database
                        //val dbHelpe = MyDatabaseHelper(this, "handphone.db", 1)
                        //打开数据库
                        //val db1 = dbHelpe.writableDatabase
                        //输入框文字提取
                        val sn = editText1.text.toString()
                        //循环查询各个表格，直到查询到结果
                        if (sn != "") {
                            for (i in 1..6) {
                                val sql =
                                    "select * from" + " " + database_name[i - 1] + " " + "where SN=" + "'" + sn + "'"
                                val data_result = db.executeQuery(sql)
                                if (data_result.next()) {
                                    if (!(data_result.getString("MSISDN").isNullOrBlank())) {
                                        MSISDN =
                                            data_result.getString("MSISDN")
                                        if (!(data_result.getString("PLACE").isNullOrBlank())) {
                                            PLACE =
                                                data_result.getString("PLACE")
                                            if (PLACE == "沧州移动") {
                                                PLACE = "沧州移动"
                                            } else {
                                                if (PLACE == "沧州新")
                                                    PLACE = "沧州新"
                                                else {
                                                    PLACE = "嘉定移动"
                                                }
                                            }
                                        } else {
                                            PLACE = "嘉定移动"
                                        }
                                        textview2.text = "卡号所属为：$PLACE"
                                        textview1.text = "手簿卡号为：$MSISDN"
                                        Looper.prepare()
                                        Toast.makeText(this, "手簿卡号为：$MSISDN", Toast.LENGTH_SHORT).show()
                                        Looper.loop()
                                        break
                                    } else {
                                        //textView显示
                                        textview1.text = "未记录此SN号所属MSISDN"
                                        textview2.text = "请从手簿拨号界面查询卡号"
                                        Looper.prepare()
                                        Toast.makeText(
                                            this,
                                            "未记录此SN号所属MSISDN",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                        Looper.loop()
                                        break
                                    }
                                } else {
                                    if (database_name[i - 1] == "R50_Pro") {
                                        textview1.text = "未查询到该SN号"
                                        textview2.text = ""
                                        Looper.prepare()
                                        Toast.makeText(this, "未查询到该SN号", Toast.LENGTH_SHORT)
                                            .show()
                                        Looper.loop()
                                        //textView显示

                                    }
                                }
                            }
                        }
                        //没有查询到则会出现提示
                        else {
                            //textView显示
                            textview1.text = "请输入SN号码"
                            textview2.text = ""
                            Looper.prepare()
                            Toast.makeText(this, "未输入SN号", Toast.LENGTH_SHORT).show()
                            Looper.loop()
                        }
                        dbHelper.close()
                    } catch (Throwable: SQLException) {
                        Throwable.printStackTrace()
                    }
                }


            val thread = Thread(runnable)
            // 隐藏虚拟键盘
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
            thread.start()
        }


        button3.setOnClickListener{
            finish()
        }

        button4.setOnClickListener{
            val intent = Intent(this,WebActivity::class.java)
            startActivity(intent)
        }

    }
}