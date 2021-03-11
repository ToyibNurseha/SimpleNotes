package com.toyibnurseha.simplenotesapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.toyibnurseha.simplenotesapp.databinding.ActivityMainBinding
import com.toyibnurseha.simplenotesapp.db.NoteHelper
import com.toyibnurseha.simplenotesapp.entity.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: NoteAdapter

    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val EXTRA_STATE = "EXTRA_STATE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Notes"

        binding.rvNotes.layoutManager = LinearLayoutManager(this)
        binding.rvNotes.setHasFixedSize(true)
        adapter = NoteAdapter(this)
        binding.rvNotes.adapter = adapter

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, NoteAddUpdateActivity::class.java)
            startActivityForResult(intent, NoteAddUpdateActivity.REQUEST_ADD)
        }

        loadNotesAsync()

        if (savedInstanceState == null) {
            loadNotesAsync()
        } else {
            val list = savedInstanceState.getParcelableArrayList<Note>(EXTRA_STATE)
            if (list != null) {
                adapter.listNotes = list
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(EXTRA_STATE, adapter.listNotes)
    }

    private fun loadNotesAsync() {
        GlobalScope.launch(Dispatchers.Main) {
            binding.progressbar.visibility = View.VISIBLE
            val noteHelper = NoteHelper.getInstance(applicationContext)
            noteHelper.open()

            val defferedNotes = async(Dispatchers.IO) {
                val cursor = noteHelper.queryAll()
                MappingHelper.mapCursorToArrayList(cursor)
            }
            binding.progressbar.visibility = View.INVISIBLE
            val notes = defferedNotes.await()
            if (notes.size > 0) {
                adapter.listNotes = notes
            } else {
                adapter.listNotes = ArrayList()
                showSnackbarMessage("Currently data is empty")
            }

            noteHelper.close()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null) {
            when (requestCode) {
                NoteAddUpdateActivity.REQUEST_ADD -> if (resultCode == NoteAddUpdateActivity.RESULT_ADD) {
                    val note =
                        data.getParcelableExtra<Note>(NoteAddUpdateActivity.EXTRA_NOTE) as Note

                    adapter.addItem(note)
                    binding.rvNotes.smoothScrollToPosition(adapter.itemCount - 1)

                    showSnackbarMessage("One item added")
                }

                NoteAddUpdateActivity.REQUEST_UPDATE -> when (resultCode) {
                    NoteAddUpdateActivity.RESULT_UPDATE -> {
                        val note =
                            data.getParcelableExtra<Note>(NoteAddUpdateActivity.EXTRA_NOTE) as Note
                        val position = data.getIntExtra(NoteAddUpdateActivity.EXTRA_POSITION, 0)

                        adapter.updateItem(position, note)
                        binding.rvNotes.smoothScrollToPosition(position)

                        showSnackbarMessage("One item changed")
                    }

                    NoteAddUpdateActivity.RESULT_DELETE -> {
                        val position = data.getIntExtra(NoteAddUpdateActivity.EXTRA_POSITION, 0)
                        adapter.removeItem(position)
                        showSnackbarMessage("One item deleted")
                    }
                }
            }
        }
    }

    private fun showSnackbarMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}