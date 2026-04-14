package org.example.services.implement;
import lombok.RequiredArgsConstructor;
import org.example.common.constant.Constants;
import org.example.common.enums.ErrorCode;
import org.example.common.exception.ConflictException;
import org.example.common.exception.DataException;
import org.example.common.exception.ResourceNotFoundException;
import org.example.dto.chat.ChatMessageResponse;
import org.example.dto.chat.ChatSendRequest;
import org.example.dto.chat.MessageResponse;
import org.example.dto.conversation.ConversationResponse;
import org.example.entities.User;
import org.example.entities.conversation.Conversation;
import org.example.entities.conversation.ConversationParticipant;
import org.example.entities.conversation.Message;
import org.example.mapper.ChatMessageMapper;
import org.example.mapper.MessageMapper;
import org.example.repositories.ConversationParticipantRepository;
import org.example.repositories.ConversationRepository;
import org.example.repositories.MessageRepository;
import org.example.repositories.UserRepository;
import org.example.services.ChatService;
import org.example.util.MessageUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepo;
    private final ConversationParticipantRepository conversationParticipantRepo;
    private final UserRepository userRepo;
    private final MessageRepository messageRepo;
    private final MessageMapper messageMapper;
    private final ChatMessageMapper chatMessageMapper;
    @Override
    @Transactional
    public ChatMessageResponse sendMessage(Long senderId, ChatSendRequest request) {
        Long receiverId = request.getReceiverId();

        validateNotSelfChat(senderId, receiverId);
        validateMessageContent(request.getContent());

        User sender = findUserById(senderId);
        User receiver = findUserById(receiverId);

        Conversation conversation = findOrCreateConversation(sender, receiver);
        Message message = buildMessage(conversation, sender, request.getContent());

        Message savedMessage = messageRepo.save(message);
        conversation.setUpdatedAt(Instant.now());
        conversationRepo.save(conversation);

        return chatMessageMapper.toResponse(savedMessage, receiver.getId());
    }

    @Override
    public List<ConversationResponse> getMyConversations(Long currentUserId) {
        List<Object[]> rows = conversationRepo.findConversationWithDetails(currentUserId);

        return rows.stream()
                .map(this::mapConversationRow)
                .toList();
    }

    @Override
    public Page<MessageResponse> getMessagesByConversation(Long currentUserId, Long conversationId, Pageable pageable) {
        validateParticipant(currentUserId, conversationId);

        Page<Message> messagePage = messageRepo.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);

        List<MessageResponse> orderedMessages = new ArrayList<>(
                messagePage.getContent()
                        .stream()
                        .map(messageMapper::toResponse)
                        .toList()
        );

        Collections.reverse(orderedMessages);

        return new PageImpl<>(orderedMessages, pageable, messagePage.getTotalElements());
    }

    @Override
    @Transactional
    public Long getOrCreateConversation(Long currentUserId, Long otherUserId) {
        validateNotSelfChat(currentUserId, otherUserId);

        User currentUser = findUserById(currentUserId);
        User otherUser = findUserById(otherUserId);

        return findOrCreateConversation(currentUser, otherUser).getId();
    }

    private User findUserById(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageUtils.getMessage(Constants.MessageKey.ENTITY_USER)
                ));
    }

    private void validateNotSelfChat(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new ConflictException(
                    MessageUtils.getMessage(Constants.MessageKey.CHAT_SELF_NOT_ALLOWED)
            );
        }
    }

    private void validateParticipant(Long currentUserId, Long conversationId) {
        boolean isParticipant = conversationParticipantRepo
                .existsByConversationIdAndUserId(conversationId, currentUserId);

        if (!isParticipant) {
            throw new DataException(
                    ErrorCode.INCOMPLETE_DATA,
                    MessageUtils.getMessage(Constants.MessageKey.CHAT_PARTICIPANT_REQUIRED)
            );
        }
    }

    private void validateMessageContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new DataException(
                    ErrorCode.INCOMPLETE_DATA,
                    MessageUtils.getMessage(Constants.MessageKey.CHAT_MESSAGE_CONTENT_BLANK)
            );
        }
    }

    private ConversationResponse mapConversationRow(Object[] row) {
        return ConversationResponse.builder()
                .id(toLong(row[0]))
                .createdAt(toInstant(row[1]))
                .updatedAt(toInstant(row[2]))
                .otherUserId(toLong(row[3]))
                .otherUsername((String) row[4])
                .lastMessage((String) row[5])
                .lastMessageAt(toInstant(row[6]))
                .build();
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).longValue();
    }

    private Instant toInstant(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant();
        }

        if (value instanceof Instant instant) {
            return instant;
        }

        throw new IllegalStateException(
                MessageUtils.getMessage(Constants.MessageKey.INTERNAL_INSTANT_CONVERSION_FAILED)
        );
    }

    private Conversation findOrCreateConversation(User currentUser, User otherUser) {
        return conversationParticipantRepo
                .findConversationIdBetweenTwoUsers(currentUser.getId(), otherUser.getId())
                .flatMap(conversationRepo::findById)
                .orElseGet(() -> createConversation(currentUser, otherUser));
    }

    private Conversation createConversation(User currentUser, User otherUser) {
        Conversation savedConversation = conversationRepo.save(Conversation.builder().build());

        ConversationParticipant currentParticipant = ConversationParticipant.builder()
                .conversation(savedConversation)
                .user(currentUser)
                .build();

        ConversationParticipant otherParticipant = ConversationParticipant.builder()
                .conversation(savedConversation)
                .user(otherUser)
                .build();

        conversationParticipantRepo.save(currentParticipant);
        conversationParticipantRepo.save(otherParticipant);

        return savedConversation;
    }

    private Message buildMessage(Conversation conversation, User sender, String content) {
        return Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content.trim())
                .read(false)
                .build();
    }
}