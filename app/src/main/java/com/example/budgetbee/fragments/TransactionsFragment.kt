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
import com.example.budgetbee.utils.SharedPrefHelper
import com.google.android.material.snackbar.Snackbar

class TransactionsFragment : Fragment() {
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPrefHelper: SharedPrefHelper
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        sharedPrefHelper = SharedPrefHelper(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(
            transactions = sharedPrefHelper.getTransactions(),
            onEdit = ::navigateToEditTransaction,
            onDelete = ::deleteTransaction,
            sharedPrefHelper = sharedPrefHelper,
            context = requireContext()
        )

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                showDeleteDialog(transactionAdapter.transactions[viewHolder.adapterPosition])
            }
        }).attachToRecyclerView(binding.recyclerTransactions)

        binding.recyclerTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
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
                transactionAdapter.updateTransactions(sharedPrefHelper.getTransactions())
            }
            .show()
    }

    private fun deleteTransaction(transaction: Transaction) {
        val transactions = sharedPrefHelper.getTransactions().toMutableList()
        transactions.removeAll { it.id == transaction.id }
        sharedPrefHelper.saveTransactions(transactions)
        transactionAdapter.updateTransactions(transactions)

        Snackbar.make(binding.root, "Transaction deleted", Snackbar.LENGTH_LONG)
            .setAction("Undo") {
                transactions.add(transaction)
                sharedPrefHelper.saveTransactions(transactions)
                transactionAdapter.updateTransactions(transactions)
            }.show()
    }


    override fun onResume() {
        super.onResume()
        transactionAdapter.updateTransactions(sharedPrefHelper.getTransactions())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}