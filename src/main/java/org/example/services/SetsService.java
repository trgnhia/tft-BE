package org.example.services;

import org.example.dto.sets.SetsRequest;
import org.example.dto.sets.SetsResponse;

import java.util.List;

public interface SetsService {
    SetsResponse getSetById (Long id);
    List<SetsResponse> getAllSet();
    SetsResponse create (SetsRequest request);
    SetsResponse update (Long id, SetsRequest request);
}
