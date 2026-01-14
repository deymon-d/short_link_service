package short_link_service.core.model;

import java.util.Objects;
import java.util.UUID;

public class User {
    private final String id;
    private final String login;
    private final String passwordHash;

    public User(String login, String password) {
        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException("Логин не может быть пустым");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }
        this.id = UUID.randomUUID().toString();
        this.login = login.trim().toLowerCase();
        this.passwordHash = hashPassword(password);
    }

    private String hashPassword(String password) {
        int hash = password.hashCode();
        return Integer.toHexString(hash);
    }

    public boolean verifyPassword(String password) {
        return passwordHash.equals(hashPassword(password));
    }

    public String getId() {
        return id;
    }
    public String getLogin() {
        return login;
    }
    public String getPasswordHash() {
        return passwordHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        User user = (User) o;
        return login.equals(user.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login);
    }

    @Override
    public String toString() {
        return "User{login='" + login + "'}";
    }
}
