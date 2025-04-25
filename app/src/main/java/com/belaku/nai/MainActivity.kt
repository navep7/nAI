package com.belaku.nai

// important library for Google adMob

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.palette.graphics.Palette
import androidx.viewpager2.widget.ViewPager2
import com.belaku.nai.R
import com.belaku.nai.databinding.ActivityMainBinding
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.vertexai.type.ImagenAspectRatio
import com.google.firebase.vertexai.type.ImagenGenerationConfig
import com.google.firebase.vertexai.type.ImagenImageFormat
import com.google.firebase.vertexai.type.ImagenPersonFilterLevel
import com.google.firebase.vertexai.type.ImagenSafetyFilterLevel
import com.google.firebase.vertexai.type.ImagenSafetySettings
import com.google.firebase.vertexai.type.PublicPreviewAPI
import com.google.firebase.vertexai.vertexAI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import org.apache.commons.lang3.StringUtils
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates
import kotlin.random.Random


class MainActivity : AppCompatActivity(), CoroutineScope {


    private val prodFlag: Boolean = true
    private lateinit var pdGenerateCaptionForQ: ProgressDialog
    private lateinit var txTuts: TextView
    private lateinit var clip: ClipData
    private lateinit var linearLayoutChat: LinearLayout
    private lateinit var progressSpinner: ProgressDialog
    private lateinit var viewPager2Adapter: ViewPager2Adapter
    private lateinit var txContents: java.util.ArrayList<String>
    private var viewPager2: ViewPager2? = null
    private lateinit var bannerAdView: AdView
    private lateinit var rlMain: RelativeLayout
    private var adLoaded: Boolean = false
    private lateinit var template: TemplateView
    private lateinit var adLoader: AdLoader
    private var newPixel by Delegates.notNull<Int>()
    private lateinit var textAnimation: AlphaAnimation
    private var handler: Handler = Handler()
    private lateinit var runnable: Runnable
    private lateinit var filteredBitmap: Bitmap
    private var imageUri: Uri? = null
    private var mediaPath: String = ""
    private val CAMERA_PIC_REQUEST: Int = 0
    val items = arrayOf("a caption", "a pop/rock song suggestion", "a story untold")


    private var dragThreshold = 10
    private var downX = 0
    private var downY = 0

    companion object {
        var clr1: Int = 0
        lateinit var originalBitmap: Bitmap
    }

    private lateinit var imageView: ImageView
    private val SELECT_PICTURE: Int = 1
    private lateinit var aiViewModel: AIViewModel


    lateinit var appContext: Context;
    private lateinit var editTextPrompt: EditText
    private lateinit var fabCamera: ExtendedFloatingActionButton
    private lateinit var fabGallery: ExtendedFloatingActionButton
    private lateinit var fabShare: FloatingActionButton
    private lateinit var fabShareInsta: FloatingActionButton
    private lateinit var fabShareFB: FloatingActionButton
    private lateinit var fabShareTw: FloatingActionButton
    private var job: Job = Job()
    private lateinit var binding: ActivityMainBinding


