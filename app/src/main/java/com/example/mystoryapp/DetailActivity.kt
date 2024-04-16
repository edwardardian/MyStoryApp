package com.example.mystoryapp

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.mystoryapp.data.response.ListStoryItem
import com.example.mystoryapp.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var item = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(KEY_DETAIL)!!
        } else {
            intent.getParcelableExtra(KEY_DETAIL, ListStoryItem::class.java)!!
        }

        binding.apply {
            Glide.with(root)
                .load(item.photoUrl)
                .into(binding.ivItemPhoto)
            tvItemDesc.text = item.description
            tvItemName.text = item.name
        }
    }

    companion object {
        const val KEY_DETAIL = "key_detail"
    }
}