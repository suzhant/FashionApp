package com.sushant.fashionapp.Utils;

public class TextUtils {

    public static String captializeAllFirstLetter(String name) {
        char[] array = name.toCharArray();
        array[0] = Character.toUpperCase(array[0]);

        for (int i = 1; i < array.length; i++) {
            if (Character.isWhitespace(array[i - 1])) {
                array[i] = Character.toUpperCase(array[i]);
            }
        }
        return new String(array);
    }

    public static StringBuilder getFirstLetter(String name) {
        StringBuilder initials = new StringBuilder();
        String[] myName = name.split(" ");
        for (String s : myName) {
            initials.append(s.charAt(0));
        }
        return initials;
    }
}
