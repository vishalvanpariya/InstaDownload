package com.insta.instadownloader


import android.Manifest
import android.app.AlertDialog
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Process
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.doubleclick.PublisherAdRequest
import kotlinx.android.synthetic.main.fragment_instagram.*
import kotlinx.android.synthetic.main.fragment_instagram.view.*
import org.jsoup.Jsoup
import java.io.File
import java.io.IOException
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class facebook : Fragment() {


    lateinit var v: View
    var mainurl:String?=null
    lateinit var dialog: ProgressDialog
    lateinit var urlinput: EditText
    lateinit var button: Button
    lateinit var download: Button

    var onComplete = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            Toast.makeText(context,"File Downloaded", Toast.LENGTH_SHORT).show()
        }
    }

    fun isInternetAvailable(): Boolean {
        val connectivityManager =
            activity!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).state == NetworkInfo.State.CONNECTED ||
            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).state == NetworkInfo.State.CONNECTED
        ) {
            true
        } else false
    }

    private fun CheckInternet() {
        if (!isInternetAvailable()) {
            val dialog = AlertDialog.Builder(activity)
            dialog.setTitle("Error")
            dialog.setMessage("Please Check Your Internet Connection")
            dialog.setCancelable(false)
            dialog.setPositiveButton(
                "Retry"
            ) { dialog, which ->
                dialog.dismiss()
                CheckInternet()
            }
            dialog.setNegativeButton(
                "Exit"
            ) { dialog, which ->
                dialog.dismiss()
                Process.killProcess(Process.myPid())
            }
            dialog.show()
        }
    }

    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.fragment_facebook, container, false)


        CheckInternet()
        MobileAds.initialize(activity, "ca-app-pub-3940256099942544~3347511713")
        mInterstitialAd = InterstitialAd(activity)
        mInterstitialAd!!.setAdUnitId("ca-app-pub-3940256099942544/1033173712")
        mInterstitialAd!!.loadAd(AdRequest.Builder().addTestDevice("15E26E887E54153EC09BEE1F160985B0").build())

        val adRequest = PublisherAdRequest.Builder().build()
        v.publisherAdView.loadAd(adRequest)

        button=v.findViewById<Button>(R.id.button)
        urlinput=v.findViewById<EditText>(R.id.urlinput)
        download=v.findViewById(R.id.download)
