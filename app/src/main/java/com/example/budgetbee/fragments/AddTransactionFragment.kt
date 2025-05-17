package com.example.budgetbee.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.budgetbee.R
import com.example.budgetbee.databinding.FragmentAddTransactionBinding
import com.example.budgetbee.models.Transaction
import com.example.budgetbee.viewModel.TransactionViewModel
import com.example.budgetbee.viewModel.UserViewModel
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*
import android.view.inputmethod.InputMethodManager
import com.google.android.material.textfield.TextInputEditText
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.budgetbee.utils.SharedPrefHelper
import android.util.Log
import com.example.budgetbee.viewModel.BudgetViewModel
import kotlinx.coroutines.delay
import com.example.budgetbee.utils.NotificationHelper

class AddTransactionFragment : Fragment() {

    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!
    private val args: AddTransactionFragmentArgs by navArgs()
    private var isEditMode = false
    private lateinit var transactionId: String
    private val userViewModel: UserViewModel by viewModels()
    private val transactionViewModel: TransactionViewModel by viewModels()
    private var isKeyboardVisible = false
    private var lastKeyboardHideTime = 0L
    private val KEYBOARD_HIDE_DELAY = 100L
    private val budgetViewModel: BudgetViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategorySpinner()
        setupDatePicker()
        setupEditMode()
        setupSaveButton()
        setupToggleListener()
        setupInputFocusListeners()
        initializeDefaultValues()

