package com.example.mobdev_lab3.presentation.concurrency

import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import java.io.File
import java.io.FileWriter
import java.util.concurrent.atomic.AtomicBoolean

class ConcurrencyViewModel : ViewModel() {

    private val _threadProgress   = MutableLiveData("Прогресс: 0%")
    private val _threadResult     = MutableLiveData("Результат: ожидание")
    private val _threadRunning    = MutableLiveData(false)
    private val _seqResult        = MutableLiveData("Результат: ожидание")
    private val _seqRunning       = MutableLiveData(false)
    private val _globalProgress   = MutableLiveData("Прогресс: 0%")
    private val _globalResult     = MutableLiveData("Результат: ожидание")
    private val _globalRunning    = MutableLiveData(false)
    private val _vmScopeProgress  = MutableLiveData("Прогресс: 0%")
    private val _vmScopeResult    = MutableLiveData("Результат: ожидание")
    private val _vmScopeRunning   = MutableLiveData(false)

    val threadProgress:  LiveData<String>  = _threadProgress
    val threadResult:    LiveData<String>  = _threadResult
    val threadRunning:   LiveData<Boolean> = _threadRunning
    val seqResult:       LiveData<String>  = _seqResult
    val seqRunning:      LiveData<Boolean> = _seqRunning
    val globalProgress:  LiveData<String>  = _globalProgress
    val globalResult:    LiveData<String>  = _globalResult
    val globalRunning:   LiveData<Boolean> = _globalRunning
    val vmScopeProgress: LiveData<String>  = _vmScopeProgress
    val vmScopeResult:   LiveData<String>  = _vmScopeResult
    val vmScopeRunning:  LiveData<Boolean> = _vmScopeRunning

    private var threadTask:       Thread? = null
    private var seqThread1:       Thread? = null
    private var seqThread2:       Thread? = null
    private var globalScopeJob:   Job? = null
    private var vmScopeJob:       Job? = null

    private val threadCancelled = AtomicBoolean(false)
    private val seqCancelled    = AtomicBoolean(false)

    private var outputFile: File? = null

    fun initOutputFile() {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        dir?.mkdirs()
        outputFile = File(dir, "laba8.txt")
    }

    private fun clearFile()  { outputFile?.delete() }
    private fun appendToFile(text: String) {
        try { FileWriter(outputFile, true).use { it.append(text).append("\n") } }
        catch (_: Exception) {}
    }

    fun startThreadTask() {
        clearFile()
        appendToFile("Запуск задачи в потоке")
        threadCancelled.set(false)
        _threadRunning.postValue(true)
        _threadProgress.postValue("Прогресс: 0%")
        _threadResult.postValue("Результат: выполнение...")

        threadTask = Thread {
            var result = 0
            try {
                for (i in 1..100) {
                    if (threadCancelled.get()) {
                        appendToFile("Задача в потоке ОТМЕНЕНА на шаге $i")
                        _threadResult.postValue("Результат: отменено")
                        _threadRunning.postValue(false)
                        return@Thread
                    }
                    result += i
                    _threadProgress.postValue("Прогресс: $i%")
                    if (i % 10 == 0) appendToFile("Прогресс: $i%, сумма: $result")
                    Thread.sleep(50)
                }
                appendToFile("Задача в потоке ЗАВЕРШЕНА: $result")
                _threadResult.postValue("Результат: сумма 1-100 = $result")
                _threadRunning.postValue(false)
            } catch (_: InterruptedException) {
                appendToFile("Задача в потоке ПРЕРВАНА")
                _threadProgress.postValue("Прогресс: 0%")
                _threadResult.postValue("Результат: отменено")
                _threadRunning.postValue(false)
            }
        }.apply { start() }
    }

    fun cancelThreadTask() {
        threadCancelled.set(true)
        threadTask?.interrupt()
        _threadProgress.postValue("Прогресс: 0%")
        _threadResult.postValue("Результат: отмена...")
    }

