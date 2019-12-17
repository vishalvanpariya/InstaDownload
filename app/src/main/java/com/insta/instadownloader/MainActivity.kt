package com.insta.instadownloader

import android.Manifest
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import org.jsoup.Jsoup
import android.media.MediaPlayer
import android.os.*
import java.io.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {

    var mainurl:String?=null
    lateinit var dialog:ProgressDialog

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ) {
                button.isEnabled = false
                requestPermissions(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ), 200
                )
            }
        }

        dialog= ProgressDialog(this)
        dialog.setTitle("")
        dialog.setMessage("Loading...")

        button.setOnClickListener {
            Log.d("xxxx","${urlinput.text}")
            mainurl=null
            geturl()
            dialog.show()
            button.isEnabled=false
        }

        download.setOnClickListener {
            if (mainurl!=null)
                download(mainurl!!)
            else
                Toast.makeText(this,"Please load Image or Video",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode==200){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                button.isEnabled=true
            }
            else{
                Toast.makeText(this,"Sorry without permission you can not download image",Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun geturl(){
        Thread(Runnable {
            kotlin.run {
                val builder = StringBuilder()
                var u=urlinput.text.toString().trim()
                if (!urlinput.text.toString().trim().contains("instagram.com")){
                    u="https://www.instagram.com/${u}/"
                }
                try {
                    val doc = Jsoup.connect(u).get()
                    var links = doc.select("meta[property=og:video]")
                    //checking whether it is video or image
                    if (links.isEmpty()) {
                        links = doc.select("meta[property=og:image]")
                        for (link in links) {
                            builder.append(link.attr("content"))
                        }
                    } else if(links.isEmpty()){
                        var username =(urlinput.text.toString().trim().split("/").last()).split("?")[0]
                        links = doc.select("img[alt=${username}'s profile picture]")
                        for (link in links) {
                            builder.append(link.attr("content"))
                        }
                    }
                    else{
                        for (link in links) {
                            builder.append(link.attr("content"))
                        }
                    }
                } catch (e: IOException) {
                }
                mainurl = builder.toString().trim()
                if (mainurl!=null) {
                    runOnUiThread(Runnable {
                        kotlin.run {
                            button.isEnabled=true
                            download.visibility = View.VISIBLE
                            if (".jpg" in mainurl!!){
                                image.visibility=View.VISIBLE
                                videoview.visibility=View.GONE
                                Glide.with(this)
                                    .load(mainurl)
                                    .into(image)
                                if (dialog.isShowing)
                                    dialog.dismiss()
                            }
                            else{
                                videoview.visibility=View.VISIBLE
                                image.visibility=View.GONE
                                var mediaController = MediaController(this)
                                mediaController.setAnchorView(videoview)
                                videoview.setMediaController(mediaController)
                                videoview.setVideoURI(Uri.parse(mainurl))
                                videoview.requestFocus()
                                videoview.start()
                                videoview.setOnInfoListener { mediaPlayer, i, i2 ->
                                    if (i== MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START){
                                        if (dialog.isShowing)
                                            dialog.dismiss()
                                        return@setOnInfoListener true
                                    }
                                    return@setOnInfoListener false
                                }

                            }
                        }
                    })
                }
                else{
                    Toast.makeText(this,"Cant loading url,Please try again",Toast.LENGTH_SHORT).show()
                }
            }
        }).start()
    }

    fun download(url:String){
        Log.d("xxxx","${url}")
        if (".jpg" in url){
            //DownloadTask(this,image,button).execute(URL(url))
            VideoDownloadTask(this).execute(url)
        }
        else{
            VideoDownloadTask(this).execute(url)
        }
    }

    private class VideoDownloadTask(var context: Context):AsyncTask<String,String,String>(){
        override fun onPreExecute() {
        }

        override fun doInBackground(vararg p0: String?): String? {
            var localname = "${System.currentTimeMillis()}_video.mp4"
            if (".jpg" in p0[0]!!){
                localname="${System.currentTimeMillis()}_image.jpg"
            }
            downloadFile(p0[0]!!,localname)
            return null
        }

        override fun onPostExecute(result: String?) {
        }

        private fun downloadFile(fileURL: String, fileName: String) {
            var folder = File("${Environment.getExternalStorageDirectory()}/Insta Downloader")
            if (!folder.exists())
                folder.mkdir()
            val request = DownloadManager.Request(Uri.parse(fileURL))
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            request.setTitle("download")
            request.setDescription("file downloading")
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir("/Insta Downloader",fileName)
            val manager= context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
        }

    }

}
