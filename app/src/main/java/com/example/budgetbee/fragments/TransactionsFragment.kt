package com.example.budgetbee.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee.R
import com.example.budgetbee.adapters.TransactionAdapter
import com.example.budgetbee.databinding.FragmentTransactionsBinding
import com.example.budgetbee.models.Transaction
import com.example.budgetbee.viewModel.TransactionViewModel
import com.example.budgetbee.viewModel.UserViewModel
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

class TransactionsFragment : Fragment() {
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionAdapter: TransactionAdapter
    private val userViewModel: UserViewModel by viewModels()
    private val transactionViewModel: TransactionViewModel by viewModels()
    private var lastTransactions: List<Transaction> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressTransactionsLoading.visibility = View.VISIBLE
        binding.recyclerTransactions.visibility = View.GONE
        binding.tvEmptyState.visibility = View.GONE
        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        val currentUser = userViewModel.currentUser.value
        if (currentUser == null) {
            // Show error message
            Snackbar.make(binding.root, "Please login first", Snackbar.LENGTH_LONG).show()
            return
        }

        transactionViewModel.setCurrentUserId(currentUser.id)
        val currencyCode = currentUser.currency ?: "USD"
        transactionAdapter = TransactionAdapter(
            transactions = emptyList(),
            currencyCode = currencyCode,
            onEdit = ::navigateToEditTransaction,
            onDelete = ::deleteTransaction
        )

        binding.recyclerTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        // Observe user changes robustly
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                transactionViewModel.setCurrentUserId(user.id)
                transactionAdapter.currencyCode = user.currency ?: "USD"
                transactionAdapter.notifyDataSetChanged()
                // Reload transactions for new user/currency
                transactionViewModel.getTransactions().observe(viewLifecycleOwner) { transactions ->
                    binding.progressTransactionsLoading.visibility = View.GONE
                    if (transactions != null) {
                        val sortedTransactions = transactions.sortedByDescending {
                            try {
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)?.time ?: 0L
                            } catch (e: Exception) {
                                0L
                            }
                        }
                        lastTransactions = sortedTransactions
                        transactionAdapter.updateTransactions(sortedTransactions)
                        updateEmptyState(sortedTransactions.isEmpty())
                    } else {
                        updateEmptyState(true)
                    }
                }
            }
        }

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                showDeleteDialog(transactionAdapter.transactions[viewHolder.adapterPosition])
            }
        }).attachToRecyclerView(binding.recyclerTransactions)
    }

    private fun setupListeners() {
        binding.fabAddTransaction.setOnClickListener {
            findNavController().navigate(R.id.action_transactionsFragment_to_addTransactionFragment)
        }
    }

    private fun navigateToEditTransaction(transaction: Transaction) {
        val action = TransactionsFragmentDirections
            .actionTransactionsFragmentToAddTransactionFragment(transaction)
        findNavController().navigate(action)
    }

    private fun showDeleteDialog(transaction: Transaction) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Yes") { _, _ ->
                deleteTransaction(transaction)
            }
            .setNegativeButton("No") { _, _ ->
                transactionAdapter.notifyDataSetChanged()
            }
            .show()
    }

    private fun deleteTransaction(transaction: Transaction) {
        transactionViewModel.deleteTransaction(transaction)
        Snackbar.make(binding.root, "Transaction deleted", Snackbar.LENGTH_LONG)
            .setAction("Undo") {
                lifecycleScope.launch {
                    try {
                        transactionViewModel.saveTransaction(transaction)
                    } catch (e: Exception) {
                        Snackbar.make(binding.root, "Failed to undo deletion", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }.show()
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.apply {
            if (isEmpty) {
                tvEmptyState.visibility = View.VISIBLE
                recyclerTransactions.visibility = View.GONE
                tvEmptyState.text = "No transactions yet\nTap + to add a new transaction"
            } else {
                tvEmptyState.visibility = View.GONE
                recyclerTransactions.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
