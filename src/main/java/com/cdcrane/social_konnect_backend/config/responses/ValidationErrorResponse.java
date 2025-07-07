package com.cdcrane.social_konnect_backend.config.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class ValidationErrorResponse {

    private Map<String, String> errors;
    private int responseCode;
    private long timestamp;


}
