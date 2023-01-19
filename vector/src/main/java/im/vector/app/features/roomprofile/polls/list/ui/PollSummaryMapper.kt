/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app.features.roomprofile.polls.list.ui

import im.vector.app.core.extensions.getVectorLastMessageContent
import im.vector.app.features.home.room.detail.timeline.factory.PollOptionViewStateFactory
import im.vector.app.features.home.room.detail.timeline.helper.PollResponseDataFactory
import im.vector.app.features.home.room.detail.timeline.item.PollResponseData
import org.matrix.android.sdk.api.session.room.model.message.MessagePollContent
import org.matrix.android.sdk.api.session.room.timeline.TimelineEvent
import javax.inject.Inject

// TODO add unit tests
class PollSummaryMapper @Inject constructor(
        private val pollResponseDataFactory: PollResponseDataFactory,
        private val pollOptionViewStateFactory: PollOptionViewStateFactory,
) {

    fun map(timelineEvent: TimelineEvent): PollSummary {
        val content = timelineEvent.getVectorLastMessageContent()
        val pollResponseData = pollResponseDataFactory.create(timelineEvent)
        val eventId = timelineEvent.root.eventId.orEmpty()
        val creationTimestamp = timelineEvent.root.originServerTs ?: 0
        if (eventId.isNotEmpty() && creationTimestamp > 0 && content is MessagePollContent && pollResponseData != null) {
            return convertToPollSummary(
                    eventId = eventId,
                    creationTimestamp = creationTimestamp,
                    messagePollContent = content,
                    pollResponseData = pollResponseData
            )
        } else {
            throw IllegalStateException("missing mandatory info about poll event with id=$eventId")
        }
    }

    private fun convertToPollSummary(
            eventId: String,
            creationTimestamp: Long,
            messagePollContent: MessagePollContent,
            pollResponseData: PollResponseData
    ): PollSummary {
        val pollCreationInfo = messagePollContent.getBestPollCreationInfo()
        val pollTitle = pollCreationInfo?.question?.getBestQuestion().orEmpty()
        return if (pollResponseData.isClosed) {
            PollSummary.EndedPoll(
                    id = eventId,
                    creationTimestamp = creationTimestamp,
                    title = pollTitle,
                    totalVotes = pollResponseData.totalVotes,
                    winnerOptions = pollOptionViewStateFactory.createPollEndedOptions(pollCreationInfo, pollResponseData)
            )
        } else {
            PollSummary.ActivePoll(
                    id = eventId,
                    creationTimestamp = creationTimestamp,
                    title = pollTitle,
            )
        }
    }
}
