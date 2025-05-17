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
import androidx.fragment.app.viewModels
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
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.text.SimpleDateFormat
import com.example.budgetbee.viewModel.UserViewModel
import android.provider.MediaStore
import android.os.Environment
import androidx.lifecycle.lifecycleScope
import com.example.budgetbee.viewModel.TransactionViewModel
import kotlinx.coroutines.launch
import com.example.budgetbee.viewModel.BudgetViewModel

class SettingsFragment : Fragment() {
    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPrefHelper: SharedPrefHelper
    private val userViewModel: UserViewModel by viewModels()
    private val transactionViewModel: TransactionViewModel by viewModels()
    private val budgetViewModel: BudgetViewModel by viewModels()

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

        // Show loading spinner
        binding.progressBudgetLoading.visibility = View.VISIBLE
        binding.tvCurrentBudget.text = "Current: --"
        binding.etBudget.setText("")

        // Observe user and set spinner to user's currency
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                val currencyCodes = listOf("USD", "EUR", "GBP", "JPY", "INR", "LKR")
                val currencyIndex = currencyCodes.indexOf(it.currency)
                binding.spinnerCurrency.setSelection(if (currencyIndex >= 0) currencyIndex else 0)
                // Fetch and show current monthly budget
                val userId = it.id.toString()
                budgetViewModel.setCurrentUserId(userId)
                budgetViewModel.totalBudget.observe(viewLifecycleOwner) { totalBudget ->
                    binding.progressBudgetLoading.visibility = View.GONE
                    val currencySymbol = try { java.util.Currency.getInstance(it.currency).symbol } catch (e: Exception) { "$" }
                    val budgetText = if (totalBudget != null && totalBudget > 0) "$currencySymbol$totalBudget" else "--"
                    binding.tvCurrentBudget.text = "Current: $budgetText"
                    if (totalBudget != null && totalBudget > 0) {
                        binding.etBudget.setText(totalBudget.toString())
                    }
                }
            }
        }
    }

    private fun setupCurrencySpinner() {
        val currencyCodes = listOf("USD", "EUR", "GBP", "JPY", "INR", "LKR")
        val currencySymbols = currencyCodes.map { code ->
            try { java.util.Currency.getInstance(code).symbol } catch (e: Exception) { code }
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencySymbols)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCurrency.adapter = adapter
    }

    private fun setupBudgetChangeListener() {
        binding.btnSaveSettings.setOnClickListener {
            try {
                val currencyCodes = listOf("USD", "EUR", "GBP", "JPY", "INR", "LKR")
                val currencyIndex = binding.spinnerCurrency.selectedItemPosition
                val currencyCode = currencyCodes.getOrNull(currencyIndex) ?: "USD"
                val userId = userViewModel.currentUser.value?.id ?: return@setOnClickListener
                userViewModel.updateCurrency(userId, currencyCode)

                // Save budget to DB for current month
                val budgetStr = binding.etBudget.text.toString()
                val budgetAmount = budgetStr.toDoubleOrNull() ?: 0.0
                budgetViewModel.setCurrentUserId(userId.toString())
                budgetViewModel.createBudget(budgetAmount, "All") // Use "All" or a default category

                // Also update SharedPreferences
                sharedPrefHelper.monthlyBudget = budgetAmount

                showMessage("Budget saved successfully")

                // Show a budget update notification using the new channel
                NotificationHelper.showTransactionSuccess(
                    requireContext(),
                    "Your monthly budget has been updated to $budgetAmount $currencyCode."
                )
            } catch (e: Exception) {
                showMessage("Error saving settings: ${e.message}")
            }
        }
    }

    private fun setupViews() {
        binding.apply {
            etBudget.setText("")
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
                val currentUser = userViewModel.currentUser.value ?: return@setOnClickListener
                transactionViewModel.setCurrentUserId(currentUser.id)
                transactionViewModel.getTransactions().observe(viewLifecycleOwner) { transactions ->
                    executeExport {
                        FileHelper.exportBudgetPdf(requireContext(), sharedPrefHelper, currentUser.id.toString(), transactions)
                    }
                }
            } else requestStoragePermission()
        }

        binding.btnExportJson.setOnClickListener {
            if (checkStoragePermission()) {
                val currentUser = userViewModel.currentUser.value ?: return@setOnClickListener
                transactionViewModel.setCurrentUserId(currentUser.id)
                transactionViewModel.getTransactions().observe(viewLifecycleOwner) { transactions ->
                    executeExport {
                        FileHelper.exportToJson(
                            requireContext(),
                            transactions,
                            currentUser.id
                        )
                    }
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
            val currentUser = userViewModel.currentUser.value ?: return
            val imported = FileHelper.importFromJson(requireContext(), uri)
            if (!imported.isNullOrEmpty()) {
                lifecycleScope.launch {
                    try {
                        // Insert each transaction into the database for the current user
                        imported.forEach { t ->
                            val tx = t.copy(userId = currentUser.id)
                            transactionViewModel.saveTransaction(tx)
                        }
                        // After all imports, fetch all transactions and trigger budget check/notifications
                        transactionViewModel.setCurrentUserId(currentUser.id)
                        transactionViewModel.getTransactions().observe(viewLifecycleOwner) {
                            val currentMonthTransactions = SharedPrefHelper(requireContext()).getCurrentMonthTransactions(currentUser.id)
                            SharedPrefHelper(requireContext()).saveTransactions(currentMonthTransactions, currentUser.id)
                            NotificationHelper.showImportSuccess(requireContext(), imported.size)
                            requireActivity().recreate() // Refresh UI
                        }
                    } catch (e: Exception) {
                        NotificationHelper.showImportFailure(requireContext(), "Error saving transactions: ${e.message}")
                    }
                }
            } else {
                NotificationHelper.showImportFailure(requireContext(), "Empty file or no valid transactions")
            }
        } catch (e: Exception) {
            NotificationHelper.showImportFailure(requireContext(), "JSON error: ${e.message}")
        }
    }

    private fun handleTextImport(uri: Uri?) {
        uri ?: return
        try {
            val currentUser = userViewModel.currentUser.value ?: return
            requireContext().contentResolver.openInputStream(uri)?.use { stream ->
                val lines = stream.bufferedReader().readLines()
                if (lines.size < 3) {
                    com.example.budgetbee.utils.NotificationHelper.showImportFailure(requireContext(), "Invalid format")
                    return
                }
                val transactions = mutableListOf<com.example.budgetbee.models.Transaction>().apply {
                    for (i in 2 until lines.size) {
                        lines[i].split(',').takeIf { it.size >= 5 }?.let { parts ->
                            add(
                                com.example.budgetbee.models.Transaction(
                                    id = java.util.UUID.randomUUID().toString(),
                                    userId = currentUser.id,
                                    date = parts[0].trim(),
                                    category = parts[1].trim(),
                                    amount = parts[2].trim().toDoubleOrNull() ?: 0.0,
                                    type = parts[3].trim(),
                                    note = parts[4].trim(),
                                    title = parts.getOrNull(5)?.trim() ?: ""
                                )                            )
                        }
                    }
                }
                if (transactions.isNotEmpty()) {
                    // Save imported transactions to DB (already done above)
                    // Now fetch all transactions for the user and update SharedPrefHelper
                    val currentMonthTransactions = SharedPrefHelper(requireContext()).getCurrentMonthTransactions(currentUser.id)
                    sharedPrefHelper.saveTransactions(currentMonthTransactions, currentUser.id)
                    com.example.budgetbee.utils.NotificationHelper.showImportSuccess(requireContext(), transactions.size)
                    navigateToTransactions()
                } else {
                    com.example.budgetbee.utils.NotificationHelper.showImportFailure(requireContext(), "No valid data")
                }
            }
        } catch (e: Exception) {
            com.example.budgetbee.utils.NotificationHelper.showImportFailure(requireContext(), "Text error: ${e.message}")
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
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13 and above uses photo picker
                true
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Android 11 and 12
                Environment.isExternalStorageManager()
            }
            else -> {
                // Android 10 and below
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    private fun requestStoragePermission() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Use photo picker for Android 13+
                val intent = Intent(MediaStore.ACTION_PICK_IMAGES)
                startActivityForResult(intent, PICK_IMAGE_REQUEST)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Request all files access for Android 11 and 12
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:${requireContext().packageName}")
                    startActivity(intent)
                }
            }
            else -> {
                // Request storage permission for Android 10 and below
                storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private fun handleAlarmPermission() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (!AlarmPermissionHelper.canScheduleExactAlarms(requireContext())) {
                    showPermissionDialog()
                } else {
                    DailyReminderReceiver.setNextAlarm(requireContext())
                    showMessage("Daily reminders enabled")
                }
            }
            else -> {
                DailyReminderReceiver.setNextAlarm(requireContext())
                showMessage("Daily reminders enabled")
            }
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

    private fun createTransaction(category: String, amount: Double): Transaction {
        val currentUser = userViewModel.currentUser.value ?: throw IllegalStateException("User not logged in")
        return Transaction(
            id = UUID.randomUUID().toString(),
            userId = currentUser.id,
            title = "Category: $category",
            amount = amount,
            type = "Expense",
            category = category,
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )
    }
}