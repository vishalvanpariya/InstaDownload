package com.insta.instadownloader

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.crashlytics.android.Crashlytics
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    lateinit var versionref:DatabaseReference
    lateinit var canuseref:DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setTitle("Downloader")
        val database = FirebaseDatabase.getInstance()
        versionref = database.getReference("version")
        canuseref = database.getReference("canuse")

        checkversion()
        canuse()
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


//        val crashButton = Button(this)
//        crashButton.text = "Crash!"
//        crashButton.setOnClickListener {
//            Crashlytics.getInstance().crash() // Force a crash
//        }
//
//        addContentView(crashButton, ViewGroup.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT))


    }

    fun checkversion(){
        versionref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                var temp = p0.value as Long
                var version=temp.toInt()
                if (!version.equals(BuildConfig.VERSION_CODE)){
                    val dialog = AlertDialog.Builder(this@MainActivity)
                    dialog.setTitle("Error")
                    dialog.setMessage("This app is outdated plase Update it")
                    dialog.setCancelable(false)
                    dialog.setPositiveButton(
                        "Ok"
                    ) { dialog, which ->
                        val appPackageName =
                            packageName // getPackageName() from Context or Activity object

                        try {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=$appPackageName")
                                )
                            )
                        } catch (anfe: ActivityNotFoundException) {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                                )
                            )
                        }
                    }
                    dialog.show()
                }

            }

        })

    }

    fun canuse(){
        canuseref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                var temp = p0.value as Long
                var version=temp.toInt()
                if (!version.equals(1)){
                    val dialog = AlertDialog.Builder(this@MainActivity)
                    dialog.setTitle("Error")
                    dialog.setMessage("Developer not allowing you to use")
                    dialog.setCancelable(false)
                    dialog.show()
                }

            }

        })
    }

    override fun onRestart() {
        super.onRestart()

        checkversion()
        canuse()

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