        // Wait for user to be loaded before enabling save
        binding.btnSave.isEnabled = false
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            binding.btnSave.isEnabled = user != null
        }
    }

    private fun setupCategorySpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.categories,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spCategory.adapter = adapter
        }
    }

    private fun setupInputFocusListeners() {
        // Clear focus when clicking outside input fields
        binding.root.setOnClickListener {
            if (isKeyboardVisible) {
                hideKeyboard()
            }
        }

        // Setup focus change listeners for all input fields
        binding.etTitle.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && isKeyboardVisible) {
                scheduleKeyboardHide()
            }
        }
        binding.etAmount.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && isKeyboardVisible) {
                scheduleKeyboardHide()
            }
        }
    }

    private fun scheduleKeyboardHide() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastKeyboardHideTime > KEYBOARD_HIDE_DELAY) {
            lastKeyboardHideTime = currentTime
            hideKeyboard()
        }
    }

    private fun hideKeyboard() {
        if (!isKeyboardVisible) return
        
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        isKeyboardVisible = false
        clearFocus()
    }

    private fun showKeyboard(view: View) {
        if (isKeyboardVisible) return
        
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        isKeyboardVisible = true
    }

    private fun clearFocus() {
        binding.etTitle.clearFocus()
        binding.etAmount.clearFocus()
        binding.etDate.clearFocus()

    }

    @SuppressLint("SetTextI18n")
    private fun setupEditMode() {
        args.transaction?.let { transaction ->
            isEditMode = true
            transactionId = transaction.id
            binding.etTitle.setText(transaction.title)
            binding.etAmount.setText(transaction.amount.toString())
            binding.etDate.setText(transaction.date)

            val adapter = binding.spCategory.adapter as? ArrayAdapter<*>
            if (adapter != null) {
                val position = (0 until adapter.count).indexOfFirst {
                    adapter.getItem(it).toString() == transaction.category
                }
                if (position != -1) {
                    binding.spCategory.setSelection(position)
                }
            }

            binding.toggleType.check(
                if (transaction.type == "Income") R.id.btnIncome else R.id.btnExpense
            )
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            if (isKeyboardVisible) {
                hideKeyboard()
            }
            if (validateInputs()) {
                saveTransaction()
            }
        }
    }

    private fun validateDate(dateStr: String): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.isLenient = false
            val inputDate = dateFormat.parse(dateStr)
            val calendar = Calendar.getInstance()
            calendar.time = inputDate ?: return false
            
            // Set time to start of day for comparison
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            val today = Calendar.getInstance()
            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)
            
            // Allow dates up to 1 year in the past
            val oneYearAgo = Calendar.getInstance()
            oneYearAgo.add(Calendar.YEAR, -1)
            oneYearAgo.set(Calendar.HOUR_OF_DAY, 0)
            oneYearAgo.set(Calendar.MINUTE, 0)
            oneYearAgo.set(Calendar.SECOND, 0)
            oneYearAgo.set(Calendar.MILLISECOND, 0)
            
            when {
                calendar.after(today) -> {
                    binding.etDate.error = "Future dates not allowed"
                    false
                }
                calendar.before(oneYearAgo) -> {
                    binding.etDate.error = "Date cannot be more than 1 year old"
                    false
                }
                else -> {
                    binding.etDate.error = null
                    true
                }
            }
        } catch (e: Exception) {
            binding.etDate.error = "Invalid date format (YYYY-MM-DD)"
            false
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validate title
        if (binding.etTitle.text.isNullOrBlank()) {
            binding.etTitle.error = "Title is required"
            isValid = false
        } else {
            binding.etTitle.error = null
        }

        // Validate amount
        val amountText = binding.etAmount.text.toString()
        if (amountText.isBlank()) {
            binding.etAmount.error = "Amount is required"
            isValid = false
        } else {
            try {
                val amount = amountText.toDouble()
                if (amount <= 0) {
                    binding.etAmount.error = "Amount must be greater than 0"
                    isValid = false
                } else {
                    binding.etAmount.error = null
                }
            } catch (e: NumberFormatException) {
                binding.etAmount.error = "Invalid amount format"
                isValid = false
            }
        }

        // Validate date
        if (binding.etDate.text.isNullOrBlank()) {
            binding.etDate.error = "Date is required"
            isValid = false
        } else {
            isValid = validateDate(binding.etDate.text.toString()) && isValid
        }

        // Validate transaction type
        if (binding.toggleType.checkedButtonId == -1) {
            Snackbar.make(binding.root, "Please select transaction type", Snackbar.LENGTH_SHORT).show()
            isValid = false
        }

        if (!isValid) {
            Snackbar.make(binding.root, "Please fix the errors above", Snackbar.LENGTH_SHORT).show()
        }

        return isValid
    }

    private fun createTransaction(userId: Long): Transaction {
        val title = binding.etTitle.text.toString()
        val amount = binding.etAmount.text.toString().toDouble()
        val date = binding.etDate.text.toString()
        val category = binding.spCategory.selectedItem.toString()
        val type = if (binding.toggleType.checkedButtonId == R.id.btnIncome) "Income" else "Expense"
        val note = "" // Add note field if needed

        return if (isEditMode) {
            Transaction(
                id = transactionId,
                userId = userId,
                title = title,
                amount = amount,
                type = type,
                category = category,
                date = date,
                note = note
            )
        } else {
            Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = title,
                amount = amount,
                type = type,
                category = category,
                date = date,
                note = note
            )
        }
    }

    private fun saveTransaction() {
        val user = userViewModel.currentUser.value ?: return
        val userId = user.id
        val transaction = createTransaction(userId)
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        lifecycleScope.launch {
            try {
                // Save to database and wait for completion
                transactionViewModel.saveTransaction(transaction)

                // Fetch current month transactions and check budget
                val transactions = transactionViewModel.getTransactionsByMonthOnce(userId, currentMonth)
                val expenses = transactions.filter { it.type.equals("expense", ignoreCase = true) }.sumOf { it.amount }
                
                // Ensure budget data is loaded
                budgetViewModel.setCurrentUserId(userId.toString())
                budgetViewModel.loadData()
                
                // Wait for budget data to be available
                var attempts = 0
                while (budgetViewModel.totalBudget.value == null && attempts < 5) {
                    delay(100)
                    attempts++
                }
                
                // Update budget progress and trigger notifications if needed
                budgetViewModel.updateBudgetProgress(expenses)
                
                // Show success notification using the new channel
                NotificationHelper.showTransactionSuccess(requireContext(), "Transaction saved successfully")
                
                // Show snackbar and navigate back
                Snackbar.make(binding.root, "Transaction saved successfully", Snackbar.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } catch (e: Exception) {
                Log.e("AddTransactionFragment", "Error saving transaction: ${e.message}", e)
                Snackbar.make(binding.root, "Failed to save transaction", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupToggleListener() {
        binding.toggleType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val incomeColor = requireContext().getColor(R.color.income_color)
                val expenseColor = requireContext().getColor(R.color.expense_color)
                val defaultBg = requireContext().getColor(R.color.background)

                // Reset colors
                binding.btnIncome.setBackgroundColor(defaultBg)
                binding.btnExpense.setBackgroundColor(defaultBg)

                when (checkedId) {
                    R.id.btnIncome -> {
                        binding.btnIncome.setBackgroundColor(incomeColor)
                    }
                    R.id.btnExpense -> {
                        binding.btnExpense.setBackgroundColor(expenseColor)
                    }
                }
            }
        }
    }

    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
            if (isKeyboardVisible) {
                hideKeyboard()
            }
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val selectedDate = Calendar.getInstance().apply {
                        set(year, month, day)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    if (!selectedDate.after(Calendar.getInstance())) {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        binding.etDate.setText(dateFormat.format(selectedDate.time))
                    } else {
                        Snackbar.make(binding.root, "Future dates not allowed", Snackbar.LENGTH_SHORT).show()
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.maxDate = System.currentTimeMillis()
            }.show()
        }
    }

    private fun initializeDefaultValues() {
        // Set default category to first item
        if (!isEditMode && binding.spCategory.adapter.count > 0) {
            binding.spCategory.setSelection(0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        if (isKeyboardVisible) {
            hideKeyboard()
        }
    }
}