    private var mInterstitialAd: InterstitialAd? = null
    private val TAG: String = "MainActivity"

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    @RequiresApi(35)
    @SuppressLint(
        "SecretInSource", "ServiceCast", "QueryPermissionsNeeded",
        "UseCompatLoadingForDrawables", "WrongConstant", "ClickableViewAccessibility"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        txContents = ArrayList<String>()
        //  txContents.add("")

        txTuts = findViewById<TextView>(R.id.tx_tuts)
        txTuts.setMovementMethod(ScrollingMovementMethod())

        viewPager2 = findViewById(R.id.viewpager)



        viewPager2?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> {
                    downX = event.rawX.toInt()
                    downY = event.rawY.toInt()
                    false
                }

                MotionEvent.ACTION_MOVE -> {
                    val distanceX = Math.abs(event.rawX.toInt() - downX)
                    val distanceY = Math.abs(event.rawY.toInt() - downY)


                    if (distanceY > distanceX && distanceY > dragThreshold) {
                        v.parent.requestDisallowInterceptTouchEvent(false)
                        true
                    } else {
                        v.parent.requestDisallowInterceptTouchEvent(true)
                        false
                    }
                }

                else -> {
                    v.parent.requestDisallowInterceptTouchEvent(true)
                    false
                }
            }
        }



        viewPager2?.bringToFront()
        viewPager2Adapter = ViewPager2Adapter(this, txContents)
        viewPager2?.setAdapter(viewPager2Adapter);
        viewPager2?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            // This method is triggered when there is any scrolling activity for the current page
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            // triggered when you select a new page
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
            }

            // triggered when there is
            // scroll state will be changed
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })


        appContext = applicationContext

        aiViewModel = AIViewModel()

        template = findViewById(R.id.nativeTemplateView)
        rlMain = findViewById(R.id.rl_main)

        // Initializing the Google Admob SDK
        MobileAds.initialize(
            this
        ) { initializationStatus -> //Showing a simple Toast Message to the user when The Google AdMob Sdk Initialization is Completed
            //   Toast.makeText( this@MainActivity, "AdMob Sdk Initialize $initializationStatus", Toast.LENGTH_LONG ).show()
        }


