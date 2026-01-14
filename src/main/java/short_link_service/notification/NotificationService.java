package short_link_service.notification;

import short_link_service.core.model.ShortLink;

public interface NotificationService {
    void notifyLinkExpired(ShortLink link);

    void notifyClickLimitExceeded(ShortLink link);

    void notifyLinkCreated(ShortLink link);
}
