package com.garsontech.rubiesafrica.Services

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.garsontech.rubiesafrica.Constants
import com.garsontech.rubiesafrica.Models.Book
import com.garsontech.rubiesafrica.Models.Page
import com.garsontech.rubiesafrica.Models.Word
import com.garsontech.rubiesafrica.ObjectSerializer
import com.garsontech.rubiesafrica.toBoolean
import java.lang.Exception

class dbService(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private val DB_NAME = "RubiesDB"
        private val DB_VERSION = 1
        private val STORIES_TABLE = "Stories"
        private val PAGES_TABLE = "Pages"
        private val GLOSSARY_TABLE = "Glossary"

        private val STORY_ID = "storyId"
        private val ANDROID_STORY_ID = "androidStoryId"
        private val TITLE = "title"
        private val SUMMARY = "summary"
        private val TOTAL_PAGES = "totalPages"
        private val AGE_GROUP_1 = "ageGroup1"
        private val AGE_GROUP_2 = "ageGroup2"
        private val AGE_GROUP_3 = "ageGroup3"
        private val PURCHASED = "purchased"
        private val COVER_IMAGE_URL = "coverImageUrl"
        private val DEMO_IMAGE1_URL = "demoImage1Url"
        private val DEMO_IMAGE2_URL = "demoImage2Url"
        private val DEMO_IMAGE3_URL = "demoImage3Url"
        private val AUTHOR = "author"
        private val EDITOR = "editor"
        private val ILLUSTRATOR = "illustrator"
        private val CREATIVE_DIRECTOR = "creativeDirector"
        private val SONG = "song"
        private val CONTENT = "content"

        private val PAGE_NUMBER = "pageNumber"
        private val PAGE_TEXT = "pageText"
        private val IMAGE_URL = "imageUrl"

        private val WORD = "word"
        private val MEANING = "meaning"
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val DROP_STORIES_TABLE = "DROP TABLE IF EXISTS $STORIES_TABLE"
        val DROP_PAGES_TABLE = "DROP TABLE IF EXISTS $PAGES_TABLE"
        val DROP_GLOSSRY_TABLE = "DROP TABLE IF EXISTS $GLOSSARY_TABLE"
        try {
            db?.execSQL(DROP_STORIES_TABLE)
            db?.execSQL(DROP_PAGES_TABLE)
            db?.execSQL(DROP_GLOSSRY_TABLE)
            onCreate(db)
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_STORIES_TABLE = "CREATE TABLE IF NOT EXISTS $STORIES_TABLE (" +
                "$STORY_ID VARCHAR PRIMARY KEY, " +
                "$ANDROID_STORY_ID VARCHAR, " +
                "$TITLE VARCHAR, " +
                "$SUMMARY VARCHAR, " +
                "$TOTAL_PAGES INT(2), " +
                "$AGE_GROUP_1 INT(1), " +
                "$AGE_GROUP_2 INT(1), " +
                "$AGE_GROUP_3 INT(1), " +
                "$PURCHASED INT(1), " +
                "$COVER_IMAGE_URL VARCHAR, " +
                "$DEMO_IMAGE1_URL VARCHAR, " +
                "$DEMO_IMAGE2_URL VARCHAR, " +
                "$DEMO_IMAGE3_URL VARCHAR, " +
                "$AUTHOR VARCHAR, " +
                "$EDITOR VARCHAR, " +
                "$ILLUSTRATOR VARCHAR, " +
                "$CREATIVE_DIRECTOR VARCHAR, " +
                "$SONG VARCHAR, " +
                "$CONTENT VARCHAR" +
                ")"

        val CREATE_PAGES_TABLE = "CREATE TABLE IF NOT EXISTS $PAGES_TABLE (" +
                "$STORY_ID VARCHAR, " +
                "$PAGE_NUMBER INT(2), " +
                "$PAGE_TEXT VARCHAR, " +
                "$IMAGE_URL VARCHAR" +
                ")"

        val CREATE_GLOSSARY_TABLE = "CREATE TABLE IF NOT EXISTS $GLOSSARY_TABLE (" +
                "$WORD VARCHAR, " +
                "$MEANING VARCHAR" +
                ")"

        try {
            println("CREATING DATABASE")
            db?.execSQL(CREATE_STORIES_TABLE)
            db?.execSQL(CREATE_PAGES_TABLE)
            db?.execSQL(CREATE_GLOSSARY_TABLE)
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }




    fun saveStoryToDataBase(book: Book): Boolean {

        val exists = checkIfStoryExists(book.storyId)
        if (!exists) {
            val db = this.writableDatabase
            val values = ContentValues()

            values.put(STORY_ID, book.storyId)
            values.put(ANDROID_STORY_ID, book.androidStoryId)
            values.put(TITLE, book.title)
            values.put(SUMMARY, book.summary)
            values.put(TOTAL_PAGES, book.totalPages)
            values.put(AGE_GROUP_1, book.ageGroup1)
            values.put(AGE_GROUP_2, book.ageGroup2)
            values.put(AGE_GROUP_3, book.ageGroup3)
            values.put(PURCHASED, book.purchased)
            values.put(COVER_IMAGE_URL, book.coverImageUrl)
            values.put(DEMO_IMAGE1_URL, book.demoImage1Url)
            values.put(DEMO_IMAGE2_URL, book.demoImage2Url)
            values.put(DEMO_IMAGE3_URL, book.demoImage3Url)
            values.put(AUTHOR, book.author)
            values.put(EDITOR, book.editor)
            values.put(ILLUSTRATOR, book.illustrator)
            values.put(CREATIVE_DIRECTOR, book.creativeDirector)
            values.put(SONG, book.song)
            val content = ObjectSerializer.serialize(book.content)
            values.put(CONTENT, content)

            try {
                val _success = db.insert(STORIES_TABLE, null, values)
                db.close()
                println("${book.title} SAVED TO DATABASE!")
                return (Integer.parseInt("$_success") != -1)
            }
            catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }
        else {
            println("${book.title} ALREADY IN DATABASE!")
            return true
        }


    }

    fun savePageToDataBase(page: Page): Boolean {

        val exists = checkIfPageExists(page)
        if (!exists) {

            val db = this.writableDatabase
            val values = ContentValues()

            values.put(STORY_ID, page.storyId)
            values.put(PAGE_NUMBER, page.pageNumber)
            values.put(PAGE_TEXT, page.pageText)
            values.put(IMAGE_URL, page.imageUrl)

            try {
                val _success = db.insert(PAGES_TABLE, null, values)
                db.close()
                println("PAGE ${page.pageNumber} SAVED TO DATABASE!")
                return (Integer.parseInt("$_success") != -1)
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }
        else {
            return true
        }
    }

    suspend fun saveWordToDatabase(word: Word): Boolean {

        val exists = checkIfWordExists(word)
        if (!exists) {
            val db = this.writableDatabase
            val values = ContentValues()

            values.put(WORD, word.word)
            values.put(MEANING, word.meaning)

            try {
                val _success = db.insert(GLOSSARY_TABLE, null, values)
                db.close()
                return (Integer.parseInt("$_success") != -1)
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }
        else {
            return true
        }

    }

    fun loadStoriesFromDatabase(): ArrayList<Book> {

        var allStories = ArrayList<Book>()
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $STORIES_TABLE"
        val cursor = db.rawQuery(selectALLQuery, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val book = Book(
                        cursor.getString(cursor.getColumnIndex(STORY_ID)),
                        cursor.getString(cursor.getColumnIndex(ANDROID_STORY_ID)),
                        cursor.getString(cursor.getColumnIndex(TITLE)),
                        cursor.getString(cursor.getColumnIndex(SUMMARY)),
                        cursor.getInt(cursor.getColumnIndex(TOTAL_PAGES)),
                        cursor.getInt(cursor.getColumnIndex(AGE_GROUP_1)).toBoolean(),
                        cursor.getInt(cursor.getColumnIndex(AGE_GROUP_2)).toBoolean(),
                        cursor.getInt(cursor.getColumnIndex(AGE_GROUP_3)).toBoolean(),
                        cursor.getInt(cursor.getColumnIndex(PURCHASED)).toBoolean(),
                        cursor.getString(cursor.getColumnIndex(COVER_IMAGE_URL)),
                        cursor.getString(cursor.getColumnIndex(DEMO_IMAGE1_URL)),
                        cursor.getString(cursor.getColumnIndex(DEMO_IMAGE2_URL)),
                        cursor.getString(cursor.getColumnIndex(DEMO_IMAGE3_URL)),
                        cursor.getString(cursor.getColumnIndex(AUTHOR)),
                        cursor.getString(cursor.getColumnIndex(EDITOR)),
                        cursor.getString(cursor.getColumnIndex(ILLUSTRATOR)),
                        cursor.getString(cursor.getColumnIndex(CREATIVE_DIRECTOR)),
                        cursor.getString(cursor.getColumnIndex(SONG)),
                        ObjectSerializer.deserialize(cursor.getString(cursor.getColumnIndex(CONTENT))) as ArrayList<String>
                    )
                    allStories.add(book)

                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()
        return allStories
    }

    fun loadPagesFromDatabase(storyId: String): ArrayList<Page> {
        var allPages = ArrayList<Page>()
        val db = readableDatabase
        val selectAllQuery = "SELECT * FROM $PAGES_TABLE WHERE $STORY_ID = '$storyId'"
        val cursor = db.rawQuery(selectAllQuery, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val page = Page(
                        cursor.getString(cursor.getColumnIndex(STORY_ID)),
                        cursor.getInt(cursor.getColumnIndex(PAGE_NUMBER)),
                        cursor.getString(cursor.getColumnIndex(PAGE_TEXT)),
                        cursor.getString(cursor.getColumnIndex(IMAGE_URL))
                    )
                    allPages.add(page)
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()
        return allPages
    }

    fun loadWordsFromDatabase() : ArrayList<Word> {
        var allWords = ArrayList<Word>()
        val db = readableDatabase
        val selectAllQuery = "SELECT * FROM $GLOSSARY_TABLE"
        val cursor = db.rawQuery(selectAllQuery, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val word = Word(
                        cursor.getString(cursor.getColumnIndex(WORD)),
                        cursor.getString(cursor.getColumnIndex(MEANING))
                    )
                    allWords.add(word)
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()
        return allWords
    }

    fun loadStoryContent(androidStoryId: String): ArrayList<String> {
        var returnArray = ArrayList<String>()
        val db = readableDatabase
        val selectQuery = "SELECT $CONTENT FROM $STORIES_TABLE WHERE $ANDROID_STORY_ID = '$androidStoryId'"
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    returnArray = ObjectSerializer.deserialize(cursor.getString(cursor.getColumnIndex(CONTENT))) as ArrayList<String>
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()
        return returnArray

    }

    fun checkIfStoryExists(storyId: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $STORIES_TABLE WHERE $STORY_ID = '$storyId'"
        val cursor = db.rawQuery(query, null)
        return (cursor.count > 0)
    }

    fun checkIfPageExists(page: Page): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $PAGES_TABLE WHERE $STORY_ID = '${page.storyId}' AND $PAGE_NUMBER = ${page.pageNumber}"
        val cursor = db.rawQuery(query, null)
        return (cursor.count > 0)
    }

    suspend fun checkIfWordExists(word:Word): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $GLOSSARY_TABLE WHERE $WORD = '${word.word}'"
        val cursor = db.rawQuery(query, null)
        return (cursor.count > 0)
    }

    fun updateStoryPurchasedStatus(storyId: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(PURCHASED, true)
        val _success = db.update(STORIES_TABLE, values, STORY_ID + "=?", arrayOf(storyId))
        db.close()
        return (Integer.parseInt("$_success") != -1)
    }

    fun deleteStory(storyId: String): Boolean {
        val db = this.writableDatabase
        val _success = db.delete(STORIES_TABLE, STORY_ID + "=?", arrayOf(storyId))
        db.close()
        return (Integer.parseInt("$_success") != -1)
    }

    fun deleteAllStories(): Boolean {
        val db = this.writableDatabase
        val _success = db.delete(STORIES_TABLE, null, null)
        db.close()
        println("STORIES DELETED")
        return (Integer.parseInt("$_success") != -1)
    }

    fun deleteAllPages(): Boolean {
        val db = this.writableDatabase
        val _success = db.delete(PAGES_TABLE, null, null)
        db.close()
        println("PAGES DELETED")
        return (Integer.parseInt("$_success") != -1)
    }

    fun deleteAllGlossary(): Boolean {
        val db = this.writableDatabase
        val _success = db.delete(GLOSSARY_TABLE, null, null)
        db.close()
        println("GLOSSARY DELETED")
        return (Integer.parseInt("$_success") != -1)
    }


}

