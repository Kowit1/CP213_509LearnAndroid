package com.example.a509lablearnandroid

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.a509lablearnandroid.ui.theme._509LabLearnAndroidTheme

class Part10Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _509LabLearnAndroidTheme {
                AppWidgetConceptScreen()
            }
        }
    }
}

// โค้ดตัวอย่างเพื่อให้เห็นหน้าตาของคลาสควบคุม App Widget ขั้นพื้นฐาน (ฝั่ง Android แบบดั้งเดิม)
// หากต้องการใช้งานจริงจะต้องทำการตั้งค่า <receiver> ลงใน AndroidManifest.xml ประกอบด้วย
class ExampleWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        // วนลูปอัปเดต Widget ทุกตัวที่ผู้ใช้นำไปวางบน Home Screen
        for (appWidgetId in appWidgetIds) {
            // การจัดการ UI ปกติบน Widget จะต้องเรียกผ่าน RemoteViews
            // val views = RemoteViews(context.packageName, R.layout.widget_layout)
            // views.setTextViewText(R.id.textView, "Widget Updated!")
            // appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppWidgetConceptScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Widget Concept") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text(
                    text = "App Widget คืออะไร?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
                )
                Text(
                    text = "App Widget เป็นแอปพลิเคชันรูปแบบขนาดย่อส่วน (Miniature Application Views) ที่เปิดทางให้เราสร้างหน้าต่างย่อยไปฝังอยู่บนหน้าจอเริ่มต้น (Home Screen) ของโทรศัพท์ได้ ช่วยให้ผู้ใช้สามารถรับรู้ข้อมูลและสั่งงานแอปได้ทันทีโดยไม่ต้องกดเข้ามาเปิดแอปพลิเคชันเต็มๆ ของเรา",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            item {
                Text(
                    text = "ส่วนประกอบสำคัญในการทำ Widget พื้นฐาน",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
                )
                Text(
                    text = "1. AppWidgetProviderInfo (ไฟล์ XML): เป็นไฟล์ระบบประเภท Meta-data ที่บอก Launcher ว่า Widget ตัวนี้ต้องการขนาดพื้นที่กี่ตารางกริดบนหน้าจอ?, อัปเดตข้อมูลอัตโนมัติรอบละกี่นาที? และหน้าตาตอนแรกเป็นไปตาม Layout ใด\n\n" +
                           "2. AppWidgetProvider (คลาส): เป็นคลาสลูกของ BroadcastReceiver คอยจัดการเกี่ยวกับวงจรชีวิตและอีเวนต์ต่างๆ โดยมีเมธอดหลักคือ onUpdate() เพื่อเขียนโปรแกรมเปลี่ยนข้อมูลบนหน้าจอ\n\n" +
                           "3. คลาสคำสั่ง RemoteViews: การทำงานบนหน้า Home Screen นั้น ตัวแอปเราไม่ได้มีสิทธิครอบครอง View หน้าจอด้วยตัวเองโดยตรง ดังนั้นจึงไม่สามารถใช้ Composable ปกติหรือ View ปกติได้ ต้องพึ่งคลาสพิเศษชื่อ RemoteViews ไปฝากจัดการให้แทน\n\n" +
                           "💡 Jetpack Glance: ในอดีตการสร้าง UI ของ Widget ยากมากเพราะจำกัดอยู่แค่ XML แต่ยุคใหม่ทาง Google ได้เปิดตัว Library ที่ชื่อว่า 'Glance' ทำให้เราสามารถใช้ภาษา และ Syntax แบบเดียวกับ Jetpack Compose เขียนตัว Widget ได้แล้ว!",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            item {
                Text(
                    text = "⚠️ ข้อควรระวังขั้นสูง",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
                )
                Text(
                    text = "การทำ Widget จะถูกประมวลผลอยู่บนสาย Process ระบบของ Launcher ไม่ใช่บน Process ของแอปเราเหมือนปกติ การสั่งอัปเดตแอนิเมชันที่ถี่ยิบ หรือการคำนวณที่หนักหน่วงจะชะงักการทำงานของ OS และสูบแบตเตอรี่แบบมหาศาล ระบบจึงมักจำกัดการผูกรอบการอัปเดตอัตโนมัติ (Update Period) ให้ห่างกันได้อย่างต่ำหลักหลายสิบนาทีเท่านั้น",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppWidgetConceptScreenPreview() {
    _509LabLearnAndroidTheme {
        AppWidgetConceptScreen()
    }
}
