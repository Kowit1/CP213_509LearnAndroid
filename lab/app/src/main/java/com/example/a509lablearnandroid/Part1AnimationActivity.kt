package com.example.a509lablearnandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.a509lablearnandroid.ui.theme._509LabLearnAndroidTheme

class Part1AnimationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _509LabLearnAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LikeButtonScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun LikeButtonScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var isLiked by remember { mutableStateOf(false) }

        // ดักจับสถานะการกดปุ่ม
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        // 1. Scale Animation: ขยายขนาดเล็กน้อยเมื่อถูกกด
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 1.1f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "scaleAnimation"
        )

        // 2. Color Animation: เปลี่ยนจากสีเทาเป็นสีชมพูเมื่อถูก Like
        val containerColor by animateColorAsState(
            targetValue = if (isLiked) Color(0xFFE91E63) else Color.Gray,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "colorAnimation"
        )

        Button(
            onClick = { isLiked = !isLiked },
            modifier = Modifier.scale(scale),
            interactionSource = interactionSource,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = Color.White
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 3. AnimatedVisibility สำหรับ Icon รูปหัวใจ
                AnimatedVisibility(visible = isLiked) {
                    Row {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Heart Icon"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
                
                Text(text = if (isLiked) "Liked" else "Like")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LikeButtonScreenPreview() {
    _509LabLearnAndroidTheme {
        LikeButtonScreen()
    }
}