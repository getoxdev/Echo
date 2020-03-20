package com.internshala.echo.Databases

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.internshala.echo.Databases.EchoDatabase.Staticated.COLUMN_SONG_ARTIST
import com.internshala.echo.Databases.EchoDatabase.Staticated.COLUMN_SONG_ID
import com.internshala.echo.Databases.EchoDatabase.Staticated.COLUMN_SONG_PATH
import com.internshala.echo.Databases.EchoDatabase.Staticated.COLUMN_SONG_TITLE
import com.internshala.echo.Databases.EchoDatabase.Staticated.TABLE_NAME
import com.internshala.echo.Songs

class EchoDatabase : SQLiteOpenHelper {

    val _songList = ArrayList<Songs>()

    object Staticated {
        var DB_VERSION = 1
        val DB_NAME = "FavouritesDatabase"
        val TABLE_NAME = "FavouriteTable"
        val COLUMN_SONG_ID = "SongID"
        val COLUMN_SONG_PATH = "SongPath"
        val COLUMN_SONG_TITLE = "SongTitle"
        val COLUMN_SONG_ARTIST = "SongArtist"
    }

    constructor(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : super(
        context,
        name,
        factory,
        version
    )

    constructor(context: Context?) : super(context, Staticated.DB_NAME, null, Staticated.DB_VERSION)

    override fun onCreate(sqLiteDatabase: SQLiteDatabase?) {
        sqLiteDatabase?.execSQL("CREATE TABLE " + TABLE_NAME + "( " + COLUMN_SONG_ID + " INTEGER," + COLUMN_SONG_PATH + " TEXT," + COLUMN_SONG_ARTIST + " TEXT," + COLUMN_SONG_TITLE + " TEXT);")
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

    fun storeAsFavourite(ID: Int?, Path: String?, Artist: String?, Title: String?) {
        val db = this.writableDatabase
        var contentValues = ContentValues()
        contentValues.put(COLUMN_SONG_ID, ID)
        contentValues.put(COLUMN_SONG_PATH, Path)
        contentValues.put(COLUMN_SONG_ARTIST, Artist)
        contentValues.put(COLUMN_SONG_TITLE, Title)
        db.insert(TABLE_NAME, null, contentValues)
        db.close()
    }

    fun queryDBList(): ArrayList<Songs>? {
        try {
            val db = this.writableDatabase
            val query_params = "SELECT * FROM " + TABLE_NAME
            var cursor = db.rawQuery(query_params, null)
            if (cursor.moveToFirst()) {
                do {
                    var _songID = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SONG_ID))
                    var _songPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SONG_PATH))
                    var _songArtist = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SONG_ARTIST))
                    var _songTitle = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SONG_TITLE))

                    _songList.add(Songs(_songID.toLong(), _songPath, _songTitle, _songArtist, 0))
                } while (cursor.moveToNext());
            } else {
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return _songList
    }

    fun checkIfIDExists(_songID: Int): Boolean {
        var storeID = -101
        val db = this.writableDatabase
        val query_params = "SELECT * FROM " + TABLE_NAME + " WHERE SongID = '$_songID'"
        val cursor = db.rawQuery(query_params, null)
        if (cursor.moveToFirst()) {
            do {
                storeID = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SONG_ID))
            } while (cursor.moveToNext())
        } else {
            return false
        }
        return storeID != -101
    }

    fun deleteFavourites(_songID: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, COLUMN_SONG_ID + " = " + _songID, null)
        db.close()
    }

    fun checkSize(): Int {
        var counter = 0
        val db = this.writableDatabase
        var query_params = "SELECT * FROM " + TABLE_NAME
        val cursor = db.rawQuery(query_params, null)
        if (cursor.moveToFirst()) {
            do {
                counter += 1
            } while (cursor.moveToNext())
        } else {
            return 0
        }
        return counter
    }
}