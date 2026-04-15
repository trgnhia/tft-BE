package org.example.imports.service;

@FunctionalInterface
public interface ImportRowPersister<T> {
    void persist(T rowDto);
}