package com.yezao.demo

import android.content.Context
import android.net.Uri
import android.util.Log

object Utils {

    val TAG = "Utils"

    fun getPathByContentUri(context:Context,uri:Uri?):String?{
        if (uri == null) {
            return null
        }
        val query = context.contentResolver.query(uri, null, null, null, null)
        if (query != null) {
            query.moveToPrevious()
            query.moveToFirst()
            Log.i(TAG,"query column count ${query.columnCount}")
            for (columnName in query.columnNames) {
                Log.i(TAG,"query column name :${columnName} index :${query.getColumnIndex(columnName)}  v:${query.getString(query.getColumnIndex(columnName))}")
            }
        }

        return null

    }
}