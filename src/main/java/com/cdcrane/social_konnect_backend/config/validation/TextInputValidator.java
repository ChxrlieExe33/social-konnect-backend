package com.cdcrane.social_konnect_backend.config.validation;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class TextInputValidator {

    public static String removeAllHtmlTags(String input) {

        return Jsoup.clean(input, Safelist.none());

    }

    public static String removeHtmlTagsAllowBasic(String input) {

        return Jsoup.clean(input, Safelist.basic());
    }
}
