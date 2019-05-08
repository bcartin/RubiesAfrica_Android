package com.garsontech.rubiesafrica

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Toast
import com.android.billingclient.api.*
import com.bumptech.glide.Glide
import com.garsontech.rubiesafrica.Models.Book
import com.garsontech.rubiesafrica.Services.fileService
import com.garsontech.rubiesafrica.Services.webService
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import com.yarolegovich.discretescrollview.transform.Pivot
import com.yarolegovich.discretescrollview.transform.ScaleTransformer
import kotlinx.android.synthetic.main.activity_book_list.*
import kotlinx.android.synthetic.main.book_layout.view.*
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

enum class bookFrameColor {
    blue, red, yellow, green
}

var currencyCode: String = "USD"

class BookListActivity : AppCompatActivity(), PurchasesUpdatedListener {

    val adapter = GroupAdapter<ViewHolder>()
    var bookColor: bookFrameColor = bookFrameColor.blue
    var bookList = ArrayList<Book>()
    private lateinit var billingClient: BillingClient
    val skuDetailsMap = HashMap<String, SkuDetails>()

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_list)

        loadBookList()
        Picasso.get().load(R.drawable.sunset_background).resize(screenWidth, screenHeight).into(backgroundImageView)
        setupBillingClient {
            if (it == null) {
                loadAdapterList()
            }
            else {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }
//        loadAdapterList()

        changeSoundIcon()
        discreteBooksView.adapter = adapter
        discreteBooksView.setOffscreenItems(4)
        val transformer = ScaleTransformer.Builder()
            .setMaxScale(1.05f)
            .setMinScale(0.8f)
            .setPivotX(Pivot.X.CENTER)
            .setPivotY(Pivot.Y.CENTER)
            .build()
        discreteBooksView.setItemTransformer(transformer)



        filterButton.setOnClickListener {
            showFilterDialog()
        }

        contactButton.setOnClickListener {
            sendContactUsEmail()
        }

        soundButton.setOnClickListener {
            changeSoundMode()
        }

        adapter.setOnItemClickListener { item, view ->
            val row = item as bookCover
            if (!row.book.purchased && row.book.storyId != "comingsoon") {
                val intent = Intent(this, PreviewActivity::class.java)
                intent.putExtra(Constants.selectedBook, row.book)
                startActivity(intent)
            }
            else if (row.book.purchased) {
                startStopSound(false)
                val intent = Intent(this, StoryActivity::class.java)
                intent.putExtra(Constants.selectedBook, row.book)
                startActivity(intent)
            }
        }

        restoreButton.setOnClickListener {
            updateLayout.visibility = View.VISIBLE
        }

        laterButton.setOnClickListener {
            updateLayout.visibility = View.INVISIBLE
        }

        continueButton.setOnClickListener {
            restorePurchases()
        }


    }

    override fun onResume() {
        super.onResume()

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        changeSoundIcon()
        startStopSound(true)

    }

    private fun setupBillingClient(callback: (Exception?) -> Unit) {
        billingClient = BillingClient
            .newBuilder(this)
            .setListener(this)
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(@BillingClient.BillingResponse billingResponseCode: Int) {
                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    // The billing client is ready. You can query purchases here.
                    println("BILLING CLIENT IS READY")
                    querySkuDetails {
                        if (it == null) {
                            callback(null)
                        }
                        else {
                            callback(it)
                        }
                    }
                }
                else {
                    val err = Exception("Error Setting Up Billing Client")
                    callback(err)
                }
            }
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                println("DISCONNECTED")
                val err = Exception("Error Setting Up Billing Client")
                callback(err)
            }
        })


    }



    private fun querySkuDetails(callback: (Exception?) -> Unit) {
        // for in-app purchase
        val skuList = ArrayList<String>()
        bookList.forEach {
            if (it.storyId != "aVisitToTheOrphanage" || it.storyId != "comingsoon") {
                skuList.add(it.androidStoryId)
            }
        }
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        billingClient.querySkuDetailsAsync(params.build()) { responseCode, skuDetailsList ->
            // Process the result.
            if (responseCode == BillingClient.BillingResponse.OK && skuDetailsList != null) {
                for (skuDetails in skuDetailsList) {
                    currencyCode = skuDetails.priceCurrencyCode
                    skuDetailsMap[skuDetails.sku] = skuDetails
                    println(skuDetails.price)
                    println(skuDetails.priceCurrencyCode)
                }
                callback(null)
            }
            else {
                val err = Exception("Error Setting Up Billing Client")
                callback(err)
            }
        }

    }

    private fun queryPurchases() { //FOR RESTORE PURCHASES???
        val purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP)

        if (purchasesResult.purchasesList.isEmpty()) {
            loadingLayout.visibility = View.INVISIBLE
            loadAdapterList()
            Toast.makeText(this, "Download Complete", Toast.LENGTH_LONG).show()
        }
        else {
            for (purchase in purchasesResult.purchasesList) {

                var count = 0
                val webHandler = webService(this@BookListActivity)
                val content = dbHandler?.loadStoryContent(purchase.sku)
                content?.forEach {
                    webHandler?.loadSingleStory(it) {
                        if (it == null) {
                            count += 1
                            if (count == purchasesResult.purchasesList.count()) {
                                loadingLayout.visibility = View.INVISIBLE
                                loadAdapterList()
                                Toast.makeText(this, "Download Complete", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    fun buyProduct(productId: String) {
        val skuDetails = skuDetailsMap[productId]
        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()
        val responseCode = billingClient.launchBillingFlow(this, flowParams)
    }

    override fun onPurchasesUpdated(@BillingClient.BillingResponse responseCode: Int, purchases: List<Purchase>?) {
        if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
            val webHandler = webService(this@BookListActivity)
            for (purchase in purchases) {
                loadingLayout.visibility = View.VISIBLE
                println("Purchase Successfull ${purchase.sku}")
                val content = dbHandler?.loadStoryContent(purchase.sku)
                content?.forEach {
                    webHandler?.loadSingleStory(it) {
                        loadingLayout.visibility = View.INVISIBLE
                        if (it == null) {
                            loadAdapterList()
                            Toast.makeText(this, "Download Complete", Toast.LENGTH_LONG).show()
                        }
                        else {
                            Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Toast.makeText(this, "There was an error completing your purchase. Try again.", Toast.LENGTH_LONG).show()
        } else {
            // Handle any other error codes.
            Toast.makeText(this, "There was an error completing your purchase. Try again.", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadBookList() {
        bookList.clear()
        bookList = dbHandler?.loadStoriesFromDatabase()!!
        bookList.sortBy { !it.purchased }
        val lastBook = Book("comingsoon", "coming_soon", "Coming Soon", "", 0, false, false, false, false, "comingsoon.png", "", "", "", "", "", "", "", "", ArrayList<String>())
        bookList.add(lastBook)
    }

    private fun loadAdapterList() {
        adapter.clear()
        bookList.forEach {
            val price = skuDetailsMap[it.androidStoryId]?.price ?: ""
            adapter.add(bookCover(it, bookColor, this, price) {
                buyProduct(it)
            })
            bookColor = getNextBookColor()
        }
    }

    private fun getNextBookColor(): bookFrameColor {
        when(bookColor) {
            bookFrameColor.blue -> return bookFrameColor.red
            bookFrameColor.red -> return bookFrameColor.yellow
            bookFrameColor.yellow -> return bookFrameColor.green
            bookFrameColor.green -> return bookFrameColor.blue
        }
    }

    private fun showFilterDialog() {

        val builder: AlertDialog.Builder? = this.let {
            AlertDialog.Builder(it)
        }

        val filterArray = arrayOf<CharSequence>("All Stories", "Ages 4-6", "Ages 7-12", "Ages 13 & above")
        builder?.setTitle("SELECT A GROUP")?.setItems(filterArray, DialogInterface.OnClickListener { dialog, which ->
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            adapter.clear()
            var filteredList = ArrayList<Book>()
            when (which) {
                0 -> filteredList = bookList
                1 -> filteredList = bookList.filter { s -> s.ageGroup1 == true } as ArrayList<Book>
                2 -> filteredList = bookList.filter { s -> s.ageGroup2 == true } as ArrayList<Book>
                3 -> filteredList = bookList.filter { s -> s.ageGroup3 == true } as ArrayList<Book>
            }

            filteredList.forEach {
                val price = skuDetailsMap[it.androidStoryId]?.price ?: "1.99"
                adapter.add(bookCover(it, bookColor, this, price) {
                    buyProduct(it)
                })
                bookColor = getNextBookColor()
            }

            filterTextView.text = filterArray[which].toString()

        })

        val dialog: AlertDialog? = builder?.create()
        dialog?.listView?.divider = ColorDrawable(Color.BLACK)
        dialog?.listView?.dividerHeight = 2
        dialog?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        dialog?.show()
    }

    private fun sendContactUsEmail() {
        val i = Intent(Intent.ACTION_SEND)
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_EMAIL, "info@familyrubies.com");
        i.putExtra(Intent.EXTRA_SUBJECT, "New Contact Us Submission");
        try {
            startActivity(Intent.createChooser(i, "Contact Us..."));
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
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
            mediaPlayer?.pause()
            soundButton.background = getDrawable(R.drawable.audio_off)
            sharedPrefs.edit()?.putBoolean(Constants.isSoundOn, false)?.apply()
        }
        else {
            mediaPlayer?.start()
            soundButton.background = getDrawable(R.drawable.audio_on)
            sharedPrefs.edit()?.putBoolean(Constants.isSoundOn, true)?.apply()
        }
    }

    private fun startStopSound(start: Boolean) {
        val sharedPrefs: SharedPreferences = this.getSharedPreferences("com.garsontech.rubiesafrica", Context.MODE_PRIVATE)
        val soundOn = sharedPrefs.getBoolean(Constants.isSoundOn, true)
        if (soundOn) {
            if (start) {
                mediaPlayer?.start()
            }
            else {
                mediaPlayer?.pause()
            }
        }
    }

    private fun restorePurchases() {
        if (isConnected()) {
            if (dbHandler?.deleteAllStories()!!) {
                if (dbHandler?.deleteAllPages()!!) {
                    loadFirstBook()
                    downloadAllStories()
                }
            }
        }
    }

    private fun downloadAllStories() = runBlocking {
        updateLayout.visibility = View.INVISIBLE
        if (isConnected()) {
            val webHandler = webService(this@BookListActivity)
            loadingTextView.text = "Downloading Content. Please Wait"
            loadingLayout.visibility = View.VISIBLE
            webHandler.loadGlossaryFromWeb()
            webHandler.loadStoriesFromWeb() {
                if (it == null) {
                    val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                    val formattedDate = originalFormat.format(Date())
                    sharedPrefs?.edit()?.putString(Constants.lastUpdate, formattedDate)?.apply()
                    queryPurchases()
                }
            }
        }
        else {
            Toast.makeText(this@BookListActivity, "No Internet Connection", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadFirstBook() {
        val book = fileService.loadStoryFromJson("main_story.json", this)
        dbHandler?.saveStoryToDataBase(book!!)
        val pages = fileService.loadPagesFromJson(book?.storyId!!, "main_pages.json", this)
        pages.forEach {
            dbHandler?.savePageToDataBase(it)
        }
        sharedPrefs?.edit()?.putBoolean(Constants.firstLoadComplete, true)?.apply()
    }



    class bookCover(val book: Book, val bookColor: bookFrameColor, val context: Context, val price: String, private val onProductClicked: (String) -> Unit): Item<ViewHolder>() {

        override fun getLayout(): Int {
            return R.layout.book_layout
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.titleTextView.text = book.title

            val height = (screenHeight * 0.8).toInt()
            val width = (height / 3) * 4
            viewHolder.itemView.bookImageView.layoutParams.height = height
            viewHolder.itemView.bookImageView.layoutParams.width = width
            viewHolder.itemView.bookImageView.requestLayout()
            viewHolder.itemView.coverImageView.layoutParams.height = (height * 0.83).toInt()
            viewHolder.itemView.coverImageView.requestLayout()
            viewHolder.itemView.buyButton.layoutParams.height = (height * 0.18).toInt()
            viewHolder.itemView.buyButton.requestLayout()

            val rightConstraint = width * 0.09
            val constraintSet = ConstraintSet()
            constraintSet.clone(viewHolder.itemView.mainLayout)
            constraintSet.connect(R.id.coverImageView, ConstraintSet.END, R.id.bookImageView, ConstraintSet.END, rightConstraint.toInt())
            constraintSet.applyTo(viewHolder.itemView.mainLayout)

            when(bookColor) {
                bookFrameColor.blue -> Glide.with(context).load(R.drawable.book_blue).override(width, height).into(viewHolder.itemView.bookImageView)
                bookFrameColor.green -> Glide.with(context).load(R.drawable.book_green).override(width, height).into(viewHolder.itemView.bookImageView)
                bookFrameColor.red -> Glide.with(context).load(R.drawable.book_red).override(width, height).into(viewHolder.itemView.bookImageView)
                bookFrameColor.yellow -> Glide.with(context).load(R.drawable.book_yellow).override(width, height).into(viewHolder.itemView.bookImageView)
            }

            viewHolder.itemView.coverImageView.clipToOutline = true
            if (book.storyId == "comingsoon") {
                Picasso.get().load(R.drawable.comingsoon).resize(width, height).into(viewHolder.itemView.coverImageView)
                viewHolder.itemView.buyButton.visibility = View.INVISIBLE
                viewHolder.itemView.pagesTextView.visibility = View.INVISIBLE
            }
            else {
                val currency = Currency.getInstance(currencyCode)
                val symbol = currency.symbol

                val imageUrl = "file:${book.coverImageUrl}"
                Picasso.get().load(imageUrl).resize(width, height).into(viewHolder.itemView.coverImageView)
                viewHolder.itemView.pagesTextView.text = "${book.totalPages}p"
                if (book.purchased) {
                    viewHolder.itemView.buyButton.visibility = View.INVISIBLE
                } else {
                    viewHolder.itemView.buyButton.text = "$price\nBUY"
                    viewHolder.itemView.buyButton.visibility = View.VISIBLE
                }
            }

            viewHolder.itemView.buyButton.setOnClickListener {
                onProductClicked(book.androidStoryId)
            }
        }


    }

}






