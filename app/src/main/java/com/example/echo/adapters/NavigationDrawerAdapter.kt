package com.example.echo.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.echo.activities.MainActivity
import com.example.echo.R
import com.example.echo.fragments.AboutUsFragment
import com.example.echo.fragments.FavouriteFragment
import com.example.echo.fragments.MainScreenFragment
import com.example.echo.fragments.SettingsFragment

class NavigationDrawerAdapter(_contentList : ArrayList<String> , _getImages : IntArray , _context : Context) :
        RecyclerView.Adapter<NavigationDrawerAdapter.NavViewHolder>()
{
    //acts as bridge between the view and its data
    var contentList : ArrayList<String>? = null
    var getImages : IntArray? = null
    var mContext : Context? = null

    init
    {
        //constructor
        this.contentList = _contentList
        this.getImages = _getImages
        this.mContext = _context
    }

    override fun onBindViewHolder(holder : NavViewHolder , position : Int)
    {
        //Called by RecyclerView to display the data at the specified position.
        holder?.icon_GET?.setBackgroundResource(getImages?.get(position) as Int)
        holder?.text_GET?.setText(contentList?.get(position))

        //ClickListener open fragments according to the position of the items when clicked
        holder?.contentHolder?.setOnClickListener {
            if (position == 0)
            {
                val mainScreenFragment = MainScreenFragment()
                (mContext as MainActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.details_fragment ,
                                 mainScreenFragment)
                        .addToBackStack("MainFrag")
                        .commit()
            }
            else if (position == 1)
            {
                val favouriteFragment = FavouriteFragment()
                (mContext as MainActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.details_fragment ,
                                 favouriteFragment)
                        .addToBackStack("FavFrag")
                        .commit()
            }
            else if (position == 2)
            {
                val settingsFragment = SettingsFragment()
                (mContext as MainActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.details_fragment ,
                                 settingsFragment)
                        .addToBackStack("SettingsFrag")
                        .commit()
            }
            else if (position == 3)
            {
                val aboutUsFragment = AboutUsFragment()
                (mContext as MainActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.details_fragment ,
                                 aboutUsFragment)
                        .addToBackStack("AboutUsFrag")
                        .commit()
            }
            MainActivity.Statified.drawerLayout?.closeDrawers()          //closes drawer automatically as the fragment loads.
        }
    }

    override fun onCreateViewHolder(parent : ViewGroup , viewType : Int) : NavViewHolder
    {
        //creates the view for the single row of the recycler view
        var itemView = LayoutInflater.from(parent?.context)
                .inflate(R.layout.row_custom_navigationdrawer ,
                         parent ,
                         false)
        val returnThis = NavViewHolder(itemView)
        return returnThis
    }

    override fun getItemCount() : Int
    {
        //returns the number of elements present in the recycler view.
        return (contentList as ArrayList).size
    }

    class NavViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)
    {
        var icon_GET : ImageView? = null
        var text_GET : TextView? = null
        var contentHolder : RelativeLayout? = null

        init
        {
            //constructor
            icon_GET = itemView?.findViewById(R.id.icon_navdrawer)
            text_GET = itemView?.findViewById(R.id.text_navdrawer)
            contentHolder = itemView?.findViewById(R.id.navdrawer_item_content_holder)
        }
    }
}