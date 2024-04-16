package com.example.mystoryapp

import com.example.mystoryapp.data.response.ListStoryItem

object DataDummy {

    fun generateDummyStoryResponse(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0..100) {
            val story = ListStoryItem(
                "https://story-api.dicoding.dev/images/stories/photos-1699012417892_XeMBGoGx.jpg",
                "2023-11-03T11:53:37.893Z",
                "didi $i",
                "ini dari galeri $i",
                107.6338462,
                "$i",
                -6.8957643,
            )
            items.add(story)
        }
        return items
    }
}