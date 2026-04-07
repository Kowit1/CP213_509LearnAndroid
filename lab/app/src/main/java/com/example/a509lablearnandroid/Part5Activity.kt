package com.example.a509lablearnandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a509lablearnandroid.ui.theme._509LabLearnAndroidTheme
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SideEffectViewModel : ViewModel() {
    // ใช้ Channel สำหรับส่ง One-time Event เช่น แสดงข้อความใน Snackbar ไม่ให้โผล่ซ้ำเวลามี Recomposition
    private val _errorChannel = Channel<String>()
    val errorFlow = _errorChannel.receiveAsFlow()

    fun triggerError() {
        viewModelScope.launch {
            _errorChannel.send("เกิดข้อผิดพลาดในการเชื่อมต่อเครือข่าย!")
        }
    }
}

class Part5Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _509LabLearnAndroidTheme {
                val viewModel: SideEffectViewModel = viewModel()
                val snackbarHostState = remember { SnackbarHostState() }

                // Observe One-time event จาก Flow หรือ Channel ภายใน LaunchedEffect (Side Effect)
                // เพื่อหลีกเลี่ยงปัญหาการเรียกฟังก์ชันโชว์ Snackbar ซ้ำหากเกิดการจัดเรียงหน้าจอ (Recomposition) ใหม่
                LaunchedEffect(Unit) {
                    viewModel.errorFlow.collect { errorMessage ->
                        snackbarHostState.showSnackbar(message = errorMessage)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(onClick = { viewModel.triggerError() }) {
                            Text("Trigger Error")
                        }
                    }
                }
            }
        }
    }
}
