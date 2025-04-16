package com.example.budgetbee.fragments

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
import com.example.budgetbee.utils.SharedPrefHelper
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionFragment : Fragment() {

    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPrefHelper: SharedPrefHelper
    private val args: AddTransactionFragmentArgs by navArgs()
    private var isEditMode = false
    private lateinit var transactionId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        sharedPrefHelper = SharedPrefHelper(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategorySpinner()
        setupDatePicker()
        setupEditMode()
        setupSaveButton()
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

    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
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

    private fun setupEditMode() {
        args.transaction?.let { transaction ->
            isEditMode = true
            transactionId = transaction.id
            binding.etTitle.setText(transaction.title)
            binding.etAmount.setText(transaction.amount.toString())
            binding.etDate.setText(transaction.date)
            binding.spCategory.setSelection(
                (binding.spCategory.adapter as ArrayAdapter<String>).getPosition(transaction.category)
            )
            binding.toggleType.check(
                if (transaction.type == "Income") R.id.btnIncome else R.id.btnExpense
            )
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            if (validateInputs()) saveTransaction()
        }
    }

    private fun validateInputs(): Boolean {
        val title = binding.etTitle.text.toString().trim()
        val amountStr = binding.etAmount.text.toString().trim()
        val date = binding.etDate.text.toString().trim()

        return when {
            title.isEmpty() -> {
                binding.etTitle.error = "Please enter title"
                false
            }
            amountStr.isEmpty() -> {
                binding.etAmount.error = "Please enter amount"
                false
            }
            date.isEmpty() -> {
                binding.etDate.error = "Please select date"
                false
            }
            amountStr.toDoubleOrNull() == null -> {
                binding.etAmount.error = "Invalid amount format"
                false
            }
            amountStr.toDouble() <= 0 -> {
                binding.etAmount.error = "Amount must be positive"
                false
            }
            !isValidDate(date) -> {
                binding.etDate.error = "Future dates not allowed"
                false
            }
            else -> true
        }
    }

    private fun isValidDate(selectedDate: String): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance().apply {
            time = sdf.parse(selectedDate) ?: return false
        }
        return !calendar.after(Calendar.getInstance())
    }

    private fun saveTransaction() {
        val transactions = sharedPrefHelper.getTransactions().toMutableList()
        val transaction = createTransaction()

        if (isEditMode) {
            val index = transactions.indexOfFirst { it.id == transactionId }
            if (index != -1) transactions[index] = transaction
        } else {
            transactions.add(transaction)
        }

        sharedPrefHelper.saveTransactions(transactions)
        Snackbar.make(binding.root, "Transaction saved", Snackbar.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    private fun createTransaction(): Transaction {
        return Transaction(
            id = if (isEditMode) transactionId else UUID.randomUUID().toString(),
            title = binding.etTitle.text.toString().trim(),
            amount = binding.etAmount.text.toString().toDouble(),
            type = if (binding.toggleType.checkedButtonId == R.id.btnIncome) "Income" else "Expense",
            category = binding.spCategory.selectedItem.toString(),
            date = binding.etDate.text.toString().trim()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}