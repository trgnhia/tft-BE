package org.example.events.sets;

public record SetChangedEvent(
        Long setId,
        String setName,
        Long createdBy,
        Action action
) {
    public enum Action {
        CREATED,
        UPDATED,
        DELETED
    }
}
