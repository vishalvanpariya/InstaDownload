package com.insta.instadownloader

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.doubleclick.PublisherAdRequest
import kotlinx.android.synthetic.main.activity_image.*
import java.util.*


class imageactivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        getSupportActionBar()!!.hide()

        val testDeviceIds = Arrays.asList("15E26E887E54153EC09BEE1F160985B0")
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        MobileAds.setRequestConfiguration(configuration)


        val adRequest = PublisherAdRequest.Builder().build()
        publisherAdViewtop.loadAd(adRequest)


        var mainurl=intent.getStringExtra("url")
        var title=intent.getStringExtra("title")
        if (".jpg" in mainurl!!){
            image.visibility= View.VISIBLE
            videoview.visibility=View.GONE
            Glide.with(this)
                .load(mainurl)
                .into(image)
            publisherAdViewbottom.loadAd(adRequest)
        }
        else {
            videoview.visibility = View.VISIBLE
            image.visibility = View.GONE
            var mediaController = MediaController(this)
            mediaController.setAnchorView(videoview)
            videoview.setMediaController(mediaController)
            videoview.setVideoURI(Uri.parse(mainurl))
            videoview.requestFocus()
            videoview.start()
            videoview.setOnInfoListener { mediaPlayer, i, i2 ->
                if (i == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    return@setOnInfoListener true
                }
                return@setOnInfoListener false
            }
        }
    }
}
