package com.example.mobdev_lab3.presentation.storage

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.mobdev_lab3.R
import com.example.mobdev_lab3.ui.storage.CsvFileFragment
import com.example.mobdev_lab3.ui.storage.SharedPreferencesFragment
import com.example.mobdev_lab3.ui.storage.TxtFileFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class StorageFragment : Fragment() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (!permissions.values.all { it }) {
            Toast.makeText(requireContext(), "Для работы с CSV необходимы разрешения", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_storage_lab, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)

        viewPager.adapter = StoragePagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "SharedPreferences"
                1 -> "TXT File"
                2 -> "CSV File"
                else -> "Tab $position"
            }
        }.attach()

        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return

        val permissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissions.isNotEmpty()) {
            AlertDialog.Builder(requireContext())
                .setTitle("Разрешения")
                .setMessage("Для работы с CSV файлом необходимы разрешения на доступ к файлам")
                .setPositiveButton("Разрешить") { _, _ ->
                    permissionLauncher.launch(permissions.toTypedArray())
                }
                .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private inner class StoragePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 3
        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> SharedPreferencesFragment()
            1 -> TxtFileFragment()
            2 -> CsvFileFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}
