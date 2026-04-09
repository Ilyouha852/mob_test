package com.example.mobdev_lab3

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.mobdev_lab3.ui.storage.CsvFileFragment
import com.example.mobdev_lab3.ui.storage.SharedPreferencesFragment
import com.example.mobdev_lab3.ui.storage.TxtFileFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

// Главная Activity для демонстрации работы с различными типами хранилищ
class StorageLabActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            Toast.makeText(
                this,
                "Для работы с CSV файлом необходимы разрешения",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage_lab)

        // Настройка Toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)

        setupViewPager()
        checkAndRequestPermissions()
    }

    private fun setupViewPager() {
        val adapter = StoragePagerAdapter(this)
        viewPager.adapter = adapter

        // Связываем TabLayout с ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "SharedPreferences"
                1 -> "TXT File"
                2 -> "CSV File"
                else -> "Tab $position"
            }
        }.attach()
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return
        }

        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissions.isNotEmpty()) {
            // Показываем объяснение пользователю
            AlertDialog.Builder(this)
                .setTitle("Разрешения")
                .setMessage("Для работы с CSV файлом в Downloads необходимы разрешения на доступ к файлам")
                .setPositiveButton("Разрешить") { _, _ ->
                    requestPermissionLauncher.launch(permissions.toTypedArray())
                }
                .setNegativeButton("Отмена") { dialog, _ ->
                    dialog.dismiss()
                    Toast.makeText(
                        this,
                        "CSV хранилище может работать некорректно без разрешений",
                        Toast.LENGTH_LONG
                    ).show()
                }
                .show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    // Адаптер для ViewPager2 с фрагментами хранилищ
    private class StoragePagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> SharedPreferencesFragment()
                1 -> TxtFileFragment()
                2 -> CsvFileFragment()
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }
    }
}
