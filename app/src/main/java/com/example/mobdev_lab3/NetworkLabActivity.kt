package com.example.mobdev_lab3

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.mobdev_lab3.network.NetworkClient
import com.example.mobdev_lab3.network.NetworkRepository
import com.example.mobdev_lab3.network.NetworkResult
import com.example.mobdev_lab3.network.model.Post
import com.example.mobdev_lab3.network.model.User
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class NetworkLabActivity : AppCompatActivity() {

    private lateinit var etUserId: EditText
    private lateinit var btnFetchUser: Button
    private lateinit var btnFetchPosts: Button
    private lateinit var btnCancel: Button
    private lateinit var btnClearCache: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView
    private lateinit var tvUserResult: TextView
    private lateinit var tvPostsResult: TextView
    private lateinit var tvCacheInfo: TextView
    private lateinit var tvProtocol: TextView

    private val repository by lazy { NetworkRepository(this) }
    private var currentJob: Job? = null
    private var lastFetchedUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_network_lab)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Сетевые запросы (HTTP/2)"

        initViews()
        setupListeners()
        updateCacheInfo()
    }

    private fun initViews() {
        etUserId        = findViewById(R.id.etUserId)
        btnFetchUser    = findViewById(R.id.btnFetchUser)
        btnFetchPosts   = findViewById(R.id.btnFetchPosts)
        btnCancel       = findViewById(R.id.btnCancel)
        btnClearCache   = findViewById(R.id.btnClearCache)
        progressBar     = findViewById(R.id.progressBar)
        tvStatus        = findViewById(R.id.tvStatus)
        tvUserResult    = findViewById(R.id.tvUserResult)
        tvPostsResult   = findViewById(R.id.tvPostsResult)
        tvCacheInfo     = findViewById(R.id.tvCacheInfo)
        tvProtocol      = findViewById(R.id.tvProtocol)

        tvProtocol.text = "Протокол: HTTP/2 (с fallback на HTTP/1.1)\n" +
                "Таймауты: подключение 15 с, чтение 20 с, запись 20 с\n" +
                "Кэширование: max-age=60 сек, размер до 10 МБ"

        btnFetchPosts.isEnabled = false
        btnCancel.isEnabled = false
    }

    private fun setupListeners() {
        btnFetchUser.setOnClickListener { startFetchUser() }
        btnFetchPosts.setOnClickListener { startFetchPosts() }
        btnCancel.setOnClickListener { cancelCurrentJob() }
        btnClearCache.setOnClickListener { clearCache() }
    }

    private fun startFetchUser() {
        val userIdText = etUserId.text.toString().trim()
        if (userIdText.isEmpty()) {
            Toast.makeText(this, "Введите ID пользователя (1–10)", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = userIdText.toIntOrNull()
        if (userId == null || userId < 1 || userId > 10) {
            Toast.makeText(this, "ID должен быть числом от 1 до 10", Toast.LENGTH_SHORT).show()
            return
        }

        lastFetchedUser = null
        btnFetchPosts.isEnabled = false
        tvUserResult.text = ""
        tvPostsResult.text = ""

        setLoading(true, "Запрос 1: Получение данных пользователя #$userId...")

        currentJob = lifecycleScope.launch {
            val result = repository.fetchUser(userId)
            setLoading(false)

            when (result) {
                is NetworkResult.Success -> {
                    lastFetchedUser = result.data
                    tvUserResult.text = formatUser(result.data)
                    tvStatus.text = "✅ Запрос 1 выполнен успешно. Теперь можно получить посты."
                    btnFetchPosts.isEnabled = true
                    updateCacheInfo()
                }
                is NetworkResult.Error -> {
                    tvUserResult.text = "❌ Ошибка: ${result.message}"
                    tvStatus.text = "Запрос 1 завершён с ошибкой"
                }
                is NetworkResult.Cancelled -> {
                    tvUserResult.text = "🚫 Запрос отменён пользователем"
                    tvStatus.text = "Запрос 1 отменён"
                }
            }
        }
    }

    private fun startFetchPosts() {
        val user = lastFetchedUser ?: run {
            Toast.makeText(this, "Сначала выполните запрос 1", Toast.LENGTH_SHORT).show()
            return
        }

        tvPostsResult.text = ""
        setLoading(true, "Запрос 2: Получение постов пользователя \"${user.name}\" (userId=${user.id})...")

        currentJob = lifecycleScope.launch {
            val result = repository.fetchUserPosts(user.id)
            setLoading(false)

            when (result) {
                is NetworkResult.Success -> {
                    tvPostsResult.text = formatPosts(result.data, user.name)
                    tvStatus.text = "✅ Запрос 2 выполнен успешно. Получено постов: ${result.data.size}"
                    updateCacheInfo()
                }
                is NetworkResult.Error -> {
                    tvPostsResult.text = "❌ Ошибка: ${result.message}"
                    tvStatus.text = "Запрос 2 завершён с ошибкой"
                }
                is NetworkResult.Cancelled -> {
                    tvPostsResult.text = "🚫 Запрос отменён пользователем"
                    tvStatus.text = "Запрос 2 отменён"
                }
            }
        }
    }

    private fun cancelCurrentJob() {
        currentJob?.cancel()
        currentJob = null
        setLoading(false)
        tvStatus.text = "🚫 Запрос отменён"
    }

    private fun clearCache() {
        val cacheDir = java.io.File(cacheDir, "http_cache")
        if (cacheDir.exists()) {
            cacheDir.deleteRecursively()
            Toast.makeText(this, "Кэш очищен", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Кэш уже пуст", Toast.LENGTH_SHORT).show()
        }
        updateCacheInfo()
    }

    private fun setLoading(isLoading: Boolean, statusText: String = "") {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnFetchUser.isEnabled = !isLoading
        btnFetchPosts.isEnabled = !isLoading && lastFetchedUser != null
        btnCancel.isEnabled = isLoading
        if (statusText.isNotEmpty()) tvStatus.text = statusText
    }

    private fun updateCacheInfo() {
        tvCacheInfo.text = NetworkClient.getCacheInfo(this)
    }

    private fun formatUser(user: User): String = buildString {
        appendLine("── Пользователь #${user.id} ──")
        appendLine("Имя:     ${user.name}")
        appendLine("Ник:     @${user.username}")
        appendLine("Email:   ${user.email}")
        appendLine("Тел.:    ${user.phone}")
        appendLine("Сайт:    ${user.website}")
        appendLine("Адрес:   ${user.address.city}, ${user.address.street}")
        appendLine("Компания:${user.company.name}")
        append(    "Слоган:  «${user.company.catchPhrase}»")
    }

    private fun formatPosts(posts: List<Post>, userName: String): String = buildString {
        appendLine("── Посты пользователя \"$userName\" (всего: ${posts.size}) ──")
        posts.take(5).forEachIndexed { index, post ->
            appendLine()
            appendLine("${index + 1}. ${post.title.replaceFirstChar { it.uppercaseChar() }}")
            appendLine("   ${post.body.take(80).replace("\n", " ")}…")
        }
        if (posts.size > 5) append("\n…и ещё ${posts.size - 5} постов")
    }

    override fun onDestroy() {
        super.onDestroy()
        currentJob?.cancel()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
