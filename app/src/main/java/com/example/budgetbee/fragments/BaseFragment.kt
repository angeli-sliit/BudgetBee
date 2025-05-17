package com.example.budgetbee.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.budgetbee.utils.SharedPrefHelper

abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    private var _binding: VB? = null
    protected val binding get() = _binding!!

    protected lateinit var sharedPrefHelper: SharedPrefHelper

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPrefHelper = SharedPrefHelper(context) // Remove requireContext() here
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = getViewBinding()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected fun withBinding(block: VB.() -> Unit) {
        _binding?.let(block)
    }

    abstract fun getViewBinding(): VB
    abstract fun updateUI()
}