package com.example.appauto

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class kuaqu : AppCompatActivity() {
    private lateinit var dbHelper: DBManager
    private var id : Int? = null
    private var auth : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_kuaqu)

        // 设置状态栏为深色，和 Toolbar 保持一致
        window.statusBarColor = Color.parseColor("#111827")
        // 使用浅色状态栏图标（白色），避免深色背景下看不清
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val text = findViewById<TextView>(R.id.editText_kq)
        val text_kq = findViewById<TextView>(R.id.textView)
        val button_correct = findViewById<Button>(R.id.correct)
        //定义设备录入按钮
        val button_equipment_input = findViewById<Button>(R.id.button_input)
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager


        dbHelper = DBManager(this)
        dbHelper.openDatabase()
        val db = dbHelper.database

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // 让返回箭头/三点菜单在深色背景上可见
        toolbar.navigationIcon?.setTint(Color.WHITE)
        toolbar.overflowIcon?.setTint(Color.WHITE)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        //retrofit网络连接
        val button_kq = findViewById<Button>(R.id.button_kq)
        button_kq.setOnClickListener {
            //获取access_token
            val sql = "select * from luowang where user=\"345\""
            val dataresult = db.rawQuery(sql, null)
            dataresult.moveToFirst()
            val access_token =
                dataresult.getString(dataresult.getColumnIndexOrThrow("accesstoken"))
            auth = "bearer $access_token"
            dataresult.close()

            //获取SN号
            val SN = text.text
            if (SN.isNullOrBlank()) {
                Toast.makeText(this, "请输入SN号", Toast.LENGTH_SHORT).show()
            }
            else {
                val retrofit = Retrofit.Builder().baseUrl("https://cloud.sinognss.com/")
                    .addConverterFactory(GsonConverterFactory.create()).build()
                val Receiverinfo = retrofit.create(getid::class.java)
                Receiverinfo.get_luowang_id(1,15,"{\"name\":\"$SN\",\"status\":null}", auth.toString()).enqueue(object : Callback<receiver>{
                    override fun onResponse(p0: Call<receiver>, p1: Response<receiver>) {
                        val list = p1.body()
                        if (list != null) {
                            if(list.data.list.isEmpty()) {
                                Toast.makeText(
                                    applicationContext,
                                    "未查询到该SN号",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            else {
                                if(list.data.list.size == 1) {
                                    id = list.data.list[0].id
                                    text_kq.text = " 设备类型：${list.data.list[0].deviceType}\n 设备SN：${list.data.list[0].sn}\n 经销商：${list.data.list[0].salesName}\n 备注：${list.data.list[0].remark}\n 罗网剩余时长：${list.data.list[0].remainingTime}\n 罗网id:${list.data.list[0].id}"
                                    button_correct.visibility = Button.VISIBLE
                                }
                                else{
                                    Toast.makeText(applicationContext, "查询到多个SN，未避免误操作，请检查SN号码", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        else{
                            Toast.makeText(applicationContext, "请重新登录", Toast.LENGTH_SHORT).show()
                            text_kq.text = ""
                        }
                    }

                    override fun onFailure(p0: Call<receiver>, p1: Throwable) {
                        p1.printStackTrace()
                        Toast.makeText(applicationContext,"查询时连接失败",Toast.LENGTH_SHORT).show()
                        text_kq.text = ""
                    }
                })
            }
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }

        text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 文本改变前的回调
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                button_correct.visibility = Button.INVISIBLE
                text_kq.text = ""
            }

            override fun afterTextChanged(s: Editable?) {
                // 文本改变后的回调
            }
        })

        button_correct.setOnClickListener{
            text.text = ""
            // 在 kuaqu 类中，导航到 kuaqu_correct
            val intent = Intent(this, kuaqu_correct::class.java)
            intent.putExtra("id", id) // 将 id 值添加到 Intent 中
            intent.putExtra("auth",auth)
            startActivity(intent)
        }

        //设备录入按钮导航到设备录入界面
        button_equipment_input.setOnClickListener {
            val intent1 = Intent(this, equipment_input::class.java)
            startActivity(intent1)
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val button_luowang = item.itemId
        if (button_luowang==R.id.luowang){
            val intent = Intent(this, lwaccount::class.java)
            startActivity(intent)
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.closeDatabase()
    }
}