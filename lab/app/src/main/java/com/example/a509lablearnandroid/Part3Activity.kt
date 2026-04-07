package com.example.a509lablearnandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.a509lablearnandroid.ui.theme._509LabLearnAndroidTheme

class Part3Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _509LabLearnAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedDonutChart(
                            values = listOf(30f, 40f, 30f),
                            colors = listOf(
                                Color(0xFFE91E63), 
                                Color(0xFF2196F3), 
                                Color(0xFFFFC107)
                            ),
                            modifier = Modifier.size(250.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedDonutChart(
    values: List<Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    // หาผลรวมของตัวเลขทั้งหมด
    val total = values.sum()
    
    // แปลงสัดส่วนแต่ละตัวให้เป็นองศาเต็มต่อ 360 องศา
    val sweepAngles = values.map { it * 360f / total }

    // ตัวแปรเก็บสถานะการเคลื่อนไหว (Animation) วิ่งจาก 0 ไป 1
    val sweepProgress = remember { Animatable(0f) }

    // เล่น Animation ทันทีที่ Composable เริ่มทำงาน
    LaunchedEffect(Unit) {
        sweepProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500) // เวลาที่ใช้วาดครบวง (1.5 วินาที)
        )
    }

    Canvas(modifier = modifier) {
        var startAngle = -90f // เริ่มต้นวาดที่หน้าปัดทิศ 12 นาฬิกา (บนสุด)

        for (i in sweepAngles.indices) {
            // คำนวณองศาที่จะวาดสัมพันธ์กับ Animation (ค่อยๆ เพิ่มไปจนสุดที่ 1f)
            val animatedSweepAngle = sweepAngles[i] * sweepProgress.value

            drawArc(
                color = colors.getOrElse(i) { Color.Gray }, // ดึงสีมาคู่กับลำดับของ value
                startAngle = startAngle,
                sweepAngle = animatedSweepAngle,
                useCenter = false, // ไม่ต้องถมสีเข้าไปในจุดศูนย์กลาง เพื่อให้เป็นโดนัท
                style = Stroke(
                    width = 40.dp.toPx(), // ความหนาของเส้น (ขอบโดนัท)
                    cap = StrokeCap.Butt // รูปแบบการตัดขอบเส้น (ใช้ Butt เพื่อให้รอยต่อชนกันพอดีไม่เหลื่อม)
                )
            )

            // เอาองศาส่วนที่วาดไปแล้วเต็มๆ มาคำนวณเป็นจุดเริ่มต้นของเศษเนื้อชิ้นถัดไป
            startAngle += sweepAngles[i]
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnimatedDonutChartPreview() {
    _509LabLearnAndroidTheme {
        AnimatedDonutChart(
            values = listOf(30f, 40f, 30f, 20f),
            colors = listOf(
                Color(0xFFE91E63),
                Color(0xFF2196F3),
                Color(0xFFFFC107),
                Color(0xFF4CAF50)
            ),
            modifier = Modifier.size(250.dp)
        )
    }
}