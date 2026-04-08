package com.codex.skilladmin.runtime;

public record RuntimeInvokeResponse(
        boolean allowed,
        String message
) {
}
