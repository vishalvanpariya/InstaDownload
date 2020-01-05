package com.insta.instadownloader


import android.Manifest
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_instagram.view.*
import org.jsoup.Jsoup
import java.io.File
import java.io.IOException
import kotlin.math.log

/**
 * A simple [Fragment] subclass.
 */
class instagram : Fragment() {

    lateinit var v: View
    var mainurl:String?=null
    lateinit var dialog: ProgressDialog
    lateinit var urlinput:EditText
    lateinit var button:Button
    lateinit var download:Button
    lateinit var image:ImageView
    lateinit var videoview:VideoView

    var onComplete = object :BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            Toast.makeText(context,"File Downloaded",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v= inflater.inflate(R.layout.fragment_instagram, container, false)

        button=v.findViewById<Button>(R.id.button)
        urlinput=v.findViewById<EditText>(R.id.urlinput)
        download=v.findViewById(R.id.download)
        image=v.findViewById(R.id.image)
        videoview=v.findViewById(R.id.videoview)

        dialog= ProgressDialog(context)
        dialog.setTitle("")
        dialog.setMessage("Loading...")

        activity!!.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))


        if (Build.VERSION.SDK_INT> Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(context!!,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context!!,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ) {
                v.button.isEnabled = false
                requestPermissions(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ), 200
                )
            }
        }


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
                Toast.makeText(context,"Please load Image or Video",Toast.LENGTH_SHORT).show()
        }

        return v
    }


    private fun scanGallery(cntx: Context, path: String) {
        try {
            MediaScannerConnection.scanFile(
                cntx,
                arrayOf(path),
                null,
                object : MediaScannerConnection.OnScanCompletedListener {
                    override fun onScanCompleted(path: String, uri: Uri) {}
                })
        } catch (e: Exception) {
            e.printStackTrace()
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
                Toast.makeText(context,"Sorry without permission you can not download image",Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun download(url:String){
        Log.d("xxxx",url)
        VideoDownloadTask(context!!).execute(url)
    }

    fun geturl(){
        Thread(Runnable {
            kotlin.run {
                val builder = StringBuilder()
                var u=urlinput.text.toString().trim()
                if (!urlinput.text.toString().trim().contains("instagram.com")){
                    if(!urlinput.text.toString().trim().contains("facebook.com")) {
                        u="https://www.instagram.com/${u}/"
                    }
                }
                try {
                    val doc = Jsoup.connect(u).get()
                    var links = doc.select("meta[property=og:video]")
                    var title = doc.select("title")
                    var titlemain=title.toString().trim().split("Instagram:")[1].replace("</title>","")
                    //checking whether it is video or image
                    if (links.isEmpty()) {
                        links = doc.select("meta[property=og:image]")
                        title = doc.select("title")
                        titlemain=title.toString().trim().split("Instagram:")[1].replace("</title>","")

                        Log.d("xxxx","$tag")
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
                    activity!!.runOnUiThread(Runnable {
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
                                var mediaController = MediaController(context)
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
                    Toast.makeText(context,"Cant loading url,Please try again", Toast.LENGTH_SHORT).show()
                }
            }
        }).start()
    }


    private class VideoDownloadTask(var context: Context): AsyncTask<String, String, String>(){
        override fun onPreExecute() {
            Toast.makeText(context,"Downloading...",Toast.LENGTH_SHORT).show()
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
            request.setTitle(fileName)
            request.setDescription("Downloading...")
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir("/Insta Downloader",fileName)
            val manager= context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)

        }

    }

}
