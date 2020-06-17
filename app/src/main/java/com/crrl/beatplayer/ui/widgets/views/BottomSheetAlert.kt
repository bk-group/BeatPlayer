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

package com.crrl.beatplayer.ui.widgets.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import com.crrl.beatplayer.R
import com.crrl.beatplayer.interfaces.ItemListener
import com.crrl.beatplayer.ui.widgets.actions.AlertItemAction
import com.crrl.beatplayer.ui.widgets.stylers.AlertItemStyle
import com.crrl.beatplayer.ui.widgets.stylers.AlertItemTheme
import com.crrl.beatplayer.ui.widgets.stylers.ItemStyle
import com.crrl.beatplayer.utils.GeneralUtils.drawRoundRectShape
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.parent_dialog_layout.view.*

class BottomSheetAlert(
    private val title: String,
    private val message: String,
    private val actions: ArrayList<AlertItemAction>,
    private val style: ItemStyle
) : BottomSheetDialogFragment(), ItemListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetAlertTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate base view
        val view = inflater.inflate(R.layout.parent_dialog_layout, container, false)

        // Set up view
        initView(view)

        return view
    }

    private fun initView(view: View) {
        style as AlertItemStyle
        with(view) {
            title.apply {
                if (this@BottomSheetAlert.title.isEmpty()) {
                    visibility = GONE
                } else {
                    text = this@BottomSheetAlert.title
                }
                setTextColor(style.textColor)
            }

            sub_title.apply {
                if (message.isEmpty()) {
                    visibility = GONE
                } else {
                    text = message
                }
                setTextColor(style.textColor)
            }

            // Configuring View Parent
            val background = drawRoundRectShape(
                container.layoutParams.width,
                container.layoutParams.height,
                style.backgroundColor,
                style.cornerRadius
            )

            container.background = background
            bottom_container.visibility = GONE
        }

        // Inflate action views
        inflateActionsView(view.findViewById(R.id.item_container), actions)
    }

    private fun inflateActionsView(actionsLayout: LinearLayout, items: ArrayList<AlertItemAction>) {
        style as AlertItemStyle
        for (item in items) {

            // Finding Views
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_item, null)
            val action = view.findViewById<Button>(R.id.action)
            val indicator = view.findViewById<View>(R.id.indicator)

            action.apply {
                text = item.title
                if (items.indexOf(item) == items.size - 1)
                    setBackgroundResource(R.drawable.list_item_ripple_bottom)
            }

            // Click listener for action.
            action.setOnClickListener {
                dismiss()

                //Store selectedState
                val oldState = item.selected

                //Add root view
                item.root = view

                item.action?.invoke(item)

                // Check if selected state changed
                if (oldState != item.selected) {
                    //Clean Selection State
                    cleanSelection(items, item)
                    //Update Item Style
                    updateItem(view, item)
                }
            }
            //Set style first time
            updateItem(view, item)

            //Set separator view background
            indicator.setBackgroundColor(style.textColor)

            // Add child to its parent
            actionsLayout.addView(view)
        }
    }

    /**
     * This method clears the selection states for each item in the array.
     * @param items: java.util.ArrayList<AlertItemAction> All the items that will be modified
     * @param currentItem: AlertItemAction to save current item state
     */
    private fun cleanSelection(
        items: java.util.ArrayList<AlertItemAction>,
        currentItem: AlertItemAction
    ) {
        for (item in items) {
            if (item != currentItem) item.selected = false
        }
    }

    /**
     * This method sets the views style
     * @param view: View
     * @param alertItemAction: AlertItemAction
     */
    override fun updateItem(view: View, alertItemAction: AlertItemAction) {
        style as AlertItemStyle
        val action = view.findViewById<Button>(R.id.action)

        // Action text color according to AlertActionStyle
        if (context != null) {
            when (alertItemAction.theme) {
                AlertItemTheme.DEFAULT -> {
                    if (alertItemAction.selected) {
                        action.setTextColor(style.selectedTextColor)
                    } else {
                        action.setTextColor(style.textColor)
                    }
                }
                AlertItemTheme.CANCEL -> {
                    action.setTextColor(style.backgroundColor)
                }
                AlertItemTheme.ACCEPT -> {
                    action.setTextColor(style.selectedTextColor)
                }
            }
        }
    }
}