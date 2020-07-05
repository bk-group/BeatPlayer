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
import com.crrl.beatplayer.R
import com.crrl.beatplayer.databinding.FragmentLyricBinding
import com.crrl.beatplayer.extensions.inflateWithBinding
import com.crrl.beatplayer.extensions.observe
import com.crrl.beatplayer.models.MediaItemData
import com.crrl.beatplayer.repository.SongsRepository
import com.crrl.beatplayer.ui.fragments.base.BaseSongDetailFragment
import com.crrl.beatplayer.utils.AutoClearBinding
import com.crrl.beatplayer.utils.LyricsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject


class LyricFragment : BaseSongDetailFragment() {

    private var binding by AutoClearBinding<FragmentLyricBinding>(this)
    private val songsRepository by inject<SongsRepository>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflater.inflateWithBinding(R.layout.fragment_lyric, container)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
    }

    private fun init() {
        songDetailViewModel.currentData.observe(this) {
            loadLyrics(it)
        }

        binding.let {
            it.title.isSelected = true
            it.viewModel = songDetailViewModel
            it.lifecycleOwner = this
            it.executePendingBindings()
        }
    }

    private fun loadLyrics(mediaItemData: MediaItemData) {
        songDetailViewModel.updateLyrics()
        launch {
            val lyric = withContext(Dispatchers.IO) {
                LyricsHelper.getEmbeddedLyrics(songsRepository, mediaItemData)
                    ?: getString(R.string.no_lyrics)
            }
            songDetailViewModel.updateLyrics(lyric)
        }
    }
}
