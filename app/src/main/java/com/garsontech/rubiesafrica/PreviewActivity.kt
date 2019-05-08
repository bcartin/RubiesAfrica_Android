package com.garsontech.rubiesafrica

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.garsontech.rubiesafrica.Models.Book
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_preview.*
import kotlinx.android.synthetic.main.fragment_page.view.*

class PreviewActivity : AppCompatActivity() {

    var book: Book? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        Picasso.get().load(R.drawable.background22).resize(screenWidth, screenHeight).into(backgroundImageView)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        val width = screenWidth * 0.4
        imageViewLeft.layoutParams.width = width.toInt()
        imageViewLeft.requestLayout()
        imageViewCenter.layoutParams.width = width.toInt()
        imageViewCenter.requestLayout()
        imageViewRight.layoutParams.width = width.toInt()
        imageViewRight.requestLayout()
        val height = width * 0.75

        book = intent.getParcelableExtra<Book>(Constants.selectedBook)
        summaryTextView.text = book?.summary

        val imageRightUrl = "file:${book?.demoImage1Url}"
        Picasso.get().load(imageRightUrl).resize(width.toInt(), height.toInt()).into(imageViewRight)

        val imageCenterUrl = "file:${book?.demoImage2Url}"
        Picasso.get().load(imageCenterUrl).resize(width.toInt(), height.toInt()).into(imageViewCenter)

        val imageLeftUrl = "file:${book?.demoImage3Url}"
        Picasso.get().load(imageLeftUrl).resize(width.toInt(), height.toInt()).into(imageViewLeft)

        homeButton.setOnClickListener {
            finish()
        }

    }

    override fun onResume() {
        super.onResume()

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
    }

    private fun resizeBitmap(bitmap: Bitmap): Bitmap {
        val width = screenWidth
        val height = screenHeight
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }
}
