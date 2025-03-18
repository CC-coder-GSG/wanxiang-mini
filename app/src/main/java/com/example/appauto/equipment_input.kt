package com.example.appauto

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class equipment_input : AppCompatActivity() {
    private lateinit var dbHelper: DBManager
    private var auth: String? = null
    private var currentPopupWindow: PopupWindow? = null // 用于跟踪当前的气泡
    private var isPopupClicked = false // 新增标志位
    private var declarCompanyId: String? = null
    private var decalrId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_equipment_input)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        //录入按钮定义
        val button_input = findViewById<Button>(R.id.button_input)
        //定义各个输入框
        val edit = findViewById<EditText>(R.id.editText)
        val edit1 = findViewById<EditText>(R.id.editText1)
        val edit2 = findViewById<EditText>(R.id.editText2)
        val edit3 = findViewById<EditText>(R.id.editText3)
        val text_jingxiaoshang = findViewById<TextView>(R.id.text_jingxiaoshang)
        val radiogroup = findViewById<RadioGroup>(R.id.radioGroup2)
        val radiobutton_active = findViewById<RadioButton>(R.id.radioButton_active)
        val radiobutton_inactive = findViewById<RadioButton>(R.id.radioButton_inactive)
        //默认配套时长为1
        edit3.setText("1")
        //键盘事件
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        //连接本地sqlite数据库
        dbHelper = DBManager(this)
        dbHelper.openDatabase()
        val db = dbHelper.database

        //RadioGroup选择事件
        var isActive: Boolean? = null
        radiogroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radioButton_active -> {
                    isActive = true
                }

                R.id.radioButton_inactive -> {
                    isActive = false
                }
            }
        }

        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(com.google.android.material.R.drawable.ic_arrow_back_black_24)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        //选择经销商输入框，当文本发生改变时的时间
        edit1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if (isPopupClicked) {
                    // 如果是点击气泡导致的文本变化，不执行后续逻辑
                    isPopupClicked = false
                    return
                }
                else{
                    //获取access_token
                    val sql = "select * from luowang where user=\"345\""
                    val dataresult = db.rawQuery(sql, null)
                    dataresult.moveToFirst()
                    val access_token =
                        dataresult.getString(dataresult.getColumnIndexOrThrow("accesstoken"))
                    auth = "bearer $access_token"
                    dataresult.close()

                    val jingxiaoshang = p0.toString()
                    if (jingxiaoshang.isNotEmpty()) {
                        //网络请求部分
                        val gson = GsonBuilder().serializeNulls().create()
                        val retrofit_jingxiaoshang =
                            Retrofit.Builder().baseUrl("https://cloud.sinognss.com/")
                                .addConverterFactory(
                                    GsonConverterFactory.create(gson)
                                ).build()
                        val jingxiaoshang_retrofit =
                            retrofit_jingxiaoshang.create(equipment_input_search::class.java)
                        jingxiaoshang_retrofit.equipment_input_search(
                            jingxiaoshang,
                            auth.toString()
                        )
                            .enqueue(object : Callback<equipment_input_search_back> {
                                override fun onResponse(
                                    p0: Call<equipment_input_search_back>,
                                    p1: Response<equipment_input_search_back>
                                ) {
                                    if (p1.body()?.data?.size != 0) {
                                        val response_jingxiaoshang =
                                            p1.body()?.data?.get(0)?.companyName.toString()
                                        val response_jingxiaoshang_tele =
                                            p1.body()?.data?.get(0)?.tel.toString()
                                        val companyId = p1.body()?.data?.get(0)?.companyId.toString()
                                        val managerId = p1.body()?.data?.get(0)?.managerId.toString()
                                        runOnUiThread {
                                            showPopupWindow(
                                                response_jingxiaoshang,
                                                response_jingxiaoshang_tele,
                                                edit1,
                                                text_jingxiaoshang,
                                                companyId,
                                                managerId
                                            )
                                        }
                                    } else {
                                        showNonePopupWindow(edit1)
                                    }
                                }

                                override fun onFailure(
                                    p0: Call<equipment_input_search_back>,
                                    p1: Throwable
                                ) {
                                    Toast.makeText(
                                        applicationContext,
                                        "查询经销商时连接失败",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            })
                    }
                }
            }
        })


        //录入按钮点击事件
        button_input.setOnClickListener {
            //获取access_token
            val sql = "select * from luowang where user=\"345\""
            val dataresult = db.rawQuery(sql, null)
            dataresult.moveToFirst()
            val access_token =
                dataresult.getString(dataresult.getColumnIndexOrThrow("accesstoken"))
            auth = "bearer $access_token"
            dataresult.close()

            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)

            val SN = edit.text.toString()
            //val jingxiaoshang = text_jingxiaoshang.text.toString()
            val remark = edit2.text.toString()
            val time_gift = edit3.text.toString()
            //请求部分
            if (SN == "" && declarCompanyId == null && time_gift == "" && decalrId == null && isActive == null) {
                Toast.makeText(
                    applicationContext,
                    "请填入必要信息",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else {
                val gson = GsonBuilder().serializeNulls().create()
                val retrofit = Retrofit.Builder().baseUrl("https://cloud.sinognss.com/")
                    .addConverterFactory(GsonConverterFactory.create(gson)).build()
                val equipmentInputInterface = retrofit.create(equipment_input_retrofit::class.java)
                equipmentInputInterface.equipment_input(
                    SN,
                    declarCompanyId,
                    decalrId,
                    remark,
                    isActive,
                    time_gift,
                    auth.toString()
                ).enqueue(object : Callback<equipment_input_callback> {
                    override fun onResponse(
                        p0: Call<equipment_input_callback>,
                        p1: Response<equipment_input_callback>
                    ) {
                        val builder = android.app.AlertDialog.Builder(this@equipment_input)
                        // 设置对话框的标题
                        builder.setTitle("提示")
                        // 设置对话框的消息内容
                        builder.setMessage(p1.body()?.message)
                        // 设置对话框的确认按钮
                        builder.setPositiveButton("确定") { dialog, which ->
                            dialog.dismiss()
                        }
                        // 创建并显示对话框
                        val dialog = builder.create()
                        dialog.show()
                    }

                    override fun onFailure(p0: Call<equipment_input_callback>, p1: Throwable) {
                        Toast.makeText(
                            applicationContext,
                            "录入设备时连接失败",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                })
            }
        }
    }

    //插入菜单（罗网账号）
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val button_luowang = item.itemId
        if (button_luowang == R.id.luowang) {
            val intent = Intent(this, lwaccount::class.java)
            startActivity(intent)
        }
        return true
    }

    //函数-生成弹出框
    @SuppressLint("SetTextI18n")
    private fun showPopupWindow(data: String?, tel: String?, edit1: EditText, text_jingxiaoshang: TextView, companyId:String?, managerId:String?) {
        currentPopupWindow?.dismiss()

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_layout, null)
        val textView = popupView.findViewById<TextView>(R.id.popup_text)

        textView.text = data + "   $tel"

        currentPopupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            false
        )

        // 设置背景，使外部触摸事件生效
        currentPopupWindow?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // 设置可触摸外部
        currentPopupWindow?.isOutsideTouchable = true

        // 设置触摸拦截器，点击外部关闭气泡
        currentPopupWindow?.setTouchInterceptor { view, event ->
            if (event.action == MotionEvent.ACTION_OUTSIDE) {
                currentPopupWindow?.dismiss()
                return@setTouchInterceptor true
            }else if (event.action == MotionEvent.ACTION_UP) {
                view.performClick() // 调用 performClick 方法处理点击事件
            }
            false
        }

        currentPopupWindow?.showAsDropDown(edit1)

        popupView.setOnClickListener {
            isPopupClicked = true // 设置标志位
            declarCompanyId = companyId
            decalrId = managerId
            edit1.setText(data)
            text_jingxiaoshang.text = "$data\ncompanyId:$companyId\nmanagerId:$managerId"
            currentPopupWindow?.dismiss()
        }
    }

    private fun showNonePopupWindow(edit1: EditText) {
        currentPopupWindow?.dismiss()

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_layout, null)
        val textView = popupView.findViewById<TextView>(R.id.popup_text)

        textView.text = "无数据"

        currentPopupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            false
        )

        // 设置背景，使外部触摸事件生效
        currentPopupWindow?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // 设置可触摸外部
        currentPopupWindow?.isOutsideTouchable = true

        // 设置触摸拦截器，点击外部关闭气泡
        currentPopupWindow?.setTouchInterceptor { view, event ->
            if (event.action == MotionEvent.ACTION_OUTSIDE) {
                currentPopupWindow?.dismiss()
                return@setTouchInterceptor true
            }else if (event.action == MotionEvent.ACTION_UP) {
                view.performClick() // 调用 performClick 方法处理点击事件
            }
            false
        }

        currentPopupWindow?.showAsDropDown(edit1)
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.closeDatabase()
    }
}