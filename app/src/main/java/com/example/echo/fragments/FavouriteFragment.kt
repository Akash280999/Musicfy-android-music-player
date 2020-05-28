package com.example.echo.fragments


import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.example.echo.R
import com.example.echo.modelclasses.Songs
import com.example.echo.adapters.FavouriteAdapter
import com.example.echo.databases.EchoDatabase
import com.example.echo.utils.CurrentSongHelper


class FavouriteFragment : Fragment()
{
    var myActivity : Activity? = null
    var noFavorites : TextView? = null
    var nowPlayingBottomBar : RelativeLayout? = null
    var playPauseButton : ImageButton? = null
    var songTitle : TextView? = null
    var recyclerView : RecyclerView? = null
    var trackPosition : Int = 0
    var favoriteContent : EchoDatabase? = null

    var refreshList : ArrayList<Songs>? = null
    var getListFromDatabase : ArrayList<Songs>? = null

    object Statified
    {
        var mediaPlayer : MediaPlayer? = null
    }

    override fun onCreateView(inflater : LayoutInflater , container : ViewGroup? , savedInstanceState : Bundle?) : View?
    {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favourite ,
                                      container ,
                                      false)
        setHasOptionsMenu(true)

        activity?.title = "Favourites"

        noFavorites = view?.findViewById(R.id.noFavorites)
        nowPlayingBottomBar = view?.findViewById(R.id.hiddenBarFavScreen)
        playPauseButton = view?.findViewById(R.id.playPauseButton)
        songTitle = view?.findViewById(R.id.songTitleFavScreen)
        recyclerView = view?.findViewById(R.id.favoriteRecycler)

        return view
    }

    override fun onAttach(context : Context?)
    {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity : Activity?)
    {
        super.onAttach(activity)
        myActivity = context as Activity
    }

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState : Bundle?)
    {
        super.onActivityCreated(savedInstanceState)

        favoriteContent = EchoDatabase(myActivity)
        display_favorites_by_searching()
        bottomBarSetup()

    }

    override fun onResume()
    {
        super.onResume()
    }

    override fun onPrepareOptionsMenu(menu : Menu?)
    {
        super.onPrepareOptionsMenu(menu)
        val item = menu?.findItem(R.id.action_sort)
        item?.isVisible = false
    }

    fun getSongsFromPhone() : ArrayList<Songs>?
    {
        var arrayList = ArrayList<Songs>()
        var contentResolver = myActivity?.contentResolver
        var songUri =
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI         //contains information about all the media files present in on device
        var songCursor = contentResolver?.query(songUri ,
                                                null ,
                                                null ,
                                                null ,
                                                null)

        if (songCursor != null && songCursor.moveToFirst())
        {
            //checks whether the result is empty or not and moves to the first result
            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)

            do
            {
                //moveToNext() returns the next row of the results
                var currentId = songCursor.getLong(songId)
                var currentTitle = songCursor.getString(songTitle)
                var currentArtist = songCursor.getString(songArtist)
                var currentData = songCursor.getString(songData)
                var currentDate = songCursor.getLong(dateIndex)
                arrayList.add(Songs(currentId ,
                                    currentTitle ,
                                    currentArtist ,
                                    currentData ,
                                    currentDate))
            }
            while (songCursor.moveToNext())
        }
        else
        {
            return null
        }
        songCursor.close()

        return arrayList
    }

    fun bottomBarSetup()
    {
        //places the bottom bar on the favorite screen when we come back from the song playing screen to the favorite screen
        try
        {
            bottomBarClickHandler()
            songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            SongPlayingFragment.Statified.mediaPlayer?.setOnCompletionListener {
                songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
                SongPlayingFragment.Staticated.onSongComplete()
            }

            if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean)
            {
                //coming back from the song playing screen
                //song was playing so bottom bar is placed
                //playPauseHelper.isPlaying = true
                nowPlayingBottomBar?.visibility = View.VISIBLE
            }
            else
            {
                //song was not playing so bottom bar is not placed
                nowPlayingBottomBar?.visibility = View.INVISIBLE
                //playPauseHelper.isPlaying = false
            }
        }
        catch (e : Exception)
        {
            e.printStackTrace()
        }
    }

    fun bottomBarClickHandler()
    {
        nowPlayingBottomBar?.setOnClickListener {

            val songPlayingFragment = SongPlayingFragment()
            Statified.mediaPlayer = SongPlayingFragment.Statified.mediaPlayer
            var args = Bundle()
            args.putString("songArtist" ,
                           SongPlayingFragment.Statified.currentSongHelper?.songArtist)
            args.putString("path" ,
                           SongPlayingFragment.Statified.currentSongHelper?.songPath)
            args.putString("songTitle" ,
                           SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            args.putInt("SongId" ,
                        SongPlayingFragment.Statified.currentSongHelper?.songId?.toInt() as Int)
            args.putInt("songPosition" ,
                        SongPlayingFragment.Statified.currentSongHelper?.currentPosition?.toInt() as Int)
            args.putParcelableArrayList("songData" ,
                                        SongPlayingFragment.Statified._fetchSongs)
            args.putString("FavBottomBar" ,
                           "success")
            songPlayingFragment.arguments = args           // linking songPlayingFragment to Bundle obj

            fragmentManager?.beginTransaction()
                    ?.replace(R.id.details_fragment ,
                              songPlayingFragment)
                    ?.addToBackStack("SongPlayingFragment")
                    ?.commit()
        }

        playPauseButton?.setOnClickListener {

            if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean)
            {
                SongPlayingFragment.Statified.mediaPlayer?.pause()
                trackPosition = SongPlayingFragment.Statified.mediaPlayer?.currentPosition as Int
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            }
            else
            {
                //song was not playing and we start the song
                SongPlayingFragment.Statified.mediaPlayer?.seekTo(trackPosition)
                SongPlayingFragment.Statified.mediaPlayer?.start()
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        }
    }

    fun display_favorites_by_searching()
    {
        if (favoriteContent?.checkSize() as Int > 0)
        {
            //checks if database has any entry or not
            refreshList = ArrayList<Songs>()
            getListFromDatabase = favoriteContent?.queryDBList()

            var fetchListfromDevice = getSongsFromPhone()

            if (fetchListfromDevice != null)
            {
                //if no songs then no favourites
                for (i in 0..fetchListfromDevice.size - 1)
                {
                    //checks all the songs in the phone
                    for (j in 0..getListFromDatabase?.size as Int - 1)
                    {
                        //iterates through every song in database
                        if (getListFromDatabase?.get(j)?.songId == (fetchListfromDevice.get(i).songId))
                        {
                            //matches the song on the favourite list with the songs on device
                            refreshList?.add((getListFromDatabase as ArrayList<Songs>)[j])
                        }
                    }
                }
            }
            else
            {
            }
            if (refreshList == null)
            {
                //refresh list is null so there are no favourites
                recyclerView?.visibility = View.INVISIBLE
                noFavorites?.visibility = View.VISIBLE
            }
            else
            {
                //displays the favourite songs
                var favoriteAdapter = FavouriteAdapter(refreshList as ArrayList<Songs> ,
                                                       myActivity as Context)
                val mLayoutManager = LinearLayoutManager(activity)
                recyclerView?.layoutManager = mLayoutManager
                recyclerView?.itemAnimator = DefaultItemAnimator()
                recyclerView?.adapter = favoriteAdapter
                recyclerView?.setHasFixedSize(true)
            }
        }
        else
        {
            //checkSize() function returned 0 so no favourites present
            recyclerView?.visibility = View.INVISIBLE
            noFavorites?.visibility = View.VISIBLE
        }
    }
}
