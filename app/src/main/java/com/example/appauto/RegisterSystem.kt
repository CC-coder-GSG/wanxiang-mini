package com.example.appauto

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputFilter
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.core.view.WindowCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.content.res.ColorStateList

class RegisterSystem : AppCompatActivity() {

    var data_until: String? = null
    var SN : String? = null
    var auth: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_system)

        // 设置状态栏为深色，和 Toolbar 保持一致
        window.statusBarColor = Color.parseColor("#111827")
        // 使用浅色状态栏图标（白色），避免深色背景下看不清
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val button_out = findViewById<Button>(R.id.button_out)
        val button_permanentRegCode = findViewById<Button>(R.id.button_permanentRegCode)
        val button_funtion = findViewById<Button>(R.id.button_funtion)
        val button_trial = findViewById<Button>(R.id.button_trial)
        val trial_editTextText = findViewById<EditText>(R.id.trial_editTextText)
        val trial_correct = findViewById<Button>(R.id.trial_correct)
        val button_tempRegDeadline = findViewById<Button>(R.id.button_tempRegDeadline)

        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // 让返回箭头/三点菜单在深色背景上可见
        toolbar.navigationIcon?.setTint(Color.WHITE)
        toolbar.overflowIcon?.setTint(Color.WHITE)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // 新增：设置输入过滤器（限制非负整数）
        trial_editTextText.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
            // 过滤非数字字符（处理复制粘贴场景）
            val filtered = source.filter { it.isDigit() }
            // 检查输入是否为有效非负整数（允许 0 和正整数）
            if (filtered.isEmpty()) {
                null // 允许删除操作（返回空表示不修改原输入）
            } else {
                // 禁止前导零（例如 "012" 会被过滤为 "12"）
                val currentText = dest.toString()
                val newText =
                    currentText.substring(0, dstart) + filtered + currentText.substring(dend)
                if (newText.startsWith("0") && newText.length > 1) {
                    // 若输入以 0 开头且长度大于 1，只保留最后输入的数字（例如输入 "0" 后再输入 "1"，结果为 "1"）
                    filtered.takeLast(1)
                } else {
                    filtered // 正常输入数字
                }
            }
        })

        //取出intent传递的值
        //判断各个功能按钮的显示
        SN = intent.getStringExtra("SN")
        val tempRegDeadline = intent.getStringExtra("tempRegDeadline")
        auth = intent.getStringExtra("auth")
        val offHostNetTime = intent.getStringExtra("offHostNetTime")
        val offNtripTime = intent.getStringExtra("offNtripTime")
        val lastUpdateTime = intent.getStringExtra("lastUpdateTime")
        val isHostNet = intent.getBooleanExtra("isHostNet", false)
        val isNtrip = intent.getBooleanExtra("isNtrip", false)
        val trialDay = intent.getIntExtra("trialDay", 0)
        val todayCanTrial = intent.getBooleanExtra("todayCanTrial", false)
        val tempRegCodeExpireTime = intent.getStringExtra("tempRegCodeExpireTime")

        //过期时间如果不是空的则说明已经出库，隐藏出库、试用相关功能
        if (tempRegCodeExpireTime != null) {
            button_out.visibility = Button.GONE
            trial_editTextText.visibility = EditText.GONE
            trial_correct.visibility = Button.GONE
            button_trial.visibility = Button.GONE
        }
        //永久码下发按钮显示
        val permanentRegCodeHave = intent.getBooleanExtra("permanentRegCodeHave", false)
        if (permanentRegCodeHave) {
            button_permanentRegCode.visibility = Button.VISIBLE
        } else {
            button_permanentRegCode.visibility = Button.GONE
        }

        //出库按钮点击事件
        button_out.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this)
            //设置对话框标题
            builder.setTitle("提示")
            //设置对话框消息
            builder.setMessage("你确定要出库吗？")
            //设置“确定”按钮
            builder.setPositiveButton("确定") { dialog, which ->
                val retrofit =
                    Retrofit.Builder().baseUrl("https://cloud.sinognss.com/").addConverterFactory(
                        GsonConverterFactory.create()
                    ).build()
                val retrofit_out = retrofit.create(out::class.java)
                if (SN == null) {
                    Toast.makeText(applicationContext, "SN异常，请检查或重试", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    retrofit_out.out(SN.toString(), auth.toString())
                        .enqueue(object : retrofit2.Callback<out_back> {
                            override fun onResponse(p0: Call<out_back>, p1: Response<out_back>) {
                                button_out.visibility = Button.GONE
                                Toast.makeText(
                                    applicationContext,
                                    p1.body()?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            override fun onFailure(p0: Call<out_back>, p1: Throwable) {
                                Toast.makeText(
                                    applicationContext,
                                    "出库时网络请求失败",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        })
                }
            }
            builder.setNegativeButton("取消") { dialog, which ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
            // —— 统一高级风格：按钮与弹窗更协调 ——
            val positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            // 确定：深色主按钮
            positiveBtn.apply {
                isAllCaps = false
                setTextColor(Color.WHITE)
                backgroundTintList = ColorStateList.valueOf(Color.parseColor("#111827"))
                // 让按钮更像卡片内主操作（48dp，高级风格且不依赖 dimen 资源）
                minHeight = (48 * resources.displayMetrics.density).toInt()
                setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)

                // 圆角与取消按钮保持一致（12dp）
                (this as? MaterialButton)?.apply {
                    cornerRadius = (12 * resources.displayMetrics.density).toInt()
                }
            }

            // 取消：白色卡片式次按钮（带轻阴影）
            negativeBtn.apply {
                isAllCaps = false
                setTextColor(Color.parseColor("#111827"))
                backgroundTintList = ColorStateList.valueOf(Color.WHITE)

                // 若为 MaterialButton，可进一步设置描边/圆角/阴影
                (this as? MaterialButton)?.apply {
                    strokeColor = ColorStateList.valueOf(Color.parseColor("#E5E7EB"))
                    strokeWidth = (1 * resources.displayMetrics.density).toInt()
                    cornerRadius = (12 * resources.displayMetrics.density).toInt()
                    elevation = 2f * resources.displayMetrics.density
                }
            }
        }
        //永久码下发按钮点击事件
        button_permanentRegCode.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            //设置对话框标题
            builder.setTitle("提示")
            //设置对话框消息
            builder.setMessage("你确定要下发永久码吗？")
            //设置“确定”按钮
            builder.setPositiveButton("确定") { dialog, which ->
                val retrofit = Retrofit.Builder().baseUrl("https://cloud.sinognss.com/")
                    .addConverterFactory(GsonConverterFactory.create()).build()
                val retrofit_permanent = retrofit.create(permanent_code::class.java)
                if (SN == null) {
                    Toast.makeText(applicationContext, "SN异常，请检查或重试", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    retrofit_permanent.permanent_code(SN.toString(), auth.toString())
                        .enqueue(object : retrofit2.Callback<permanent_code_back> {
                            override fun onResponse(
                                p0: Call<permanent_code_back>,
                                p1: Response<permanent_code_back>
                            ) {
                                if (p1.isSuccessful) {
                                    it.isEnabled = false
                                    it.setBackgroundColor(
                                        ContextCompat.getColor(
                                            this@RegisterSystem,
                                            android.R.color.darker_gray
                                        )
                                    ) // 改变按钮颜色
                                    button_permanentRegCode.text = "永久码已下发"
                                    Toast.makeText(
                                        applicationContext,
                                        p1.body()?.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        applicationContext,
                                        "永久码下发异常",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onFailure(p0: Call<permanent_code_back>, p1: Throwable) {
                                Toast.makeText(
                                    applicationContext,
                                    "永久码下发时网络连接失败",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                }
            }
            builder.setNegativeButton("取消") { dialog, which ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
        }

        //功能授权按钮点击事件
        button_funtion.setOnClickListener {
            // 创建一个包含两个开关 + 底部取消/确定按钮的视图（布局内按钮）
            val view = layoutInflater.inflate(R.layout.dialog_function_authorization, null)
            val switch1 = view.findViewById<SwitchCompat>(R.id.switch1)
            val switch2 = view.findViewById<SwitchCompat>(R.id.switch2)

            if (isNtrip) {
                switch1.isChecked = true
            }
            if (isHostNet) {
                switch2.isChecked = true
            }

            val dialog = AlertDialog.Builder(this)
                .setView(view)
                .create()

            // 去掉系统默认标题/按钮背景，让内容卡片风格更干净
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // 绑定布局内按钮（新样式）
            val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)
            val btnConfirm = view.findViewById<MaterialButton>(R.id.btnConfirm)

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnConfirm.setOnClickListener {
                val isSwitch1Checked = switch1.isChecked
                val isSwitch2Checked = switch2.isChecked

                val gson = GsonBuilder().serializeNulls().create()
                val retrofit = Retrofit.Builder().baseUrl("https://cloud.sinognss.com/")
                    .addConverterFactory(GsonConverterFactory.create(gson)).build()
                val retrofit_function = retrofit.create(function_given::class.java)

                retrofit_function.function_given(
                    function_information(
                        HostNetDeadlineTimeEditable = true,
                        NtripDeadlineTimeEditable = true,
                        companyId = "",
                        initHostNetRegDeadline = offHostNetTime,
                        initNtripRegDeadline = offNtripTime,
                        isHostNet = isSwitch2Checked,
                        isNtrip = isSwitch1Checked,
                        isSelectall = false,
                        keyword = SN,
                        lastUpdateTime = lastUpdateTime,
                        offHostNetTime = offHostNetTime,
                        offNtripTime = offNtripTime,
                        snList = listOf(SN.toString()),
                        trialStatus = null,
                        type = 2
                    ), auth.toString()
                ).enqueue(object : Callback<function_given_back> {
                    override fun onResponse(
                        p0: Call<function_given_back>,
                        p1: Response<function_given_back>
                    ) {
                        if (p1.isSuccessful) {
                            Toast.makeText(
                                applicationContext,
                                p1.body()?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "修改失败，尝试重新登陆罗网或重新查询设备",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(p0: Call<function_given_back>, p1: Throwable) {
                        Toast.makeText(
                            applicationContext,
                            "功能授权时网络连接失败",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })

                // 点击确定后关闭弹窗（逻辑无变化）
                dialog.dismiss()
            }

            dialog.show()
        }

        //试用按钮点击事件
        button_trial.setOnClickListener {
            val retrofit = Retrofit.Builder().baseUrl("https://cloud.sinognss.com/")
                .addConverterFactory(GsonConverterFactory.create()).build()
            val retrofit_trial = retrofit.create(trial::class.java)
            retrofit_trial.trial_given(SN.toString(), 1, auth.toString())
                .enqueue(object : Callback<Trial_back> {
                    override fun onResponse(p0: Call<Trial_back>, p1: Response<Trial_back>) {
                        if (p1.isSuccessful) {
                            Toast.makeText(
                                applicationContext,
                                p1.body()?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(applicationContext, "试用失败", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

                    override fun onFailure(p0: Call<Trial_back>, p1: Throwable) {
                        Toast.makeText(
                            applicationContext,
                            "试用时网络连接失败",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                })
        }

        //修改试用天数系列
        trial_correct.setOnClickListener {
            if (trial_editTextText.text.toString() != "")
            {
                val day = trial_editTextText.text.toString().toInt()
                val retrofit = Retrofit.Builder().baseUrl("https://cloud.sinognss.com/")
                    .addConverterFactory(GsonConverterFactory.create()).build()
                val retrofit_trial = retrofit.create(tiral_updata::class.java)
                retrofit_trial.trial_updata(SN.toString(), day, auth.toString())
                    .enqueue(object : Callback<Trial_back> {
                        override fun onResponse(p0: Call<Trial_back>, p1: Response<Trial_back>) {
                            if (p1.isSuccessful) {
                                Toast.makeText(
                                    applicationContext,
                                    p1.body()?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    "修改试用时间失败",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        override fun onFailure(p0: Call<Trial_back>, p1: Throwable) {
                            Toast.makeText(
                                applicationContext,
                                "修改试用时间时网络连接失败",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                // 隐藏虚拟键盘
                inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
                }
            else{
                Toast.makeText(applicationContext, "试用天数不能输入空值", Toast.LENGTH_SHORT).show()
            }
            }

        //修改注册截至时间点击时间
        button_tempRegDeadline.setOnClickListener {
            showDatePicker()
        }
    }

        private fun showDatePicker() {
            val calendarConstraintsBuilder = CalendarConstraints.Builder()
            val today = MaterialDatePicker.todayInUtcMilliseconds()

            // 计算两年前的今天
            val twoYearsAgo = today - TimeUnit.DAYS.toMillis(365 * 2)
            // 计算三年后的今天
            val threeYearsFromToday = today + TimeUnit.DAYS.toMillis(365 * 3)

            // 设置日期范围，从两年前的今天到三年后的今天
            calendarConstraintsBuilder.setStart(twoYearsAgo)
            calendarConstraintsBuilder.setEnd(threeYearsFromToday)

            // 设置初始打开时显示的日期为今天
            calendarConstraintsBuilder.setOpenAt(today)

            // 使用 CompositeDateValidator 组合日期验证器
            val validators = listOf(
                DateValidatorPointForward.from(twoYearsAgo),
                DateValidatorPointBackward.before(threeYearsFromToday)
            )
            val dateValidator = CompositeDateValidator.allOf(validators)
            calendarConstraintsBuilder.setValidator(dateValidator)

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("选择日期")
                .setTheme(R.style.ThemeOverlay_APPAuto_MaterialDatePicker)
                .setSelection(today)
                .setCalendarConstraints(calendarConstraintsBuilder.build())
                .build()

            datePicker.show(supportFragmentManager, "datePicker")

            datePicker.addOnPositiveButtonClickListener { selection ->
            // 将选择的日期转换为 Date 对象
            val selectedDate = Date(selection)
            // 创建 SimpleDateFormat 对象，指定日期格式
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            // 使用 SimpleDateFormat 格式化日期
            val formattedDate = dateFormat.format(selectedDate).toString()
            // 执行其他操作
            data_until = formattedDate
            //执行网络请求
                val retrofit = Retrofit.Builder().baseUrl("https://cloud.sinognss.com/")
                    .addConverterFactory(GsonConverterFactory.create()).build()
                val retrofit_tempRegDeadline = retrofit.create(tempRegDeadline_f::class.java)
                retrofit_tempRegDeadline.tempRegDeadline_send(
                    tempRegDeadline_send(
                        false,
                        listOf(SN.toString()), data_until.toString()
                    ), auth.toString()
                ).enqueue(object : Callback<Trial_back> {
                    override fun onResponse(p0: Call<Trial_back>, p1: Response<Trial_back>) {
                        if (p1.isSuccessful) {
                            Toast.makeText(
                                applicationContext,
                                p1.body()?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "修改截至时间失败",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(p0: Call<Trial_back>, p1: Throwable) {
                        Toast.makeText(
                            applicationContext,
                            "修改截至时间时网络连接失败",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
        }
    }