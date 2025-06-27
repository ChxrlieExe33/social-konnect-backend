package com.cdcrane.social_konnect_backend.config.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ExceptionErrorResponse {

    private String message;
    private int responseCode;
    private long timestamp;

}
