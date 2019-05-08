package com.garsontech.rubiesafrica

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaPlayer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import com.bumptech.glide.Glide
import com.garsontech.rubiesafrica.Services.dbService
import com.garsontech.rubiesafrica.Services.fileService
import com.garsontech.rubiesafrica.Services.webService
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

var appContext: Context? = null
var dbHandler: dbService? = null
var audioManager: AudioManager? = null
var mediaPlayer: MediaPlayer? = null
var sharedPrefs: SharedPreferences? = null
var screenWidth: Int = 0
var screenHeight: Int = 0

class MainActivity : AppCompatActivity() {



    @Throws(InterruptedException::class, IOException::class)
    fun isConnected(): Boolean {

        val command = "ping -c 1 google.com"
        var connected = false
        try {
            connected =  Runtime.getRuntime().exec(command).waitFor() == 0
        }
        catch (e: Exception) {
            connected = false
        }
        return connected
    }

    var isConnected: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPrefs = this.getSharedPreferences("com.garsontech.rubiesafrica", Context.MODE_PRIVATE)

        runBlocking {
            launch(Dispatchers.IO) { playAudio() }
        }

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels

        Glide.with(this).load(R.drawable.app_opening_screen).override(screenWidth, screenHeight).into(backgroundImageView)
//        Picasso.get().load(R.drawable.app_opening_screen).resize(screenWidth, screenHeight).into(backgroundImageView)

        appContext = this
        dbHandler = dbService(this)


        isConnected = isConnected()

        startButton.setOnClickListener {
            presentBookList()
        }

        tryAgainButton.setOnClickListener {
            noConnectionLayout.visibility = View.INVISIBLE
            checkIfFirstRun()
        }

        laterButton.setOnClickListener {
            laterTapped()
        }

        continueButton.setOnClickListener {
            continueTapped()
        }

//        dbHandler?.deleteAllStories()
//        dbHandler?.deleteAllPages()
        checkIfFirstRun()

    }

    override fun onResume() {
        super.onResume()

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
    }

    private fun presentBookList() {
        slideOutTitle()

    }

    private fun displayTitle() {

        rubiesImageView.animate().alpha(1f).setDuration(2000)
        africaImageView.animate().alpha(1f).setDuration(2000)
            .withEndAction {
                tapTextView.animate().alpha(1f).setDuration(1000)
                startButton.animate().alpha(1f).setDuration(1000)
                startButton.visibility = View.VISIBLE
            }
    }

    private fun slideOutTitle() {
        rubiesImageView.animate().translationXBy(-1000f).setDuration(2000)
        africaImageView.animate().translationXBy(1000f).setDuration(2000)
        tapTextView.animate().translationYBy(-1000f).setDuration(2000)
            .withEndAction {
                val intent = Intent(this, BookListActivity::class.java)
                startActivity(intent)
            }
    }

     private suspend fun playAudio() {
         val sharedPrefs: SharedPreferences = this.getSharedPreferences("com.garsontech.rubiesafrica", Context.MODE_PRIVATE)
        val isSoundOn = sharedPrefs.getBoolean(Constants.isSoundOn, true)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mediaPlayer = MediaPlayer.create(this, R.raw.cute)
        mediaPlayer?.isLooping = true

        if (isSoundOn) {
            mediaPlayer?.start()
            sharedPrefs.edit()?.putBoolean(Constants.isSoundOn, true)?.apply()
        }
    }

    private fun loadFirstBook() {
        dbHandler = dbService(this)
        val book = fileService.loadStoryFromJson("main_story.json", this)
        dbHandler?.saveStoryToDataBase(book!!)
        val pages = fileService.loadPagesFromJson(book?.storyId!!, "main_pages.json", this)
        pages.forEach {
            dbHandler?.savePageToDataBase(it)
        }
        sharedPrefs?.edit()?.putBoolean(Constants.firstLoadComplete, true)?.apply()
    }

    private fun checkIfFirstRun() {
        val webHandler = webService(this@MainActivity)
        val firstRunComplete = sharedPrefs?.getBoolean(Constants.firstLoadComplete, false)
        if (!firstRunComplete!!) {
            loadFirstBook()
        }
        if (isConnected()) {
            webHandler.checkIfUpdateAvailable {
                if (it) {
                    updateAvailableLayout.visibility = View.VISIBLE
                } else {
                    displayTitle()
                }
            }
        }
    }

    private fun laterTapped() {
        updateAvailableLayout.visibility = View.INVISIBLE
        displayTitle()
    }

    private fun continueTapped() = runBlocking {
        updateAvailableLayout.visibility = View.INVISIBLE
        if (isConnected()) {
            val webHandler = webService(this@MainActivity)
            loadingTextView.text = "Downloading new Content. Please Wait"
            loadingLayout.visibility = View.VISIBLE
            webHandler.loadGlossaryFromWeb()
            webHandler.loadStoriesFromWeb() {
                if (it == null) {
                    val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                    val formattedDate = originalFormat.format(Date())
                    sharedPrefs?.edit()?.putString(Constants.lastUpdate, formattedDate)?.apply()
                    loadingLayout.visibility = View.INVISIBLE
                    displayTitle()
                }
            }
        }
    }



}
