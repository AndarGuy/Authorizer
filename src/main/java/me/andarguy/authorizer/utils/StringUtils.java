package me.andarguy.authorizer.utils;

import java.util.regex.Pattern;

public class StringUtils {
    private static final Pattern validator = Pattern.compile("^[A-Za-z0-9_]{3,16}$");

    public static boolean validate(String nickname) {
        return validator.matcher(nickname).matches();
    }

}
