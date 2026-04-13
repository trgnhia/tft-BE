package org.example.repositories;


import org.example.entities.conversation.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query(value = """
        SELECT
            c.id,
            c.created_at,
            c.updated_at,
            u.id AS other_user_id,
            u.username AS other_username,
            lm.content AS last_message,
            lm.created_at AS last_message_at
        FROM conversations c
        JOIN conversation_participants cp1
            ON cp1.conversation_id = c.id
        JOIN conversation_participants cp2
            ON cp2.conversation_id = c.id
        JOIN users u
            ON u.id = cp2.user_id
        LEFT JOIN (
            SELECT *
            FROM (
                SELECT
                    m.*,
                    ROW_NUMBER() OVER (
                        PARTITION BY m.conversation_id
                        ORDER BY m.created_at DESC
                    ) AS rn
                FROM messages m
            ) t
            WHERE t.rn = 1
        ) lm
            ON lm.conversation_id = c.id
        WHERE cp1.user_id = :currentUserId
          AND cp2.user_id <> :currentUserId
        ORDER BY c.updated_at DESC
        """, nativeQuery = true)
    List<Object[]> findConversationWithDetails(Long currentUserId);
}