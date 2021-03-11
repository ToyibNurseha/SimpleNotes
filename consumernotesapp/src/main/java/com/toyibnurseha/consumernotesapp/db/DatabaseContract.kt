package com.toyibnurseha.consumernotesapp.db

import android.net.Uri
import android.provider.BaseColumns
object DatabaseContract {

    const val AUTHORITY = "com.toyibnurseha.simplenotesapp"
    const val SCHEME = "content"

    internal class NoteColumns : BaseColumns {
        companion object {
            const val TABLE_NAME = "note"
            const val _ID = "_id"
            const val TITLE = "title"
            const val DESCRIPTION = "description"
            const val DATE = "date"

            //for creating URI content://com.toyibnurseha.simplenotesapp/note
            val CONTENT_URI: Uri = Uri.Builder().scheme(SCHEME)
                .authority(AUTHORITY)
                .appendPath(TABLE_NAME)
                .build()
        }
    }
}