    fun startSequentialThreads() {
        clearFile()
        appendToFile("Запуск последовательного выполнения в потоках")
        seqCancelled.set(false)
        _seqRunning.postValue(true)
        _seqResult.postValue("Результат: выполнение...")

        seqThread1 = Thread {
            try {
                var sum1 = 0
                for (i in 1..50) {
                    if (seqCancelled.get()) {
                        _seqResult.postValue("Результат: отменено в потоке 1")
                        _seqRunning.postValue(false)
                        return@Thread
                    }
                    sum1 += i
                    Thread.sleep(30)
                }
                appendToFile("Поток 1: сумма = $sum1")
                _seqResult.postValue("Результат: Поток 1: сумма 1-50 = $sum1\nПередача в поток 2...")

                seqThread2 = Thread {
                    try {
                        Thread.sleep(100)
                        if (seqCancelled.get()) {
                            _seqResult.postValue("Результат: отменено в потоке 2")
                            _seqRunning.postValue(false)
                            return@Thread
                        }
                        val sq = sum1 * sum1
                        appendToFile("Поток 2: $sum1² = $sq")
                        _seqResult.postValue("Результат: Поток 1: $sum1\nПоток 2: $sum1² = $sq")
                        _seqRunning.postValue(false)
                    } catch (_: InterruptedException) {
                        _seqResult.postValue("Результат: отменено в потоке 2")
                        _seqRunning.postValue(false)
                    }
                }.apply { start() }
            } catch (_: InterruptedException) {
                _seqResult.postValue("Результат: отменено в потоке 1")
                _seqRunning.postValue(false)
            }
        }.apply { start() }
    }

    fun cancelSequentialThreads() {
        seqCancelled.set(true)
        seqThread1?.interrupt()
        seqThread2?.interrupt()
        _seqResult.postValue("Результат: отмена...")
    }

    fun startGlobalScopeTask() {
        clearFile()
        appendToFile("Запуск задачи в корутине (GlobalScope + Dispatchers.Default)")
        _globalRunning.postValue(true)
        _globalProgress.postValue("Прогресс: 0%")
        _globalResult.postValue("Результат: выполнение...")

        @OptIn(DelicateCoroutinesApi::class)
        globalScopeJob = GlobalScope.launch(Dispatchers.Default) {
            var result = 0
            try {
                for (i in 1..100) {
                    ensureActive()
                    result += i
                    _globalProgress.postValue("Прогресс: $i%")
                    if (i % 10 == 0) appendToFile("Прогресс: $i%, сумма: $result")
                    delay(50)
                }
                appendToFile("Задача GlobalScope ЗАВЕРШЕНА: $result")
                _globalResult.postValue("Результат: сумма 1-100 = $result")
                _globalRunning.postValue(false)
            } catch (_: CancellationException) {
                appendToFile("Задача GlobalScope ОТМЕНЕНА")
                _globalProgress.postValue("Прогресс: 0%")
                _globalResult.postValue("Результат: отменено")
                _globalRunning.postValue(false)
            }
        }
    }

    fun cancelGlobalScopeTask() {
        globalScopeJob?.cancel()
        _globalProgress.postValue("Прогресс: 0%")
        _globalResult.postValue("Результат: отменено")
        _globalRunning.postValue(false)
    }

    fun startViewModelScopeTask() {
        clearFile()
        appendToFile("Запуск задачи в корутине (viewModelScope + Dispatchers.IO)")
        _vmScopeRunning.postValue(true)
        _vmScopeProgress.postValue("Прогресс: 0%")
        _vmScopeResult.postValue("Результат: выполнение...")

        vmScopeJob = viewModelScope.launch(Dispatchers.IO) {
            var result = 0
            try {
                for (i in 1..100) {
                    ensureActive()
                    result += i
                    _vmScopeProgress.postValue("Прогресс: $i%")
                    if (i % 10 == 0) appendToFile("Прогресс: $i%, сумма: $result")
                    delay(50)
                }
                appendToFile("Задача viewModelScope ЗАВЕРШЕНА: $result")
                _vmScopeResult.postValue("Результат: сумма 1-100 = $result")
                _vmScopeRunning.postValue(false)
            } catch (_: CancellationException) {
                appendToFile("Задача viewModelScope ОТМЕНЕНА")
                _vmScopeProgress.postValue("Прогресс: 0%")
                _vmScopeResult.postValue("Результат: отменено")
                _vmScopeRunning.postValue(false)
            }
        }
    }

    fun cancelViewModelScopeTask() {
        vmScopeJob?.cancel()
        _vmScopeProgress.postValue("Прогресс: 0%")
        _vmScopeResult.postValue("Результат: отменено")
        _vmScopeRunning.postValue(false)
    }

    override fun onCleared() {
        super.onCleared()
        threadCancelled.set(true)
        seqCancelled.set(true)
        threadTask?.interrupt()
        seqThread1?.interrupt()
        seqThread2?.interrupt()
        globalScopeJob?.cancel()
    }
}
