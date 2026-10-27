package com.notasapp.ui.notes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.notasapp.data.model.NoteResponse
import com.notasapp.databinding.ItemNoteBinding
import com.notasapp.utils.DateFormatter

class NotesAdapter(
    private val onDeleteClick: (NoteResponse) -> Unit
) : ListAdapter<NoteResponse, NotesAdapter.NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding, onDeleteClick)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NoteViewHolder(
        private val binding: ItemNoteBinding,
        private val onDeleteClick: (NoteResponse) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: NoteResponse) {
            binding.tvNoteTitle.text = note.title
            binding.tvNoteContent.text = note.content
            binding.tvNoteDate.text = DateFormatter.formatIso(note.createdAt)
            binding.btnDeleteNote.setOnClickListener { onDeleteClick(note) }
        }
    }

    private class NoteDiffCallback : DiffUtil.ItemCallback<NoteResponse>() {
        override fun areItemsTheSame(oldItem: NoteResponse, newItem: NoteResponse): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: NoteResponse, newItem: NoteResponse): Boolean =
            oldItem == newItem
    }
}