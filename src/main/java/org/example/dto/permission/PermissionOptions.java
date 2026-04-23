package org.example.dto.permission;

import java.util.List;

public record PermissionOptions(
        List<String> resources,
        List<String> actions) {
}
