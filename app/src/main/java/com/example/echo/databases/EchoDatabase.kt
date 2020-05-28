package com.example.echo.databases

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.echo.modelclasses.Songs
import java.util.*


class EchoDatabase : SQLiteOpenHelper
{
    //manages the database for the application to store the favourites
    var _songList = ArrayList<Songs>()          //List for storing the favorite songs

    object Staticated
    {
        val DB_VERSION = 13
        val DB_NAME = "FavoriteDatabase"
        val TABLE_NAME = "FavoriteTable"
        val COLUMN_ID = "SongID"
        val COLUMN_SONG_TITLE = "SongTitle"
        val COLUMN_SONG_ARTIST = "SongArtist"
        val COLUMN_SONG_PATH = "SongPath"
    }

    override fun onCreate(sqliteDatabase : SQLiteDatabase?)
    {
        //called when the application first creates the database
        sqliteDatabase?.execSQL("CREATE TABLE " + Staticated.TABLE_NAME + " ( " + Staticated.COLUMN_ID + " INTEGER, " + Staticated.COLUMN_SONG_ARTIST + " STRING, " + Staticated.COLUMN_SONG_TITLE + " STRING, " + Staticated.COLUMN_SONG_PATH + " STRING);")
        //CREATE TABLE FavouriteTable (SongsID INTEGER, SongArtist STRING, SongTitle STRING, SongPath STRING);
    }

    // When we upgrade our app or add new columns to our table
    override fun onUpgrade(p0 : SQLiteDatabase? , p1 : Int , p2 : Int)
    {
    }

    constructor(context : Context? , name : String? , factory : SQLiteDatabase.CursorFactory? ,
                version : Int) : super(context ,
                                       name ,
                                       factory ,
                                       version)

    constructor(context : Context?) : super(context ,
                                            Staticated.DB_NAME ,
                                            null ,
                                            Staticated.DB_VERSION)


    // Function to insert values in the database
    fun storeAsFavourite(id : Int? , artist : String? , songTitle : String? , path : String?)
    {
        //used to store the songs as favourites
        val db = this.writableDatabase          //opens the db for editing so that changes can be made to the database
        var contentValues = ContentValues()             //stores the values which are pushed into the database
        contentValues.put(Staticated.COLUMN_ID ,
                          id)
        contentValues.put(Staticated.COLUMN_SONG_ARTIST ,
                          artist)
        contentValues.put(Staticated.COLUMN_SONG_TITLE ,
                          songTitle)
        contentValues.put(Staticated.COLUMN_SONG_PATH ,
                          path)
        db.insert(Staticated.TABLE_NAME ,
                  null ,
                  contentValues)            //inserts the values into the table
        db.close()               //should be closed in order to prevent data leakage
    }

    fun queryDBList() : ArrayList<Songs>?
    {
        //asks the database for the list of Songs stored as favourite
        try
        {
            val db = this.readableDatabase
            val query_params = "select * from " + Staticated.TABLE_NAME             //SELECT * FROM FavoriteTable
            val cursor = db.rawQuery(query_params ,
                                     null)      // Cursor object
            if (cursor.moveToFirst())
            {
                do
                {
                    var _id = cursor.getInt(cursor.getColumnIndexOrThrow(Staticated.COLUMN_ID))
                    var _artist = cursor.getString(cursor.getColumnIndexOrThrow(Staticated.COLUMN_SONG_ARTIST))
                    var _title = cursor.getString(cursor.getColumnIndexOrThrow(Staticated.COLUMN_SONG_TITLE))
                    var _songPath = cursor.getString(cursor.getColumnIndexOrThrow(Staticated.COLUMN_SONG_PATH))

                    _songList.add(Songs(_id.toLong() ,
                                        _title ,
                                        _artist ,
                                        _songPath ,
                                        0))

                }
                while (cursor.moveToNext())
            }
            else
            {
                return null
            }

        }
        catch (e : Exception)
        {
            e.printStackTrace()
        }
        return _songList
    }

    fun checkifIdExists(_id : Int) : Boolean
    {
        var storeId = -1090 //Random id which does not exist
        val db = this.readableDatabase
        val query_params = "SELECT * FROM " + Staticated.TABLE_NAME + " WHERE SongId = '$_id'"
        val cursor = db.rawQuery(query_params ,
                                 null)
        if (cursor.moveToFirst())
        {
            //checks whether the query returned an empty set and it moves the cursor to the first result
            do
            {
                storeId = cursor.getInt(cursor.getColumnIndexOrThrow(Staticated.COLUMN_ID))
            }
            while (cursor.moveToNext())              //moves the cursor to the first result
        }
        else
        {
            return false
        }
        return storeId != -1090                 //returns a boolean value
    }

    fun deleteFavourite(_id : Int)
    {
        //used to delete the songs from the favourite
        val db = this.writableDatabase
        db.delete(Staticated.TABLE_NAME ,
                  Staticated.COLUMN_ID + " = " + _id ,
                  null)
        db.close()
    }

    fun checkSize() : Int
    {
        var counter = 0
        val db = this.readableDatabase
        val query_params = "SELECT * FROM " + Staticated.TABLE_NAME
        val cursor = db.rawQuery(query_params ,
                                 null)               //stores the entries returned by the database
        if (cursor.moveToFirst())
        {
            do
            {
                counter++
            }
            while (cursor.moveToNext())
        }
        else
        {
            return 0
        }
        return counter                  //returns the number of elements in the database
    }
}