package com.shadspace.kahani

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.shadspace.kahani.adapter.AudioListAdapter
import com.shadspace.kahani.databinding.ActivityAudioListBinding
import com.shadspace.kahani.models.CategoryModel

class AudioListActivity : AppCompatActivity() {

    companion object{
        lateinit var category : CategoryModel
    }

    lateinit var  binding: ActivityAudioListBinding
    lateinit var audioListAdapter: AudioListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioListBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.nameTextView.text  = category.name
        Glide.with(binding.coverImageView).load(category.coverUrl)
            .apply(
                RequestOptions().transform(RoundedCorners(32))
            )
            .into(binding.coverImageView)


        setupAudioListRecyclerView()
    }

    fun setupAudioListRecyclerView(){
        audioListAdapter = AudioListAdapter(category.audio)
        binding.audioListRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.audioListRecyclerView.adapter = audioListAdapter
    }

}