//Initializing the AdLoader   objects

        //Initializing the AdLoader   objects
        adLoader = AdLoader.Builder(this, resources.getString(R.string.native_adid))
            .forNativeAd { nativeAd ->
                var background: ColorDrawable = ColorDrawable()
                val styles =
                    NativeTemplateStyle.Builder().withMainBackgroundColor(background).build()

                template.visibility = View.VISIBLE
                template.setStyles(styles)
                template.setNativeAd(nativeAd)
                adLoaded = true
                // Showing a simple Toast message to user when Native an ad is Loaded and ready to show
                //    Toast.makeText(this@MainActivity, "Native Ad is loaded, now you can show the native ad", Toast.LENGTH_LONG).show()
            }.build()




        imageView = findViewById(R.id.image_view)
        //     txDesc = findViewById(R.id.tx_description)
        editTextPrompt = findViewById(R.id.edtx_prompt)

        imageView.setImageDrawable(resources.getDrawable(R.drawable.ai_wall))
        val colorPalette: Palette =
            Palette.from((imageView.drawable as BitmapDrawable).bitmap).generate()
        clr1 = colorPalette.getLightVibrantColor(Color.WHITE)


        //   txDesc.setMovementMethod(ScrollingMovementMethod());F
        editTextPrompt.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                originalBitmap = (imageView.drawable as BitmapDrawable).bitmap
                GenerateCaptionForQ(editTextPrompt.text.toString())
                handled = true
            }
            handled
        })
        editTextPrompt.onDrawableEndClick {
            // TODO clear action
            editTextPrompt.setText("")
            //       txDesc.setText("")
        }
        editTextPrompt.bringToFront()
        editTextPrompt.setFocusableInTouchMode(true);
        editTextPrompt.requestFocus();
        editTextPrompt.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(view: View?, keyCode: Int, keyevent: KeyEvent): Boolean {
                //If the keyevent is a key-down event on the "enter" button
                if ((keyevent.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    //...
                    // Perform your action on key press here
                    makeToast("H#R#")
                    // ...
                    return true
                }
                return false
            }
        })
        fabShare = findViewById(R.id.fab_share)
        fabShareInsta = findViewById(R.id.fab_share_insta)
        //   fabShareInsta.setImageBitmap(textAsBitmap("Insta", 40F, Color.WHITE));
        fabShareFB = findViewById(R.id.fab_share_fb)
        //   fabShareFB.setImageBitmap(textAsBitmap("FB", 40F, Color.WHITE));
        fabShareTw = findViewById(R.id.fab_share_tw)
        //   fabShareTw.setImageBitmap(textAsBitmap("Tweet", 40F, Color.WHITE));
        fabGallery = findViewById(R.id.fab_gallery)
        fabCamera = findViewById(R.id.fab_camera)
        fabShare = findViewById(R.id.fab_share)

        textAnimation = AlphaAnimation(0.0f, 1.0f)
        textAnimation.duration = 50 //You can manage the blinking time with this parameter
        textAnimation.startOffset = 20
        textAnimation.repeatMode = Animation.REVERSE
        textAnimation.repeatCount = Animation.INFINITE

        val imageAnimation = RotateAnimation(0f, 350f, 15f, 15f)
        imageAnimation.interpolator = LinearInterpolator()
        imageAnimation.repeatCount = Animation.INFINITE
        imageAnimation.duration = 700





        fabShareFB.setOnClickListener(View.OnClickListener {
            val share = Intent(Intent.ACTION_SEND)

            share.setType("image/*")
            //   share.putExtra(Intent.EXTRA_TEXT, txDesc.text)
            share.putExtra(Intent.EXTRA_STREAM, imageUri);
            share.setPackage("com.facebook.katana") //Facebook App package
            startActivity(Intent.createChooser(share, "Title of the dialog the system will open"))
        })

        fabShareTw.setOnClickListener(View.OnClickListener {

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                //         putExtra(Intent.EXTRA_TEXT, txDesc.text)
            }
            intent.setPackage("com.twitter.android")
            startActivity(intent)
        })
        fabShare.setOnClickListener(View.OnClickListener {

            if (fabShareInsta.isVisible) {
                fabShareInsta.visibility = View.INVISIBLE
                fabShareFB.visibility = View.INVISIBLE
                fabShareTw.visibility = View.INVISIBLE
            } else {

                val clipboard: ClipboardManager =
                    getSystemService(CLIPBOARD_SERVICE) as ClipboardManager



                clip = ClipData.newPlainText("label", txContents[viewPager2?.currentItem!!])
                clip?.let { it1 -> clipboard.setPrimaryClip(it1) }

                fabShareInsta.visibility = View.VISIBLE
                fabShareFB.visibility = View.VISIBLE
                fabShareTw.visibility = View.VISIBLE
            }
        })

        fabShareInsta.setOnClickListener(View.OnClickListener {

            val i = Intent(Intent.ACTION_SEND)
            i.setType("image/*")
            i.putExtra(Intent.EXTRA_STREAM, imageUri)
            i.setPackage("com.instagram.android")
            startActivity(i)

        })

        fabGallery.setOnClickListener(View.OnClickListener {
            txContents.clear()
            val i = Intent()
            i.setType("image/*")
            i.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE)
        })

        fabCamera.setOnClickListener(View.OnClickListener {

            txContents.clear()
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                println("Permissions Denied")
                requestPermission()
                println("Passed Command")
            } else {
                println("PERMISSIONS GRANTED")
                cameraOps()
            }


        })

        setSupportActionBar(binding.toolbar)

    }

    private fun GenerateCaptionForQ(prompt: String) {


        pdGenerateCaptionForQ = ProgressDialog(this@MainActivity)
        pdGenerateCaptionForQ.setMessage("loading")
        pdGenerateCaptionForQ.show()

        showNativeAd()
        showBannerAd()
        showInterstitialAd()

        aiViewModel.sendPromptQuestion(prompt)

        var org: String
        var desc: String
        handler.postDelayed(java.lang.Runnable {
            handler.postDelayed(runnable, 1000)
            //        txDesc.append(" .")
            org = aiViewModel.uiState.value.toString();
            if (org.startsWith("Success")) {
                //      txDesc.clearAnimation()
                handler.removeCallbacks(runnable)
                desc = org.substring(org.lastIndexOf(":\n") + 1)
                if (desc.contains("outputText"))
                    desc = StringUtils.substringBetween(
                        desc,
                        "outputText=",
                        desc.get(desc.length - 1).toString()
                    )
                txTuts.setMovementMethod(ScrollingMovementMethod())
                txTuts.text = desc
                pdGenerateCaptionForQ.dismiss()
            }
        }.also { runnable = it }, 1)


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    @SuppressLint("WrongConstant")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add ->         //add the function to perform here
            {
                val dialog = Dialog(this@MainActivity)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCancelable(false)
                dialog.setContentView(R.layout.custom_chat_dialog)

                linearLayoutChat = dialog.findViewById(R.id.ll_chat) as LinearLayout
                val edtxChat = dialog.findViewById(R.id.edtx_chat) as EditText
                val btnSend = dialog.findViewById(R.id.btn_chat_send) as ImageButton
                val btnClose = dialog.findViewById(R.id.btn_close) as Button

                btnClose.setOnClickListener {
                    dialog.dismiss()
                }

                btnSend.setOnClickListener {
                    if (edtxChat.text.length > 0) {
                        var tx = TextView(appContext)
                        tx.text = edtxChat.text
                        tx.background = resources.getDrawable(android.R.drawable.alert_light_frame)
                        linearLayoutChat.addView(tx)
                        val imm =
                            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
                        getGeminiChatResponse(tx.text.toString())
                    }
                }
                dialog.show()
            }
        }
        return (super.onOptionsItemSelected(item))
    }

    private fun getGeminiChatResponse(str: String): String {

        aiViewModel.sendPromptQuestion(str)

        //    txContents.add("AI - " + prompt)
        viewPager2Adapter.notifyDataSetChanged()

        viewPager2!!.visibility = View.VISIBLE
        viewPager2!!.bringToFront()

        var org: String
        var desc = "abc"
        handler.postDelayed(java.lang.Runnable {
            handler.postDelayed(runnable, 1000)
            //        txDesc.append(" .")
            org = aiViewModel.uiState.value.toString();

            if (org.startsWith("Success")) {
                //      txDesc.clearAnimation()
                handler.removeCallbacks(runnable)
                desc = org.substring(org.lastIndexOf(":\n") + 1)
                if (desc.contains("outputText"))
                    desc = StringUtils.substringBetween(
                        desc,
                        "outputText=",
                        desc.get(desc.length - 1).toString()
                    )
                //   setText(0, desc.strip())
                var txR = TextView(appContext)
                txR.text = desc.strip()
                txR.setTextColor(resources.getColor(R.color.white))
                txR.background = resources.getDrawable(android.R.drawable.alert_dark_frame)
                linearLayoutChat.addView(txR)

            }
        }.also { runnable = it }, 1)

        return desc.strip()

    }

    fun EditText.onDrawableEndClick(action: () -> Unit) {
        setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                v as EditText
                val end =
                    if (v.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL)
                        v.left else v.right
                if (event.rawX >= (end - v.compoundPaddingEnd)) {
                    action.invoke()
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }
    }

    private fun showBannerAd() {
        if (prodFlag) {
            bannerAdView = findViewById(R.id.banner_adview)
            val adRequest = AdRequest.Builder().build()
            bannerAdView.loadAd(adRequest)
            bannerAdView.visibility = View.VISIBLE
            bannerAdView.bringToFront()

        }
    }

    // Get the ad size with screen width.
    fun getAdSize(): AdSize {
        val displayMetrics = resources.displayMetrics
        var adWidthPixels = displayMetrics.widthPixels

        if (VERSION.SDK_INT >= VERSION_CODES.R) {
            val windowMetrics = this.windowManager.currentWindowMetrics
            adWidthPixels = windowMetrics.bounds.width()
        }

        val density = displayMetrics.density
        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
    }


    private fun showInterstitialAd() {
        if (prodFlag) {

            val adRequest = AdRequest.Builder().build()

            InterstitialAd.load(
                this, resources.getString(R.string.intr_adid), adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd
                        makeToast("onAdLoaded")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        // Handle the error
                        makeToast("Fld - " + loadAdError.toString())
                        mInterstitialAd = null
                    }
                })

            if (Random.nextInt() % 2 == 0)
                if (mInterstitialAd != null) {
                    mInterstitialAd?.show(this)
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.")
                }

        }
    }

    private fun loadNativeAd() {
        // Creating  an Ad Request
        val adRequest: AdRequest = AdRequest.Builder().build()

        // load Native Ad with the Request
        adLoader.loadAd(adRequest)

        // Showing a simple Toast message to user when Native an ad is Loading
        //   Toast.makeText(this@MainActivity, "Native Ad is loading ", Toast.LENGTH_LONG).show()
    }

    private fun showNativeAd() {
        if (prodFlag)
            if (adLoaded) {
                template.visibility = View.VISIBLE
                // Showing a simple Toast message to user when an Native ad is shown to the user
                 Toast.makeText(this@MainActivity, "Native Ad  is loaded and Now showing ad  ", Toast.LENGTH_LONG).show()
            } else {
                //Load the Native ad if it is not loaded
                loadNativeAd()

                // Showing a simple Toast message to user when Native ad is not loaded
                   Toast.makeText(this@MainActivity, "Native Ad is not Loaded ", Toast.LENGTH_LONG).show()
            }
    }

    @RequiresApi(35)
    private fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            )
        ) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PIC_REQUEST)
        } else {
            // Eh, prompt anyway
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PIC_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PIC_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                //       Toast.makeText(this, "ok", Toast.LENGTH_SHORT).show()
                cameraOps()
            } else {
                //     Toast.makeText(this, "not ok", Toast.LENGTH_SHORT).show()
                // Permission denied by the user.
            }
        }
    }

    private fun cameraOps() {
        try {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST)
        } catch (e: Exception) {
            makeToast("Couldn't load photo - " + e.toString())
        }
    }

    fun textAsBitmap(text: String?, textSize: Float, textColor: Int): Bitmap {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textSize = textSize
        paint.color = textColor
        paint.textAlign = Paint.Align.LEFT
        val baseline = -paint.ascent() // ascent() is negative
        val width = (paint.measureText(text) + 0.0f).toInt() // round
        val height = (baseline + paint.descent() + 0.0f).toInt()
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(image)
        canvas.drawText(text!!, 0f, baseline, paint)
        return image
    }

    private fun applyFilterAsync(oBitmap: Bitmap, strFilter: String) {
        Thread {
            filteredBitmap = oBitmap.copy(Bitmap.Config.ARGB_8888, true)
            val imageHeight = filteredBitmap.height
            val imageWidth = filteredBitmap.width

            for (i in 0 until imageWidth) {
                for (j in 0 until imageHeight) {
                    // getting each pixel

                    val oldPixel: Int = oBitmap.getPixel(i, j)

                    // each pixel is made from RED_BLUE_GREEN_ALPHA
                    // so, getting current values of pixel
                    val oldRed = Color.red(oldPixel)
                    val oldBlue = Color.blue(oldPixel)
                    val oldGreen = Color.green(oldPixel)
                    val oldAlpha = Color.alpha(oldPixel)

                    // write your Algorithm for getting new values
                    // after calculation of filter
                    val intensity = (oldRed + oldBlue + oldGreen) / 3

                    if (strFilter.equals("grayScale")) {
                        val newRed = intensity
                        val newBlue = intensity
                        val newGreen = intensity
                        // applying new pixel values from above to newBitmap
                        newPixel = Color.argb(oldAlpha, newRed, newGreen, newBlue)
                        //      main.setImageBitmap(filteredBitmap)
                    } else if (strFilter.equals("sepia")) {
                        val newRed = (0.393 * oldRed + 0.769 * oldGreen + 0.189 * oldBlue).toInt()
                        val newGreen = (0.349 * oldRed + 0.686 * oldGreen + 0.168 * oldBlue).toInt()
                        val newBlue = (0.272 * oldRed + 0.534 * oldGreen + 0.131 * oldBlue).toInt()
                        newPixel = Color.argb(oldAlpha, newRed, newGreen, newBlue)
                        //     main.setImageBitmap(filteredBitmap)
                    } else if (strFilter.equals("customSepia")) {
                        var newRed = (0.393 * oldRed + 0.769 * oldGreen + 0.189 * oldBlue).toInt()
                        var newGreen = (0.349 * oldRed + 0.686 * oldGreen + 0.168 * oldBlue).toInt()
                        var newBlue = (0.272 * oldRed + 0.534 * oldGreen + 0.131 * oldBlue).toInt()

                        newRed = if (newRed > 255) 255 else newRed
                        newGreen = if (newGreen > 255) 255 else newGreen
                        newBlue = if (newBlue > 255) 255 else newBlue

                        // applying new pixel value to newBitmap
                        // condition for Diagonals ,setting GREY values to particular pixel comes in this range only
                        newPixel = 0
                        newPixel = if (i < j - imageHeight / 2) {
                            // apply sepia at lower
                            Color.argb(oldAlpha, newRed, newGreen, newBlue)
                        } else if ((i - (imageHeight / 2)) > j) {
                            // apply sepia upper
                            Color.argb(oldAlpha, newRed, newGreen, newBlue)
                        } else {
                            //  don't apply sepia
                            oldPixel
                        }
                        //    main.setImageBitmap(filteredBitmap)
                    } else if (strFilter.equals("pixelate")) {

                    }
                    filteredBitmap.setPixel(i, j, newPixel)
                }

            }
            // Update the UI on the main thread
            runOnUiThread { imageView.setImageBitmap(filteredBitmap) }
        }.start()
    }

    private fun filtering(strFilter: String) {

        // copying to newBitmap for manipulation
        filteredBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)

        // height and width of Image
        val imageHeight = filteredBitmap.height
        val imageWidth = filteredBitmap.width

        // traversing each pixel in Image as an 2D Array
        for (i in 0 until imageWidth) {
            for (j in 0 until imageHeight) {
                // getting each pixel

                val oldPixel: Int = originalBitmap.getPixel(i, j)

                // each pixel is made from RED_BLUE_GREEN_ALPHA
                // so, getting current values of pixel
                val oldRed = Color.red(oldPixel)
                val oldBlue = Color.blue(oldPixel)
                val oldGreen = Color.green(oldPixel)
                val oldAlpha = Color.alpha(oldPixel)

                // write your Algorithm for getting new values
                // after calculation of filter
                val intensity = (oldRed + oldBlue + oldGreen) / 3

                if (strFilter.equals("grayScale")) {
                    val newRed = intensity
                    val newBlue = intensity
                    val newGreen = intensity
                    // applying new pixel values from above to newBitmap
                    val newPixel = Color.argb(oldAlpha, newRed, newGreen, newBlue)
                    filteredBitmap.setPixel(i, j, newPixel)
                    imageView.setImageBitmap(filteredBitmap)
                } else if (strFilter.equals("sepia")) {
                    val newRed = (0.393 * oldRed + 0.769 * oldGreen + 0.189 * oldBlue).toInt()
                    val newGreen = (0.349 * oldRed + 0.686 * oldGreen + 0.168 * oldBlue).toInt()
                    val newBlue = (0.272 * oldRed + 0.534 * oldGreen + 0.131 * oldBlue).toInt()
                    val newPixel = Color.argb(oldAlpha, newRed, newGreen, newBlue)
                    filteredBitmap.setPixel(i, j, newPixel)
                    imageView.setImageBitmap(filteredBitmap)
                } else if (strFilter.equals("customSepia")) {
                    var newRed = (0.393 * oldRed + 0.769 * oldGreen + 0.189 * oldBlue).toInt()
                    var newGreen = (0.349 * oldRed + 0.686 * oldGreen + 0.168 * oldBlue).toInt()
                    var newBlue = (0.272 * oldRed + 0.534 * oldGreen + 0.131 * oldBlue).toInt()

                    newRed = if (newRed > 255) 255 else newRed
                    newGreen = if (newGreen > 255) 255 else newGreen
                    newBlue = if (newBlue > 255) 255 else newBlue

                    // applying new pixel value to newBitmap
                    // condition for Diagonals ,setting GREY values to particular pixel comes in this range only
                    var newPixel = 0
                    newPixel = if (i < j - imageHeight / 2) {
                        // apply sepia at lower
                        Color.argb(oldAlpha, newRed, newGreen, newBlue)
                    } else if ((i - (imageWidth / 2)) > j) {
                        // apply sepia upper
                        Color.argb(oldAlpha, newRed, newGreen, newBlue)
                    } else {
                        //  don't apply sepia
                        oldPixel
                    }

                    filteredBitmap.setPixel(i, j, newPixel)
                    imageView.setImageBitmap(filteredBitmap)
                } else if (strFilter.equals("pixelate")) {

                }
            }

        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            editTextPrompt.visibility = View.INVISIBLE
            //       txDesc.bringToFront()
            //        spinnerPrompts.bringToFront()
            imageView.visibility = View.VISIBLE
            fabShare.visibility = View.VISIBLE
            editTextPrompt.visibility = View.INVISIBLE

            if (requestCode == CAMERA_PIC_REQUEST) {
                originalBitmap = (data!!.extras!!["data"] as Bitmap?)!!
                imageUri = getImageUri(appContext, originalBitmap)
                imageView.setImageBitmap(originalBitmap)
                viewPager2?.bringToFront()
            } else {
                imageUri = data?.data
                mediaPath =
                    getPath(getApplicationContext(), imageUri)

                originalBitmap = imageUri?.let { uriToBitmap(it) }!!
                imageView.setImageBitmap(originalBitmap)
            }

            val colorPalette: Palette = Palette.from(originalBitmap).generate()
            clr1 = colorPalette.getLightVibrantColor(Color.WHITE)

            GenerateCaption(originalBitmap, items)

        }
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    fun getPath(context: Context, uri: Uri?): String {
        var result: String? = null
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri!!, proj, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(proj[0])
                result = cursor.getString(column_index)
            }
            cursor.close()
        }
        if (result == null) {
            result = "Not found"
        }
        return result
    }

    @RequiresApi(35)
    @OptIn(PublicPreviewAPI::class)
    private suspend fun GenerateImage(prompt: String) {
        val imagenModel = Firebase.vertexAI.imagenModel(
            modelName = "imagen-3.0-generate-001",
            generationConfig = ImagenGenerationConfig(
                negativePrompt = "frogs",
                numberOfImages = 2,
                aspectRatio = ImagenAspectRatio.LANDSCAPE_16x9,
                imageFormat = ImagenImageFormat.jpeg(compressionQuality = 100),
                addWatermark = false
            ),
            safetySettings = ImagenSafetySettings(
                safetyFilterLevel = ImagenSafetyFilterLevel.BLOCK_LOW_AND_ABOVE,
                personFilterLevel = ImagenPersonFilterLevel.BLOCK_ALL
            )
        )

        val imageResponse = imagenModel.generateImages(
            prompt = "An astronaut riding a horse",
        )
        val image = imageResponse.images.first
        val bitmapImage = image.asBitmap()

        findViewById<ImageView>(R.id.imgv_g).setImageBitmap(bitmapImage)
    }


    private fun GenerateCaption(originalBitmap: Bitmap, prompts: Array<String>) {

        showNativeAd()
        showBannerAd()
        showInterstitialAd()

        progressSpinner = ProgressDialog(this@MainActivity)
        progressSpinner.setCancelable(false)
        progressSpinner.setCanceledOnTouchOutside(false);
        progressSpinner.setMessage("Phrasing a Caption...")
        progressSpinner.show()

        aiViewModel.sendPrompt(originalBitmap, prompts[0])

        //    txContents.add("AI - " + prompt)
        viewPager2Adapter.notifyDataSetChanged()

        viewPager2!!.visibility = View.VISIBLE
        viewPager2!!.bringToFront()

        var org: String
        var desc: String
        handler.postDelayed(java.lang.Runnable {
            handler.postDelayed(runnable, 1000)
            //        txDesc.append(" .")
            org = aiViewModel.uiState.value.toString();
            if (org.startsWith("Success")) {
                //          txDesc.clearAnimation()
                handler.removeCallbacks(runnable)
                desc = org.substring(org.lastIndexOf(":\n") + 1)
                if (desc.contains("outputText"))
                    desc = StringUtils.substringBetween(
                        desc,
                        "outputText=",
                        desc.get(desc.length - 1).toString()
                    )
                setText(1, desc.strip())


                aiViewModel.sendPrompt(originalBitmap, prompts[1])
                progressSpinner.dismiss()
                progressSpinner.setMessage("Penning a Song...")
                progressSpinner.show()

                var org: String
                var desc: String
                handler.postDelayed(java.lang.Runnable {
                    handler.postDelayed(runnable, 1000)
                    //        txDesc.append(" .")
                    org = aiViewModel.uiState.value.toString();
                    if (org.startsWith("Success")) {
                        //          txDesc.clearAnimation()
                        handler.removeCallbacks(runnable)
                        desc = org.substring(org.lastIndexOf(":\n") + 1)
                        if (desc.contains("outputText"))
                            desc = StringUtils.substringBetween(
                                desc,
                                "outputText=",
                                desc.get(desc.length - 1).toString()
                            )
                        setText(2, desc.strip())

                        aiViewModel.sendPrompt(originalBitmap, prompts[2])
                        progressSpinner.dismiss()
                        progressSpinner.setMessage("Scripting a Story...")
                        progressSpinner.show()

                        var org: String
                        var desc: String
                        handler.postDelayed(java.lang.Runnable {
                            handler.postDelayed(runnable, 1000)
                            //        txDesc.append(" .")
                            org = aiViewModel.uiState.value.toString();
                            if (org.startsWith("Success")) {
                                //          txDesc.clearAnimation()
                                handler.removeCallbacks(runnable)
                                desc = org.substring(org.lastIndexOf(":\n") + 1)
                                if (desc.contains("outputText"))
                                    desc = StringUtils.substringBetween(
                                        desc,
                                        "outputText=",
                                        desc.get(desc.length - 1).toString()
                                    )
                                setText(3, desc.strip())
                                progressSpinner.dismiss()

                            }
                        }.also { runnable = it }, 1)

                    }
                }.also { runnable = it }, 1)

            }
        }.also { runnable = it }, 1)


    }

    fun setText(s1: Int, s: String) {

        var str = s.replace("Option", "")

        val colorPalette: Palette = Palette.from(originalBitmap).generate()
        clr1 = colorPalette.getLightVibrantColor(Color.WHITE)


        if (s1 == 0)
            txContents.add("...\n" + str)
        else if (s1 == 1)
            txContents.add("...a Caption \n\n" + str)
        else if (s1 == 2)
            txContents.add("...a Song \n\n" + str)
        else if (s1 == 3)
            txContents.add("...a Story \n\n" + str)
        viewPager2Adapter.notifyDataSetChanged()
        //     txDesc.append("\n\n" + s)


    }

    private fun makeToast(str: String) {
        Toast.makeText(appContext, str, Toast.LENGTH_LONG).show()
    }

    private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
        try {
            val parcelFileDescriptor =
                contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)

            parcelFileDescriptor!!.close()
            return image
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }


}