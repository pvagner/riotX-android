/*
 * Copyright 2019 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.riotredesign.features.home.room.list

import androidx.annotation.StringRes
import com.airbnb.epoxy.TypedEpoxyController
import im.vector.matrix.android.api.session.room.model.RoomSummary
import im.vector.riotredesign.R
import im.vector.riotredesign.core.resources.StringProvider

class RoomSummaryController(private val stringProvider: StringProvider
) : TypedEpoxyController<RoomListViewState>() {

    private var isDirectRoomsExpanded = true
    private var isGroupRoomsExpanded = true
    private var isFavoriteRoomsExpanded = true
    private var isLowPriorityRoomsExpanded = true
    private var isServerNoticeRoomsExpanded = true

    var callback: Callback? = null

    override fun buildModels(viewState: RoomListViewState) {
        val roomSummaries = viewState.asyncRooms()

        buildRoomCategory(viewState, R.string.room_list_favourites, isFavoriteRoomsExpanded) {
            isFavoriteRoomsExpanded = !isFavoriteRoomsExpanded
        }
        if (isFavoriteRoomsExpanded) {
            buildRoomModels(roomSummaries?.favourites ?: emptyList(), viewState.selectedRoomId)
        }

        buildRoomCategory(viewState, R.string.room_list_direct, isDirectRoomsExpanded) {
            isDirectRoomsExpanded = !isDirectRoomsExpanded
        }
        if (isDirectRoomsExpanded) {
            buildRoomModels(roomSummaries?.directRooms ?: emptyList(), viewState.selectedRoomId)
        }

        buildRoomCategory(viewState, R.string.room_list_group, isGroupRoomsExpanded) {
            isGroupRoomsExpanded = !isGroupRoomsExpanded
        }
        if (isGroupRoomsExpanded) {
            buildRoomModels(roomSummaries?.groupRooms ?: emptyList(), viewState.selectedRoomId)
        }

        buildRoomCategory(viewState, R.string.room_list_low_priority, isLowPriorityRoomsExpanded) {
            isLowPriorityRoomsExpanded = !isLowPriorityRoomsExpanded
        }
        if (isLowPriorityRoomsExpanded) {
            buildRoomModels(roomSummaries?.lowPriorities ?: emptyList(), viewState.selectedRoomId)
        }

        buildRoomCategory(viewState, R.string.room_list_system_alert, isServerNoticeRoomsExpanded) {
            isServerNoticeRoomsExpanded = !isServerNoticeRoomsExpanded
        }
        if (isServerNoticeRoomsExpanded) {
            buildRoomModels(roomSummaries?.serverNotices ?: emptyList(), viewState.selectedRoomId)
        }

    }

    private fun buildRoomCategory(viewState: RoomListViewState, @StringRes titleRes: Int, isExpanded: Boolean, mutateExpandedState: () -> Unit) {
        RoomCategoryItem(
                title = stringProvider.getString(titleRes).toUpperCase(),
                isExpanded = isExpanded,
                listener = {
                    mutateExpandedState()
                    setData(viewState)
                }
        )
                .id(titleRes)
                .addTo(this)
    }

    private fun buildRoomModels(summaries: List<RoomSummary>, selectedRoomId: String?) {
        summaries.forEach { roomSummary ->
            val unreadCount = roomSummary.notificationCount
            val showHighlighted = roomSummary.highlightCount > 0
            val isSelected = roomSummary.roomId == selectedRoomId
            RoomSummaryItem(
                    roomName = roomSummary.displayName,
                    avatarUrl = roomSummary.avatarUrl,
                    isSelected = isSelected,
                    showHighlighted = showHighlighted,
                    unreadCount = unreadCount,
                    listener = { callback?.onRoomSelected(roomSummary) }
            )
                    .id(roomSummary.roomId)
                    .addTo(this)
        }
    }

    interface Callback {
        fun onRoomSelected(room: RoomSummary)
    }

}
