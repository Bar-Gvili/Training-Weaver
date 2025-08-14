package com.example.training_weaver.UI

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.training_weaver.R
import com.training_weaver.databinding.FragmentSettingBinding

class settingFragment : Fragment(R.layout.fragment_setting) {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingBinding.bind(view)

        binding.btnLogout.setOnClickListener {
            signOut()
        }
    }

    private fun signOut() {
        try {
            // התנתקות מ-Firebase
            FirebaseAuth.getInstance().signOut()

            // ניסיון להתנתק גם מחשבון Google (אם קיים)
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
            val googleClient = GoogleSignIn.getClient(requireContext(), gso)

            googleClient.signOut().addOnCompleteListener {
                // אחרי התנתקות – ניווט למסך ההתחברות וניקוי ה-back stack
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, /*inclusive=*/true)
                    .build()
                findNavController().navigate(R.id.loginFragment, null, navOptions)
            }
        } catch (e: Exception) {
            // גם אם נפלה שגיאה – ננסה להחזיר למסך התחברות
            Toast.makeText(requireContext(), "Logout failed: ${e.message}", Toast.LENGTH_SHORT).show()
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
            findNavController().navigate(R.id.loginFragment, null, navOptions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
