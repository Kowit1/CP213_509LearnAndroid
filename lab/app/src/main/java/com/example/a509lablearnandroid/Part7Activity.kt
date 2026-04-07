package com.example.a509lablearnandroid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityOptionsCompat
import com.example.a509lablearnandroid.ui.theme._509LabLearnAndroidTheme

class Part7Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _509LabLearnAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(onClick = {
                            val intent = Intent(this@Part7Activity, Part7DetailActivity::class.java).apply {
                                putExtra("EXTRA_MESSAGE", "ส่งข้อความทักทายไปยังหน้า DetailActivity")
                            }
                            
                            // ใช้ ActivityOptionsCompat สร้าง Custom Animation (Slide Up) ตอนหน้าจอใหม่เปิด
                            // R.anim.slide_in_up: ให้หน้าใหม่สไลด์ขึ้นมา
                            // R.anim.hold: ให้หน้าเก่าอยู่กับที่
                            val options = ActivityOptionsCompat.makeCustomAnimation(
                                this@Part7Activity,
                                R.anim.slide_in_up,
                                R.anim.hold
                            )
                            
                            startActivity(intent, options.toBundle())
                        }) {
                            Text("Open Detail (Slide Up)")
                        }
                    }
                }
            }
        }
    }
}

class Part7DetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // รับค่า String จาก Intent
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "No Message"

        setContent {
            _509LabLearnAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Message Received:\n$message", 
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                            Button(onClick = {
                                // ปิดหน้าต่างนี้
                                finish()
                            }) {
                                Text("Close Detail (Slide Down)")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun finish() {
        super.finish()
        // สั่ง Override การจางหายไป ให้เป็นสไลด์ลงล่าง
        // (รองรับใน Android หลายเวอร์ชั่นเก่าๆ ได้ หากรันบน SDK สูงอาจจะมีเตือนว่า deprecate แต่วิธีนี้ทำความเข้าใจง่ายที่สุด)
        overridePendingTransition(R.anim.hold, R.anim.slide_out_down)
    }
}
