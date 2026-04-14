package org.example.services;

import org.example.dto.user.UserImportDto;

public interface UserImportPersistenceService {
    void persist(UserImportDto dto);
}
