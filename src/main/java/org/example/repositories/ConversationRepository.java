package org.example.repositories;


import org.example.entities.conversation.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("""
        select c
        from Conversation c
        join ConversationParticipant cp on cp.conversation.id = c.id
        where cp.user.id = :userId
        order by c.updatedAt desc
    """)
    List<Conversation> findAllByUserIdOrderByUpdatedAtDesc(Long userId);
}