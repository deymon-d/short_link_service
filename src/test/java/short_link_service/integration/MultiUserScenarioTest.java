package short_link_service.integration;

import short_link_service.core.generator.SecureHashLinkGenerator;
import short_link_service.core.model.ShortLink;
import short_link_service.core.service.ShortLinkService;
import short_link_service.exception.*;
import short_link_service.notification.ConsoleNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiUserScenarioTest {
    private ShortLinkService linkService;
    private String user1;
    private String password1;
    private String user2;
    private String password2;

    @BeforeEach
    void setUp() {

        SecureHashLinkGenerator generator = new SecureHashLinkGenerator();
        ConsoleNotificationService notificationService = new ConsoleNotificationService();

        linkService = new ShortLinkService(generator, notificationService);
        user1 = "user1";
        password1 = "pass1";
        user2 = "user2";
        password2 = "pass2";

        linkService.register(user1, password1);
        linkService.register(user2, password2);
    }

    @Test
    void sameUrlDifferentUsersShouldGenerateDifferentShortUrls() throws ValidationException, AccessDeniedException {

        String originalUrl = "https://www.example.com";
        linkService.login(user1, password1);
        ShortLink link1 = linkService.createShortLink(originalUrl, null, null, null, null);
        linkService.logout();
        linkService.login(user2, password2);
        ShortLink link2 = linkService.createShortLink(originalUrl, null, null, null, null);
        linkService.logout();

        assertNotEquals(link1.getShortUrl(), link2.getShortUrl());
        assertEquals(originalUrl, link1.getOriginalUrl());
        assertEquals(originalUrl, link2.getOriginalUrl());
    }

    @Test
    void userCanOnlyEditOwnLinks() {
        linkService.login(user1, password1);
        ShortLink link = linkService.createShortLink("https://www.example.com", null, null, null, null);
        assertDoesNotThrow(() -> linkService.updateLink(link.getShortUrl(), "New Title", null, null, null));

        linkService.logout();
        linkService.login(user2, password2);
        assertThrows(AccessDeniedException.class,
                () -> linkService.updateLink(link.getShortUrl(), "Hacked Title", null, null, null));
        linkService.logout();
    }

    @Test
    void userCanOnlyDeleteOwnLinks() {
        linkService.login(user1, password1);
        ShortLink link = linkService.createShortLink("https://www.example.com", null, null, null, null);

        linkService.logout();
        linkService.login(user2, password2);
        assertThrows(AccessDeniedException.class, () -> linkService.deleteLink(link.getShortUrl()));

        linkService.logout();
        linkService.login(user1, password1);
        assertDoesNotThrow(() -> linkService.deleteLink(link.getShortUrl()));
        linkService.logout();
        assertThrows(LinkNotFoundException.class, () -> linkService.getLink(link.getShortUrl()));
    }

    @Test
    void multipleLinksPerUserShouldWorkCorrectly() {
        linkService.login(user1, password1);
        ShortLink link1 = linkService.createShortLink("https://www.example1.com", null, null, null, null);
        ShortLink link2 = linkService.createShortLink("https://www.example2.com", null, null, null, null);
        ShortLink link3 = linkService.createShortLink("https://www.example3.com", null, null, null, null);

        var userLinks = linkService.getUserLinks();
        assertEquals(3, userLinks.size());
        assertEquals(3, userLinks.stream().map(ShortLink::getShortUrl).distinct().count());
        linkService.logout();
    }
}
