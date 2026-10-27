package com.notasapp.ui.notes

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.notasapp.R
import com.notasapp.data.api.RetrofitClient
import com.notasapp.data.repository.NoteRepository
import com.notasapp.databinding.ActivityCreateNoteBinding
import com.notasapp.utils.Resource
import com.notasapp.utils.ViewModelFactory

class CreateNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateNoteBinding

    private val viewModel: CreateNoteViewModel by viewModels {
        ViewModelFactory {
            CreateNoteViewModel(NoteRepository(RetrofitClient.getApiService(applicationContext)))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = getString(R.string.create_note_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSave.setOnClickListener { attemptSave() }
        observeCreateState()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun attemptSave() {
        val title = binding.etTitle.text?.toString()?.trim().orEmpty()
        val content = binding.etContent.text?.toString()?.trim().orEmpty()

        if (!validateInputs(title, content)) return

        viewModel.createNote(title, content)
    }

    private fun validateInputs(title: String, content: String): Boolean {
        binding.tilTitle.error = null
        binding.tilContent.error = null
        var isValid = true

        if (title.isEmpty()) {
            binding.tilTitle.error = getString(R.string.error_title_required)
            isValid = false
        }
        if (content.isEmpty()) {
            binding.tilContent.error = getString(R.string.error_content_required)
            isValid = false
        }
        return isValid
    }

    private fun observeCreateState() {
        viewModel.createState.observe(this) { state ->
            when (state) {
                is Resource.Loading -> setLoading(true)
                is Resource.Success<*> -> {
                    setLoading(false)
                    Toast.makeText(this, R.string.note_created_success, Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Resource.Error -> {
                    setLoading(false)
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !isLoading
        binding.etTitle.isEnabled = !isLoading
        binding.etContent.isEnabled = !isLoading
    }
}
