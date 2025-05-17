package com.example.budgetbee.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.budgetbee.R
import com.example.budgetbee.activities.MainActivity
import com.example.budgetbee.databinding.FragmentLoginBinding
import com.example.budgetbee.viewModel.UserViewModel
import com.google.android.material.snackbar.Snackbar

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeAuthResult()
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            
            // Validate input
            when {
                email.isBlank() -> {
                    showError("Email is required")
                    return@setOnClickListener
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    showError("Invalid email format")
                    return@setOnClickListener
                }
                password.isBlank() -> {
                    showError("Password is required")
                    return@setOnClickListener
                }
            }
            
            // Show loading state and disable UI
            setLoadingState(true)
            
            Log.d("LoginFragment", "Attempting login with email: $email")
            userViewModel.loginUser(email, password)
        }

        binding.signupButton.setOnClickListener {
            Log.d("LoginFragment", "Navigating to signup fragment")
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }
    }

    private fun observeAuthResult() {
        userViewModel.authResult.observe(viewLifecycleOwner) { result ->
            // Hide loading state
            setLoadingState(false)

            when (result) {
                is UserViewModel.AuthResult.Success -> {
                    Log.d("LoginFragment", "Login successful, navigating to main activity")
                    try {
                        // Use post to ensure UI operations happen after current frame
                        view?.post {
                            val intent = Intent(requireContext(), MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            requireActivity().finish()
                        }
                    } catch (e: Exception) {
                        Log.e("LoginFragment", "Navigation error", e)
                        showError("Navigation error: ${e.message}")
                    }
                }
                is UserViewModel.AuthResult.Error -> {
                    Log.e("LoginFragment", "Login error: ${result.message}")
                    showError(result.message)
                }
                null -> {
                    Log.d("LoginFragment", "Auth result is null")
                }
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.loginButton.isEnabled = !isLoading
        binding.signupButton.isEnabled = !isLoading
        binding.emailEditText.isEnabled = !isLoading
        binding.passwordEditText.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(R.color.error_color, null))
            .setTextColor(resources.getColor(R.color.white, null))
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 
