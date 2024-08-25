package com.shadspace.kahani.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.shadspace.kahani.AudioListActivity
import com.shadspace.kahani.databinding.CategoryItemRecyclerRowBinding
import com.shadspace.kahani.models.CategoryModel


class CategoryAdapter (private val categoryList : List<CategoryModel>) :
    RecyclerView.Adapter<CategoryAdapter.MyViewHolder>() {

    class MyViewHolder(private val binding : CategoryItemRecyclerRowBinding) :
        RecyclerView.ViewHolder(binding.root){
        //bind the data with views
        fun bindData(category : CategoryModel){
            binding.nameTextView.text = category.name
            Glide.with(binding.coverImageView).load(category.coverUrl)
                .apply(
                    RequestOptions().transform(RoundedCorners(32))
                )
                .into(binding.coverImageView)

            //Start SongsList Activity
            val context = binding.root.context
            binding.root.setOnClickListener {
                AudioListActivity.category = category
                context.startActivity(Intent(context,AudioListActivity::class.java))
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = CategoryItemRecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindData(categoryList[position])
    }

}