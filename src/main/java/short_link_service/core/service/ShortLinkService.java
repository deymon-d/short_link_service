package short_link_service.core.service;

import short_link_service.config.AppConfig;
import short_link_service.core.generator.LinkGenerator;
import short_link_service.core.model.ShortLink;
import short_link_service.core.model.User;
import short_link_service.core.validator.UrlValidator;
import short_link_service.exception.*;
import short_link_service.notification.NotificationService;

import java.time.LocalDateTime;
import java.util.*;

public class ShortLinkService {
    private final LinkGenerator linkGenerator;
    private final NotificationService notificationService;
    private final Map<String, User> users;
    private final Map<String, ShortLink> links;
    private User currentUser;

    public ShortLinkService(LinkGenerator linkGenerator, NotificationService notificationService) {
        this.linkGenerator = linkGenerator;
        this.notificationService = notificationService;
        this.users = new HashMap<>();
        this.links = new HashMap<>();
    }

    public void login(String login, String password) {
        User user = users.get(login.toLowerCase());
        if (user == null) {
            throw new UserNotFoundException("Пользователь с логином '" + login + "' не найден");
        }
        if (!user.verifyPassword(password)) {
            throw new ValidationException("Неверный пароль");
        }
        currentUser = user;
    }

    public void register(String login, String password) {
        String normalizedLogin = login.trim().toLowerCase();
        if (users.containsKey(normalizedLogin)) {
            throw new ValidationException("Пользователь с логином '" + login + "' уже существует");
        }

        User user = new User(login, password);
        users.put(user.getLogin(), user);
    }

    public void logout() {
        currentUser = null;
    }

    public ShortLink createShortLink(String originalUrl, String title, String description, Integer maxClicks,
            LocalDateTime expirationDate) {
        checkUserLoggedIn();
        UrlValidator.validateUrl(originalUrl);
        int actualMaxClicks = maxClicks != null ? maxClicks : AppConfig.DEFAULT_MAX_CLICKS;
        UrlValidator.validateMaxClicks(actualMaxClicks);
        LocalDateTime actualExpiration = expirationDate != null
                ? expirationDate
                : LocalDateTime.now().plusHours(AppConfig.DEFAULT_EXPIRATION_HOURS);
        UrlValidator.validateExpiration(actualExpiration);
        String shortUrlCode = linkGenerator.generateShortUrl(originalUrl);
        String shortUrl = AppConfig.SHORT_URL_PREFIX + shortUrlCode;
        while (links.containsKey(shortUrl)) {
            shortUrlCode = linkGenerator.generateShortUrl(originalUrl);
            shortUrl = AppConfig.SHORT_URL_PREFIX + shortUrlCode;
        }
        ShortLink link = new ShortLink(originalUrl, shortUrl, currentUser.getId(), actualExpiration, actualMaxClicks);

        if (title != null) {
            link.setTitle(title);
        }
        if (description != null) {
            link.setDescription(description);
        }
        links.put(link.getShortUrl(), link);
        notificationService.notifyLinkCreated(link);
        return link;
    }

    public String redirect(String shortUrl) {
        ShortLink link = links.get(shortUrl);
        if (link == null) {
            throw new LinkNotFoundException("Ссылка не обнаружена: " + shortUrl);
        }
        if (link.isExpired()) {
            notificationService.notifyLinkExpired(link);
            if (isUserLoggedIn() && link.getUserId().equals(currentUser.getId())) {
                links.remove(shortUrl);
            }
            throw new LinkExpiredException("Ссылка истекла");
        }
        if (link.isClickLimitExceeded()) {
            notificationService.notifyClickLimitExceeded(link);
            if (isUserLoggedIn() && link.getUserId().equals(currentUser.getId())) {
                links.remove(shortUrl);
            }
            throw new LinkLimitExceededException(
                    "Превышен лимит переходов по ссылке: " + link.getClickCount() + "/" + link.getMaxClicks());
        }
        link.incrementClickCount();
        if (link.isClickLimitExceeded()) {
            notificationService.notifyClickLimitExceeded(link);
            if (isUserLoggedIn() && link.getUserId().equals(currentUser.getId())) {
                links.remove(shortUrl);
            }
        }
        return link.getOriginalUrl();
    }

    public ShortLink updateLink(String shortUrl, String title, String description, Integer maxClicks,
            LocalDateTime expirationDate) {
        checkUserLoggedIn();
        ShortLink link = links.get(shortUrl);
        if (link == null) {
            throw new LinkNotFoundException("Ссылка не обнаружена: " + shortUrl);
        }
        if (!link.getUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException("У вас нет прав на модификацию данной ссылки");
        }

        if (maxClicks != null) {
            UrlValidator.validateMaxClicks(maxClicks);
        }
        if (expirationDate != null) {
            UrlValidator.validateExpiration(expirationDate);
        }
        link.update(title, description, maxClicks, expirationDate);
        return link;
    }

    public void deleteLink(String shortUrl) {
        checkUserLoggedIn();
        ShortLink link = links.get(shortUrl);
        if (link == null) {
            throw new LinkNotFoundException("Ссылка не обнаружена: " + shortUrl);
        }
        if (!link.getUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException("У вас нет прав на модификацию данной ссылки");
        }
        links.remove(shortUrl);
    }

    public List<ShortLink> getUserLinks() {
        return links.values().stream().filter(link -> link.getUserId().equals(currentUser.getId())).toList();
    }

    public ShortLink getLink(String shortUrl) {
        ShortLink link = links.get(shortUrl);
        if (link == null) {
            throw new LinkNotFoundException("Ссылка не обнаружена: " + shortUrl);
        }
        return link;
    }

    public boolean isUserLoggedIn() {
        return currentUser != null;
    }

    private void checkUserLoggedIn() {
        if (!isUserLoggedIn()) {
            throw new IllegalStateException("Пользователь не авторизован");
        }
    }

    public void checkAndDeleteInactiveLinks() {
        List<ShortLink> inactiveLinks = getUserLinks().stream()
                .filter(link -> link.isClickLimitExceeded() || link.isClickLimitExceeded()).toList();
        inactiveLinks.stream().filter(ShortLink::isExpired).forEach(notificationService::notifyLinkExpired);
        inactiveLinks.stream().filter(ShortLink::isClickLimitExceeded)
                .forEach(notificationService::notifyClickLimitExceeded);
        inactiveLinks.forEach(link -> links.remove(link.getShortUrl()));
    }
}
