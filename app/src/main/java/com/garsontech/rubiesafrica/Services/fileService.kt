package com.garsontech.rubiesafrica.Services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
//import android.graphics.ImageDecoder
import android.util.Log
import com.garsontech.rubiesafrica.Constants
import com.garsontech.rubiesafrica.Models.Book
import com.garsontech.rubiesafrica.Models.Page
import com.garsontech.rubiesafrica.appContext
import com.garsontech.rubiesafrica.screenWidth
import org.json.JSONException
import org.json.JSONObject
import java.io.*

object fileService {

    fun saveImageFile(fileName: String, bitmap: Bitmap): String? {
        var outputStream: FileOutputStream? = null

        val context =  appContext ?: return null
        try {
            outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            val uri = context.getFileStreamPath(fileName)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            return uri.toString()
        } catch (error: Exception) {
            error.printStackTrace()
            return null
        }
        finally {
            outputStream?.close()
        }
    }

    suspend fun saveImageFileAsync(fileName: String, bitmap: Bitmap): String? {
        var outputStream: FileOutputStream? = null

        val context =  appContext ?: return null
        try {
            outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            val uri = context.getFileStreamPath(fileName)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            return uri.toString()
        } catch (error: Exception) {
            error.printStackTrace()
            return null
        }
        finally {
            outputStream?.close()
        }
    }

    fun loadImageFile(context: Context, imageName: String): Bitmap? {
        var b: Bitmap? = null
        var fis: FileInputStream? = null
        try {
            fis = context.openFileInput(imageName)
            b = BitmapFactory.decodeStream(fis)
        } catch (e: FileNotFoundException) {
            Log.d("TAG", "file not found")
            e.printStackTrace()
        } catch (e: IOException) {
            Log.d("TAG", "io exception")
            e.printStackTrace()
        } finally {
            fis?.close()
        }
        return b
    }

    fun deleteFile(fileName: String) {
        val context =  appContext ?: return
        try {
            context.deleteFile(fileName)
            println("FILE DELETED")
        }
        catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    fun getFileList() { //GET FILE
        val context =  appContext ?: return
        try {
            val list = context.filesDir.list()
//            val imageBM: Bitmap
//            val imageTask = ImageDownloader()
//            imageBM = imageTask.execute("/data/user/0/com.garsontech.rubiesafrica/files/Rain Rain Go Away").get()
            println(list.size)

        }
        catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun loadStoryFromJson(fileName: String, context: Context): Book? {
        try {
            val jsonString = loadJsonFromFile(fileName, context)
            val bookJSON = JSONObject(jsonString)

            val storyId = bookJSON.getString(Constants.storyId)
            val imageFileName = bookJSON.getString(Constants.coverImageUrl)
            val fileStream = context.assets.open(imageFileName)
            val imageBitmap = BitmapFactory.decodeStream(fileStream)
            val resizedBitmap = resizeBitmap(imageBitmap)
            val coverPageFileName = "$storyId" + "_coverImage"
            val coverImageUrl =  saveImageFile(coverPageFileName, resizedBitmap)

            val firstBook = Book(
                bookJSON.getString(Constants.storyId),
                bookJSON.getString(Constants.androidStoryId),
                bookJSON.getString(Constants.title),
                bookJSON.getString(Constants.summary),
                bookJSON.getInt(Constants.totalPages),
                bookJSON.getBoolean(Constants.ageGroup1),
                bookJSON.getBoolean(Constants.ageGroup2),
                bookJSON.getBoolean(Constants.ageGroup3),
                bookJSON.getBoolean(Constants.purchased),
                coverImageUrl ?: "",
                bookJSON.getString(Constants.demoImage1Url),
                bookJSON.getString(Constants.demoImage2Url),
                bookJSON.getString(Constants.demoImage3Url),
                bookJSON.getString(Constants.author),
                bookJSON.getString(Constants.editor),
                bookJSON.getString(Constants.illustrator),
                bookJSON.getString(Constants.creativeDirector),
                bookJSON.getString(Constants.song),
                ArrayList<String>()
                )
            return firstBook
        }
        catch (e: JSONException) {
            return null
        }
    }

    fun loadPagesFromJson(storyId: String, fileName: String, context: Context): ArrayList<Page> {
        val pages = ArrayList<Page>()

        try {

            val jsonString = loadJsonFromFile(fileName, context)
            val pageJSON = JSONObject(jsonString)
            val jsonPages = pageJSON.getJSONArray("pages")

            for (index in 0 until jsonPages.length()) {
                val imageFileName = jsonPages.getJSONObject(index).getString(Constants.imageUrl)
                val pageNumber = jsonPages.getJSONObject(index).getInt(Constants.pageNumber)
                val pageText = jsonPages.getJSONObject(index).getString(Constants.pageText)
                val fileStream = context.assets.open(imageFileName)
                val imageBitmap = BitmapFactory.decodeStream(fileStream)
                val resizedBitmap = resizeBitmap(imageBitmap)
                val pageFileName = "${storyId}_page${pageNumber}"
                val imageUrl = saveImageFile(pageFileName, resizedBitmap)
                val page = Page(storyId, pageNumber, pageText, imageUrl ?: "")
                pages.add(page)
            }
        }
        catch (e: JSONException) {
            return pages
        }
        return pages
    }

    private fun loadJsonFromFile(filename: String, context: Context): String {
        var json = ""

        try {
            val input = context.assets.open(filename)
            val size = input.available()
            val buffer = ByteArray(size)
            input.read(buffer)
            input.close()
            json = buffer.toString(Charsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return json
    }

    private fun resizeBitmap(bitmap: Bitmap): Bitmap {
        val width = screenWidth
        val height = screenWidth * 0.75
        return Bitmap.createScaledBitmap(bitmap, width, height.toInt(), false)
    }



}