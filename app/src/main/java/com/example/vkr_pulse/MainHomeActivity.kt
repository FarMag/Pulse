package com.example.vkr_pulse

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.forEach
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.vkr_pulse.databinding.ActivityMainHomeBinding

class MainHomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainHomeBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { view, insets ->
            // Получаем отступы для системных панелей (например, навигационной)
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Применяем нижний отступ
            view.updatePadding(bottom = systemBarsInsets.bottom)
            insets
        }

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController.apply {
            // Добавляем слушатель изменения фрагментов
            addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.homeFragment -> setBackground(R.drawable.yellow)
                    R.id.trainingFragment -> setBackground(R.drawable.pink)
                    R.id.nutritionFragment -> setBackground(R.drawable.blue)
                    R.id.profileFragment -> setBackground(R.drawable.green)
                    // Добавьте остальные фрагменты
                }
            }
        }

        binding.bottomNavigation.setupWithNavController(navController)
    }

    private fun setBackground(resId: Int) {
        binding.mainContainer.setBackgroundResource(resId)
    }
}