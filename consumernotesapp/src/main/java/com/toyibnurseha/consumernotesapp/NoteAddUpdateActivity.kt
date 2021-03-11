package com.toyibnurseha.consumernotesapp

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.toyibnurseha.consumernotesapp.databinding.ActivityNoteAddUpdateBinding
import com.toyibnurseha.consumernotesapp.db.DatabaseContract
import com.toyibnurseha.consumernotesapp.db.DatabaseContract.NoteColumns.Companion.CONTENT_URI
import com.toyibnurseha.consumernotesapp.db.DatabaseContract.NoteColumns.Companion.DATE
import com.toyibnurseha.consumernotesapp.entity.Note
import java.text.SimpleDateFormat
import java.util.*

class NoteAddUpdateActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_NOTE = "extra_note"
        const val EXTRA_POSITION = "extra_position"
        const val ALERT_DIALOG_CLOSE = 10
        const val ALERT_DIALOG_DELETE = 20
    }

    private var isEdit = false
    private var note: Note? = null
    private var position = 0
    private lateinit var binding: ActivityNoteAddUpdateBinding
    private lateinit var uriWithId: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteAddUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBarTitle: String
        val btnTitle: String

//        noteHelper = NoteHelper.getInstance(applicationContext)
//        noteHelper.open()

        note = intent.getParcelableExtra(EXTRA_NOTE)

        if (note != null) {
            position = intent.getIntExtra(EXTRA_POSITION, 0)
            isEdit = true
        } else {
            note = Note()
        }

        if (isEdit) {
            // Uri yang di dapatkan disini akan digunakan untuk ambil data dari provider
            // content://com.toyibnurseha.simplenotesapp/note/id

            uriWithId = Uri.parse(CONTENT_URI.toString() + "/" + note?.id)

            val cursor = contentResolver.query(uriWithId, null, null, null, null)
            if (cursor != null) {
                note = MappingHelper.mapCursorToObject(cursor)
                cursor.close()
            }
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

        val values = ContentValues().apply {
            put(DatabaseContract.NoteColumns.TITLE, title)
            put(DatabaseContract.NoteColumns.DESCRIPTION, description)
        }

        if (isEdit) {
            // Gunakan uriWithId dari intent activity ini
            // content://com.toyibnurseha.simplenotesapp/note/id
            contentResolver.update(uriWithId, values, null, null)
            Toast.makeText(this, "One item changed", Toast.LENGTH_SHORT).show()
            finish()
        }else {
            values.put(DATE, getCurrentDate())
            // Gunakan content uri untuk insert
            // content://com.toyibnurseha.simplenotesapp/note/
            contentResolver.insert(CONTENT_URI, values)
            Toast.makeText(this, "One item saved", Toast.LENGTH_SHORT).show()
            finish()
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
                    // Gunakan uriWithId untuk delete
                    contentResolver.delete(uriWithId, null, null)
                    Toast.makeText(this@NoteAddUpdateActivity, "One item deleted", Toast.LENGTH_SHORT).show()
                    finish()
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