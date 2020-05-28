package com.example.echo.utils

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.example.echo.activities.MainActivity
import com.example.echo.R
import com.example.echo.fragments.SongPlayingFragment

class CaptureBroadcast : BroadcastReceiver()
{
    object Statified
    {
        var incomingFlag = false
    }

    override fun onReceive(context : Context? , intent : Intent?)
    {
        if (intent?.action == Intent.ACTION_NEW_OUTGOING_CALL)
        {
            CaptureBroadcast.Statified.incomingFlag = false
            //checks whether the user has an outgoing call or not
            try
            {
                MainActivity.Statified.notificationManager?.cancel(1111)

                if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean)
                {
                    //If song was playing  we pause it
                    SongPlayingFragment.Statified.mediaPlayer?.pause()
                    SongPlayingFragment.Statified.playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
                }
            }
            catch (e : Exception)
            {
                e.printStackTrace()
            }
        }
        else
        {
            val tm : TelephonyManager = context?.getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager

            when (tm.callState)
            {
                //check the call state and if there is incoming call we pause
                TelephonyManager.CALL_STATE_RINGING ->
                {
                    CaptureBroadcast.Statified.incomingFlag = true;
                    try
                    {
                        MainActivity.Statified.notificationManager?.cancel(1111)

                        if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean)
                        {
                            SongPlayingFragment.Statified.mediaPlayer?.pause()
                            SongPlayingFragment.Statified.playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
                        }
                    }
                    catch (e : Exception)
                    {
                        e.printStackTrace()
                    }
                }
                else ->
                {
                }
            }
        }
    }
}
