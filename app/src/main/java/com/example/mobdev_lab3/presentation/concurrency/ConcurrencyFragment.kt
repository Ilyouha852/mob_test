package com.example.mobdev_lab3.presentation.concurrency

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.mobdev_lab3.R

class ConcurrencyFragment : Fragment() {

    private val viewModel: ConcurrencyViewModel by viewModels()

    private lateinit var btnStartThreadTask: Button
    private lateinit var btnCancelThreadTask: Button
    private lateinit var tvThreadProgress: TextView
    private lateinit var tvThreadResult: TextView

    private lateinit var btnStartSeq: Button
    private lateinit var btnCancelSeq: Button
    private lateinit var tvSeqResult: TextView

    private lateinit var btnStartGlobal: Button
    private lateinit var btnCancelGlobal: Button
    private lateinit var tvGlobalProgress: TextView
    private lateinit var tvGlobalResult: TextView

    private lateinit var btnStartVmScope: Button
    private lateinit var btnCancelVmScope: Button
    private lateinit var tvVmScopeProgress: TextView
    private lateinit var tvVmScopeResult: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_concurrency_lab, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnStartThreadTask  = view.findViewById(R.id.btnStartThreadTask)
        btnCancelThreadTask = view.findViewById(R.id.btnCancelThreadTask)
        tvThreadProgress    = view.findViewById(R.id.tvThreadProgress)
        tvThreadResult      = view.findViewById(R.id.tvThreadResult)

        btnStartSeq   = view.findViewById(R.id.btnStartSequentialThreads)
        btnCancelSeq  = view.findViewById(R.id.btnCancelSequentialThreads)
        tvSeqResult   = view.findViewById(R.id.tvSequentialResult)

        btnStartGlobal  = view.findViewById(R.id.btnStartGlobalScopeTask)
        btnCancelGlobal = view.findViewById(R.id.btnCancelGlobalScopeTask)
        tvGlobalProgress = view.findViewById(R.id.tvGlobalScopeProgress)
        tvGlobalResult  = view.findViewById(R.id.tvGlobalScopeResult)

        btnStartVmScope  = view.findViewById(R.id.btnStartViewModelScopeTask)
        btnCancelVmScope = view.findViewById(R.id.btnCancelViewModelScopeTask)
        tvVmScopeProgress = view.findViewById(R.id.tvViewModelScopeProgress)
        tvVmScopeResult   = view.findViewById(R.id.tvViewModelScopeResult)

        viewModel.initOutputFile()
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        btnStartThreadTask.setOnClickListener  { viewModel.startThreadTask() }
        btnCancelThreadTask.setOnClickListener { viewModel.cancelThreadTask() }
        btnStartSeq.setOnClickListener         { viewModel.startSequentialThreads() }
        btnCancelSeq.setOnClickListener        { viewModel.cancelSequentialThreads() }
        btnStartGlobal.setOnClickListener      { viewModel.startGlobalScopeTask() }
        btnCancelGlobal.setOnClickListener     { viewModel.cancelGlobalScopeTask() }
        btnStartVmScope.setOnClickListener     { viewModel.startViewModelScopeTask() }
        btnCancelVmScope.setOnClickListener    { viewModel.cancelViewModelScopeTask() }
    }

    private fun observeViewModel() {
        viewModel.threadProgress.observe(viewLifecycleOwner)  { tvThreadProgress.text  = it }
        viewModel.threadResult.observe(viewLifecycleOwner)    { tvThreadResult.text    = it }
        viewModel.threadRunning.observe(viewLifecycleOwner)   { running ->
            btnStartThreadTask.isEnabled  = !running
            btnCancelThreadTask.isEnabled = running
        }

        viewModel.seqResult.observe(viewLifecycleOwner)  { tvSeqResult.text = it }
        viewModel.seqRunning.observe(viewLifecycleOwner) { running ->
            btnStartSeq.isEnabled  = !running
            btnCancelSeq.isEnabled = running
        }

        viewModel.globalProgress.observe(viewLifecycleOwner) { tvGlobalProgress.text = it }
        viewModel.globalResult.observe(viewLifecycleOwner)   { tvGlobalResult.text   = it }
        viewModel.globalRunning.observe(viewLifecycleOwner)  { running ->
            btnStartGlobal.isEnabled  = !running
            btnCancelGlobal.isEnabled = running
        }

        viewModel.vmScopeProgress.observe(viewLifecycleOwner) { tvVmScopeProgress.text = it }
        viewModel.vmScopeResult.observe(viewLifecycleOwner)   { tvVmScopeResult.text   = it }
        viewModel.vmScopeRunning.observe(viewLifecycleOwner)  { running ->
            btnStartVmScope.isEnabled  = !running
            btnCancelVmScope.isEnabled = running
        }
    }
}
