package com.example.mobdev_lab3.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.mobdev_lab3.R
import com.example.mobdev_lab3.model.SortMode

class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModels({ requireActivity() })

    private lateinit var radioByName: RadioButton
    private lateinit var radioByType: RadioButton
    private lateinit var radioByDate: RadioButton
    private lateinit var checkShowHidden: CheckBox
    private lateinit var checkShowSystem: CheckBox
    private lateinit var buttonSave: Button
    private lateinit var buttonReset: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        radioByName     = view.findViewById(R.id.radioByName)
        radioByType     = view.findViewById(R.id.radioByType)
        radioByDate     = view.findViewById(R.id.radioByDate)
        checkShowHidden = view.findViewById(R.id.checkBoxShowHidden)
        checkShowSystem = view.findViewById(R.id.checkBoxShowSystemFiles)
        buttonSave      = view.findViewById(R.id.buttonSave)
        buttonReset     = view.findViewById(R.id.buttonReset)

        viewModel.settings.observe(viewLifecycleOwner) { settings ->
            when (settings.sortMode) {
                SortMode.BY_NAME -> radioByName.isChecked = true
                SortMode.BY_TYPE -> radioByType.isChecked = true
                SortMode.BY_DATE -> radioByDate.isChecked = true
            }
            checkShowHidden.isChecked = settings.showHidden
            checkShowSystem.isChecked = settings.showSystemFiles
        }

        buttonSave.setOnClickListener {
            val sortMode = when {
                radioByName.isChecked -> SortMode.BY_NAME
                radioByType.isChecked -> SortMode.BY_TYPE
                radioByDate.isChecked -> SortMode.BY_DATE
                else -> SortMode.BY_NAME
            }
            viewModel.save(AppSettings(sortMode, checkShowHidden.isChecked, checkShowSystem.isChecked))
            Toast.makeText(requireContext(), "Настройки сохранены", Toast.LENGTH_SHORT).show()
        }

        buttonReset.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Сброс настроек")
                .setMessage("Сбросить настройки к значениям по умолчанию?")
                .setPositiveButton("Сбросить") { _, _ ->
                    viewModel.reset()
                    Toast.makeText(requireContext(), "Настройки сброшены", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Отмена", null)
                .show()
        }
    }
}
