package org.example.services;

import org.example.core.api.PageResponse;
import org.example.dto.sets.SetsRequest;
import org.example.dto.sets.SetsResponse;

import java.util.List;

public interface SetsService {
    SetsResponse getSetById (Long id);
    PageResponse<SetsResponse> getAllSet(int page, int size, Boolean deleted);
    List<SetsResponse> getAllPublishedSet();
    SetsResponse create (SetsRequest request);
    SetsResponse update (Long id, SetsRequest request);
    void delete (Long id);
}
