package com.shadspace.kahani

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import kotlin.math.abs
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.shadspace.kahani.adapter.ImageAdapter
import com.shadspace.kahani.databinding.ActivityHomeBinding
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.shadspace.kahani.adapter.CategoryAdapter
import com.shadspace.kahani.adapter.SectionAudioListAdapter
import com.shadspace.kahani.models.CategoryModel

class Home : AppCompatActivity() {

    lateinit var binding: ActivityHomeBinding
    lateinit var categoryAdapter: CategoryAdapter

    private lateinit var viewPager2: ViewPager2
    private lateinit var handler: Handler
    private lateinit var imageList: ArrayList<Int>
    private lateinit var adapter: ImageAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()


        //Start the shimmer effect
        binding.simmerViewHome.visibility = View.VISIBLE
        binding.simmerViewHome.startShimmer()
        binding.dataView.visibility = View.GONE



        binding.profileImage.setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
        }


        init()
        setUpTransformer()

        getCategories()

        setupSection("section_1", binding.section1MainLayout, binding.section1Title, binding.section1RecyclerView) //setupSection()


        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 2000)
            }
        })
    }


    // For Categories
    fun getCategories() {
        FirebaseFirestore.getInstance().collection("category")
            .get().addOnSuccessListener {
                val categoryList = it.toObjects(CategoryModel::class.java)
                setupCategoryRecyclerView(categoryList)

                // Hide shimmer and show the content view once data is loaded
                binding.simmerViewHome.visibility = View.GONE
                binding.dataView.visibility = View.VISIBLE
            }
            .addOnFailureListener {
                // Handle any errors here, and stop shimmer if needed
                Log.e("FirestoreError", "Error loading categories", it)

                // Also stop shimmer and show a message or keep the shimmer, as needed
                binding.simmerViewHome.stopShimmer()
                binding.simmerViewHome.visibility = View.GONE
                binding.dataView.visibility = View.VISIBLE  // You may want to show an error message here
            }
    }


    fun setupCategoryRecyclerView(categoryList: List<CategoryModel>) {
        categoryAdapter = CategoryAdapter(categoryList)
        binding.categoriesRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.categoriesRecyclerView.adapter = categoryAdapter
    }


    //Showing Player View
    fun showPlayerView() {
        binding.playerView.setOnClickListener {
            startActivity(Intent(this, PlayerActivity::class.java))
        }
        MyExoplayer.getCurrentSong()?.let {
            binding.playerView.visibility = View.VISIBLE
            binding.songTitleTextView.text = "Playing : " + it.title
            Glide.with(binding.songCoverImageView).load(it.coverUrl)
                .apply(
                    RequestOptions().transform(RoundedCorners(32))
                ).into(binding.songCoverImageView)
        } ?: run {
            binding.playerView.visibility = View.GONE
        }
    }


    // For Sections

    fun setupSection(
        id: String,
        mainLayout: RelativeLayout,
        titleView: TextView,
        recyclerView: RecyclerView
    ) {
        FirebaseFirestore.getInstance().collection("sections")
            .document(id)
            .get().addOnSuccessListener {
                val section = it.toObject(CategoryModel::class.java)
                section?.apply {
                    mainLayout.visibility = View.VISIBLE
                    titleView.text = name
                    recyclerView.layoutManager =
                        LinearLayoutManager(this@Home, LinearLayoutManager.HORIZONTAL, false)
                    recyclerView.adapter = SectionAudioListAdapter(audio)
                    mainLayout.setOnClickListener {
                        AudioListActivity.category = section
                        startActivity(Intent(this@Home, AudioListActivity::class.java))
                    }
                }
            }
    }


    // For Image Slider Above

    override fun onPause() {
        super.onPause()

        handler.removeCallbacks(runnable)
        binding.simmerViewHome.stopShimmer()   // Stop shimmer when the view is not visible to avoid memory leaks

    }

    override fun onResume() {
        super.onResume()
        //Calling for player view
        showPlayerView()
        binding.simmerViewHome.startShimmer()  // Start shimmer when the view is visible



        handler.postDelayed(runnable, 2000)
    }

    private val runnable = Runnable {
        viewPager2.currentItem = viewPager2.currentItem + 1
    }

    private fun setUpTransformer() {
        val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(40))
        transformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.14f
        }

        viewPager2.setPageTransformer(transformer)
    }

    private fun init() {
        viewPager2 = findViewById(R.id.viewPager2)
        handler = Handler(Looper.myLooper()!!)
        imageList = ArrayList()

        imageList.add(R.drawable.one)
        imageList.add(R.drawable.two)
        imageList.add(R.drawable.three)


        adapter = ImageAdapter(imageList, viewPager2)

        viewPager2.adapter = adapter
        viewPager2.offscreenPageLimit = 3
        viewPager2.clipToPadding = false
        viewPager2.clipChildren = false
        viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

    }


}

