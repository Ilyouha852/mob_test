package com.example.mobdev_lab3.presentation.network

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.mobdev_lab3.R
import com.example.mobdev_lab3.network.NetworkClient
import com.example.mobdev_lab3.network.model.Post
import com.example.mobdev_lab3.network.model.User

class NetworkFragment : Fragment() {

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

    private val viewModel: NetworkViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_network_lab, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etUserId      = view.findViewById(R.id.etUserId)
        btnFetchUser  = view.findViewById(R.id.btnFetchUser)
        btnFetchPosts = view.findViewById(R.id.btnFetchPosts)
        btnCancel     = view.findViewById(R.id.btnCancel)
        btnClearCache = view.findViewById(R.id.btnClearCache)
        progressBar   = view.findViewById(R.id.progressBar)
        tvStatus      = view.findViewById(R.id.tvStatus)
        tvUserResult  = view.findViewById(R.id.tvUserResult)
        tvPostsResult = view.findViewById(R.id.tvPostsResult)
        tvCacheInfo   = view.findViewById(R.id.tvCacheInfo)
        tvProtocol    = view.findViewById(R.id.tvProtocol)

        tvProtocol.text = "Протокол: HTTP/2 (с fallback на HTTP/1.1)\n" +
                "Таймауты: подключение 15 с, чтение 20 с, запись 20 с\n" +
                "Кэширование: max-age=60 сек, размер до 10 МБ"

        setupListeners()
        observeViewModel()
        updateCacheInfo()
    }

    private fun setupListeners() {
        btnFetchUser.setOnClickListener {
            val text = etUserId.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(requireContext(), "Введите ID пользователя (1–10)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val id = text.toIntOrNull()
            if (id == null || id < 1 || id > 10) {
                Toast.makeText(requireContext(), "ID должен быть числом от 1 до 10", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            tvUserResult.text = ""
            tvPostsResult.text = ""
            viewModel.fetchUser(id)
        }

        btnFetchPosts.setOnClickListener { viewModel.fetchUserPosts() }
        btnCancel.setOnClickListener { viewModel.cancelRequest() }
        btnClearCache.setOnClickListener {
            val cacheDir = java.io.File(requireContext().cacheDir, "http_cache")
            if (cacheDir.exists()) {
                cacheDir.deleteRecursively()
                Toast.makeText(requireContext(), "Кэш очищен", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Кэш уже пуст", Toast.LENGTH_SHORT).show()
            }
            updateCacheInfo()
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is NetworkUiState.Idle -> {
                    setLoading(false)
                    tvStatus.text = "Готов к выполнению запросов."
                    btnFetchPosts.isEnabled = false
                }
                is NetworkUiState.Loading -> {
                    setLoading(true, "Выполнение запроса...")
                }
                is NetworkUiState.UserLoaded -> {
                    setLoading(false)
                    tvUserResult.text = formatUser(state.user)
                    tvStatus.text = "Запрос 1 выполнен. Теперь можно получить посты."
                    btnFetchPosts.isEnabled = true
                    updateCacheInfo()
                }
                is NetworkUiState.PostsLoaded -> {
                    setLoading(false)
                    tvPostsResult.text = formatPosts(state.posts, state.userName)
                    tvStatus.text = "Запрос 2 выполнен. Получено постов: ${state.posts.size}"
                    updateCacheInfo()
                }
                is NetworkUiState.Error -> {
                    setLoading(false)
                    tvStatus.text = "Ошибка: ${state.message}"
                }
                is NetworkUiState.Cancelled -> {
                    setLoading(false)
                    tvStatus.text = "Запрос отменён"
                }
            }
        }
    }

    private fun setLoading(loading: Boolean, status: String = "") {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnFetchUser.isEnabled = !loading
        btnCancel.isEnabled = loading
        if (status.isNotEmpty()) tvStatus.text = status
    }

    private fun updateCacheInfo() {
        tvCacheInfo.text = NetworkClient.getCacheInfo(requireContext())
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
        append("Слоган:  «${user.company.catchPhrase}»")
    }

    private fun formatPosts(posts: List<Post>, userName: String): String = buildString {
        appendLine("── Посты \"$userName\" (всего: ${posts.size}) ──")
        posts.take(5).forEachIndexed { i, post ->
            appendLine()
            appendLine("${i + 1}. ${post.title.replaceFirstChar { it.uppercaseChar() }}")
            appendLine("   ${post.body.take(80).replace("\n", " ")}…")
        }
        if (posts.size > 5) append("\n…и ещё ${posts.size - 5} постов")
    }
}
