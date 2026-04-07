package com.example.a509lablearnandroid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.a509lablearnandroid.utils.SharedPreferencesUtil

class MenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Button(onClick = {
                    startActivity(Intent(this@MenuActivity, RPGCardActivity::class.java))
                }) {
                    Text("RPGCardActivity")
                }
                Button(onClick = {
                    startActivity(Intent(this@MenuActivity, PokedexActivity::class.java))
                }) {
                    Text("PokedexActivity")
                }
                Button(onClick = {
                    startActivity(Intent(this@MenuActivity, LifeCycleComposeActivity::class.java))
                }) {
                    Text("LifeCycleComposeActivity")
                }
                Button(onClick = {
                    startActivity(Intent(this@MenuActivity, SharedPreferencesActivity::class.java))
                }) {
                    Text("SharedPreferencesUtil")
                }
                Button(onClick = {
                    startActivity(Intent(this@MenuActivity, GalleryActivity::class.java))
                }) {
                    Text("GalleryActivity")
                }
                Button(onClick = {
                    startActivity(Intent(this@MenuActivity, SensorActivity::class.java))
                }) {
                    Text("SensorActivity")
                }
                Button(onClick = {
                    startActivity(Intent(this@MenuActivity, Part1AnimationActivity::class.java))
                }) {
                    Text("Part1AnimationActivity")
                }
                Button(onClick = {
                    startActivity(Intent(this@MenuActivity, Part2Activity::class.java))
                }) {
                    Text("Part2Activity")
                }
                Button(onClick = {
                    startActivity(Intent(this@MenuActivity, Part3Activity::class.java))
                }) {
                    Text("Part3Activity")
                }
                Button(onClick = {
                    startActivity(Intent(this@MenuActivity, Part4Activity::class.java))
                }) {
                    Text("Part4Activity")
                }
                Button(onClick = {
                    startActivity(Intent(this@MenuActivity, Part5Activity::class.java))
                }) {
                    Text("Part5Activity")
                }
                Button(onClick = {
                    startActivity(Intent(this@MenuActivity, Part6Activity::class.java))
                }) {
                    Text("Part6Activity")
                }
                Button(onClick = {
                    startActivity(Intent(this@MenuActivity, Part7Activity::class.java))
                }) {
                    Text("Part7Activity")
                }
                Button(onClick = {
                    startActivity(Intent(this@MenuActivity, Part8Activity::class.java))
                }) {
                    Text("Part8Activity")
                }
                Button(onClick = {
                    startActivity(Intent(this@MenuActivity, Part9Activity::class.java))
                }) {
                    Text("Part9Activity")
                }
                Button(onClick = {
                    startActivity(Intent(this@MenuActivity, Part10Activity::class.java))
                }) {
                    Text("Part10Activity")
                }
                Button(onClick = {
                    startActivity(Intent(this@MenuActivity, Part11Activity::class.java))
                }) {
                    Text("Part11Activity")
                }
                Button(onClick = {
                    startActivity(Intent(this@MenuActivity, Part12Activity::class.java))
                }) {
                    Text("Part12Activity")
                }
            }
        }
    }
}
