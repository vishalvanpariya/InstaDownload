package com.insta.instadownloader

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.Handler
import android.os.Process
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setTitle("Downloader")


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
