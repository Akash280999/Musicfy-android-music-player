package com.example.echo.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.example.echo.R


class AboutUsFragment : Fragment()
{
    override fun onCreateView(inflater : LayoutInflater , container : ViewGroup? , savedInstanceState : Bundle?) : View?
    {
        val view = inflater.inflate(R.layout.fragment_about_us ,
                                    container ,
                                    false)
        activity!!.title = "About us"
        return view
    }

    override fun onPrepareOptionsMenu(menu : Menu)
    {
        val item = menu.findItem(R.id.action_sort)
        item.isVisible = false
    }

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

}
