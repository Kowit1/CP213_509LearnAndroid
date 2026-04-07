package com.example.a509lablearnandroid

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a509lablearnandroid.ui.theme._509LabLearnAndroidTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WebViewModel : ViewModel() {
    // กำหนด URL เริ่มต้น
    private val _url = MutableStateFlow("https://www.google.com")
    val url: StateFlow<String> = _url.asStateFlow()

    fun updateUrl(newUrl: String) {
        var formattedUrl = newUrl
        // ช่วยเติม https:// ให้อัตโนมัติหากผู้ใช้ไม่ได้พิมพ์มา เพื่อให้ WebView เรียกใช้งานได้
        if (!formattedUrl.startsWith("http://") && !formattedUrl.startsWith("https://")) {
            formattedUrl = "https://$formattedUrl"
        }
        _url.value = formattedUrl
    }
}

class Part6Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _509LabLearnAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val viewModel: WebViewModel = viewModel()
                    WebViewScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun WebViewScreen(viewModel: WebViewModel, modifier: Modifier = Modifier) {
    // อัปเดต state ของ URL เพื่อบังคับให้ AndroidView ไปเรียกใช้ update block
    val currentUrl by viewModel.url.collectAsState()
    
    // State สำหรับ TextField เวลากรอกข้อความ
    var inputUrl by remember { mutableStateOf(currentUrl) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputUrl,
                onValueChange = { inputUrl = it },
                modifier = Modifier.weight(1f),
                label = { Text("Enter URL") },
                singleLine = true
            )
            Button(
                onClick = { viewModel.updateUrl(inputUrl) },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Go")
            }
        }

        /* 
         * AndroidView ทำหน้าเชื่อมระหว่าง View ปกติของ Android XML 
         * factory: ทำงานครั้งเดียวตอนสร้าง View ตั้งค่าแรกเริ่ม
         * update: ทำงานทุกครั้งที่ State บน Compose เปลี่ยน (Recomposition)
         */
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = WebViewClient() // กำหนดไม่ให้เว็บบราวเซอร์แยกตัวเปิดแอปนอก
                }
            },
            update = { webView ->
                // โหลด URL ใหม่ทุกครั้งที่ค่าของ currentUrl จาก ViewModel โดนอัปเดต
                webView.loadUrl(currentUrl)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
