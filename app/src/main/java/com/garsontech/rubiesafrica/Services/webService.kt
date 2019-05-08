package com.garsontech.rubiesafrica.Services

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.AsyncTask
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.RequestOptions
import com.garsontech.rubiesafrica.Constants
import com.garsontech.rubiesafrica.Models.Book
import com.garsontech.rubiesafrica.Models.Page
import com.garsontech.rubiesafrica.Models.Word
import com.garsontech.rubiesafrica.dbHandler
import com.garsontech.rubiesafrica.screenWidth
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.lang.Exception
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class webService(val context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("com.garsontech.rubiesafrica", Context.MODE_PRIVATE)

    inner class NewImageDownloader(context: Context) : AsyncTask<String, Void, Bitmap>() {
        private var mContext: WeakReference<Context> = WeakReference(context)

        override fun doInBackground(vararg params: String?): Bitmap? {
            val url = params[0]
            var imageBitmap: Bitmap? = null
            val requestOptions = RequestOptions().override(1200,900)
                .downsample(DownsampleStrategy.CENTER_INSIDE)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)

            mContext.get()?.let {
                val bitmap = Glide.with(it)
                    .asBitmap()
                    .load(url)
                    .apply(requestOptions)
                    .submit()
                    .get()

                imageBitmap =  bitmap
            }

            return imageBitmap
        }
    }

    fun loadSingleStory(storyId: String, callback: (Exception?) -> Unit) {
        val storyUpdated = dbHandler?.updateStoryPurchasedStatus(storyId) ?: false
        if (storyUpdated) {
            loadPagesFromWeb(storyId) {
                callback(it)
            }
        }
        else {
            val exception = Exception("Error Updating Database")
            callback(exception)
        }
    }

    fun loadStoriesFromWeb(callback: (Exception?) -> Unit) {
        val storiesRef = FirebaseDatabase.getInstance().getReference("/Stories")
        storiesRef.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) = runBlocking {
                val children = p0.children
                var count = 0
                children.forEach() {
                    val book = it.getValue(Book::class.java)!!
                    //CHECK IF STORY EXISTS
                    val exists = dbHandler?.checkIfStoryExists(book?.storyId) ?: false
                    if (!exists) {
                        try {
                            //Save cover image
                            val coverImageTask = NewImageDownloader(context)
                            var imageBitmap = coverImageTask.execute(book.coverImageUrl).get()
                            var resizedBitmap = resizeBitmap(imageBitmap)
                            var title = "${book.storyId}_coverImage"
                            val imageUrl = async(Dispatchers.IO) { fileService.saveImageFileAsync(title, resizedBitmap) }
                            book.coverImageUrl = imageUrl.await() ?: ""

                            //Save demo image 1
                            val demoImage1Task = NewImageDownloader(context)
                            imageBitmap = demoImage1Task.execute(book.demoImage1Url).get()
                            resizedBitmap = resizeBitmap(imageBitmap)
                            title = "${book.storyId}_demoImage1"
                            val demoImage1Url = async(Dispatchers.IO) { fileService.saveImageFileAsync(title, resizedBitmap) }
                            book.demoImage1Url = demoImage1Url.await() ?: ""

                            //Save demo image 2
                            val demoImage2Task = NewImageDownloader(context)
                            imageBitmap = demoImage2Task.execute(book.demoImage2Url).get()
                            resizedBitmap = resizeBitmap(imageBitmap)
                            title = "${book.storyId}_demoImage2"
                            val demoImage2Url = async(Dispatchers.IO) { fileService.saveImageFileAsync(title, resizedBitmap) }
                            book.demoImage2Url = demoImage2Url.await() ?: ""

                            //Save demo image 3
                            val demoImage3Task = NewImageDownloader(context)
                            imageBitmap = demoImage3Task.execute(book.demoImage3Url).get()
                            resizedBitmap = resizeBitmap(imageBitmap)
                            title = "${book.storyId}_demoImage3"
                            val demoImage3Url = async(Dispatchers.IO) { fileService.saveImageFileAsync(title, resizedBitmap) }
                            book.demoImage3Url = demoImage3Url.await() ?: ""

                            //Save story to database
                            val saved = dbHandler?.saveStoryToDataBase(book)
                            if (saved!!) {
                                // If book is purchased, load pages
                                if (book.purchased) {
                                    loadPagesFromWeb(book.storyId) {
                                        if (it == null) {
                                            count += 1
                                            if (count == p0.childrenCount.toInt()) {
                                                    println("STORY COUNT $count, of ${p0.childrenCount}")
                                                    callback(null)
                                            }
                                        } else {
                                            callback(it)
                                        }
                                    }
                                }
                                else {
                                    count += 1
                                    if (count == p0.childrenCount.toInt()) {
                                        println("STORY COUNT $count, of ${p0.childrenCount}")
                                        callback(null)
                                    }
                                }
                            }
                        }
                        catch (e: Exception) {
                            e.printStackTrace()
                            callback(e)
                        }
                    }
                    else {
                        count += 1
                        println("${book.title} STORY ALREADY IN DB")
                        if (count == p0.childrenCount.toInt()) {
                            callback(null)
                        }
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                callback(p0.toException())
            }
        })
    }

    fun loadPagesFromWeb(storyId: String, callback: (Exception?) -> Unit) {
        var pagesRef = FirebaseDatabase.getInstance().getReference("/Pages/$storyId")
        pagesRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) { callback(p0.toException())}

            override fun onDataChange(p0: DataSnapshot) = runBlocking {
                try {
                    val children = p0.children
                    var count = 0
                    children.forEach {

                        val page = it.getValue(Page::class.java)
                        page?.storyId = storyId

                        //check if page already exists in db
                        val exists = dbHandler?.checkIfPageExists(page!!) ?: false
                        if (!exists) {
                            val pageImageTask = NewImageDownloader(context)
                            val imageBitmap = pageImageTask.execute(page?.imageUrl).get()
                            val resizedBitmap = resizeBitmap(imageBitmap)
                            val title = "${page?.storyId}_page${page?.pageNumber}"
                            page?.imageUrl = async(Dispatchers.IO) { fileService.saveImageFileAsync(title, resizedBitmap) ?: "" }.await()

                            val saved = dbHandler?.savePageToDataBase(page!!)
                            if (saved!!) {
                                count += 1
                                println("PAGE COUNT $count, of ${p0.childrenCount}")
                                if (count == p0.childrenCount.toInt()) {
                                    callback(null)
                                }
                            }
                        }
                        else {
                            count += 1
                            println("${page?.storyId} PAGE ${page?.pageNumber} ALREADY IN DATABASE")
                            if (count == p0.childrenCount.toInt()) {
                                println("PAGE COUNT $count, of ${p0.childrenCount}")
                                callback(null)
                            }
                        }
                    }
                }
                catch (e: Exception) {
                    e.printStackTrace()
                    callback(e)
                }
            }

        })

    }

    fun checkIfUpdateAvailable(callback: (Boolean) -> Unit) {

        val lastAppUpdateString = sharedPreferences.getString(Constants.lastUpdate, "1999-01-01")
        val serverRef = FirebaseDatabase.getInstance().getReference("/${Constants.lastUpdate}")

        serverRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                callback(false)
            }

            override fun onDataChange(p0: DataSnapshot) {
                val lastServerUpdateString = p0.value as String
                val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                val lastAppUpdate = originalFormat.parse(lastAppUpdateString)
                val lastServerUpdate = originalFormat.parse(lastServerUpdateString)
                val newUpdate = lastAppUpdate.before(lastServerUpdate)
                callback(newUpdate)
            }

        })
    }

    suspend fun loadGlossaryFromWeb() {
        val glossaryRef = FirebaseDatabase.getInstance().getReference("/Glossary")
        glossaryRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) = runBlocking {
                val children = p0.children
                children.forEach {
                    val key = it.key.toString()
                    val value = it.value.toString()
                    val word = Word(key, value)
                    val exists = async(Dispatchers.IO) { dbHandler?.checkIfWordExists(word) ?: false }.await()
                    if (!exists) {
                        val saved = async(Dispatchers.IO) { dbHandler?.saveWordToDatabase(word) ?: false }.await()
                        if (saved) println("WORD ${word.word} SAVED TO DATABASE")
                    }
                }

            }

        })
    }

    private fun resizeBitmap(bitmap: Bitmap): Bitmap {
        val width = screenWidth
        val height = screenWidth * 0.75
        return Bitmap.createScaledBitmap(bitmap, width, height.toInt(), false)
    }
}