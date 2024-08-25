package com.shadspace.kahani.models

data class CategoryModel(
    val name : String,
    val coverUrl : String,
    val audio : List<String>
) {
    constructor() : this("","", listOf())
}