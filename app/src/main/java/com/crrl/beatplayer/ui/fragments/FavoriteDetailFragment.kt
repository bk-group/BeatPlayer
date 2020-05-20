/*
 * Copyright (c) 2020. Carlos René Ramos López. All rights reserved.
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

package com.crrl.beatplayer.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.crrl.beatplayer.R
import com.crrl.beatplayer.databinding.FragmentFavoriteDetailBinding
import com.crrl.beatplayer.extensions.*
import com.crrl.beatplayer.models.MediaItemData
import com.crrl.beatplayer.models.Song
import com.crrl.beatplayer.ui.adapters.SongAdapter
import com.crrl.beatplayer.ui.fragments.base.BaseFragment
import com.crrl.beatplayer.ui.viewmodels.FavoriteViewModel
import com.crrl.beatplayer.ui.viewmodels.PlaylistViewModel
import com.crrl.beatplayer.utils.BeatConstants
import kotlinx.android.synthetic.main.layout_recyclerview.*
import org.koin.android.ext.android.inject

class FavoriteDetailFragment : BaseFragment<Song>() {

    private lateinit var binding: FragmentFavoriteDetailBinding
    private lateinit var songAdapter: SongAdapter
    private val favoriteViewModel by inject<FavoriteViewModel>()
    private val playlistViewModel by inject<PlaylistViewModel>()

    private val viewModel by inject<FavoriteViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflater.inflateWithBinding(R.layout.fragment_favorite_detail, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        postponeEnterTransition()
        super.onActivityCreated(savedInstanceState)
        init()
    }

    private fun init() {
        val id = arguments!!.getLong(BeatConstants.FAVORITE_KEY)
        binding.favorite = favoriteViewModel.getFavorite(id)

        songAdapter = SongAdapter(context, songDetailViewModel).apply {
            showHeader = true
            isAlbumDetail = true
            itemClickListener = this@FavoriteDetailFragment
        }

        viewModel.songListFavorite(id).observe(this) {
            if (it.isEmpty()) {
                favoriteViewModel.deleteFavorites(longArrayOf(id))
                safeActivity.onBackPressed()
            } else if (!songAdapter.songList.deepEquals(it)) {
                songAdapter.updateDataSet(it)
                mainViewModel.reloadQueueIds(it.toIDList(), binding.favorite!!.title)
                (view?.parent as? ViewGroup)?.doOnPreDraw {
                    startPostponedEnterTransition()
                }
            }
        }

        songDetailViewModel.lastData.observe(this) { mediaItemData ->
            val position = songAdapter.songList.indexOfFirst { it.id == mediaItemData.id } + 1
            if(settingsUtility.didStop){
                songAdapter.notifyDataSetChanged()
                settingsUtility.didStop = false
            } else songAdapter.notifyItemChanged(position)
        }

        songDetailViewModel.currentState.observe(this) {
            val mediaItemData = songDetailViewModel.currentData.value ?: MediaItemData()
            val position = songAdapter.songList.indexOfFirst { it.id == mediaItemData.id } + 1
            songAdapter.notifyItemChanged(position)
        }

        songDetailViewModel.currentData.observe(this) { mediaItemData ->
            val position = songAdapter.songList.indexOfFirst { it.id == mediaItemData.id } + 1
            songAdapter.notifyItemChanged(position)
        }

        binding.apply {
            list.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = songAdapter
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
        }

        binding.let {
            it.viewModel = viewModel
            it.lifecycleOwner = this
            it.executePendingBindings()
        }
    }

    override fun onItemClick(view: View, position: Int, item: Song) {
        val extras = getExtraBundle(songAdapter.songList.toIDList(), binding.favorite!!.title)
        mainViewModel.mediaItemClicked(item.toMediaItem(), extras)
    }

    override fun onShuffleClick(view: View) {
    }

    override fun onPlayAllClick(view: View) {
        val extras = getExtraBundle(songAdapter.songList.toIDList(), binding.favorite!!.title)
        mainViewModel.mediaItemClicked(songAdapter.songList.first().toMediaItem(), extras)
    }

    override fun onPopupMenuClick(view: View, position: Int, item: Song, itemList: List<Song>) {
        super.onPopupMenuClick(view, position, item, itemList)
        powerMenu!!.showAsAnchorRightTop(view)
        playlistViewModel.playLists().observe(this) {
            buildPlaylistMenu(it, item)
        }
    }
}