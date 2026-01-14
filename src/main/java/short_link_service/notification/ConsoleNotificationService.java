package short_link_service.notification;

import short_link_service.core.model.ShortLink;

public class ConsoleNotificationService implements NotificationService {
    @Override
    public void notifyLinkExpired(ShortLink link) {
        System.out.printf("[Уведомление] Ссылка %s истекла. Создана: %s, Истекла: %s%n", link.getShortUrl(),
                link.getCreationDate(), link.getExpirationDate());
    }

    @Override
    public void notifyClickLimitExceeded(ShortLink link) {
        System.out.printf("[Уведомление] Достигнут лимит переходов для ссылки %s. Максимум: %d, Текущее: %d%n",
                link.getShortUrl(), link.getMaxClicks(), link.getClickCount());
    }

    @Override
    public void notifyLinkCreated(ShortLink link) {
        System.out.printf("[Уведомление] Создана новая ссылка: %s -> %s%n", link.getShortUrl(), link.getOriginalUrl());
        System.out.printf("    Истекает: %s, Лимит переходов: %d%n", link.getExpirationDate(), link.getMaxClicks());
    }
}
