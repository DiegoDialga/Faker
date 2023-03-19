package com.deigo.faker

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private val activityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(this@MainActivity, FloatingService::class.java))
            }
            else{
                Toast.makeText(this@MainActivity, "Please Grant Permission", Toast.LENGTH_SHORT).show()
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.elevation = 0F
        supportActionBar?.setBackgroundDrawable(getDrawable(R.drawable.gradient_background))


        val cardViewItems = arrayOf(
            CardViewItems(R.drawable.gradient_background, null),
            CardViewItems(R.drawable.snow_flakes_ss, null),
            CardViewItems(R.color.black, null),
            CardViewItems(R.drawable.black_snow_fall_ss, R.color.black),

        )

        val adapter = CardViewAdapter(this@MainActivity, cardViewItems)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = CardViewAdapter(this@MainActivity, cardViewItems)
        val startButton = findViewById<ImageView?>(R.id.start_button)

        startButton.setOnClickListener {
            if(!Settings.canDrawOverlays(this@MainActivity)){

                Toast.makeText(this@MainActivity, "Floating Service has started", Toast.LENGTH_SHORT).show()

                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                activityResultLauncher.launch(intent)


            }
            else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(Intent(this@MainActivity, FloatingService::class.java))
                }
                else{
                    startService(Intent(this@MainActivity, FloatingService::class.java))
                }
            }
        }
    }
}