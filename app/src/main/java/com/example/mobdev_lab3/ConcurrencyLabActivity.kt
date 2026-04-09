package com.example.mobdev_lab3

import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import java.io.File
import java.io.FileWriter
import java.util.concurrent.atomic.AtomicBoolean

class ConcurrencyLabActivity : AppCompatActivity() {
    
    private lateinit var outputFile: File
    
    private lateinit var btnStartThreadTask: Button
    private lateinit var btnCancelThreadTask: Button
    private lateinit var tvThreadProgress: TextView
    private lateinit var tvThreadResult: TextView
    
    private lateinit var btnStartSequentialThreads: Button
    private lateinit var btnCancelSequentialThreads: Button
    private lateinit var tvSequentialThreadsResult: TextView
    
    private lateinit var btnStartGlobalScopeTask: Button
    private lateinit var btnCancelGlobalScopeTask: Button
    private lateinit var tvGlobalScopeProgress: TextView
    private lateinit var tvGlobalScopeResult: TextView
    
    private lateinit var btnStartLifecycleScopeTask: Button
    private lateinit var btnCancelLifecycleScopeTask: Button
    private lateinit var tvLifecycleScopeProgress: TextView
    private lateinit var tvLifecycleScopeResult: TextView
    
    private var globalScopeJob: Job? = null
    private var lifecycleScopeJob: Job? = null
    private var sequentialCoroutinesJob: Job? = null
    
    private var threadTask: Thread? = null
    private var sequentialThread1: Thread? = null
    private var sequentialThread2: Thread? = null
    
    private val threadTaskCancelled = AtomicBoolean(false)
    private val sequentialThreadsCancelled = AtomicBoolean(false)
    
