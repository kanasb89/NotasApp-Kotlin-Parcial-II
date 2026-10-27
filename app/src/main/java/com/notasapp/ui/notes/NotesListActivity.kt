package com.notasapp.ui.notes

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.notasapp.R
import com.notasapp.data.api.RetrofitClient
import com.notasapp.data.model.NoteResponse
import com.notasapp.data.repository.NoteRepository
import com.notasapp.databinding.ActivityNotesListBinding
import com.notasapp.utils.Resource
import com.notasapp.utils.ViewModelFactory

class NotesListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotesListBinding
    private val adapter = NotesAdapter { note -> confirmDelete(note) }

    private val viewModel: NotesListViewModel by viewModels {
        ViewModelFactory {
            NotesListViewModel(NoteRepository(RetrofitClient.getApiService(applicationContext)))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = getString(R.string.notes_list_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.rvNotes.layoutManager = LinearLayoutManager(this)
        binding.rvNotes.adapter = adapter
        binding.btnRetry.setOnClickListener { viewModel.loadNotes() }

        observeNotesState()
        observeDeleteState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadNotes()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun confirmDelete(note: NoteResponse) {
        AlertDialog.Builder(this)
            .setTitle(R.string.note_delete_confirm_title)
            .setMessage(getString(R.string.note_delete_confirm_message, note.title))
            .setPositiveButton(R.string.note_delete) { _, _ -> viewModel.deleteNote(note.id) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun observeNotesState() {
        viewModel.notesState.observe(this) { state ->
            when (state) {
                is Resource.Loading -> showLoading()
                is Resource.Success -> showNotes(state.data)
                is Resource.Error -> showError(state.message)
            }
        }
    }

    private fun observeDeleteState() {
        viewModel.deleteState.observe(this) { state ->
            when (state) {
                is Resource.Success -> {
                    Toast.makeText(this, R.string.note_deleted_success, Toast.LENGTH_SHORT).show()
                    viewModel.consumeDeleteState()
                    viewModel.loadNotes()
                }
                is Resource.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    viewModel.consumeDeleteState()
                }
                else -> Unit
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        binding.btnRetry.visibility = View.GONE
    }

    private fun showNotes(notes: List<NoteResponse>) {
        binding.progressBar.visibility = View.GONE
        binding.btnRetry.visibility = View.GONE
        adapter.submitList(notes)

        val isEmpty = notes.isEmpty()
        binding.tvEmpty.text = getString(R.string.notes_empty)
        binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvNotes.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.btnRetry.visibility = View.VISIBLE
        binding.tvEmpty.text = message
        binding.tvEmpty.visibility = View.VISIBLE
        binding.rvNotes.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}