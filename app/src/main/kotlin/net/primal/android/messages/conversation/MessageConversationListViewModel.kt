package net.primal.android.messages.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.core.compose.feed.asMediaResourceUi
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.messages.conversation.MessageConversationListContract.UiEvent
import net.primal.android.messages.conversation.MessageConversationListContract.UiState
import net.primal.android.messages.conversation.model.MessageConversationUi
import net.primal.android.messages.db.MessageConversation
import net.primal.android.messages.domain.ConversationRelation
import net.primal.android.messages.repository.MessageRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.badges.BadgesManager
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class MessageConversationListViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val badgesManager: BadgesManager,
    private val messageRepository: MessageRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        value = UiState(
            activeRelation = ConversationRelation.Follows,
            conversations = messageRepository
                .newestConversations(ConversationRelation.Follows)
                .mapAsPagingDataOfMessageConversationUI(),
        )
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val _event: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { _event.emit(event) }

    init {
        observeEvents()
        subscribeToActiveAccount()
        subscribeToBadgesUpdates()
        fetchConversations()
    }

    private fun observeEvents() = viewModelScope.launch {
        _event.collect {
            when (it) {
                is UiEvent.ChangeRelation -> changeRelation(relation = it.relation)
                UiEvent.MarkAllConversationsAsRead -> Unit
            }
        }
    }

    private fun subscribeToActiveAccount() = viewModelScope.launch {
        activeAccountStore.activeUserAccount.collect {
            setState {
                copy(activeAccountAvatarUrl = it.pictureUrl)
            }
        }
    }

    private fun subscribeToBadgesUpdates() = viewModelScope.launch {
        badgesManager.badges.collect {
            setState {
                copy(badges = it)
            }
        }
    }

    private fun fetchConversations() = viewModelScope.launch {
        messageRepository.fetchFollowConversations()
        messageRepository.fetchNonFollowsConversations()
    }

    private fun changeRelation(relation: ConversationRelation) {
        setState {
            copy(
                activeRelation = relation,
                conversations = messageRepository
                    .newestConversations(relation = relation)
                    .mapAsPagingDataOfMessageConversationUI(),
            )
        }
    }

    private fun Flow<PagingData<MessageConversation>>.mapAsPagingDataOfMessageConversationUI() =
        map { pagingData -> pagingData.map { it.mapAsMessageConversationUi() } }

    private fun MessageConversation.mapAsMessageConversationUi() =
        MessageConversationUi(
            participantId = this.participant.ownerId,
            participantUsername = this.participant.usernameUiFriendly(),
            lastMessageSnippet = this.lastMessage.content,
            lastMessageAt = Instant.ofEpochSecond(this.lastMessage.createdAt),
            participantInternetIdentifier = this.participant.internetIdentifier,
            participantAvatarUrl = this.participant.picture,
            participantMediaResources = this.participantResources.map { it.asMediaResourceUi() },
            unreadMessagesCount = this.data.unreadMessagesCount,
        )
}
