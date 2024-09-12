package com.shadspace.kahani.ui

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
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shadspace.kahani.AudioListActivity
import com.shadspace.kahani.MyExoplayer
import com.shadspace.kahani.PlayerActivity
import com.shadspace.kahani.Profile
import com.shadspace.kahani.R
import com.shadspace.kahani.SharedPrefManager.getUserEmail
import com.shadspace.kahani.Subscribe
import com.shadspace.kahani.adapter.CategoryAdapter
import com.shadspace.kahani.adapter.SectionAudioListAdapter
import com.shadspace.kahani.adapter.VerticleAudioListAdapter
import com.shadspace.kahani.models.AudioModel
import com.shadspace.kahani.models.CategoryModel
import com.shadspace.kahani.util.SubscriptionUtils

class Home : AppCompatActivity() {

    lateinit var binding: ActivityHomeBinding
    lateinit var categoryAdapter: CategoryAdapter

    private lateinit var viewPager2: ViewPager2
    private lateinit var handler: Handler
    private lateinit var adapter: ImageAdapter

    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        val userEmail = getUserEmail(this)

        if (userEmail != null) {
           // Toast.makeText(this, "Welcome, $userEmail", Toast.LENGTH_SHORT).show()
            binding.profileImage.visibility = View.VISIBLE
            loadUserProfilePhoto()

            // Check subscription status using SubscriptionUtils
            SubscriptionUtils.checkSubscriptionStatus(this) { isActive ->
                if (isActive) {
                    binding.relFreeTrial.visibility = View.GONE
                } else {
                    binding.relFreeTrial.visibility = View.VISIBLE
                }
            }
        } else {
            binding.profileImage.visibility = View.GONE
        }


        //Start the shimmer effect
        binding.simmerViewHome.visibility = View.VISIBLE
        binding.simmerViewHome.startShimmer()
        binding.dataView.visibility = View.GONE



