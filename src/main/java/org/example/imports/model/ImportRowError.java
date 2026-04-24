package org.example.imports.model;

import java.util.List;

public record ImportRowError(long rowNumber, List<String> errors) {
}
