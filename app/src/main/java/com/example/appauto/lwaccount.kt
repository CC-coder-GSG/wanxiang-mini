package com.example.appauto

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.core.view.WindowCompat

class lwaccount : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lwaccount)

        // 设置状态栏为深色，和 Toolbar 保持一致
        window.statusBarColor = Color.parseColor("#111827")
        // 使用浅色状态栏图标（白色），避免深色背景下看不清
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false


        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(com.google.android.material.R.drawable.ic_arrow_back_black_24)
        toolbar.setNavigationOnClickListener{
            finish()
        }

        val text_acc = findViewById<EditText>(R.id.editText_acc)
        val text_pss = findViewById<EditText>(R.id.editText_pss)
        //数据库复制
        val dbHelper = DBManager(this)
        dbHelper.openDatabase()
        val db = dbHelper.database
        val sql = "select * from luowang where user=\"345\""
        val dataresult = db.rawQuery(sql,null)
        if (dataresult.moveToFirst()) {
            val acc1 = dataresult.getString(dataresult.getColumnIndexOrThrow("acc"))
            val pss1 = dataresult.getString(dataresult.getColumnIndexOrThrow("pss"))
            text_acc.setText(acc1)
            text_pss.setText(pss1)
        }
        //dataresult.close()


        val buttton_acc = findViewById<Button>(R.id.button_acc)
        buttton_acc.setOnClickListener{
            // 先进入“登陆中”状态
            buttton_acc.isEnabled = false
            buttton_acc.text = "登陆中"
            buttton_acc.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray))

            fun resetLoginButton() {
                buttton_acc.isEnabled = true
                buttton_acc.text = "登陆"
                buttton_acc.setBackgroundColor(Color.parseColor("#111827"))
            }

            val acc = text_acc.text?.toString()?.trim().orEmpty()
            val pss = text_pss.text?.toString()?.trim().orEmpty()
            if (acc.isBlank() || pss.isBlank()) {
                Toast.makeText(this, "请输入账号或密码", Toast.LENGTH_SHORT).show()
                resetLoginButton()
                return@setOnClickListener
            } else {
                val sql1 = "UPDATE luowang SET acc='$acc',pss='$pss' WHERE user=\"345\""
                db.execSQL(sql1)

                if (dataresult.moveToFirst()) {
                    fun get_access_token(acc: String, pss: String) {
                        val retrofit = Retrofit.Builder().baseUrl("https://cloud.sinognss.com/")
                            .addConverterFactory(GsonConverterFactory.create()).build()
                        val login = retrofit.create(loginService::class.java)
                        login.login(
                            acc,
                            pss,
                            "password",
                            "NaviCloud",
                            "NaviCloud_Secret",
                            "all",
                            "false"
                        ).enqueue(object :
                            Callback<logindata> {
                            override fun onResponse(
                                call: Call<logindata>, response: Response<logindata>
                            ) {
                                val list = response.body()
                                if (!response.isSuccessful || list == null) {
                                    resetLoginButton()
                                    Toast.makeText(applicationContext, "登录失败", Toast.LENGTH_SHORT).show()
                                    return
                                }
                                if (list.message == "操作成功") {
                                    val access_token = list.data.access_token
                                    Toast.makeText(
                                        applicationContext,
                                        list.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val sql_accesstoken =
                                        "UPDATE luowang SET accesstoken='$access_token'"
                                    db.execSQL(sql_accesstoken)
                                    dataresult.close()
                                    db.close()
                                    dbHelper.closeDatabase()
                                    finish()
                                }
                                else{
                                    resetLoginButton()
                                    Toast.makeText(
                                        applicationContext,
                                        list.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onFailure(p0: Call<logindata>, p1: Throwable) {
                                p1.printStackTrace()
                                resetLoginButton()
                                Toast.makeText(
                                    applicationContext,
                                    "用户名或密码错误，输入错误5次后账号将锁定",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                    }
                    get_access_token(acc.toString(), pss.toString())

                    /*db.close()
                dbHelper.closeDatabase()*/
                } else {
                    resetLoginButton()
                    Toast.makeText(this, "未配表", Toast.LENGTH_SHORT).show()
                    db.close()
                    dataresult.close()
                    dbHelper.closeDatabase()
                }

                //关闭数据库
                //db.close()
                //dataresult.close()
                //dbHelper.closeDatabase()
            }
        }
    }
}