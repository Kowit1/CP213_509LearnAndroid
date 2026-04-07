package com.example.a509lablearnandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.a509lablearnandroid.ui.theme._509LabLearnAndroidTheme

class Part9Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _509LabLearnAndroidTheme {
                CollapsingToolbarScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingToolbarScreen() {
    // 1. สร้าง ScrollBehavior ที่เป็นตัวกำหนดพฤติกรรมการย่อ/ขยายของ AppBar
    // exitUntilCollapsedScrollBehavior: พับ AppBar จนเหลือขนาดปกติ (Small) เมื่อเลื่อนลง และขยายสุดเมื่อเลื่อนกลับไปบนสุด
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        // 2. ผูก nestedScroll Connection เข้ากับ Scaffold เพื่อให้ Scroll Event จาก LazyColumn สื่อสารมาคุยให้ AppBar หด/ขยายได้
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        "Collapsing Concept",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* ไม่ได้ทำ action ให้แค่แสดงประกอบ */ }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menu Icon"
                        )
                    }
                },
                // 3. นำ Behavior ที่สร้างมาใส่เข้ากับ AppBar ด้วย prop scrollBehavior
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text(
                    text = "Concept ของ Collapsing App Bar (Material 3)",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp)
                )
            }
            item {
                Text(
                    text = "Collapsing Toolbar หรือ App Bar แบบยืดหดได้ เป็นเทคนิค UI ที่ช่วยให้แอปพลิเคชันสามารถแสดงหัวข้อใหญ่ๆ หรือรูปภาพปกในตอนแรกได้อย่างเต็มตา และเมื่อผู้ใช้เริ่มเลื่อนดูเนื้อหาในลิสต์ (Scroll down) ไปเรื่อยๆ พื้นที่ของ App Bar จะค่อยๆ เล็กลง (พับตัว) เพื่อคืนพื้นที่แสดงผลเนื้อหาบนหน้าจอให้มากขึ้น\n\n" +
                           "ในระบบของ Jetpack Compose (หัวใจของ Material 3) การทำ Collapsing จะประกอบไปด้วยกลไกส่วนสำคัญ 3 อย่าง ได้แก่:\n\n" +
                           "1. ScrollBehavior: เป็นกฏสำหรับกำหนดพฤติกรรมของ AppBar เช่น การใช้ TopAppBarDefaults.exitUntilCollapsedScrollBehavior() ซึ่งแปลกว่าจะเล็กลงไปจนถึงขนาดปกติสุด ไม่หายไปมิดจอ\n\n" +
                           "2. NestedScrollConnection: หน้าที่ของส่วนนี้คือช่วย 'รับรู้และส่งต่อ' การเลื่อนนิ้วของข้อมูลในลิสต์ด้านล่าง (เช่น LazyColumn) ขึ้นมาข้างบน เราส่งต่อพฤติกรรมได้โดยวาง Modifier.nestedScroll(scrollBehavior.nestedScrollConnection) ไว้ที่ตัว Scaffold หรือ Parent Container หลัก\n\n" +
                           "3. App Bar Component: มักจะใช้ LargeTopAppBar หรือ MediumTopAppBar แล้วส่งค่าตัวแปร scrollBehavior ที่สร้างไว้มาเป็น Parameter เพื่อบอกให้ตัว TopBar มันม้วนตัวตอบสนองต่อนิ้วของเรา",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(30) { index ->
                Text(
                    text = "รายการเนื้อหาทดสอบที่ ${index + 1}",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CollapsingToolbarScreenPreview() {
    _509LabLearnAndroidTheme {
        CollapsingToolbarScreen()
    }
}
