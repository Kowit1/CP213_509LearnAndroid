package com.example.a509lablearnandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.a509lablearnandroid.ui.theme._509LabLearnAndroidTheme

class Part8Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _509LabLearnAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AdaptiveProfileScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AdaptiveProfileScreen(modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        if (maxWidth < 600.dp) {
            // หน้าจอแคบ (เช่น มือถือแนวตั้ง) ให้แสดงผลแบบ Column
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfilePicture()
                Spacer(modifier = Modifier.height(24.dp))
                ProfileInfo(modifier = Modifier.fillMaxWidth())
            }
        } else {
            // หน้าจอกว้าง (เช่น มือถือแนวนอน หรือ แท็บเล็ต) ให้แสดงผลแบบ Row
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfilePicture()
                Spacer(modifier = Modifier.width(32.dp))
                
                // ใช้ weight เพื่อให้ ProfileInfo กินพื้นที่ที่เหลือในแนวนอนทั้งหมด
                ProfileInfo(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ProfilePicture() {
    // กล่องสมมติสีเทาแทน 'รูปโปรไฟล์'
    Box(
        modifier = Modifier
            .size(150.dp)
            .clip(CircleShape)
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Profile Pic", color = Color.DarkGray)
    }
}

@Composable
fun ProfileInfo(modifier: Modifier = Modifier) {
    // ข้อมูลส่วนตัวแสดงเรียงต่อกันแนวตั้งเสมอ
    Column(modifier = modifier) {
        Text(text = "สมชาย ใจดี", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "นักพัฒนาแอปพลิเคชัน", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "มีความมุ่งมั่นที่จะพัฒนาแอปพลิเคชันให้สามารถใช้งานได้ทุกรูปแบบหน้าจอ ไม่ว่าจะเป็นทีวี แท็บเล็ต หรือมือถือรุ่นเล็กสุด ด้วย Compose Adaptive Layout",
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AdaptivePreviewPortrait() {
    _509LabLearnAndroidTheme {
        AdaptiveProfileScreen()
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun AdaptivePreviewLandscape() {
    _509LabLearnAndroidTheme {
        AdaptiveProfileScreen()
    }
}
