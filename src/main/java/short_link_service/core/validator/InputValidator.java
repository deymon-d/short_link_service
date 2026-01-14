package short_link_service.core.validator;

import java.util.regex.Pattern;

public class InputValidator {
    private static final Pattern LOGIN_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^.{4,}$");

    public static void validateLogin(String login) {
        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException("Логин не может быть пустым");
        }
        if (!LOGIN_PATTERN.matcher(login).matches()) {
            throw new IllegalArgumentException("Логин должен содержать 3-20 символов (буквы, цифры, подчеркивания)");
        }
    }

    public static void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Пароль должен содержать минимум 4 символа");
        }
    }
}
