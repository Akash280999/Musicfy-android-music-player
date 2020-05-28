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
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.example.echo.utils.CurrentSongHelper
import com.example.echo.R
import com.example.echo.modelclasses.Songs
import com.example.echo.adapters.MainScreenAdapter
import java.util.*
import kotlin.collections.ArrayList

class MainScreenFragment : Fragment()
{
    //displays the songs on the main screen
    var getSongsList : ArrayList<Songs>? = null             //storing the songs along with the data
    var nowPlayingBottomBar : RelativeLayout? = null
    var playPauseButton : ImageButton? = null
    var songTitle : TextView? = null
    var visibleLayout : RelativeLayout? =
            null              //layout which is used to display the songs and the bottom bar
    var noSongs : RelativeLayout? = null
    var recyclerView : RecyclerView? = null                 //recycler view displaying the list of songs
    var trackPosition : Int = 0

    var myActitvity : Activity? = null

    var _mainScreenAdapter : MainScreenAdapter? = null

    object Statified
    {
        var mediaPlayer : MediaPlayer? = null
    }

    override fun onCreateView(inflater : LayoutInflater , container : ViewGroup? , savedInstanceState : Bundle?) : View?
    {
        //called to draw user interface for the first time.
        val view = inflater!!.inflate(R.layout.fragment_main_screen ,
                                      container ,
                                      false)

        setHasOptionsMenu(true)

        activity?.title = "All Songs"

        visibleLayout = view?.findViewById<RelativeLayout>(R.id.visibleLayout)
        noSongs = view?.findViewById<RelativeLayout>(R.id.noSongs)
        nowPlayingBottomBar = view?.findViewById<RelativeLayout>(R.id.hiddenBarMainScreen)
        songTitle = view?.findViewById<TextView>(R.id.songTitleMainScreen)
        playPauseButton = view?.findViewById<ImageButton>(R.id.playPauseButton)
        recyclerView = view?.findViewById<RecyclerView>(R.id.contentMain)

        return view
    }

    override fun onActivityCreated(savedInstanceState : Bundle?)
    {
        //called when the fragment's activity has been created
        super.onActivityCreated(savedInstanceState)

        //saves the sorting order which we select
        getSongsList = getSongsFromPhone()

        val prefs = activity?.getSharedPreferences("action_sort" ,
                                                   Context.MODE_PRIVATE)
        val action_sort_ascending = prefs?.getString("action_sort_ascending" ,
                                                     "true")
        val action_sort_recent = prefs?.getString("action_sort_recent" ,
                                                  "false")

        if (getSongsList == null)
        {
            visibleLayout?.visibility = View.INVISIBLE
            noSongs?.visibility = View.VISIBLE
        }
        else
        {
            _mainScreenAdapter = MainScreenAdapter(getSongsList as ArrayList<Songs> ,
                                                   myActitvity as Context)

            val mLayoutManager = LinearLayoutManager(myActitvity)
            recyclerView?.layoutManager = mLayoutManager
            recyclerView?.itemAnimator = DefaultItemAnimator()
            recyclerView?.adapter = _mainScreenAdapter
        }



        if (getSongsList != null)
        {
            //songs list not empty and checks for sorting order
            if (action_sort_ascending!!.equals("true" ,
                                               true))
            {
                Collections.sort(getSongsList ,
                                 Songs.Statified.nameComparator)
                _mainScreenAdapter?.notifyDataSetChanged()
            }
            else if (action_sort_recent!!.equals("true" ,
                                                 true))
            {
                Collections.sort(getSongsList ,
                                 Songs.Statified.dateComparator)
                _mainScreenAdapter?.notifyDataSetChanged()
            }
        }
        bottomBarSetup()
    }

    override fun onAttach(context : Context?)
    {
        //called wehen fragment is first attached to its context
        super.onAttach(context)
        myActitvity = context as Activity
    }

    override fun onAttach(activity : Activity?)
    {
        //used as to prevent some crashes on some older devices
        super.onAttach(activity)
        myActitvity = activity
    }

    override fun onCreateOptionsMenu(menu : Menu? , inflater : MenuInflater?)
    {
        menu?.clear()
        inflater?.inflate(R.menu.main ,
                          menu)
        super.onCreateOptionsMenu(menu ,
                                  inflater)
    }

    override fun onOptionsItemSelected(item : MenuItem?) : Boolean
    {
        val switcher = item?.itemId

        if (switcher == R.id.action_sort_ascending)
        {

            val editor = myActitvity?.getSharedPreferences("action_sort" ,
                                                           Context.MODE_PRIVATE)
                    ?.edit()
            editor?.putString("action_sort_ascending" ,
                              "true")
            editor?.putString("action_sort_recent" ,
                              "false")
            editor?.apply()

            if (getSongsList != null)
            {
                Collections.sort(getSongsList ,
                                 Songs.Statified.nameComparator)
                _mainScreenAdapter?.notifyDataSetChanged()
            }

            return false
        }
        else if (switcher == R.id.action_sort_recent)
        {
            val editor = myActitvity?.getSharedPreferences("action_sort" ,
                                                           Context.MODE_PRIVATE)
                    ?.edit()
            editor?.putString("action_sort_ascending" ,
                              "false")
            editor?.putString("action_sort_recent" ,
                              "true")
            editor?.apply()
            if (getSongsList != null)
            {
                Collections.sort(getSongsList ,
                                 Songs.Statified.dateComparator)
                _mainScreenAdapter?.notifyDataSetChanged()
            }
            return false
        }
        return super.onOptionsItemSelected(item)
    }

    fun getSongsFromPhone() : ArrayList<Songs>
    {
        //used to fetch the songs present in phones
        var arrayList = ArrayList<Songs>()
        var contentResolver = myActitvity?.contentResolver
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songUri ,
                                                null ,
                                                null ,
                                                null ,
                                                null)

        if (songCursor != null && songCursor.moveToFirst())
        {
            //moveToFirst() function returns the first row of the results
            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            while (songCursor.moveToNext())
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
                                    currentDate))           //fetched songs are added to the arraylist
            }
        }
        return arrayList
    }

    fun bottomBarSetup()
    {
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
                nowPlayingBottomBar?.visibility = View.VISIBLE
            }
            else
            {
                nowPlayingBottomBar?.visibility = View.INVISIBLE
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
            MainScreenFragment.Statified.mediaPlayer = SongPlayingFragment.Statified.mediaPlayer

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
            args.putString("MainScreenBottomBar" ,
                           "success")
            songPlayingFragment.arguments = args       // linking songPlayingFragment to Bundle obj


            fragmentManager?.beginTransaction()
                    ?.replace(R.id.details_fragment ,
                              songPlayingFragment ,
                              "SongPlayingFragment")
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
                SongPlayingFragment.Statified.mediaPlayer?.seekTo(trackPosition)
                SongPlayingFragment.Statified.mediaPlayer?.start()
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        }
    }
}

