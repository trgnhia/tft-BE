package org.example.services;

import org.example.core.api.PageResponse;
import org.example.dto.sets.SetOptionResponse;
import org.example.dto.sets.SetsRequest;
import org.example.dto.sets.SetsResponse;

import java.util.List;

public interface SetsService {
    SetsResponse getSetById (Long id);
    PageResponse<SetsResponse> getAllSet(int page, int size, String keyword, Boolean deleted);
    List<SetsResponse> getAllPublishedSet();
    List<SetOptionResponse> getSetOptions();
    List<SetOptionResponse> getSetOptionsForCms();
    SetsResponse create (SetsRequest request);
    SetsResponse update (Long id, SetsRequest request);
    void delete (Long id);
    void deletedMany (List<Long> ids);
}
