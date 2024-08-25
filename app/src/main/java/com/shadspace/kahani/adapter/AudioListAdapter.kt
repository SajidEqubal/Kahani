package com.shadspace.kahani.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.shadspace.kahani.databinding.AudioListItemRecyclerRowBinding
import com.shadspace.kahani.models.AudioModel

class AudioListAdapter(private  val audioIdList : List<String>) :
    RecyclerView.Adapter<AudioListAdapter.MyViewHolder>() {

    class MyViewHolder(private val binding: AudioListItemRecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){
        //bind data with view
        fun bindData(audioId : String){

            FirebaseFirestore.getInstance().collection("audio")
                .document(audioId).get()
                .addOnSuccessListener {
                    val audio = it.toObject(AudioModel::class.java)
                    audio?.apply {
                        binding.songTitleTextView.text = title
                        binding.songSubtitleTextView.text = subtitle
                        Glide.with(binding.songCoverImageView).load(coverUrl)
                            .apply(
                                RequestOptions().transform(RoundedCorners(32))
                            )
                            .into(binding.songCoverImageView)
                    }
                }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = AudioListItemRecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return audioIdList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindData(audioIdList[position])
    }

}