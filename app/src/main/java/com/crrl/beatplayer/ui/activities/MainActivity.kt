/*
 * Copyright 2019 Carlos René Ramos López. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.crrl.beatplayer.ui.activities

import android.content.ContentUris
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.crrl.beatplayer.R
import com.crrl.beatplayer.databinding.ActivityMainBinding
import com.crrl.beatplayer.extensions.addFragment
import com.crrl.beatplayer.extensions.observe
import com.crrl.beatplayer.extensions.replaceFragment
import com.crrl.beatplayer.extensions.toast
import com.crrl.beatplayer.models.Song
import com.crrl.beatplayer.playback.MusicService
import com.crrl.beatplayer.ui.activities.base.BaseActivity
import com.crrl.beatplayer.ui.fragments.*
import com.crrl.beatplayer.ui.viewmodels.MainViewModel
import com.crrl.beatplayer.utils.PlayerConstants
import com.github.florent37.kotlin.pleaseanimate.please
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class MainActivity : BaseActivity() {

    val viewModel: MainViewModel by viewModel { parametersOf(this) }
    private lateinit var binding: ActivityMainBinding
    private var placeholder: Drawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel.getCurrentSong().observe(this) {
            updateView(it)
        }
        viewModel.getCurrentSongList().observe(this) {
            viewModel.musicService.updateData(it)
        }
        if (savedInstanceState == null) {
            replaceFragment(
                R.id.nav_host_fragment,
                LibraryFragment(),
                PlayerConstants.LIBRARY
            )
            update(Song())
        }
    }

    private fun updateView(song: Song) {
        binding.let {
            it.viewModel = viewModel
        }
    }

    fun isPermissionsGranted(): Boolean {
        return permissionsGranted
    }

    override fun onResume() {
        super.onResume()
        showMiniPlayer()
    }

    fun onSongInfoClick(v: View) {
        addFragment(
            R.id.nav_host_fragment,
            SongDetailFragment(),
            PlayerConstants.NOW_PLAYING,
            true
        )
    }

    override fun onBackPressed() {
        var isDismiss = true
        supportFragmentManager.fragments.forEach {
            isDismiss = when (it) {
                is AlbumDetailFragment -> it.onBackPressed()
                is ArtistDetailFragment -> it.onBackPressed()
                is PlaylistDetailFragment -> it.onBackPressed()
                is FolderDetailFragment -> it.onBackPressed()
                else -> true
            }
        }
        if (isDismiss) super.onBackPressed()
    }

    fun hideMiniPlayer() {
        if (bottom_controls != null) {
            bottom_controls.isEnabled = false
            please(100) {
                animate(bottom_controls) {
                    belowOf(main_container)
                }
            }.start()
        }
    }

    fun showMiniPlayer() {
        if (bottom_controls != null) {
            bottom_controls.isEnabled = true
            please(100) {
                animate(bottom_controls) {
                    bottomOfItsParent()
                }
            }.start()
        }
    }

    fun update(song: Song) {
        viewModel.update(song)
    }

    fun update(newList: List<Song>) {
        viewModel.update(newList)
    }
}
