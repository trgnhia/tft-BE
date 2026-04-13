package org.example.repositories;

import org.example.entities.conversation.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {
    List<ConversationParticipant> findByConversationId(Long conversationId);

    List<ConversationParticipant> findByUserId(Long userId);

    boolean existsByConversationIdAndUserId (Long userId, Long conversationId);
    @Query("""
        select cp1.conversation.id
        from ConversationParticipant cp1
        join ConversationParticipant cp2
            on cp1.conversation.id = cp2.conversation.id
        where cp1.user.id = :userId1
          and cp2.user.id = :userId2
    """)
    Optional<Long> findConversationIdBetweenTwoUsers(Long userId1, Long userId2);
}
