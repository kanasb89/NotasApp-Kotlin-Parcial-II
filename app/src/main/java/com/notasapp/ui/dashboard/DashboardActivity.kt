package com.notasapp.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.notasapp.R
import com.notasapp.databinding.ActivityDashboardBinding
import com.notasapp.ui.login.LoginActivity
import com.notasapp.ui.notes.CreateNoteActivity
import com.notasapp.ui.notes.NotesListActivity
import com.notasapp.utils.SessionManager

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(applicationContext)

        if (!sessionManager.isLoggedIn()) {
            navigateToLogin()
            return
        }

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = getString(R.string.app_name)

        binding.tvWelcome.text = getString(R.string.dashboard_welcome, sessionManager.getUserName())

        binding.btnCreateNote.setOnClickListener {
            startActivity(Intent(this, CreateNoteActivity::class.java))
        }
        binding.btnListNotes.setOnClickListener {
            startActivity(Intent(this, NotesListActivity::class.java))
        }
        binding.btnLogout.setOnClickListener {
            sessionManager.clearSession()
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
