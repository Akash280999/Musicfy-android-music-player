package com.example.echo.activities

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import com.example.echo.adapters.NavigationDrawerAdapter
import com.example.echo.fragments.MainScreenFragment
import com.example.echo.fragments.SongPlayingFragment
import com.example.echo.R

class MainActivity : AppCompatActivity()
{
    var navigationDrawerIconsList : ArrayList<String> = arrayListOf()       //storing the names of the items in list
    var images_for_navdrawer : IntArray = intArrayOf(R.drawable.navigation_allsongs ,
                                                     R.drawable.navigation_favorites ,
                                                     R.drawable.navigation_settings ,
                                                     R.drawable.navigation_aboutus)

    object Statified
    {
        //made object so it can be used as same inside the adapter class without any change in its value
        var drawerLayout : DrawerLayout? = null
        var notificationManager : NotificationManager? = null
    }

    var trackNotificationBuilder : Notification? = null

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)

    override fun onCreate(savedInstanceState : Bundle?)
    {
        //Auto generated
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)          //contains the id's for each file and widgets present in Android project

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)            //makes toolbar an action bar which can support multiple actions thus making it interactive
        MainActivity.Statified.drawerLayout = findViewById(R.id.drawer_layout)

        //Adding names of the titles
        navigationDrawerIconsList.add("All Songs")
        navigationDrawerIconsList.add("Favourites")
        navigationDrawerIconsList.add("Settings")
        navigationDrawerIconsList.add("About Us")

        val toggle = ActionBarDrawerToggle(this@MainActivity ,          //activity where toggle is placed
                                           MainActivity.Statified.drawerLayout ,
                                           toolbar ,
                                           R.string.navigation_drawer_open ,             //function of drawer when toggle is click
                                           R.string.navigation_drawer_close             //" "
                                          )             //3 parallel horizontal lines

        MainActivity.Statified.drawerLayout?.addDrawerListener(toggle)
        toggle.syncState()          //3 parallel horizontal lines changes shape when clicked

        val mainScreenFragment = MainScreenFragment()

        this.supportFragmentManager          //helps to interact with the fragment
                .beginTransaction()
                .add(R.id.details_fragment ,
                     mainScreenFragment ,
                     "MainScreenFragment")           //Adds new fragment in place of the details fragment
                .commit()           //saves the changes made and makes main screen fragment visible to the user

        //navigation drawer code
        var _navigationAdapter = NavigationDrawerAdapter(navigationDrawerIconsList ,
                                                         images_for_navdrawer ,
                                                         this)

        _navigationAdapter.notifyDataSetChanged()           //data changed and thus should refresh the list

        var navigation_recycler_view = findViewById<RecyclerView>(R.id.navigation_recycler_view)
        navigation_recycler_view.layoutManager =
                LinearLayoutManager(this)           //aligns the items in a recycler view
        navigation_recycler_view.itemAnimator = DefaultItemAnimator()
        navigation_recycler_view.adapter = _navigationAdapter
        navigation_recycler_view.setHasFixedSize(true)          //number of items present in the recycler view are fixed and won't change

        val intent = Intent(this@MainActivity ,
                            MainActivity::class.java)
        val preIntent = PendingIntent.getActivity(this@MainActivity ,
                                                  System.currentTimeMillis().toInt() as Int ,
                                                  intent ,
                                                  0)

        trackNotificationBuilder = Notification.Builder(this)
                .setContentTitle("A track is playing in the background.")
                .setSmallIcon(R.drawable.musicfy)
                .setContentIntent(preIntent)
                .setOngoing(true)
                .setAutoCancel(true)
                .build()

        Statified.notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStart()
    {
        try
        {
            Statified.notificationManager?.cancel(1111)
        }
        catch (e : Exception)
        {
            e.printStackTrace()
        }
        super.onStart()
    }

    override fun onResume()
    {
        try
        {
            Statified.notificationManager?.cancel(1111)
        }
        catch (e : Exception)
        {
            e.printStackTrace()
        }
        super.onResume()
    }

    override fun onStop()
    {
        try
        {
            if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean)
            {
                Statified.notificationManager?.notify(1111 ,
                                                      trackNotificationBuilder)
            }
        }
        catch (e : Exception)
        {
            e.printStackTrace()
        }
        super.onStop()
    }

    override fun onDestroy()
    {
        try
        {
            if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean)
            {
                Statified.notificationManager?.notify(1111 ,
                                                      trackNotificationBuilder)
            }
        }
        catch (e : Exception)
        {
            e.printStackTrace()
        }
        super.onDestroy()
    }
}
