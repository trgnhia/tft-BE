package org.example.imports.model;

import java.util.List;

public record ImportRow(long rowNumber, List<String> values) {
}