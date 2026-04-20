package org.example.repositories.projection;

public interface ChampRestoreCandidate {
    Long getChampId();
    Boolean getChampDeleted();
    Long getSetId();
    Boolean getSetDeleted();
}
