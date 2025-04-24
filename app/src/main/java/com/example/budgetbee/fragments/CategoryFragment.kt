package com.example.budgetbee.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetbee.R
import com.example.budgetbee.adapters.CategoryAdapter
import com.example.budgetbee.databinding.FragmentCategoryAnalysisBinding
import com.example.budgetbee.models.Category
import com.example.budgetbee.models.TransactionType
import com.example.budgetbee.utils.SharedPrefHelper
import java.text.SimpleDateFormat
import java.util.*

class CategoryFragment : Fragment() {
    private var _binding: FragmentCategoryAnalysisBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPrefHelper: SharedPrefHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryAnalysisBinding.inflate(inflater, container, false)
        sharedPrefHelper = SharedPrefHelper(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        val monthlyExpenses = sharedPrefHelper.getTransactions()
            .filter { transaction ->
                try {
                    val transactionDate = transaction.date
                    val transactionCalendar = Calendar.getInstance().apply { time = transactionDate }
                    transactionCalendar.get(Calendar.MONTH) == currentMonth &&
                            transactionCalendar.get(Calendar.YEAR) == currentYear &&
                            transaction.type == TransactionType.EXPENSE
                } catch (e: Exception) {
                    false
                }
            }


        val categories = monthlyExpenses
            .groupBy { it.category }
            .map { (name, transactions) ->
                Category(name, R.drawable.ic_category, 0, transactions.sumOf { it.amount })
            }
            .sortedByDescending { it.amount }

        if (categories.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.recyclerCategories.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.recyclerCategories.visibility = View.VISIBLE
            binding.recyclerCategories.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerCategories.adapter = CategoryAdapter(categories)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}