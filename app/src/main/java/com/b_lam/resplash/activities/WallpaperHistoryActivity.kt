package com.b_lam.resplash.activities

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.b_lam.resplash.R
import com.b_lam.resplash.adapters.WallpaperListAdapter
import com.b_lam.resplash.data.db.Wallpaper
import com.b_lam.resplash.util.ThemeUtils
import com.b_lam.resplash.viewmodels.WallpaperListViewModel
import kotlinx.android.synthetic.main.activity_wallpaper_history.*

class WallpaperHistoryActivity : BaseActivity(), WallpaperListAdapter.OnItemClickListener {

    private var mWallpaperHistoryRecyclerView: RecyclerView? = null
    private var mWallpaperListAdapter: WallpaperListAdapter? = null

    private var mWallpaperListViewModel: WallpaperListViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallpaper_history)

        val upArrow = resources.getDrawable(R.drawable.abc_ic_ab_back_material, theme)
        upArrow.setColorFilter(ThemeUtils.getThemeAttrColor(this, R.attr.menuIconColor),
                PorterDuff.Mode.SRC_ATOP)
        setSupportActionBar(toolbar_wallpaper_history)
        supportActionBar!!.setHomeAsUpIndicator(upArrow)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle(R.string.auto_wallpaper_history)

        mWallpaperHistoryRecyclerView = findViewById(R.id.recycler_view_wallpaper_history)
        mWallpaperListAdapter = WallpaperListAdapter(arrayListOf(), this)

        mWallpaperHistoryRecyclerView!!.layoutManager = LinearLayoutManager(this)
        mWallpaperHistoryRecyclerView!!.adapter = mWallpaperListAdapter

        mWallpaperListViewModel = ViewModelProviders.of(this).get(WallpaperListViewModel::class.java)

        mWallpaperListViewModel!!.getAllWallpapers().observe(this, Observer { wallpapers ->
            mWallpaperListAdapter!!.addWallpapers(wallpapers!!)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.wallpaper_history, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.remove_all_items -> {
                deleteAllWallpaperHistory()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteAllWallpaperHistory() {
        mWallpaperListViewModel!!.deleteAllWallpapers()
    }

    private fun deleteOldWallpaperHistory() {
        mWallpaperListViewModel!!.deleteOldWallpapers()
    }

    override fun onItemClick(wallpaper: Wallpaper) {
        val intent = Intent(applicationContext, DetailActivity::class.java)
        intent.putExtra(DetailActivity.DETAIL_ACTIVITY_PHOTO_ID_KEY, wallpaper.id)
        startActivity(intent)
    }
}
