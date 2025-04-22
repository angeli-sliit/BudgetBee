package com.example.budgetbee.fragments

import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import com.example.budgetbee.utils.AlarmPermissionHelper
import com.example.budgetbee.utils.DailyReminderReceiver
import com.example.budgetbee.utils.FileHelper
import com.example.budgetbee.utils.NotificationHelper
import com.example.budgetbee.utils.SharedPrefHelper
import com.google.android.material.snackbar.Snackbar

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
            showMessage("Exact alarm permission is required")
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            exportPdfData()
        } else {
            showMessage("Permission denied, cannot export PDF")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
    }
    private fun setupBudgetChangeListener() {
        binding.btnSaveSettings.setOnClickListener {
            try {
                val newBudget = binding.etBudget.text.toString().toDouble()
                sharedPrefHelper.monthlyBudget = newBudget
                showMessage("Budget updated successfully")

                // Show notification about budget update
                NotificationHelper.showBudgetUpdateNotification(
                    requireContext(),
                    "Budget Updated",
                    "Your monthly budget has been set to ${sharedPrefHelper.getFormattedAmount(newBudget)}"
                )
            } catch (e: NumberFormatException) {
                showMessage("Please enter a valid budget amount")
            }
        }
    }
    private fun setupCurrencySpinner() {
        ArrayAdapter.createFromResource(
            requireContext(), R.array.currencies, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCurrency.adapter = adapter
            val position = resources.getStringArray(R.array.currencies)
                .indexOf(sharedPrefHelper.currency)
            binding.spinnerCurrency.setSelection(if (position >= 0) position else 0)
        }
    }

    private fun setupViews() {
        binding.apply {
            etBudget.setText(sharedPrefHelper.monthlyBudget.toString())
            switchDailyReminder.isChecked = sharedPrefHelper.dailyReminderEnabled
            btnImport.isEnabled = FileHelper.backupExists(requireContext())
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setupListeners() {
        // PDF Export Button
        binding.btnExportPdf.setOnClickListener {
            if (checkStoragePermission()) {
                exportPdfData()
            } else {
                requestStoragePermission()
            }
        }

        // JSON Import Button
        binding.btnImport.setOnClickListener {
            FileHelper.importFromFile(requireContext())?.let { transactions ->
                sharedPrefHelper.saveTransactions(transactions)
                findNavController().navigate(R.id.transactionsFragment)
                showMessage("Imported ${transactions.size} transactions")
            } ?: showMessage("Import failed or no data found")
        }

        // Daily Reminder Switch
        binding.switchDailyReminder.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefHelper.dailyReminderEnabled = isChecked
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    !AlarmPermissionHelper.canScheduleExactAlarms(requireContext())
                ) {
                    showPermissionNeededDialog()
                } else {
                    DailyReminderReceiver.setNextAlarm(requireContext())
                    showMessage("Daily reminders enabled")
                }
            } else {
                DailyReminderReceiver.cancelAlarm(requireContext())
                showMessage("Daily reminders disabled")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun exportPdfData() {
        val success = FileHelper.exportBudgetPdf(requireContext(), sharedPrefHelper)
        showMessage(if (success) "PDF exported successfully" else "Export failed")
    }

    private fun checkStoragePermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            exportPdfData()
        }
    }

    private fun showPermissionNeededDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("Please grant exact alarm permission to enable daily reminders")
            .setPositiveButton("Grant") { _, _ ->
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                exactAlarmPermissionLauncher.launch(intent)
            }
            .setNegativeButton("Cancel") { _, _ ->
                binding.switchDailyReminder.isChecked = false
            }
            .show()
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
