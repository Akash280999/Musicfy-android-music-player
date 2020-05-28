package com.example.echo.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.widget.Toast
import com.example.echo.R

class SplashActivity : AppCompatActivity()
{
    //Manifest class contains the different permissions used in Android
    // An array storing all the required permissions
    var permissionStrings = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE ,
                                    Manifest.permission.MODIFY_AUDIO_SETTINGS ,
                                    Manifest.permission.READ_PHONE_STATE ,
                                    Manifest.permission.PROCESS_OUTGOING_CALLS ,
                                    Manifest.permission.RECORD_AUDIO)

    override fun onCreate(savedInstanceState : Bundle?)
    {
        //savedInstanceState saves the state of the activity when the activity is launched for the second time and onwards.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)            //sets the view to the activity

        if (!hasPermission(this@SplashActivity ,
                           *permissionStrings))
        {
            //Permission not granted
            ActivityCompat.requestPermissions(this@SplashActivity ,
                                              permissionStrings ,
                                              131)              //Requests the user for permissions
        }
        else
        {
            //Permission granted
            displaySplashScreen()
        }
    }

    override fun onRequestPermissionsResult(requestCode : Int , permissions : Array<out String> ,
                                            grantResults : IntArray)
    {
        //Called when all permissions granted
        super.onRequestPermissionsResult(requestCode ,
                                         permissions ,
                                         grantResults)
        when (requestCode)
        {
            //matches the request code which is unique integer
            131  ->
            {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED && grantResults[3] == PackageManager.PERMISSION_GRANTED && grantResults[4] == PackageManager.PERMISSION_GRANTED)
                {
                    //if all permissions granted and result is not empty
                    displaySplashScreen()
                }
                else
                {
                    //permissions not granted
                    Toast.makeText(this@SplashActivity ,
                                   "Please Grant All Permissions to continue" ,
                                   Toast.LENGTH_SHORT)
                            .show() //to prompt a message at the bottom of the screen
                    this.finish()
                }
                return
            }
            else ->
            {
                //something goes wrong with android
                Toast.makeText(this@SplashActivity ,
                               "Something Went Wrong" ,
                               Toast.LENGTH_SHORT)
                        .show()
                this.finish()
                return
            }
        }
    }

    fun hasPermission(context : Context , vararg permissions : String) : Boolean
    {
        //checks whether the user has granted all the permissions
        var hasAllPermissions = true          //flag
        for (permission in permissions)
        {
            //checks every single permission
            val res = context.checkCallingOrSelfPermission(permission)
            if (res != PackageManager.PERMISSION_GRANTED)
            {
                //not granted
                hasAllPermissions = false
            }
        }
        return hasAllPermissions
    }

    // Function to display the splash screen with a 1 sec delay
    fun displaySplashScreen()
    {
        Handler().postDelayed({
                                  val startAct = Intent(this@SplashActivity ,
                                                        MainActivity::class.java)           //navigate from one activity to another
                                  startActivity(startAct)           //launches the activity
                                  this.finish()
                              } ,
                              2000)              //Delays the opening of the next activity with a delay of 2s
    }
}
