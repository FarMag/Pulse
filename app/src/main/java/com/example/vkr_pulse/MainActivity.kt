package com.example.vkr_pulse

import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.animation.ObjectAnimator
import android.graphics.drawable.LayerDrawable
import android.widget.TextView


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Находим элементы UI
        val welcomeText: TextView = findViewById(R.id.welcomeText)
        val startButton: Button = findViewById(R.id.startButton)

        startButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left) // Анимация входа/выхода
        }

        // Находим фон, который является LayerDrawable
        val rootLayout = findViewById<android.widget.RelativeLayout>(R.id.rootLayout)
        val background = rootLayout.background as LayerDrawable

        // Анимируем радиус для первого слоя
        val gradientDrawable = background.getDrawable(0) as android.graphics.drawable.GradientDrawable
        val animator = ObjectAnimator.ofFloat(
            gradientDrawable,
            "gradientRadius",
            400f, 900f // Радиус пульсации
        ).apply {
            duration = 1000 // Длительность анимации
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
        }

        animator.start()
    }
}