//        image=v.findViewById(R.id.image)
//        videoview=v.findViewById(R.id.videoview)

        dialog= ProgressDialog(context)
        dialog.setTitle("")
        dialog.setMessage("Loading...")

        activity!!.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))


        mInterstitialAd!!.adListener = object : AdListener() {
            override fun onAdClosed() {
                super.onAdClosed()
                if (mainurl!=null)
                    download(mainurl!!)
                else
                    Toast.makeText(context,"Please load Image or Video",Toast.LENGTH_SHORT).show()

                MobileAds.initialize(activity, "ca-app-pub-3940256099942544~3347511713")
                mInterstitialAd = InterstitialAd(activity)
                mInterstitialAd!!.adUnitId = "ca-app-pub-3940256099942544/1033173712"
                mInterstitialAd!!.loadAd(
                    AdRequest.Builder().addTestDevice(
                        "15E26E887E54153EC09BEE1F160985B0"
                    ).build()
                )
            }
        }


        button.setOnClickListener {
            mainurl=null
            geturl()
            dialog.show()
            button.isEnabled=false
        }

        download.setOnClickListener {
            if (mInterstitialAd!!.isLoaded){
                mInterstitialAd!!.show()
            }
            else{
                if (mainurl!=null)
                    download(mainurl!!)
                else
                    Toast.makeText(context,"Please load Image or Video",Toast.LENGTH_SHORT).show()
            }
        }


        return v
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
                if (urlinput.text.toString().trim().contains("instagram.com")){
                    activity!!.runOnUiThread(Runnable{
                        Toast.makeText(context,"It's Look like Instagram URL please use Facebook URL",Toast.LENGTH_SHORT).show()
                        if (dialog.isShowing)
                            dialog.dismiss()
                        button.isEnabled=true
                    })
                    return@Runnable
                }
                var title=""
                if(!URLUtil.isValidUrl(u)){
                    activity!!.runOnUiThread(Runnable {
                        Toast.makeText(context, "Plase enter Valid URL", Toast.LENGTH_SHORT).show()
                        if (dialog.isShowing)
                            dialog.dismiss()
                        button.isEnabled = true
                    })
                    return@Runnable
                }
                try {
                    val doc = Jsoup.connect(u).get()
                    var links = doc.select("meta[property=og:video]")
                    Log.d("xxxx","$links")
                    title=doc.select("title").toString().split("<title>").last().split("</title>").first().split("Instagram:").last().split(">").last()
                    //checking whether it is video or image
                    if (links.isEmpty()) {
                        links = doc.select("meta[property=og:image]")
                        for (link in links) {
                            builder.append(link.attr("content"))
                        }
                        title=doc.select("title").toString().split("<title>").last().split("</title>").first().split("Instagram:").last().split(">").last()
                    } else if(links.isEmpty()){
                        var username =(urlinput.text.toString().trim().split("/").last()).split("?")[0]
                        links = doc.select("img[alt=${username}'s profile picture]")
                        for (link in links) {
                            builder.append(link.attr("content"))
                        }
                        title=username.split("insta").first()
                    }
                    else{
                        for (link in links) {
                            builder.append(link.attr("content"))
                        }
                    }
                } catch (e: IOException) {
                }

                mainurl = builder.toString().trim()
                if (mainurl!=null && mainurl!="") {
                    activity!!.runOnUiThread(Runnable {
                        kotlin.run {
                            button.isEnabled=true
                            download.visibility = View.VISIBLE

                            var urllist= LinkedList<String>()
                            var titlelist= LinkedList<String>()
                            urllist.add(mainurl!!)
                            titlelist.add(title)
                            recycler.adapter=RecyclerAdapter(context!!,urllist,titlelist)
                            recycler.layoutManager= LinearLayoutManager(context)
                            if (dialog.isShowing)
                                dialog.dismiss()
                        }
                    })
                }
                else{
                    activity!!.runOnUiThread(Runnable{
                        Toast.makeText(context,"Cant loading url,Please try again", Toast.LENGTH_SHORT).show()
                        button.isEnabled=true
                        if (dialog.isShowing)
                            dialog.dismiss()
                    })


                }
            }
        }).start()
    }

    class RecyclerAdapter(
        private val context: Context,
        private val urllist: LinkedList<String>,
        private val title: LinkedList<String>
    ) : RecyclerView.Adapter<RecyclerAdapter.Myholder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Myholder {
            return Myholder(LayoutInflater.from(context).inflate(R.layout.rowitem, parent, false))
        }

        override fun onBindViewHolder(holder: Myholder, position: Int) {
            Glide.with(context)
                .load(urllist[position])
                .into(holder.imageView)
            holder.textView.text = title[position]
            holder.itemView.setOnClickListener {
                context.startActivity(Intent(context,imageactivity::class.java).putExtra("url",urllist[position]).putExtra("title",title[position]))
            }
        }

        override fun getItemCount(): Int {
            return urllist.size
        }

        inner class Myholder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            var imageView: ImageView
            var textView: TextView

            init {
                imageView = itemView.findViewById(R.id.image)
                textView = itemView.findViewById(R.id.title)
            }


        }

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
            if (".jpg" in fileName!!) {
                request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_PICTURES,
                    "/Insta Downloader/$fileName"
                )
            }
            else{
                request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_MOVIES,
                    "/Insta Downloader/$fileName"
                )
            }
            val manager= context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)

        }

    }

}
