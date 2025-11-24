package com.example.appauto

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException


class FirstActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //添加返回按钮


        setContentView(R.layout.first_layout)
        val button1 = findViewById<Button>(R.id.button1)
        button1.setOnClickListener{
            val intent = Intent("com.example.appauto.ACTION_START")
            startActivity(intent)
        }

        val button_list = findViewById<Button>(R.id.button_list)
        button_list.setOnClickListener{
            val intent1 = Intent(this, ListviewActivity::class.java)
            startActivity(intent1)
        }

        val button_scaner = findViewById<Button>(R.id.button_scaner)
        button_scaner.setOnClickListener{
            val intent2 = Intent(this, Label::class.java)
            startActivity(intent2)
        }

        val button_taojian = findViewById<Button>(R.id.button_taojian)
        button_taojian.setOnClickListener{
            val intent5 = Intent(this, Taojian_List::class.java)
            startActivity(intent5)
        }

        val button_kq = findViewById<Button>(R.id.button_kq)
        button_kq.setOnClickListener{
            val intent3 = Intent(this, kuaqu::class.java)
            startActivity(intent3)
        }

        val button_register = findViewById<Button>(R.id.button_register)
        button_register.setOnClickListener{
            val intent3 = Intent(this, Register::class.java)
            startActivity(intent3)
        }

        val button_software_register = findViewById<Button>(R.id.button_software_register)
        button_software_register.setOnClickListener{
            val intent4 = Intent(this, softwore_register::class.java)
            startActivity(intent4)
        }

        val button_account_cors = findViewById<Button>(R.id.button_account_cors)
        button_account_cors.setOnClickListener {
            val intent5 = Intent(this, account_of_cors::class.java)
            startActivity(intent5)
        }

    }
    /*
        override fun onCreateOptionsMenu(menu: Menu?): Boolean {
            menuInflater.inflate(R.menu.main, menu)
            return true
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            when(item.itemId){
                R.id.add_item -> Toast.makeText(this, "You clicked Add",
                    Toast.LENGTH_SHORT).show()
                R.id.remove_item -> Toast.makeText(this, "You clicked Remove",
                    Toast.LENGTH_SHORT).show()

            }
            return true
        }
        */
}