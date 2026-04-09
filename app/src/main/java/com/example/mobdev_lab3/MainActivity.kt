package com.example.mobdev_lab3

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.mobdev_lab3.database.DatabaseManager
import com.example.mobdev_lab3.presentation.about.AboutFragment
import com.example.mobdev_lab3.presentation.bookmarks.BookmarksFragment
import com.example.mobdev_lab3.presentation.concurrency.ConcurrencyFragment
import com.example.mobdev_lab3.presentation.filemanager.FileManagerFragment
import com.example.mobdev_lab3.presentation.filemetadata.FileMetadataFragment
import com.example.mobdev_lab3.presentation.network.NetworkFragment
import com.example.mobdev_lab3.presentation.settings.SettingsFragment
import com.example.mobdev_lab3.presentation.storage.StorageFragment
import com.example.mobdev_lab3.viewmodel.FileManagerViewModel
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var navigationView: NavigationView

    val fileManagerViewModel: FileManagerViewModel by viewModels()

    companion object {
        const val EXTRA_CURRENT_PATH = "current_path"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DatabaseManager.init(this)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            navigateTo(FileManagerFragment(), "Файловый менеджер")
            navigationView.setCheckedItem(R.id.nav_file_manager)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        when (item.itemId) {
            R.id.nav_file_manager    -> navigateTo(FileManagerFragment(),   "Файловый менеджер")
            R.id.nav_bookmarks       -> navigateTo(BookmarksFragment(),     "Закладки")
            R.id.nav_storage_lab     -> navigateTo(StorageFragment(),       "Хранилища данных")
            R.id.nav_concurrency_lab -> navigateTo(ConcurrencyFragment(),   "Потоки и корутины")
            R.id.nav_network_lab     -> navigateTo(NetworkFragment(),       "Сетевые запросы")
            R.id.nav_file_metadata   -> navigateTo(FileMetadataFragment(),  "Метаданные файлов")
            R.id.nav_settings        -> navigateTo(SettingsFragment(),      "Настройки")
            R.id.nav_about           -> navigateTo(AboutFragment(),         "О приложении")
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun navigateTo(fragment: Fragment, title: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        supportActionBar?.title = title
    }

    override fun onBackPressed() {
        when {
            drawerLayout.isDrawerOpen(GravityCompat.START) ->
                drawerLayout.closeDrawer(GravityCompat.START)
            supportFragmentManager.backStackEntryCount > 0 -> {
                supportFragmentManager.popBackStack()
                val entry = if (supportFragmentManager.backStackEntryCount > 1)
                    supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 2)
                else null
                supportActionBar?.title = entry?.name ?: "Файловый менеджер"
            }
            else -> super.onBackPressed()
        }
    }
}
