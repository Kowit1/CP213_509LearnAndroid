package com.example.a509lablearnandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.a509lablearnandroid.ui.theme._509LabLearnAndroidTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Part11Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _509LabLearnAndroidTheme {
                SkeletonConceptScreen()
            }
        }
    }
}

// สร้าง Custom Modifier เพื่อทำแสงวิบวับ (Shimmer Effect) เลียนแบบโครงกระดูก (Skeleton)
// ใน Compose มาตรฐานยังไม่มี Component นี้ตรงๆ เราจึงสามารถสร้าง Modifier ประกอบร่างสีปัดเงาเองได้
fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition()
    
    // ตั้งค่าแอนิเมชันให้วิ่งจากหน้าไปหลัง ซ้ำวนไปเรื่อยๆ (infiniteRepeatable)
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200)
        ),
        label = "shimmer"
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.LightGray.copy(alpha = 0.6f),
                Color.LightGray.copy(alpha = 0.2f),
                Color.LightGray.copy(alpha = 0.6f),
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned {
        size = it.size
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkeletonConceptScreen() {
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // จำลองการโหลดข้อมูล 3 วินาทีครั้งแรกที่เปิดมา
    LaunchedEffect(Unit) {
        delay(3000)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Skeleton Loading Concept") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // ส่วนอธิบาย Concept
            Text(
                text = "Skeleton Loading คืออะไร?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = "คือเทคนิคการออกแบบ UI เพื่อหลอกสายตา ช่วยบรรเทาความอึดอัดของผู้ใช้งานระหว่างรอระบบดาวน์โหลดข้อมูล แทนที่จะปล่อยให้แสดงหน้าจอขาวโพลน หรือมีแค่ไอคอนหมุนวน (Spinner / CircularProgressIndicator) อย่างเดียว\n\n" +
                       "โดยเราจะทำการเอา 'กล่องสีเทา' ที่มี 'แสงพาดวิบวับ' (Shimmer Effect) ไปวางจัดเรียงให้คล้ายโครงสร้างจริง (Skeleton) ของข้อมูลที่จะเข้ามาเติมเต็มในอนาคต ทำให้แอปดูมีรากฐาน มั่นคง และทำให้ผู้ใช้รู้สึกในทางจิตวิทยากว่าว่าแอปโหลดเร็วกว่าปกติ",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "ตัวอย่างการเปลี่ยนแอนิเมชันตอนโหลด:",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // ส่วนแสดงผลจำลองสถานการณ์ต่างๆ 3 กล่อง
            for (i in 1..3) {
                if (isLoading) {
                    // ตอนกำลังโหลด ให้โชว์โครงกระดูก (กล่องสีเทาติด shimmer)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .shimmerEffect() // ใช้งาน Modifier แสงแวบๆ ที่สร้างด้านบน
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(20.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .shimmerEffect()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(20.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .shimmerEffect()
                            )
                        }
                    }
                } else {
                    // แสดงเนื้อหาข้อมูลจริง หลังดาวน์โหลดเสร็จ
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("IMG", color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("เนื้อหาจริงๆ สมบูรณ์ $i", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("ดาวน์โหลดและประมวลผลให้คุณดูครบถ้วน", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Button(
                onClick = { 
                    isLoading = true 
                    coroutineScope.launch {
                        delay(2500)
                        isLoading = false
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
            ) {
                Text("จำลองการรีเฟรชโหลดข้อมูลใหม่ (2.5 วินาที)")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SkeletonConceptScreenPreview() {
    _509LabLearnAndroidTheme {
        SkeletonConceptScreen()
    }
}
