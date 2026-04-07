package com.example.a509lablearnandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a509lablearnandroid.ui.theme._509LabLearnAndroidTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContactViewModel : ViewModel() {
    private val _contacts = MutableStateFlow(generateMockData(1, 20))
    val contacts: StateFlow<List<String>> = _contacts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentPage = 1

    fun loadMoreContacts() {
        if (_isLoading.value) return
        
        viewModelScope.launch {
            _isLoading.value = true
            delay(2000) // หน่วงเวลาจำลองโหลด API 2 วินาที
            
            currentPage++
            val newContacts = generateMockData(currentPage, 20)
            val currentList = _contacts.value.toMutableList()
            currentList.addAll(newContacts)
            // เรียงลำดับตามตัวอักษร
            currentList.sort()
            
            _contacts.value = currentList
            _isLoading.value = false
        }
    }

    private fun generateMockData(page: Int, count: Int): List<String> {
        val list = mutableListOf<String>()
        val startOffset = (page - 1) * count
        val alphabets = ('A'..'Z').toList()
        for (i in startOffset until (startOffset + count)) {
            val char = alphabets[i % alphabets.size]
            list.add("$char - Contact Person ${i + 1}")
        }
        return list.sorted()
    }
}

class Part2Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _509LabLearnAndroidTheme {
                val viewModel: ContactViewModel = viewModel()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ContactListScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactListScreen(viewModel: ContactViewModel, modifier: Modifier = Modifier) {
    val contacts by viewModel.contacts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // จัดกลุ่มรายชื่อตามตัวอักษรตัวแรก
    val groupedContacts = remember(contacts) {
        contacts.groupBy { it.first().toString() }
    }

    val listState = rememberLazyListState()

    // ใช้ derivedStateOf เพื่อตรวจสอบว่าเลื่อนถึงสุดหรือไม่ (เหลือบ 2-3 item ล่างก็โหลดล่วงหน้าได้)
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            lastVisibleItemIndex >= totalItemsCount - 3 && totalItemsCount > 0
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !isLoading) {
            viewModel.loadMoreContacts()
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        groupedContacts.forEach { (initial, contactsForInitial) ->
            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = initial,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            items(contactsForInitial) { contact ->
                Text(
                    text = contact,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }

        // แสดง Indicator ด้านล่างสุดเมื่อกำลังโหลดข้อมูลเพิ่มเติม
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}