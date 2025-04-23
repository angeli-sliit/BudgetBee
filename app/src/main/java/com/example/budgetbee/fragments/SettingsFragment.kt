package com.example.budgetbee.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.budgetbee.R
import com.example.budgetbee.databinding.FragmentSettingsBinding
import com.example.budgetbee.models.Transaction
import com.example.budgetbee.utils.AlarmPermissionHelper
import com.example.budgetbee.utils.DailyReminderReceiver
import com.example.budgetbee.utils.FileHelper
import com.example.budgetbee.utils.NotificationHelper
import com.example.budgetbee.utils.SharedPrefHelper
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPrefHelper: SharedPrefHelper

    private val exactAlarmPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (AlarmPermissionHelper.canScheduleExactAlarms(requireContext())) {
            DailyReminderReceiver.setNextAlarm(requireContext())
            showMessage("Daily reminders enabled")
        } else {
            binding.switchDailyReminder.isChecked = false
            sharedPrefHelper.dailyReminderEnabled = false
            showMessage("Exact alarm permission required")
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) showMessage("Storage permission denied")
    }

    private val importJsonLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> handleJsonImport(uri) }

    private val importTextLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> handleTextImport(uri) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        sharedPrefHelper = SharedPrefHelper(requireContext())
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCurrencySpinner()
        setupViews()
        setupListeners()
        setupBudgetChangeListener()
        setupExportImportListeners()
    }

    private fun setupBudgetChangeListener() {
        binding.btnSaveSettings.setOnClickListener {
            try {
                val newBudget = binding.etBudget.text.toString().toDouble()
                sharedPrefHelper.monthlyBudget = newBudget
                NotificationHelper.showBudgetUpdateNotification(
                    requireContext(),
                    "Budget Updated",
                    "New budget: ${sharedPrefHelper.getFormattedAmount(newBudget)}"
                )
                showMessage("Budget updated successfully")
            } catch (e: NumberFormatException) {
                showMessage("Invalid budget amount")
            }
        }
    }

    private fun setupCurrencySpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.currencies,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCurrency.adapter = adapter
            val currencyIndex = sharedPrefHelper.currency.let { currency ->
                resources.getStringArray(R.array.currencies).indexOf(currency)
            }
            binding.spinnerCurrency.setSelection(currencyIndex)
        }
    }

    private fun setupViews() {
        binding.apply {
            etBudget.setText(sharedPrefHelper.monthlyBudget.toString())
            switchDailyReminder.isChecked = sharedPrefHelper.dailyReminderEnabled
        }
    }

    private fun setupListeners() {
        binding.switchDailyReminder.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefHelper.dailyReminderEnabled = isChecked
            if (isChecked) handleAlarmPermission() else cancelAlarms()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setupExportImportListeners() {
        binding.btnExportPdf.setOnClickListener {
            if (checkStoragePermission()) {
                executeExport { FileHelper.exportBudgetPdf(requireContext(), sharedPrefHelper) }
            } else requestStoragePermission()
        }

        binding.btnExportJson.setOnClickListener {
            if (checkStoragePermission()) {
                executeExport {
                    FileHelper.exportToJson(
                        requireContext(),
                        sharedPrefHelper.getTransactions()
                    )
                }
            } else requestStoragePermission()
        }



        binding.btnImportJson.setOnClickListener {
            importJsonLauncher.launch(arrayOf("application/json"))
        }
    }

    private fun handleJsonImport(uri: Uri?) {
        uri ?: return
        try {
            requireContext().contentResolver.openInputStream(uri)?.use { stream ->
                val json = stream.bufferedReader().readText()
                val type = object : TypeToken<List<Transaction>>() {}.type
                Gson().fromJson<List<Transaction>>(json, type)?.let { transactions ->
                    if (transactions.isNotEmpty()) {
                        sharedPrefHelper.saveTransactions(transactions)
                        NotificationHelper.showImportSuccess(requireContext(), transactions.size)
                        navigateToTransactions()
                    } else {
                        NotificationHelper.showImportFailure(requireContext(), "Empty file")
                    }
                }
            }
        } catch (e: Exception) {
            NotificationHelper.showImportFailure(requireContext(), "JSON error: ${e.message}")
        }
    }

    private fun handleTextImport(uri: Uri?) {
        uri ?: return
        try {
            requireContext().contentResolver.openInputStream(uri)?.use { stream ->
                val lines = stream.bufferedReader().readLines()
                if (lines.size < 3) {
                    NotificationHelper.showImportFailure(requireContext(), "Invalid format")
                    return
                }

                val transactions = mutableListOf<Transaction>().apply {
                    for (i in 2 until lines.size) {
                        lines[i].split(',').takeIf { it.size >= 5 }?.let { parts ->
                            add(
                                Transaction(
                                    id = UUID.randomUUID().toString(),
                                    date = parts[0].trim(),
                                    category = parts[1].trim(),
                                    amount = parts[2].trim().toDoubleOrNull() ?: 0.0,
                                    type = parts[3].trim(),
                                    note = parts[4].trim(),
                                    title = parts.getOrNull(5)?.trim() ?: ""
                                )
                            )
                        }
                    }
                }

                if (transactions.isNotEmpty()) {
                    sharedPrefHelper.saveTransactions(transactions)
                    NotificationHelper.showImportSuccess(requireContext(), transactions.size)
                    navigateToTransactions()
                } else {
                    NotificationHelper.showImportFailure(requireContext(), "No valid data")
                }
            }
        } catch (e: Exception) {
            NotificationHelper.showImportFailure(requireContext(), "Text error: ${e.message}")
        }
    }

    private fun navigateToTransactions() {
        findNavController().navigate(R.id.transactionsFragment)
    }

    private fun executeExport(exportOperation: () -> Boolean) {
        val success = exportOperation()
        if (success) NotificationHelper.showExportSuccess(requireContext(), "Export")
        else NotificationHelper.showExportFailure(requireContext(), "Export")
        showMessage(if (success) "Export successful" else "Export failed")
    }

    private fun checkStoragePermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun handleAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !AlarmPermissionHelper.canScheduleExactAlarms(requireContext())
        ) {
            showPermissionDialog()
        } else {
            DailyReminderReceiver.setNextAlarm(requireContext())
            showMessage("Daily reminders enabled")
        }
    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("Allow exact alarm permission for reminders")
            .setPositiveButton("Allow") { _, _ ->
                exactAlarmPermissionLauncher.launch(
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                )
            }
            .setNegativeButton("Cancel") { _, _ ->
                binding.switchDailyReminder.isChecked = false
            }
            .show()
    }

    private fun cancelAlarms() {
        DailyReminderReceiver.cancelAlarm(requireContext())
        showMessage("Daily reminders disabled")
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}