package com.example.a509lablearnandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.a509lablearnandroid.ui.theme._509LabLearnAndroidTheme

class Part12Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _509LabLearnAndroidTheme {
                DialogAndBottomSheetScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogAndBottomSheetScreen() {
    // สถานะสำหรับโชว์/ซ่อน Dialog
    var showDialog by remember { mutableStateOf(false) }
    // สถานะสำหรับโชว์/ซ่อน Bottom Sheet
    var showBottomSheet by remember { mutableStateOf(false) }
    // รักษาสถานะตำแหน่งและแอนิเมชันของ Bottom Sheet
    val sheetState = rememberModalBottomSheetState()
    
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dialog & Bottom Sheet Concept") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // --- อธิบาย Middle Dialog ---
            Text(
                text = "Middle Dialog (หน้าต่างป๊อปอัป)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Dialog หรือหน้าต่างข้อความตรงกลาง (AlertDialog) เหมาะสำหรับการเรียกร้องความสนใจจากผู้ใช้แบบ 'เร่งด่วนและบังคับสายตา' (Interruptive)\n\n" +
                       "มักจะสงวนสิทธิ์ใช้สำหรับให้ผู้ใช้ 'ยืนยันเพื่อความแน่ใจ', 'ประกาศการเตือน', หรือสิ่งที่ 'ต้องตอบสนองเท่านั้นถึงจะไปต่อหน้าอื่นได้' เช่น การลบของสำคัญออกจากระบบ",
                style = MaterialTheme.typography.bodyLarge
            )

            Button(
                onClick = { showDialog = true }, 
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("แสดง Middle Dialog")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // --- อธิบาย Bottom Sheet ---
            Text(
                text = "Modal Bottom Sheet (หน้าต่างเลื่อนขึ้น)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Modal Bottom Sheet คือการเลื่อนแผงป้ายเมนูขึ้นมาจากล่างหน้าจอ เหมาะสำหรับการให้แสดง 'ตัวเลือกหลายทาง' (Options) หรือ 'การกระทำเสริม' สภาพแวดล้อมที่ 'ไม่ต้องการขัดจังหวะความลื่นไหล' ของผู้ใช้อย่างรุนแรงเท่า Dialog\n\n" +
                       "ข้อดีคือผู้ใช้สามารถเลื่อนแท็บลง (Swipe down) ไปเพื่อปิดหน้าต่างทิ้งได้อย่างเป็นจังหวะอิสระ นิยมใช้กับหน้าเลือกเมนูเพิ่มเติม (More), แชร์ข้อมูลลง Social Media หรือใส่ฟิลเตอร์แบบซับซ้อน",
                style = MaterialTheme.typography.bodyLarge
            )

            Button(
                onClick = { showBottomSheet = true }, 
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("แสดง Modal Bottom Sheet")
            }
        }
    }

    // --- ส่วนตรรกะการเรียกใช้ Middle Dialog ---
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { 
                // ทำงานเมื่อเรากดพื้นที่ว่างๆ นอกกรอบ หรือกดปุ่ม Back 
                showDialog = false 
            },
            title = { Text("คำเตือนสำคัญ") },
            text = { Text("นี่คือ Middle Dialog สำหรับการยืนยันแบบขัดจังหวะ หากไม่กดปุ่ม ก็จะทำงานต่อไม่ได้ คุณแน่ใจหรือไม่ว่าต้องการดำเนินการ?") },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) { Text("ยอมรับและไปต่อ") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("ยกเลิกที") }
            }
        )
    }

    // --- ส่วนตรรกะการเรียกใช้ Modal Bottom Sheet ---
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { 
                // ปิดเมื่อผู้ใช้ปัดทิ้ง
                showBottomSheet = false 
            },
            sheetState = sheetState
        ) {
            // โครงสร้างเนื้อหาที่จะวางใน Bottom Sheet (ลากใส่ Compose ปรุงรูปร่างตามใจชอบได้เลย)
            Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 48.dp)) {
                Text(
                    "การจัดการเพิ่มเติม (Options)", 
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text("- ส่งต่อคัดลอก (Share Link)")
                Spacer(modifier = Modifier.height(12.dp))
                Text("- บันทึกภาพลงเครื่อง (Download Image)")
                Spacer(modifier = Modifier.height(12.dp))
                Text("- เพิ่มลงรายการโปรด (Add to Favorites)")
                
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { showBottomSheet = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ปิดหน้าต่าง")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DialogAndBottomSheetScreenPreview() {
    _509LabLearnAndroidTheme {
        DialogAndBottomSheetScreen()
    }
}
