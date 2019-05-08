package com.garsontech.rubiesafrica.Models

class Page(
    var storyId: String,
    val pageNumber: Int,
    val pageText: String,
    var imageUrl: String
) {

    constructor() : this("", 0, "", "")

}