        binding.profileImage.setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
        }
        binding.freeTrial.setOnClickListener {
            startActivity(Intent(this, Subscribe::class.java))
        }

        //Implementing on users/email/subscription_status : "active" hide the relFreeTrial


        imageSlider()
        setUpTransformer()

        getCategories()

        setupSection(
            "section_1",
            binding.section1MainLayout,
            binding.section1Title,
            binding.section1RecyclerView
        ) //setupSection()

        topPicksSection(
            "top_picks",
            binding.section2MainLayout,
            binding.section2Title,
            binding.section2RecyclerView
        )

        setupMostlyPlayed(
            "mostly_played",
            binding.mostlyPlayedMainLayout,
            binding.mostlyPlayedTitle,
            binding.mostlyPlayedRecyclerView
        ) //setupSection()


        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 2000)
            }
        })
    }


    private fun loadUserProfilePhoto() {
        val user: FirebaseUser? = auth.currentUser
        if (user != null) {
            val photoUrl = user.photoUrl

            Glide.with(this)
                .load(photoUrl)
                .circleCrop()
                .error(R.drawable.logo) // on error image
                .into(binding.profileImage)
        } else {
            binding.profileImage.setImageResource(R.drawable.logo) // default image
        }

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
                binding.dataView.visibility =
                    View.VISIBLE  // You may want to show an error message here
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

        MyExoplayer.getCurrentSong()?.let { currentSong ->
            // Show the player view and set song details
            binding.playerView.visibility = View.VISIBLE
            binding.songTitleTextView.text = "Now Playing \n${currentSong.title}"

            // Load song cover image using Glide
            Glide.with(binding.songCoverImageView)
                .load(currentSong.coverUrl)
                .apply(RequestOptions().transform(RoundedCorners(32)))
                .into(binding.songCoverImageView)

            // Set play/pause button state based on whether the song is playing
            if (MyExoplayer.getInstance()?.isPlaying == true) {
                binding.playPauseImage.setImageResource(R.drawable.ic_pause) // Pause icon
            } else {
                binding.playPauseImage.setImageResource(R.drawable.ic_play) // Play icon
            }

            // Handle play/pause button click
            binding.playPauseImage.setOnClickListener {
                val exoPlayer = MyExoplayer.getInstance()
                exoPlayer?.let {
                    if (exoPlayer.isPlaying) {
                        exoPlayer.pause()
                        binding.playPauseImage.setImageResource(R.drawable.ic_play) // Change to play icon
                    } else {
                        exoPlayer.play()
                        binding.playPauseImage.setImageResource(R.drawable.ic_pause) // Change to pause icon
                    }
                }
            }

            // Handle close button click
            binding.closeImage.setOnClickListener {
                // Stop the audio playback
                MyExoplayer.stop()

                // Hide the player view
                binding.playerView.visibility = View.GONE
            }

        } ?: run {
            // If no song is playing, hide the player view
            binding.playerView.visibility = View.GONE
        }
    }


    // For Sections
    fun setupSection(
        id: String, mainLayout: RelativeLayout, titleView: TextView, recyclerView: RecyclerView
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

    fun topPicksSection(
        id: String, mainLayout: RelativeLayout, titleView: TextView, recyclerView: RecyclerView
    ) {
        FirebaseFirestore.getInstance().collection("sections")
            .document(id)
            .get().addOnSuccessListener {
                val section = it.toObject(CategoryModel::class.java)
                section?.apply {
                    mainLayout.visibility = View.VISIBLE
                    titleView.text = name
                    recyclerView.layoutManager =
                        LinearLayoutManager(this@Home, LinearLayoutManager.VERTICAL, false)
                    recyclerView.adapter = VerticleAudioListAdapter(audio)
                    mainLayout.setOnClickListener {
                        AudioListActivity.category = section
                        startActivity(Intent(this@Home, AudioListActivity::class.java))
                    }
                }
            }
    }


    fun setupMostlyPlayed(
        id: String, mainLayout: RelativeLayout, titleView: TextView, recyclerView: RecyclerView
    ) {
        // Get an instance of Firestore
        val firestore = FirebaseFirestore.getInstance()

        // Access the "sections" collection and get the document with the provided ID
        firestore.collection("sections")
            .document(id)
            .get()
            .addOnSuccessListener { sectionSnapshot ->
                // If section document retrieval is successful, proceed to get most played songs
                firestore.collection("audio")
                    .orderBy("count", Query.Direction.DESCENDING)
                    .limit(5)
                    .get()
                    .addOnSuccessListener { audioListSnapshot ->
                        // Convert Firestore documents to AudioModel objects
                        val audioModelList = audioListSnapshot.toObjects(AudioModel::class.java)
                        // Get the list of audio IDs
                        val audioIdList = audioListSnapshot.map { it.id }

                        // Convert the section document to a CategoryModel object
                        val section = sectionSnapshot.toObject(CategoryModel::class.java)
                        section?.apply {
                            // If section is not null, set the views and adapters
                            mainLayout.visibility = View.VISIBLE
                            titleView.text = name
                            recyclerView.layoutManager = LinearLayoutManager(
                                recyclerView.context, // Use recyclerView context here
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                            recyclerView.adapter = SectionAudioListAdapter(audioIdList)

                            // Set up a click listener for the main layout
                            mainLayout.setOnClickListener {
                                AudioListActivity.category = section
                                recyclerView.context.startActivity(
                                    Intent(
                                        recyclerView.context,
                                        AudioListActivity::class.java
                                    )
                                )
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Handle error if fetching audio documents fails
                        Log.e("FirestoreError", "Error fetching audio documents", exception)
                    }
            }
            .addOnFailureListener { exception ->
                // Handle error if fetching section document fails
                Log.e("FirestoreError", "Error fetching section document", exception)
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
        handler.postDelayed(runnable, 4000)
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

    private fun imageSlider() {
        viewPager2 = findViewById(R.id.viewPager2)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        handler = Handler(Looper.myLooper()!!)
        val imageUrlList = ArrayList<String>()



        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("banner").get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        val coverUrl = document.getString("coverUrl")
                        if (!coverUrl.isNullOrEmpty()) {
                            imageUrlList.add(coverUrl)
                        }
                    }

                    if (imageUrlList.isNotEmpty()) {
                        adapter = ImageAdapter(imageUrlList, viewPager2)
                        viewPager2.adapter = adapter
                        viewPager2.offscreenPageLimit = 3
                        viewPager2.clipToPadding = false
                        viewPager2.clipChildren = false
                        viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

                        // Set up TabLayout indicators
                        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
                            // You can customize the tabs here if needed
                        }.attach()


                        // Set a PageChangeCallback to create an infinite loop effect
                        viewPager2.registerOnPageChangeCallback(object :
                            ViewPager2.OnPageChangeCallback() {
                            override fun onPageSelected(position: Int) {
                                super.onPageSelected(position)
                                handler.removeCallbacks(runnable)
                                handler.postDelayed(runnable, 3000) // Adjust delay as needed
                            }
                        })
                    }

                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error fetching banner data", exception)
            }
    }

    // Runnable to handle auto-scrolling
    private val runnable = Runnable {
        if (viewPager2.currentItem == adapter.itemCount - 1) {
            viewPager2.currentItem = 0
        } else {
            viewPager2.currentItem = viewPager2.currentItem + 1
        }
    }


}

