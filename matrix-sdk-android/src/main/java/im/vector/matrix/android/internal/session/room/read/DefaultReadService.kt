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

package im.vector.matrix.android.internal.session.room.read

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.zhuinden.monarchy.Monarchy
import im.vector.matrix.android.api.MatrixCallback
import im.vector.matrix.android.api.session.room.model.ReadReceipt
import im.vector.matrix.android.api.session.room.read.ReadService
import im.vector.matrix.android.internal.database.RealmLiveData
import im.vector.matrix.android.internal.database.mapper.ReadReceiptsSummaryMapper
import im.vector.matrix.android.internal.database.model.ReadReceiptsSummaryEntity
import im.vector.matrix.android.internal.database.query.isEventRead
import im.vector.matrix.android.internal.database.query.where
import im.vector.matrix.android.internal.di.UserId
import im.vector.matrix.android.internal.task.TaskExecutor
import im.vector.matrix.android.internal.task.configureWith

internal class DefaultReadService @AssistedInject constructor(@Assisted private val roomId: String,
                                                              private val monarchy: Monarchy,
                                                              private val taskExecutor: TaskExecutor,
                                                              private val setReadMarkersTask: SetReadMarkersTask,
                                                              private val readReceiptsSummaryMapper: ReadReceiptsSummaryMapper,
                                                              @UserId private val userId: String
) : ReadService {

    @AssistedInject.Factory
    interface Factory {
        fun create(roomId: String): ReadService
    }

    override fun markAllAsRead(callback: MatrixCallback<Unit>) {
        val params = SetReadMarkersTask.Params(roomId, markAllAsRead = true)
        setReadMarkersTask
                .configureWith(params) {
                    this.callback = callback
                }
                .executeBy(taskExecutor)
    }

    override fun setReadReceipt(eventId: String, callback: MatrixCallback<Unit>) {
        val params = SetReadMarkersTask.Params(roomId, fullyReadEventId = null, readReceiptEventId = eventId)
        setReadMarkersTask
                .configureWith(params) {
                    this.callback = callback
                }
                .executeBy(taskExecutor)
    }

    override fun setReadMarker(fullyReadEventId: String, callback: MatrixCallback<Unit>) {
        val params = SetReadMarkersTask.Params(roomId, fullyReadEventId = fullyReadEventId, readReceiptEventId = null)
        setReadMarkersTask
                .configureWith(params) {
                    this.callback = callback
                }
                .executeBy(taskExecutor)
    }


    override fun isEventRead(eventId: String): Boolean {
        return isEventRead(monarchy, userId, roomId, eventId)
    }

    override fun getEventReadReceiptsLive(eventId: String): LiveData<List<ReadReceipt>> {
        val liveEntity = RealmLiveData(monarchy.realmConfiguration) { realm ->
            ReadReceiptsSummaryEntity.where(realm, eventId)
        }
        return Transformations.map(liveEntity) { realmResults ->
            realmResults.firstOrNull()?.let {
                readReceiptsSummaryMapper.map(it)
            }?.sortedByDescending {
                it.originServerTs
            } ?: emptyList()
        }
    }
}