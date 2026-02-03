package com.example.a509lablearnandroid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateBounds
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a509lablearnandroid.ui.theme._509LabLearnAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RPGCardView(
                onNextActivity = {
                    startActivity(Intent(this, ListActivity::class.java))
                }
            )


        }
    }
}
@Composable
fun RPGCardView(onNextActivity: () -> Unit){
    val locale = Locale.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
            .padding(32.dp)) {
        //hp
        Box (modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(color = Color.LightGray)
        ){
            Text(
                text = "HP",
                modifier = Modifier
                    .align(alignment = Alignment.CenterStart)
                    .fillMaxWidth(fraction = 0.20f)
                    .background(color = Color.Green)
                    .padding(8.dp)
            )

        }

        //image
        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "Profile",
            modifier = Modifier
                .size(300.dp)
                .align((Alignment.CenterHorizontally))
                .padding(top = 16.dp)
                .clickable {
                    onNextActivity.invoke()

                }
        )
        var str by remember { mutableStateOf(10) }
        var int by remember { mutableStateOf(10) }
        var agi by remember { mutableStateOf(10) }
        var cat by remember { mutableStateOf(100) }

        //status
        Row (
            modifier = Modifier.fillMaxWidth().background(color = Color.LightGray).padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ){
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
            ){
                Button(onClick = {
                    str++
                }) {
                    Image(
                        painter = painterResource(R.drawable.outline_arrow_circle_up_24),
                        contentDescription = "up",
                        modifier = Modifier.size(30.dp)
                    )
                }
                Text(text = "Str", fontSize = 32.sp)
                Text(text = str.toString(), fontSize = 32.sp)
                Button(onClick = {str--}) {
                    Image(
                        painter = painterResource(R.drawable.outline_arrow_circle_down_24),
                        contentDescription = "up",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
            ) {
                Button(onClick = {agi++}) {
                    Image(
                        painter = painterResource(R.drawable.outline_arrow_circle_up_24),
                        contentDescription = "up",
                        modifier = Modifier.size(30.dp)
                    )
                }
                Text(text = "Agi", fontSize = 32.sp)
                Text(text = agi.toString(), fontSize = 32.sp)
                Button(onClick = {agi--}) {
                    Image(
                        painter = painterResource(R.drawable.outline_arrow_circle_down_24),
                        contentDescription = "up",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
            ) {
                Button(onClick = {int++}) {
                    Image(
                        painter = painterResource(R.drawable.outline_arrow_circle_up_24),
                        contentDescription = "up",
                        modifier = Modifier.size(30.dp)
                    )
                }
                Text(text = "Int", fontSize = 32.sp)
                Text(text = int.toString(), fontSize = 32.sp)
                Button(onClick = {int--}) {
                    Image(
                        painter = painterResource(R.drawable.outline_arrow_circle_down_24),
                        contentDescription = "up",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
            ){
                Button(onClick = {cat++}) {
                    Image(
                        painter = painterResource(R.drawable.outline_arrow_circle_up_24),
                        contentDescription = "up",
                        modifier = Modifier.size(30.dp)
                    )
                }
                Text(text = "Cat", fontSize = 32.sp)
                Text(text = cat.toString(), fontSize = 32.sp)
                Button(onClick = {cat--}) {
                    Image(
                        painter = painterResource(R.drawable.outline_arrow_circle_down_24),
                        contentDescription = "up",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun previewScreen() {
    RPGCardView({})
}