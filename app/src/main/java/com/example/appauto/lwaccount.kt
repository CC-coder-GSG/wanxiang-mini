package com.example.appauto

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

class lwaccount : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lwaccount)

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
            buttton_acc.isEnabled = false
            buttton_acc.text = "登陆中"
            buttton_acc.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray))

            val acc = text_acc.text
            val pss = text_pss.text
            if (acc.isNullOrBlank() || pss.isNullOrBlank()) {
                Toast.makeText(this, "请输入账号或密码", Toast.LENGTH_SHORT).show()
            }
            else {
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
                                if (list?.message == "操作成功") {
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
                                    buttton_acc.isEnabled = true
                                    buttton_acc.text = "登陆"
                                    buttton_acc.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.purple_500))
                                    Toast.makeText(
                                    applicationContext,
                                    list?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                                }
                            }

                            override fun onFailure(p0: Call<logindata>, p1: Throwable) {
                                p1.printStackTrace()
                                buttton_acc.isEnabled = true
                                buttton_acc.text = "登陆"
                                buttton_acc.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.purple_500))
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
                    buttton_acc.isEnabled = true
                    buttton_acc.text = "登陆"
                    buttton_acc.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500))
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