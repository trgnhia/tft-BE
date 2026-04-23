package org.example.imports.service;

import org.example.imports.model.ImportRowOutcome;

@FunctionalInterface
public interface ImportRowPersisterWithOutcome<T> {
    ImportRowOutcome persist(T rowDto);
}
