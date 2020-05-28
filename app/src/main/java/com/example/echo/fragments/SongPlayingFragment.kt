package com.example.echo.fragments

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.example.echo.utils.CurrentSongHelper
import com.example.echo.R
import com.example.echo.modelclasses.Songs
import com.example.echo.databases.EchoDatabase
import com.example.echozzz.utils.SeekBarController
import java.util.*
import java.util.concurrent.TimeUnit

class SongPlayingFragment : Fragment()
{
    object Statified
    {
        //contains all variables
        var myActivity : Activity? = null
        var mediaPlayer : MediaPlayer? = null

        var startTimeText : TextView? = null
        var endTimeText : TextView? = null

        var playpauseImageButton : ImageButton? = null
        var previousImageButtom : ImageButton? = null
        var nextImageButton : ImageButton? = null
        var loopImageButton : ImageButton? = null
        var seekbar : SeekBar? = null

        var songArtistView : TextView? = null
        var songTitleView : TextView? = null
        var shuffleImageButton : ImageButton? = null

        var _currentPosition : Int = 0
        var _fetchSongs : ArrayList<Songs>? = null

        var currentSongHelper : CurrentSongHelper? = null          //store the details of the current song being played

        var audioVisualisation : AudioVisualization? = null           //used for the visual aspects of sound
        var glView : GLAudioVisualizationView? = null           //visualization view
        var fab : ImageButton? = null           //handles the favourite button

        var favoriteContent : EchoDatabase? = null              //variable for database functions


        var mSensorManager : SensorManager? = null
        var mSensorListener : SensorEventListener? = null

        var MY_PREFS_NAME : String = "ShakeFeature"

        var updateSongTime = object : Runnable
        {
            override fun run()
            {
                try
                {
                    val getCurrent = mediaPlayer?.currentPosition
                    startTimeText?.setText(String.format("%d:%d" ,
                                                         TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long) ,
                                                         (TimeUnit.MILLISECONDS.toSeconds(getCurrent?.toLong() as Long) - TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long))) % 60))

