package com.example.training_weaver.UI

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.training_weaver.R
import com.training_weaver.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var googleClient: GoogleSignInClient

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(res.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val token = account.idToken
            if (token.isNullOrEmpty()) {
                binding.progress.isVisible = false
                Toast.makeText(requireContext(), "Missing ID token", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            firebaseAuthWithGoogle(token)
        } catch (e: ApiException) {
            binding.progress.isVisible = false
            Log.e("LoginFragment", "Google sign-in failed", e)
            Toast.makeText(requireContext(), "Sign-in failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.default_web_client_id)) // מתוך google-services.json
            .build()
        googleClient = GoogleSignIn.getClient(requireActivity(), gso)

        // אם כבר מחובר ל-Firebase – דלג הלאה
        auth.currentUser?.let {
            findNavController().navigate(R.id.action_loginFragment_to_workoutListFragment)
            return
        }

        binding.btnGoogleSignIn.setOnClickListener {
            binding.progress.isVisible = true
            signInLauncher.launch(googleClient.signInIntent)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                binding.progress.isVisible = false
                if (task.isSuccessful) {
                    // עכשיו FirebaseConnections.currentUid() יחזיר UID תקין
                    findNavController().navigate(R.id.action_loginFragment_to_workoutListFragment)
                } else {
                    Log.e("LoginFragment", "Firebase sign-in failed", task.exception)
                    Toast.makeText(
                        requireContext(),
                        "Firebase sign-in failed: ${task.exception?.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
