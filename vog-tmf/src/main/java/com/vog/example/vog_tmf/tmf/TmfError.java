package com.vog.example.vog_tmf.tmf;

import org.springframework.http.HttpStatus;

/** TMF630 error body — every error response uses this shape. */
public record TmfError(String code, String reason, String message, String status) {

    public static TmfError of(HttpStatus status, String message) {
        return new TmfError(String.valueOf(status.value()), status.getReasonPhrase(),
                message, String.valueOf(status.value()));
    }
}