                    seekbar?.setProgress(getCurrent?.toInt() as Int)
                    Handler().postDelayed(this ,
                                          1000)       //updating the time at each second
                }
                catch (e : Exception)
                {
                    e.printStackTrace()
                }
            }
        } //used to update the song time

    }

    object Staticated
    {
        var MY_PREFS_SHUFFLE = "Shuffle feature"
        var MY_PREFS_LOOP = "Loop feature"

        fun onSongComplete()
        {
            if (Statified.currentSongHelper?.isShuffle as Boolean)
            {
                //if shuffle was on then play a random next song
                playNext("PlayNextLikeNormalShuffle")
                Statified.currentSongHelper?.isPlaying = true
            }
            else
            {
                //if shuffle was off
                if (Statified.currentSongHelper?.isLoop as Boolean)
                {
                    //if loop was on then play the same song again
                    Statified.currentSongHelper?.isPlaying = true
                    var nextSong = Statified._fetchSongs?.get(Statified._currentPosition)

                    Statified.currentSongHelper?.songPath = nextSong?.songData
                    Statified.currentSongHelper?.songTitle = nextSong?.songTitle
                    Statified.currentSongHelper?.songId = nextSong?.songId as Long
                    Statified.currentSongHelper?.currentPosition = Statified._currentPosition

                    updateTextViews(Statified.currentSongHelper?.songTitle as String ,
                                    Statified.currentSongHelper?.songArtist as String)

                    Statified.mediaPlayer?.reset()
                    try
                    {
                        Statified.mediaPlayer?.setDataSource(Statified.myActivity ,
                                                             Uri.parse(Statified.currentSongHelper?.songPath))
                        Statified.mediaPlayer?.prepare()
                        Statified.mediaPlayer?.start()

                        processInformation(Statified.mediaPlayer as MediaPlayer)
                    }
                    catch (e : Exception)
                    {
                        e.printStackTrace()
                    }

                }
                else
                {
                    //if loop was off then normally play the next song
                    playNext("PlayNextNormal")
                    Statified.currentSongHelper?.isPlaying = true
                }
            }
            if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean)
            {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context ,
                                                                          R.drawable.favorite_on))
            }
            else
            {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context ,
                                                                          R.drawable.favorite_off))
            }
        }

        fun updateTextViews(songTitle : String , songArtist : String)
        {
            //update the views of songs and their artist names
            var songTitleUpdated = songTitle
            var songArtistUpdated = songArtist
            if (songTitle?.equals("<unknown>" ,
                                  true))
            {
                songTitleUpdated = "Unknown"
            }
            if (songArtist?.equals("<unknown>" ,
                                   true))
            {
                songArtistUpdated = "Unknown"
            }

            Statified.songTitleView?.setText(songTitleUpdated)
            Statified.songArtistView?.setText(songArtistUpdated)
        }

        // Updating The time text views
        fun processInformation(mediaPlayer : MediaPlayer)
        {
            val finalTime = mediaPlayer.duration
            val startTime = mediaPlayer.currentPosition

            Statified.seekbar?.max = finalTime        //max time of seek barr

            Statified.startTimeText?.setText(String.format("%d:%d" ,
                                                           TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()) ,
                                                           (TimeUnit.MILLISECONDS.toSeconds(startTime.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime.toLong())) % 60) as Long))       //set to the start time

            Statified.endTimeText?.setText(String.format("%d:%d" ,
                                                         TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()) ,
                                                         TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong())) as Long))

            Statified.seekbar?.setProgress(startTime)

            Handler().postDelayed(Statified.updateSongTime ,
                                  1000)
        }

        fun playNext(check : String)
        {
            if (check.equals("PlayNextNormal" ,
                             true))
            {
                //next song played normally
                Statified._currentPosition = Statified._currentPosition + 1

            }
            else if (check.equals("PlayNextLikeNormalShuffle" ,
                                  true))
            {
                //next song played randomly
                var randomObject = Random()
                var randomPosition =
                        randomObject.nextInt(Statified._fetchSongs?.size?.plus(1) as Int)       //used to get a random number between 0(inclusive)and the number passed in this argument(exclusive)
                Statified._currentPosition = randomPosition

            }
            if (Statified._currentPosition == Statified._fetchSongs?.size)
            {
                //current position points to the end of the list
                Statified._currentPosition = 0
            }

            Statified.currentSongHelper?.isLoop = false
            var nextSong = Statified._fetchSongs?.get(Statified._currentPosition)
            Statified.currentSongHelper?.songPath = nextSong?.songData
            Statified.currentSongHelper?.songTitle = nextSong?.songTitle
            Statified.currentSongHelper?.songArtist = nextSong?.artist
            Statified.currentSongHelper?.songId = nextSong?.songId as Long
            Statified.currentSongHelper?.currentPosition = Statified._currentPosition

            updateTextViews(Statified.currentSongHelper?.songTitle as String ,
                            Statified.currentSongHelper?.songArtist as String)

            Statified.mediaPlayer?.reset()
            try
            {
                Statified.mediaPlayer?.setDataSource(Statified.myActivity ,
                                                     Uri.parse(Statified.currentSongHelper?.songPath))
                Statified.mediaPlayer?.prepare()
                Statified.mediaPlayer?.start()
                Statified.playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
                processInformation(Statified.mediaPlayer as MediaPlayer)
            }
            catch (e : Exception)
            {
                e.printStackTrace()
            }

            if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean)
            {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context ,
                                                                          R.drawable.favorite_on))
            }
            else
            {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context ,
                                                                          R.drawable.favorite_off))
            }
        }

        fun playPrevious(check : String)
        {
            if (check.equals("PlayNextNormal" ,
                             true)) Statified._currentPosition = Statified._currentPosition - 1
            else if (check.equals("PlayNextLikeNormalShuffle" ,
                                  true))
            {
                var randomObject = Random()
                var randomPosition = randomObject.nextInt(Statified._fetchSongs?.size?.plus(1) as Int)
                Statified._currentPosition = randomPosition
            }

            if (Statified._currentPosition == -1)
            {
                //current position becomes less than 0, so size-1 last song in the list
                Statified._currentPosition = (Statified._fetchSongs?.size as Int) - 1
            }
            Statified.currentSongHelper?.isLoop = false
            var nextSong = Statified._fetchSongs?.get(Statified._currentPosition)
            Statified.currentSongHelper?.songPath = nextSong?.songData
            Statified.currentSongHelper?.songTitle = nextSong?.songTitle
            Statified.currentSongHelper?.songArtist = nextSong?.artist
            Statified.currentSongHelper?.songId = nextSong?.songId as Long
            Statified.currentSongHelper?.currentPosition = Statified._currentPosition

            updateTextViews(Statified.currentSongHelper?.songTitle as String ,
                            Statified.currentSongHelper?.songArtist as String)

            Statified.mediaPlayer?.reset()

            try
            {
                Statified.mediaPlayer?.setDataSource(Statified.myActivity ,
                                                     Uri.parse(Statified.currentSongHelper?.songPath))
                Statified.mediaPlayer?.prepare()
                Statified.mediaPlayer?.start()
                Statified.playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
                processInformation(Statified.mediaPlayer as MediaPlayer)
            }
            catch (e : Exception)
            {
                e.printStackTrace()
            }

            if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean)
            {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context ,
                                                                          R.drawable.favorite_on))
                //Statified.fab?.setBackgroundResource(R.drawable.favorite_on)
            }
            else
            {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context ,
                                                                          R.drawable.favorite_off))
                //Statified.fab?.setBackgroundResource(R.drawable.favorite_off)
            }
        }
    }

    var mAcceleration : Float = 0f
    var mAccelerationCurrent : Float = 0f           // current acceleration including gravity
    var mAccelerationLast : Float = 0f             // last acceleration including gravity

    override fun onCreateView(inflater : LayoutInflater , container : ViewGroup? , savedInstanceState : Bundle?) : View?
    {
        // Inflate the layout for this fragment
        var view = inflater!!.inflate(R.layout.fragment_song_playing ,
                                      container ,
                                      false)
        setHasOptionsMenu(true)

        activity?.title = "Now Playing"

        Statified.seekbar = view?.findViewById(R.id.seekBar)
        Statified.startTimeText = view?.findViewById(R.id.startTime)
        Statified.endTimeText = view?.findViewById(R.id.endTime)
        Statified.playpauseImageButton = view?.findViewById(R.id.playPauseButton)
        Statified.nextImageButton = view?.findViewById(R.id.nextButton)
        Statified.previousImageButtom = view?.findViewById(R.id.previousButton)
        Statified.loopImageButton = view?.findViewById(R.id.loopButton)
        Statified.shuffleImageButton = view?.findViewById(R.id.shuffleButton)
        Statified.songArtistView = view?.findViewById(R.id.songArtist)
        Statified.songTitleView = view?.findViewById(R.id.songTitle)

        Statified.glView = view?.findViewById(R.id.visualizer_view)

        Statified.fab = view?.findViewById(R.id.favoriteButton)

        Statified.fab?.alpha = 0.6f        //Fading the favorite icon

        return view
    }

    override fun onViewCreated(view : View , savedInstanceState : Bundle?)
    {
        super.onViewCreated(view ,
                            savedInstanceState)

        Statified.audioVisualisation =
                Statified.glView as AudioVisualization           //Connecting the audio visualization with the view
    }

    override fun onAttach(context : Context?)
    {
        super.onAttach(context)
        Statified.myActivity = context as Activity
    }

    override fun onAttach(activity : Activity?)
    {
        super.onAttach(activity)
        Statified.myActivity = activity
    }

    override fun onResume()
    {
        super.onResume()
        Statified.audioVisualisation?.onResume()
        Statified.mSensorManager?.registerListener(Statified.mSensorListener ,
                                                   Statified.mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ,
                                                   SensorManager.SENSOR_DELAY_NORMAL)

    }

    override fun onPause()
    {
        super.onPause()
        Statified.audioVisualisation?.onPause()
        Statified.mSensorManager?.unregisterListener(Statified.mSensorListener)
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        Statified.audioVisualisation?.release()
    }

    override fun onActivityCreated(savedInstanceState : Bundle?)
    {
        super.onActivityCreated(savedInstanceState)

        Statified.currentSongHelper = CurrentSongHelper()
        Statified.currentSongHelper?.isPlaying = true
        Statified.currentSongHelper?.isLoop = false
        Statified.currentSongHelper?.isShuffle = false

        Statified.favoriteContent = EchoDatabase(Statified.myActivity)

        var path : String? = null
        var _songTitle : String? = null
        var _songArttist : String? = null
        var songId : Long = 0

        try
        {
            path = arguments?.getString("path")
            _songTitle = arguments?.getString("songTitle")
            _songArttist = arguments?.getString("songArtist")
            songId = arguments?.getInt("SongId")?.toLong() as Long
            Statified._currentPosition = arguments?.getInt("songPosition")?.toInt() as Int
            Statified._fetchSongs = arguments?.getParcelableArrayList("songData")

            Statified.currentSongHelper?.songPath = path
            Statified.currentSongHelper?.songTitle = _songTitle
            Statified.currentSongHelper?.songArtist = _songArttist
            Statified.currentSongHelper?.songId = songId
            Statified.currentSongHelper?.currentPosition = Statified._currentPosition

            Staticated.updateTextViews(Statified.currentSongHelper?.songTitle as String ,
                                       Statified.currentSongHelper?.songArtist as String)

        }
        catch (e : Exception)
        {
            e.printStackTrace()
        }

        //navigate to the song playing fragment via tapping on a song or by bottom bar
        var fromFavBottomBar = arguments?.get("FavBottomBar") as? String
        var fromMainScreenBottomBar = arguments?.get("MainScreenBottomBar") as? String

        if (fromFavBottomBar != null)
        {
            //via favourite screen bottom bar
            Statified.mediaPlayer = FavouriteFragment.Statified.mediaPlayer
        }
        else if (fromMainScreenBottomBar != null)
        {
            //via main screen bottom bar
            Statified.mediaPlayer = MainScreenFragment.Statified.mediaPlayer
        }
        else
        { //via clicking in any song
            Statified.mediaPlayer = MediaPlayer()
            Statified.mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)

            try
            {
                Statified.mediaPlayer?.setDataSource(Statified.myActivity ,
                                                     Uri.parse(path))
                Statified.mediaPlayer?.prepare()
            }
            catch (e : Exception)
            {
                e.printStackTrace()
            }
            Statified.mediaPlayer?.start()       //If everything goes well we start the music
        }

        Staticated.processInformation(Statified.mediaPlayer as MediaPlayer)

        if (Statified.mediaPlayer?.isPlaying as Boolean)
        {
            Statified.playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        }
        else
        {
            Statified.playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }

        // When a song completes
        Statified.mediaPlayer?.setOnCompletionListener {
            Staticated.onSongComplete()
        }

        clickHandler()

        var visualizationHandler = DbmHandler.Factory.newVisualizerHandler(Statified.myActivity as Context ,
                                                                           0)
        Statified.audioVisualisation?.linkTo(visualizationHandler)

        var prefsForShuffle = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE ,
                                                                         Context.MODE_PRIVATE)
        var isShuffleAllowed = prefsForShuffle?.getBoolean("feature" ,
                                                           false)
        if (isShuffleAllowed as Boolean)
        {
            //suffle was on
            Statified.currentSongHelper?.isShuffle = true
            Statified.currentSongHelper?.isLoop = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        }
        else
        {
            //suffle was off
            Statified.currentSongHelper?.isShuffle = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }

        var prefsForLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP ,
                                                                      Context.MODE_PRIVATE)
        var isLoopAllowed = prefsForLoop?.getBoolean("feature" ,
                                                     false)
        if (isLoopAllowed as Boolean)
        {
            //loop was on
            Statified.currentSongHelper?.isLoop = true
            Statified.currentSongHelper?.isShuffle = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
        }
        else
        {
            //loop was off
            Statified.currentSongHelper?.isLoop = false
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        }

        if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean)
        {
            //favourite on
            //Statified.fab?.setBackgroundResource(R.drawable.favorite_on)
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context ,
                                                                      R.drawable.favorite_on))
        }
        else
        {
            //favourite off
            //Statified.fab?.setBackgroundResource(R.drawable.favorite_off)
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context ,
                                                                      R.drawable.favorite_off))
        }

        seekbarHandler()
    }

    override fun onDestroy()
    {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        Statified.mSensorManager = Statified.myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        mAcceleration = 0.0f
        mAccelerationCurrent = SensorManager.GRAVITY_EARTH          //earth's gravitational value to be default
        mAccelerationLast = SensorManager.GRAVITY_EARTH

        bindShakeListener()
    }

    override fun onCreateOptionsMenu(menu : Menu? , inflater : MenuInflater?)
    {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu ,
                          menu)
        super.onCreateOptionsMenu(menu ,
                                  inflater)
    }

    override fun onPrepareOptionsMenu(menu : Menu?)
    {
        super.onPrepareOptionsMenu(menu)

        val item : MenuItem? = menu?.findItem(R.id.action_redirect1)
        item?.isVisible = true

        val item2 : MenuItem? = menu?.findItem(R.id.action_sort)
        item2?.isVisible = false
    }

    override fun onOptionsItemSelected(item : MenuItem?) : Boolean
    {
        when (item?.itemId)
        {
            R.id.action_redirect1 ->
            {
                Statified.myActivity?.onBackPressed()
                return false
            }
        }
        return false
    }

    fun clickHandler()
    {

        Statified.fab?.setOnClickListener {
            if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean)
            {
                //remove from favourites
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context ,
                                                                          R.drawable.favorite_off))
                Statified.favoriteContent?.deleteFavourite(Statified.currentSongHelper?.songId?.toInt() as Int)
                Toast.makeText(Statified.myActivity ,
                               "Removed from Favorites" ,
                               Toast.LENGTH_SHORT)
                        .show()
            }
            else
            {
                //add to favourites
                Statified.favoriteContent?.storeAsFavourite(Statified.currentSongHelper?.songId?.toInt() ,
                                                            Statified.currentSongHelper?.songArtist ,
                                                            Statified.currentSongHelper?.songTitle ,
                                                            Statified.currentSongHelper?.songPath)
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context ,
                                                                          R.drawable.favorite_on))
                Toast.makeText(Statified.myActivity ,
                               "Added to Favorites" ,
                               Toast.LENGTH_SHORT)
                        .show()
            }
        }

        Statified.shuffleImageButton?.setOnClickListener {

            var editorShuffle = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE ,
                                                                           Context.MODE_PRIVATE)
                    ?.edit()
            var editorLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP ,
                                                                        Context.MODE_PRIVATE)
                    ?.edit()
            if (Statified.currentSongHelper?.isShuffle as Boolean)
            {
                //suffle was on, we make it off
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                Statified.currentSongHelper?.isShuffle = false
                editorShuffle?.putBoolean("feature" ,
                                          false)
                editorShuffle?.apply()
            }
            else
            {
                //suffle was off, we make it on and loop to off
                Statified.currentSongHelper?.isShuffle = true
                Statified.currentSongHelper?.isLoop = false
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorShuffle?.putBoolean("feature" ,
                                          true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature" ,
                                       false)
                editorLoop?.apply()
            }
        }

        Statified.nextImageButton?.setOnClickListener {
            Statified.currentSongHelper?.isPlaying = true
            if (Statified.currentSongHelper?.isShuffle as Boolean)
            {
                //shuffle button was enabled and we play next song randomly
                Staticated.playNext("PlayNextLikeNormalShuffle")
            }
            else
            { //shuffle button was not enabled and we play next song normally

                Staticated.playNext("PlayNextNormal")
            }
        }

        Statified.previousImageButtom?.setOnClickListener {
            Statified.currentSongHelper?.isPlaying = true
            if (Statified.currentSongHelper?.isShuffle as Boolean)
            {
                Staticated.playPrevious("PlayNextLikeNormalShuffle")
            }
            else
            {
                Staticated.playPrevious("PlayNextNormal")
            }
        }

        Statified.loopImageButton?.setOnClickListener {

            var editorShuffle = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE ,
                                                                           Context.MODE_PRIVATE)
                    ?.edit()
            var editorLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP ,
                                                                        Context.MODE_PRIVATE)
                    ?.edit()

            if (Statified.currentSongHelper?.isLoop as Boolean)
            {
                //loop was enabled and we turn it off
                Statified.currentSongHelper?.isLoop = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorLoop?.putBoolean("feature" ,
                                       false)
                editorLoop?.apply()
            }
            else
            {
                //loop was not enabled, we turn it on
                Statified.currentSongHelper?.isLoop = true
                Statified.currentSongHelper?.isShuffle = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle?.putBoolean("feature" ,
                                          false)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature" ,
                                       true)
                editorLoop?.apply()
            }
        }

        Statified.playpauseImageButton?.setOnClickListener {

            if (Statified.mediaPlayer?.isPlaying as Boolean)
            {
                //song is playing and then play/pause button is tapped then we pause the media player and change the button to play button
                Statified.mediaPlayer?.pause()

                Statified.currentSongHelper?.isPlaying = false
                Statified.playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            }
            else
            {
                //song is not playing and then play/pause button is tapped then we play the media player and change the button to pause button
                Statified.mediaPlayer?.start()

                Statified.currentSongHelper?.isPlaying = true
                Statified.playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        }

    }

    fun bindShakeListener()
    {
        Statified.mSensorListener = object : SensorEventListener
        {
            override fun onAccuracyChanged(p0 : Sensor? , p1 : Int)
            {
            }

            override fun onSensorChanged(p0 : SensorEvent)
            {
                val x = p0.values[0]
                val y = p0.values[1]
                val z = p0.values[2]            //3 dimensions i.e. the x, y and z in which the changes can occur

                mAccelerationLast = mAccelerationCurrent
                mAccelerationCurrent = Math.sqrt(((x * x + y * y + z * z).toDouble()))
                        .toFloat()               //calculation of Euclidean distance to get the normalized distance
                val delta = mAccelerationCurrent - mAccelerationLast //change in acceleration
                mAcceleration = mAcceleration * 0.9f + delta

                if (mAcceleration > 12)
                {
                    val prefs = Statified.myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME ,
                                                                           Context.MODE_PRIVATE)
                    val isAllowed = prefs?.getBoolean("feature" ,
                                                      false)

                    if (isAllowed as Boolean)
                    {
                        Staticated.playNext("PlayNextNormal")
                    }
                }
            }
        }
    }

    fun seekbarHandler()
    {
        val seekbarListener = SeekBarController()
        Statified.seekbar?.setOnSeekBarChangeListener(seekbarListener)
    }
}
