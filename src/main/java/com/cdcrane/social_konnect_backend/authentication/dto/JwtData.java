package com.cdcrane.social_konnect_backend.authentication.dto;

import java.util.Date;

public record JwtData(String token, String username, Date expirationDate) {
}
