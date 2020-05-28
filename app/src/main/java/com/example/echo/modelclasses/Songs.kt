package com.example.echo.modelclasses

import android.os.Parcel
import android.os.Parcelable

class Songs(var songId : Long , var songTitle : String , var artist : String , var songData : String ,
            var dateAdded : Long) : Parcelable
{
    //Model class used for saving the complete details of a song together.
    override fun writeToParcel(p0 : Parcel? , p1 : Int)
    {
    }

    override fun describeContents() : Int
    {
        return 0
    }

    object Statified
    {
        //sorting the songs according to their names
        var nameComparator : Comparator<Songs> = Comparator<Songs> { song1 , song2 ->
            val songOne = song1.songTitle.toUpperCase()
            val songTwo = song2.songTitle.toUpperCase()

            songOne.compareTo(songTwo)
        }

        //sorting the songs according to the date
        var dateComparator : Comparator<Songs> = Comparator<Songs> { song1 , song2 ->
            val songOne = song1.dateAdded.toDouble()
            val songTwo = song2.dateAdded.toDouble()

            songOne.compareTo(songTwo)
        }
    }
}