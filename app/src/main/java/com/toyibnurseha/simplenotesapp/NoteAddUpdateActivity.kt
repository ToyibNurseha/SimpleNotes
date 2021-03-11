package com.toyibnurseha.simplenotesapp

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.toyibnurseha.simplenotesapp.databinding.ActivityNoteAddUpdateBinding
import com.toyibnurseha.simplenotesapp.db.DatabaseContract
import com.toyibnurseha.simplenotesapp.db.DatabaseContract.NoteColumns.Companion.DATE
import com.toyibnurseha.simplenotesapp.db.NoteHelper
import com.toyibnurseha.simplenotesapp.entity.Note
import java.text.SimpleDateFormat
import java.util.*

class NoteAddUpdateActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_NOTE = "extra_note"
        const val EXTRA_POSITION = "extra_position"
        const val REQUEST_ADD = 100
        const val RESULT_ADD = 101
        const val REQUEST_UPDATE = 200
        const val RESULT_UPDATE = 201
        const val RESULT_DELETE = 301
        const val ALERT_DIALOG_CLOSE = 10
        const val ALERT_DIALOG_DELETE = 20
    }

    private var isEdit = false
    private var note: Note? = null
    private var position = 0
    private lateinit var noteHelper: NoteHelper
    private lateinit var binding: ActivityNoteAddUpdateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteAddUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBarTitle: String
        val btnTitle: String

        noteHelper = NoteHelper.getInstance(applicationContext)
        noteHelper.open()

        note = intent.getParcelableExtra(EXTRA_NOTE)

        if (note != null) {
            position = intent.getIntExtra(EXTRA_POSITION, 0)
            isEdit = true
        } else {
            note = Note()
        }

        if (isEdit) {
            actionBarTitle = "Change"
            btnTitle = "Update"

            note?.let {
                binding.edtTitle.setText(it.title)
                binding.edtDescription.setText(it.description)
            }
        } else {
            actionBarTitle = "Add"
            btnTitle = "Save"
        }

        supportActionBar?.title = actionBarTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSubmit.text = btnTitle

        onClickHandler()
    }

    private fun onClickHandler() {
        binding.btnSubmit.setOnClickListener {
            handleSubmit()
        }
    }

    private fun handleSubmit() {
        val title = binding.edtTitle.text.toString().trim()
        val description = binding.edtDescription.text.toString().trim()

        if (title.isEmpty()) {
            binding.edtTitle.error = "Field can not be empty"
            return
        }

        note?.title = title
        note?.description = description

        val intent = Intent().apply {
            putExtra(EXTRA_NOTE, note)
            putExtra(EXTRA_POSITION, position)
        }

        val values = ContentValues().apply {
            put(DatabaseContract.NoteColumns.TITLE, title)
            put(DatabaseContract.NoteColumns.DESCRIPTION, description)
        }

        if (isEdit) {
            val result = noteHelper.update(note?.id.toString(), values).toLong()
            if (result > 0) {
                setResult(RESULT_UPDATE, intent)
                finish()
            }else {
                Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show()
            }
        }else {
            note?.date = getCurrentDate()
            values.put(DATE, getCurrentDate())
            val result = noteHelper.insert(values)
            
            if (result > 0) {
                note?.id = result.toInt()
                setResult(RESULT_ADD, intent)
                finish()
            }else {
                Toast.makeText(this, "Failed to add data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentDate(): String? {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH::mm:ss", Locale.getDefault())
        val date = Date()
        
        return dateFormat.format(date)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (isEdit) {
            menuInflater.inflate(R.menu.menu_form, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)  {
            
            R.id.action_delete -> showAlertDialog(ALERT_DIALOG_DELETE)
            android.R.id.home -> showAlertDialog(ALERT_DIALOG_CLOSE)
            
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAlertDialog(type: Int) {
        val isDialogClose = type == ALERT_DIALOG_CLOSE
        val dialogTitle: String
        val dialogMessage: String
        
        if (isDialogClose) {
            dialogTitle = "Cancel"
            dialogMessage = "Are you sure want to cancel change in form"
        }else {
            dialogMessage = "Are you sure want to delete this item?"
            dialogTitle = "Delete note"
        }
        
        val alertDialogBuilder = AlertDialog.Builder(this).apply { 
            setTitle(dialogTitle)
            setMessage(dialogMessage)
            setCancelable(false)
            setPositiveButton("Yes") {_, _, -> 
                if (isDialogClose) {
                    finish()
                }else {
                    val result = noteHelper.deleteById(note?.id.toString()).toLong()
                    if (result > 0) {
                        val intent = Intent()
                        intent.putExtra(EXTRA_POSITION, position)
                        setResult(RESULT_DELETE, intent)
                        finish()
                    }else {
                        Toast.makeText(
                            this@NoteAddUpdateActivity,
                            "Failed to delete data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                setNegativeButton("No") {dialog, _ ->
                    dialog.cancel()
                }
            }
        }
        alertDialogBuilder.create().show()
    }

    override fun onBackPressed() {
        showAlertDialog(ALERT_DIALOG_CLOSE)
    }
}