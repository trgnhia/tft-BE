package org.example.services.implement;
import lombok.RequiredArgsConstructor;
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
import org.example.mapper.ConversationMapper;
import org.example.mapper.MessageMapper;
import org.example.repositories.ConversationParticipantRepository;
import org.example.repositories.ConversationRepository;
import org.example.repositories.MessageRepository;
import org.example.repositories.UserRepository;
import org.example.services.ChatService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepo;
    private final ConversationParticipantRepository conversationParticipantRepo;
    private final UserRepository userRepo;
    private final MessageRepository messageRepo;
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final ChatMessageMapper chatMessageMapper;

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(Long senderId, ChatSendRequest request) {
        Long receiverId = request.getReceiverId();
        validateNotSelfChat(senderId, receiverId);

        User sender = findUserById(senderId, "Sender not found");
        User receiver = findUserById(receiverId, "Receiver not found");

        Conversation conversation = findOrCreateConversation(sender, receiver);
        Message message = buildMessage(conversation, sender, request.getContent());

        Message savedMessage = messageRepo.save(message);
        conversation.setUpdatedAt(Instant.now());
        conversationRepo.save(conversation);

        return chatMessageMapper.toResponse(savedMessage, receiver.getId());
    }

    @Override
    public List<ConversationResponse> getMyConversations(Long currentUserId) {
        List<Conversation> conversations = conversationRepo.findAllByUserIdOrderByUpdatedAtDesc(currentUserId);
        return conversationMapper.toResponseList(conversations);
    }

    @Override
    public List<MessageResponse> getMessagesByConversation(Long currentUserId, Long conversationId) {
        validateParticipant(currentUserId, conversationId);

        List<Message> messages = messageRepo.findByConversationIdOrderByCreatedAtAsc(conversationId);
        return messageMapper.toResponseList(messages);
    }

    @Override
    @Transactional
    public Long getOrCreateConversation(Long currentUserId, Long otherUserId) {
        validateNotSelfChat(currentUserId, otherUserId);

        User currentUser = findUserById(currentUserId, "Current user not found");
        User otherUser = findUserById(otherUserId, "Other user not found");

        return findOrCreateConversation(currentUser, otherUser).getId();
    }


    private User findUserById(Long id, String message) {
        return userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(message));
    }

    private void validateNotSelfChat(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Cannot chat with yourself");
        }
    }

    private void validateParticipant(Long currentUserId, Long conversationId) {
        boolean isParticipant = conversationParticipantRepo.existsByConversationIdAndUserId(conversationId, currentUserId);
        if (!isParticipant) {
            throw new IllegalArgumentException("You are not a participant of this conversation");
        }
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