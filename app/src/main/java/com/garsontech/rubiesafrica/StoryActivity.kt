package com.garsontech.rubiesafrica

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.media.AudioManager
import android.media.MediaPlayer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.support.v4.view.ViewPager
import android.support.v7.widget.DividerItemDecoration
import com.garsontech.rubiesafrica.Models.Book
import com.garsontech.rubiesafrica.Models.Page
import com.garsontech.rubiesafrica.Models.Word
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_story.*
import kotlinx.android.synthetic.main.glossary_word_layout.view.*

class StoryActivity : AppCompatActivity() {

    private lateinit var pagerAdapter: StoryPagerAdapter
    val wordsAdapter = GroupAdapter<ViewHolder>()
    var pages = ArrayList<Page>()
    var words = ArrayList<Word>()
    var storyAudioManager: AudioManager? = null
    var storyMediaPlayer: MediaPlayer? = null
    var glossaryIsShowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        Picasso.get().load(R.drawable.background22).resize(screenWidth, screenHeight).into(backgroundImageView)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        val book = intent.getParcelableExtra<Book>(Constants.selectedBook)
        changeSoundIcon()
        setUpAudio(book.song)
        hideGlossary()

        pages = dbHandler?.loadPagesFromDatabase(book.storyId)!!
        words = dbHandler?.loadWordsFromDatabase()!!

        pagerAdapter = StoryPagerAdapter(supportFragmentManager, pages, book)
        viewPager.adapter = pagerAdapter

        glossaryRecyclerView.adapter = wordsAdapter
        glossaryRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        words.forEach {
            wordsAdapter.add(WordForGlossary(it))
        }

        homeButton.setOnClickListener {
            storyMediaPlayer?.stop()
            finish()
        }

        soundButton.setOnClickListener {
            changeSoundMode()
        }

        glossaryButton.setOnClickListener {
            toggleGlossary()
        }

    }


    private fun setUpAudio(song: String) {
        val sharedPrefs: SharedPreferences = this.getSharedPreferences("com.garsontech.rubiesafrica", Context.MODE_PRIVATE)
        val isSoundOn = sharedPrefs.getBoolean(Constants.isSoundOn, true)
        storyAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val file = resources.getIdentifier(song, "raw", packageName)
        storyMediaPlayer = MediaPlayer.create(this, file)
        storyMediaPlayer?.isLooping = true

        if (isSoundOn) {
            storyMediaPlayer?.start()
            sharedPrefs.edit()?.putBoolean(Constants.isSoundOn, true)?.apply()
        }
    }

    private fun changeSoundIcon() {
        val sharedPrefs: SharedPreferences = this.getSharedPreferences("com.garsontech.rubiesafrica", Context.MODE_PRIVATE)
        val isSoundOn = sharedPrefs.getBoolean(Constants.isSoundOn, true)
        if (isSoundOn) {
            soundButton.background = getDrawable(R.drawable.audio_on)
        }
        else {
            soundButton.background = getDrawable(R.drawable.audio_off)
        }
    }

    private fun changeSoundMode() {
        val sharedPrefs: SharedPreferences = this.getSharedPreferences("com.garsontech.rubiesafrica", Context.MODE_PRIVATE)
        val soundOn = sharedPrefs.getBoolean(Constants.isSoundOn, true)
        if (soundOn) {
            storyMediaPlayer?.pause()
            soundButton.background = getDrawable(R.drawable.audio_off)
            sharedPrefs.edit()?.putBoolean(Constants.isSoundOn, false)?.apply()
        }
        else {
            storyMediaPlayer?.start()
            soundButton.background = getDrawable(R.drawable.audio_on)
            sharedPrefs.edit()?.putBoolean(Constants.isSoundOn, true)?.apply()
        }
    }

    private fun hideGlossary() {
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val height = size.y
        glossaryLayout.y = height.toFloat()
    }

    private fun toggleGlossary() {
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        var height = size.y
        if (!glossaryIsShowing) {
            height = -height
        }
        glossaryLayout.animate().translationYBy(height.toFloat()).setDuration(1000)
        glossaryIsShowing = !glossaryIsShowing
    }


    class WordForGlossary(val word: Word): Item<ViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.glossary_word_layout
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.meaningTextView.text = "${word.word}: ${word.meaning}"
        }

    }

    private fun resizeBitmap(bitmap: Bitmap): Bitmap {
        val width = screenWidth
        val height = screenHeight
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }


}


