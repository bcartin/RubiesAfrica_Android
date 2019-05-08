package com.garsontech.rubiesafrica

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.garsontech.rubiesafrica.Models.Book
import com.garsontech.rubiesafrica.Models.Page
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.book_layout.view.*
import kotlinx.android.synthetic.main.fragment_lastpage.view.*
import kotlinx.android.synthetic.main.fragment_page.*
import kotlinx.android.synthetic.main.fragment_page.view.*

class PageFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_page, container, false)

        view.pageTextView.alpha = 0f

        val args = arguments
        val imageUrl = "file:${args?.getString(Constants.imageUrl)}"
//        Picasso.get().load(imageUrl).into(view.pageImageView)
        Glide.with(this).load(imageUrl).override(screenWidth, screenHeight).into(view.pageImageView)
        view.pageTextView.text = args?.getString(Constants.pageText)
        view.pageNumberTextView.text = "${args?.getInt(Constants.pageNumber)} / ${args?.getInt(Constants.totalPages)}"

        if (args?.getInt(Constants.pageNumber) == 1) {
            view.pageTextView.animate().alpha(1.0f).setDuration(1500)
        }

        return view
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if (isVisibleToUser) {
            view?.pageTextView?.animate()?.alpha(1.0f)?.setDuration(1000)
        }
    }


    companion object {

        // Method for creating new instances of the fragment
        fun newInstance(page: Page, totalPages: Int): PageFragment {

            // Store the page data in a Bundle object
            val args = Bundle()
            args.putString(Constants.imageUrl, page.imageUrl)
            args.putString(Constants.pageText, page.pageText)
            args.putInt(Constants.pageNumber, page.pageNumber)
            args.putInt(Constants.totalPages, totalPages)

            // Create a new PageFragment and set the Bundle as the arguments
            // to be retrieved and displayed when the view is created
            val fragment = PageFragment()
            fragment.arguments = args
            return fragment
        }
    }

}

class LastPageFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_lastpage, container, false)

        val args = arguments
        view.authoTextView.text = args?.getString(Constants.author)
        view.editorTextView.text = args?.getString(Constants.editor)
        view.illustratorTextView.text = args?.getString(Constants.illustrator)
        view.creativeDirectorTextView.text = args?.getString(Constants.creativeDirector)

        view.shareButton.setOnClickListener {
            shareStory()
        }

        view.facebookButton.setOnClickListener {
            shareStory()
        }

        view.instagramButton.setOnClickListener {
            shareStory()
        }

        view.twitterButton.setOnClickListener {
            shareStory()
        }

        return view
    }

    fun shareStory() {
        val i = Intent(Intent.ACTION_SEND)
        i.setType("text/plain")
        i.putExtra(Intent.EXTRA_TEXT, "I just read a great story on this app! http://www.familyrubies.com/app/")
        try {
            startActivity(Intent.createChooser(i, "Share your experience"))
        } catch (ex: android.content.ActivityNotFoundException) {
            println("ERROR SHARING")
        }
    }

    companion object {

        fun newInstance(book: Book): LastPageFragment {

            val args = Bundle()
            args.putString(Constants.author, book.author)
            args.putString(Constants.editor, book.editor)
            args.putString(Constants.illustrator, book.illustrator)
            args.putString(Constants.creativeDirector, book.creativeDirector)

            val fragment = LastPageFragment()
            fragment.arguments = args
            return fragment
        }
    }

}