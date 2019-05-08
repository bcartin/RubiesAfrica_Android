package com.garsontech.rubiesafrica.Models

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import java.io.FileOutputStream
import android.graphics.BitmapFactory
import android.os.Parcelable
import android.util.Log
import com.garsontech.rubiesafrica.appContext
import kotlinx.android.parcel.Parcelize
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*

@Parcelize
class Book (
    val storyId: String,
    val androidStoryId: String,
    val title: String,
    val summary: String,
    val totalPages: Int,
    val ageGroup1: Boolean,
    val ageGroup2: Boolean,
    val ageGroup3: Boolean,
    var purchased: Boolean,
    var coverImageUrl: String,
    var demoImage1Url: String,
    var demoImage2Url: String,
    var demoImage3Url: String,
    val author: String,
    val editor: String,
    val illustrator: String,
    val creativeDirector: String,
    val song: String,
    val content: ArrayList<String>
) : Parcelable

{

//    constructor() : this("","","",0,false,false,false,false,"","",
//        "","","","","","","", ArrayList<String>() )

    constructor() : this(storyId = "", androidStoryId= "", title = "", summary = "", totalPages = 0, ageGroup1 = false, ageGroup2 = false, ageGroup3 = false, purchased = false, coverImageUrl = "", demoImage1Url = "",
        demoImage2Url = "", demoImage3Url = "", author = "", editor = "", illustrator = "", creativeDirector = "", song = "", content = ArrayList<String>())




}



