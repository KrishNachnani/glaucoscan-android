package com.glaucoma.ai.data.model

class DummyApiResponseModel : ArrayList<DummyApiItem>()

data class DummyApiItem(
    val id: Int,
    val imdbId: String,
    val posterURL: String,
    val title: String
)