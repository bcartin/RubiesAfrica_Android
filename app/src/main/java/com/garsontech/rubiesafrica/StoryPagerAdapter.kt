package com.garsontech.rubiesafrica

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.garsontech.rubiesafrica.Models.Book
import com.garsontech.rubiesafrica.Models.Page

class StoryPagerAdapter (fragmentManager: FragmentManager, private val pages: ArrayList<Page>, private val book: Book) :
    FragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        if (position < pages.count()) {
            return PageFragment.newInstance(pages[position], pages.count())
        }
        else {
            // change to last page fragment
            return LastPageFragment.newInstance(book)
        }

    }

    override fun getCount(): Int {
        return pages.count() + 1
    }
}