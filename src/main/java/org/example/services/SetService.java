package org.example.services;

import org.example.dto.set.SetRequest;
import org.example.dto.set.SetResponse;

import java.util.List;

public interface SetService {
    SetResponse getSetById (Long id);
    List<SetResponse> getAllSet();
    SetResponse create (SetRequest request);
    SetResponse update (Long id, SetRequest request);
}
