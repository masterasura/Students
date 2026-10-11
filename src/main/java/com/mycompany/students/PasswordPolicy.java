package com.mycompany.students;

public final class PasswordPolicy {

    private PasswordPolicy() {
    }

    public static String validationError(String password) {
        if (password == null) {
            return "Password cannot be empty.";
        }

        boolean uppercase = false;
        boolean lowercase = false;
        boolean number = false;
        boolean special = false;

        for (char character : password.toCharArray()) {
            if (Character.isUpperCase(character)) {
                uppercase = true;
            } else if (Character.isLowerCase(character)) {
                lowercase = true;
            } else if (Character.isDigit(character)) {
                number = true;
            } else if (!Character.isWhitespace(character)) {
                special = true;
            }
        }

        if (!uppercase || !lowercase || !number || !special) {
            return "Password must include at least one uppercase letter, "
                    + "one lowercase letter, one number, and one special character.";
        }

        return null;
    }
}
