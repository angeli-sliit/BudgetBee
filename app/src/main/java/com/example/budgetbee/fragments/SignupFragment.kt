package com.example.budgetbee.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.budgetbee.R
import com.example.budgetbee.databinding.FragmentSignupBinding
import com.example.budgetbee.viewModel.UserViewModel
import com.google.android.material.snackbar.Snackbar

class SignupFragment : Fragment() {
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserViewModel by viewModels {
        androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeAuthResult()
    }

    private fun setupListeners() {
        binding.signupButton.setOnClickListener {
            val username = binding.usernameInput.text.toString()
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()
            val confirmPassword = binding.confirmPasswordInput.text.toString()

            when {
                username.isBlank() -> showError("Username cannot be empty")
                email.isBlank() -> showError("Email cannot be empty")
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> 
                    showError("Invalid email format")
                password.isBlank() -> showError("Password cannot be empty")
                password.length < 8 -> showError("Password must be at least 8 characters")
                !password.any { it.isDigit() } -> 
                    showError("Password must contain at least one number")
                !password.any { it.isUpperCase() } -> 
                    showError("Password must contain at least one uppercase letter")
                !password.any { it.isLowerCase() } -> 
                    showError("Password must contain at least one lowercase letter")
                password != confirmPassword -> showError("Passwords do not match")
                else -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.signupButton.isEnabled = false
                    viewModel.registerUser(username, email, password)
                }
            }
        }
        binding.loginLink.setOnClickListener {
            findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
        }
    }

    private fun observeAuthResult() {
        viewModel.authResult.observe(viewLifecycleOwner) { result ->
            binding.progressBar.visibility = View.GONE
            binding.signupButton.isEnabled = true

            when (result) {
                is UserViewModel.AuthResult.Success -> {
                    showSuccess("Registration successful! Please login.")
                    findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
                }
                is UserViewModel.AuthResult.Error -> {
                    showError(result.message)
                }
                null -> {
                    // Initial state, do nothing
                }
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(requireContext().getColor(R.color.error_color))
            .setTextColor(requireContext().getColor(R.color.white))
            .show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(requireContext().getColor(R.color.success_color))
            .setTextColor(requireContext().getColor(R.color.white))
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 