    private val uiHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_concurrency_lab)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Потоки и корутины"

        initOutputFile()

        initViews()
        setupListeners()
    }

    // Инициализация файла laba8.txt в корне внешнего хранилища
    private fun initOutputFile() {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
        outputFile = File(downloadsDir, "laba8.txt")
    }

    // Очистка файла перед новой записью
    private fun clearOutputFile() {
        if (outputFile.exists()) {
            outputFile.delete()
        }
    }

    // Запись строки в файл
    private fun appendToFile(text: String) {
        try {
            FileWriter(outputFile, true).use { writer ->
                writer.append(text).append("\n")
            }
        } catch (_: Exception) {
        }
    }

    private fun initViews() {
        // Потоки
        btnStartThreadTask = findViewById(R.id.btnStartThreadTask)
        btnCancelThreadTask = findViewById(R.id.btnCancelThreadTask)
        tvThreadProgress = findViewById(R.id.tvThreadProgress)
        tvThreadResult = findViewById(R.id.tvThreadResult)

        // Последовательное выполнение в потоках
        btnStartSequentialThreads = findViewById(R.id.btnStartSequentialThreads)
        btnCancelSequentialThreads = findViewById(R.id.btnCancelSequentialThreads)
        tvSequentialThreadsResult = findViewById(R.id.tvSequentialThreadsResult)

        // Корутины (GlobalScope)
        btnStartGlobalScopeTask = findViewById(R.id.btnStartGlobalScopeTask)
        btnCancelGlobalScopeTask = findViewById(R.id.btnCancelGlobalScopeTask)
        tvGlobalScopeProgress = findViewById(R.id.tvGlobalScopeProgress)
        tvGlobalScopeResult = findViewById(R.id.tvGlobalScopeResult)

        // Корутины (lifecycleScope)
        btnStartLifecycleScopeTask = findViewById(R.id.btnStartLifecycleScopeTask)
        btnCancelLifecycleScopeTask = findViewById(R.id.btnCancelLifecycleScopeTask)
        tvLifecycleScopeProgress = findViewById(R.id.tvLifecycleScopeProgress)
        tvLifecycleScopeResult = findViewById(R.id.tvLifecycleScopeResult)
    }

    private fun setupListeners() {
        // Потоки

        btnStartThreadTask.setOnClickListener {
            startThreadTask()
        }

        btnCancelThreadTask.setOnClickListener {
            cancelThreadTask()
        }

        // Последовательное выполнение в потоках

        btnStartSequentialThreads.setOnClickListener {
            startSequentialThreads()
        }

        btnCancelSequentialThreads.setOnClickListener {
            cancelSequentialThreads()
        }

        // Корутины (GlobalScope)

        btnStartGlobalScopeTask.setOnClickListener {
            startGlobalScopeTask()
        }

        btnCancelGlobalScopeTask.setOnClickListener {
            cancelGlobalScopeTask()
        }

        // Корутины (lifecycleScope)

        btnStartLifecycleScopeTask.setOnClickListener {
            startLifecycleScopeTask()
        }

        btnCancelLifecycleScopeTask.setOnClickListener {
            cancelLifecycleScopeTask()
        }
    }

    private fun startThreadTask() {
        clearOutputFile()
        appendToFile("Запуск задачи в потоке")
        threadTaskCancelled.set(false)
        btnStartThreadTask.isEnabled = false
        btnCancelThreadTask.isEnabled = true
        tvThreadProgress.text = getString(R.string.progress_0)
        tvThreadResult.text = getString(R.string.result_executing)

        threadTask = Thread {
            var result = 0
            try {
                for (i in 1..100) {
                    if (threadTaskCancelled.get()) {
                        appendToFile("Задача в потоке ОТМЕНЕНА на шаге $i")
                        uiHandler.post {
                            tvThreadResult.text = getString(R.string.result_cancelled)
                            btnStartThreadTask.isEnabled = true
                            btnCancelThreadTask.isEnabled = false
                        }
                        return@Thread
                    }

                    result += i
                    val progress = i
                    uiHandler.post {
                        tvThreadProgress.text = getString(R.string.progress_percent, progress)
                    }

                    if (i % 10 == 0) {
                        appendToFile("Прогресс: $i%, текущая сумма: $result")
                    }
                    Thread.sleep(50)
                }

                val finalResult = result
                appendToFile("Задача в потоке ЗАВЕРШЕНА")
                appendToFile("Финальный результат: сумма 1-100 = $finalResult")
                uiHandler.post {
                    tvThreadResult.text = getString(R.string.result_sum_1_100, finalResult)
                    btnStartThreadTask.isEnabled = true
                    btnCancelThreadTask.isEnabled = false
                }
            } catch (_: InterruptedException) {
                // Поток был прерван
                appendToFile("Задача в потоке ПРЕРВАНА")
                uiHandler.post {
                    tvThreadProgress.text = getString(R.string.progress_0)
                    tvThreadResult.text = getString(R.string.result_cancelled)
                    btnStartThreadTask.isEnabled = true
                    btnCancelThreadTask.isEnabled = false
                }
            }
        }.apply {
            start()
        }
    }

    // Отмена задачи в потоке
    private fun cancelThreadTask() {
        threadTaskCancelled.set(true)
        threadTask?.interrupt()
        tvThreadProgress.text = getString(R.string.progress_0)
        tvThreadResult.text = getString(R.string.result_cancelling)
    }

    private fun startSequentialThreads() {
        clearOutputFile()
        appendToFile(" Запуск последовательного выполнения в потоках ")
        sequentialThreadsCancelled.set(false)
        btnStartSequentialThreads.isEnabled = false
        btnCancelSequentialThreads.isEnabled = true
        tvSequentialThreadsResult.text = getString(R.string.result_executing)

        // Поток 1: вычисляет сумму чисел от 1 до 50
        sequentialThread1 = Thread {
            try {
                var sum1 = 0
                appendToFile("Поток 1: начало вычисления суммы 1-50")
                for (i in 1..50) {
                    if (sequentialThreadsCancelled.get()) {
                        appendToFile(" Последовательное выполнение в потоках ОТМЕНЕНО в потоке 1 на шаге $i ")
                        uiHandler.post {
                            tvSequentialThreadsResult.text = getString(R.string.cancelled_thread1)
                            resetSequentialThreadButtons()
                        }
                        return@Thread
                    }
                    sum1 += i
                    if (i % 10 == 0) {
                        appendToFile("Поток 1: прогресс $i/50, текущая сумма: $sum1")
                    }
                    Thread.sleep(30)
                }

                val finalSum1 = sum1
                appendToFile("Поток 1: вычисление завершено, сумма = $finalSum1")
                appendToFile("Поток 1: передача результата в поток 2")
                uiHandler.post {
                    tvSequentialThreadsResult.text = getString(R.string.result_thread1_sum, finalSum1)
                }

                // Поток 2: использует результат потока 1 для вычисления квадрата
                sequentialThread2 = Thread {
                    try {
                        appendToFile("Поток 2: начало вычисления квадрата")
                        Thread.sleep(100) // Небольшая задержка для наглядности
                        if (sequentialThreadsCancelled.get()) {
                            appendToFile(" Последовательное выполнение в потоках ОТМЕНЕНО в потоке 2 ")
                            uiHandler.post {
                                tvSequentialThreadsResult.text = getString(R.string.cancelled_thread2)
                                resetSequentialThreadButtons()
                            }
                            return@Thread
                        }

                        val finalResult = finalSum1 * finalSum1
                        appendToFile("Поток 2: вычисление завершено, $finalSum1² = $finalResult")
                        appendToFile(" Последовательное выполнение в потоках ЗАВЕРШЕНО ")
                        uiHandler.post {
                            tvSequentialThreadsResult.text = getString(R.string.result_thread1_value, finalSum1, finalSum1, finalResult)
                            resetSequentialThreadButtons()
                        }
                    } catch (_: InterruptedException) {
                        appendToFile(" Последовательное выполнение в потоках ПРЕРВАНО в потоке 2 ")
                        uiHandler.post {
                            tvSequentialThreadsResult.text = getString(R.string.cancelled_thread2)
                            resetSequentialThreadButtons()
                        }
                    }
                }.apply {
                    start()
                }
            } catch (_: InterruptedException) {
                appendToFile(" Последовательное выполнение в потоках ПРЕРВАНО в потоке 1 ")
                uiHandler.post {
                    tvSequentialThreadsResult.text = getString(R.string.cancelled_thread1)
                    resetSequentialThreadButtons()
                }
            }
        }.apply {
            start()
        }
    }

    private fun cancelSequentialThreads() {
        sequentialThreadsCancelled.set(true)
        sequentialThread1?.interrupt()
        sequentialThread2?.interrupt()
        tvSequentialThreadsResult.text = getString(R.string.result_cancelling)
    }

    private fun resetSequentialThreadButtons() {
        btnStartSequentialThreads.isEnabled = true
        btnCancelSequentialThreads.isEnabled = false
    }

    private fun startGlobalScopeTask() {
        clearOutputFile()
        appendToFile(" Запуск задачи в корутине (GlobalScope) ")
        appendToFile("Контекст: Dispatchers.Default")
        btnStartGlobalScopeTask.isEnabled = false
        btnCancelGlobalScopeTask.isEnabled = true
        tvGlobalScopeProgress.text = getString(R.string.progress_0)
        tvGlobalScopeResult.text = getString(R.string.result_executing)

        globalScopeJob = GlobalScope.launch(Dispatchers.Default) {
            var result = 0
            try {
                for (i in 1..100) {
                    // Проверка отмены корутины
                    ensureActive()
                    result += i

                    // Обновление UI с помощью withContext(Dispatchers.Main)
                    withContext(Dispatchers.Main) {
                        tvGlobalScopeProgress.text = getString(R.string.progress_percent, i)
                    }

                    if (i % 10 == 0) {
                        appendToFile("Прогресс: $i%, текущая сумма: $result")
                    }
                    delay(50)
                }

                appendToFile(" Задача в корутине (GlobalScope) ЗАВЕРШЕНА ")
                appendToFile("Финальный результат: сумма 1-100 = $result")
                withContext(Dispatchers.Main) {
                    tvGlobalScopeResult.text = getString(R.string.result_sum_1_100, result)
                    btnStartGlobalScopeTask.isEnabled = true
                    btnCancelGlobalScopeTask.isEnabled = false
                }
            } catch (_: CancellationException) {
                appendToFile(" Задача в корутине (GlobalScope) ОТМЕНЕНА ")
                withContext(Dispatchers.Main) {
                    tvGlobalScopeProgress.text = getString(R.string.progress_0)
                    tvGlobalScopeResult.text = getString(R.string.result_cancelled)
                    btnStartGlobalScopeTask.isEnabled = true
                    btnCancelGlobalScopeTask.isEnabled = false
                }
            }
        }
    }

    private fun cancelGlobalScopeTask() {
        globalScopeJob?.cancel()
        tvGlobalScopeProgress.text = getString(R.string.progress_0)
        tvGlobalScopeResult.text = getString(R.string.result_cancelled)
        btnStartGlobalScopeTask.isEnabled = true
        btnCancelGlobalScopeTask.isEnabled = false
    }

    private fun startLifecycleScopeTask() {
        clearOutputFile()
        appendToFile(" Запуск задачи в корутине (lifecycleScope) ")
        appendToFile("Контекст: Dispatchers.IO")
        btnStartLifecycleScopeTask.isEnabled = false
        btnCancelLifecycleScopeTask.isEnabled = true
        tvLifecycleScopeProgress.text = getString(R.string.progress_0)
        tvLifecycleScopeResult.text = getString(R.string.result_executing)

        lifecycleScopeJob = lifecycleScope.launch(Dispatchers.IO) {
            var result = 0
            try {
                for (i in 1..100) {
                    ensureActive()
                    result += i

                    withContext(Dispatchers.Main) {
                        tvLifecycleScopeProgress.text = getString(R.string.progress_percent, i)
                    }

                    if (i % 10 == 0) {
                        appendToFile("Прогресс: $i%, текущая сумма: $result")
                    }
                    delay(50)
                }

                appendToFile(" Задача в корутине (lifecycleScope) ЗАВЕРШЕНА ")
                appendToFile("Финальный результат: сумма 1-100 = $result")
                withContext(Dispatchers.Main) {
                    tvLifecycleScopeResult.text = getString(R.string.result_sum_1_100, result)
                    btnStartLifecycleScopeTask.isEnabled = true
                    btnCancelLifecycleScopeTask.isEnabled = false
                }
            } catch (_: CancellationException) {
                appendToFile(" Задача в корутине (lifecycleScope) ОТМЕНЕНА ")
                withContext(Dispatchers.Main) {
                    tvLifecycleScopeProgress.text = getString(R.string.progress_0)
                    tvLifecycleScopeResult.text = getString(R.string.result_cancelled)
                    btnStartLifecycleScopeTask.isEnabled = true
                    btnCancelLifecycleScopeTask.isEnabled = false
                }
            }
        }
    }

    private fun cancelLifecycleScopeTask() {
        lifecycleScopeJob?.cancel()
        tvLifecycleScopeProgress.text = getString(R.string.progress_0)
        tvLifecycleScopeResult.text = getString(R.string.result_cancelled)
        btnStartLifecycleScopeTask.isEnabled = true
        btnCancelLifecycleScopeTask.isEnabled = false
    }

    override fun onDestroy() {
        super.onDestroy()
        threadTask?.interrupt()
        sequentialThread1?.interrupt()
        sequentialThread2?.interrupt()
        globalScopeJob?.cancel()
        lifecycleScopeJob?.cancel()
        sequentialCoroutinesJob?.cancel()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}