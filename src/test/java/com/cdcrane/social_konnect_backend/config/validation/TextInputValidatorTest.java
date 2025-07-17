package com.cdcrane.social_konnect_backend.config.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class TextInputValidatorTest {

    @Test
    void shouldRemoveAllHtmlTags(){

        // Given
        String badUsername = "<h1>Bad Username</h1>";

        // When
        String cleaned = TextInputValidator.removeAllHtmlTags(badUsername);

        // Then
        assertThat(cleaned).isNotEqualTo(badUsername);

    }

    @Test
    void shouldRemoveDangerousHtml(){

        // Given
        String fineUsername = "<b>Username</b>";
        String badUsername = "<script>alert(document.cookie)</script>";

        // When
        String cleanedSafe = TextInputValidator.removeHtmlTagsAllowBasic(fineUsername);
        String cleanedBad = TextInputValidator.removeHtmlTagsAllowBasic(badUsername);

        // Then
        assertThat(cleanedSafe).isEqualTo(fineUsername); // Make sure styling tags are permitted
        assertThat(cleanedBad).isNotEqualTo(badUsername); // Make sure dangerous tags are not permitted

    }

    @Test
    void shouldCheckIfUsernameIsSafe() {

        // Given
        String fineUsername = "Username";
        String badUsername = "<script>alert(document.cookie)</script>";

        // When
        boolean safeResult = TextInputValidator.isValidUsername(fineUsername);
        boolean badResult = TextInputValidator.isValidUsername(badUsername);

        // Then
        assertThat(safeResult).isTrue();
        assertThat(badResult).isFalse();

    }

}