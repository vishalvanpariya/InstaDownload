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
import android.media.MediaScannerConnection
import android.os.*
import java.io.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        viewpager.adapter=PagerAdapter(supportFragmentManager)
        tablayout.setupWithViewPager(viewpager)
        tablayout.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabReselected(p0: TabLayout.Tab?) {

            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {

            }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                viewpager.setCurrentItem(p0!!.position)
            }

        })

    }

    class PagerAdapter(fm:FragmentManager) : FragmentPagerAdapter(fm) {

        var data = arrayOf("Instagram","Facebook")

        override fun getItem(position: Int): Fragment {
            if (position==0){
                return instagram()
            }
            else{
                return facebook()
            }
        }

        override fun getCount(): Int {
            return data.size
        }


        override fun getPageTitle(position: Int): CharSequence? {
            return data[position]
        }

    }
}
