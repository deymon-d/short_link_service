package short_link_service.cli;

import short_link_service.config.AppConfig;
import short_link_service.core.generator.LinkGenerator;
import short_link_service.core.generator.SecureHashLinkGenerator;
import short_link_service.core.service.ShortLinkService;
import short_link_service.core.model.ShortLink;
import short_link_service.core.validator.InputValidator;
import short_link_service.exception.*;
import short_link_service.notification.ConsoleNotificationService;
import short_link_service.notification.NotificationService;

import java.awt.*;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;

public class CliController {
    private final ShortLinkService linkService;
    private final Scanner scanner;
    private boolean running = true;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public CliController() {
        LinkGenerator linkGenerator = new SecureHashLinkGenerator();
        NotificationService notificationService = new ConsoleNotificationService();
        this.linkService = new ShortLinkService(linkGenerator, notificationService);
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        HelpPrinter.printWelcome();
        authenticateUser();
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            try {
                handleChoice(choice);
            } catch (Exception e) {
                printError("Error: " + e.getMessage());
            }
        }
    }

    private void authenticateUser() {
        while (running && !linkService.isUserLoggedIn()) {
            printHeader("Authentication");
            System.out.println("1. Login");
            System.out.println("2. Register new user");
            System.out.println("3. Redirect by short link");
            System.out.println("4. Exit");
            System.out.print("Choose option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    login();
                    break;
                case "2":
                    register();
                    break;
                case "3":
                    redirectToLink();
                    break;
                case "4":
                    exit();
                    break;
                default :
                    printError("Invalid choice");
            }
        }
    }

    private void login() {
        System.out.print("Логин: ");
        String login = scanner.nextLine().trim();

        System.out.print("Пароль: ");
        String password = scanner.nextLine();

        try {
            linkService.login(login, password);
            printSuccess("Вход выполнен успешно!");
        } catch (Exception e) {
            printError(e.getMessage());
        }
    }

    private void register() {
        System.out.print("Логин: ");
        String login = scanner.nextLine().trim();

        System.out.print("Пароль: ");
        String password = scanner.nextLine();

        try {
            InputValidator.validateLogin(login);
            InputValidator.validatePassword(password);

            linkService.register(login, password);
            printSuccess("Регистрация успешна! Теперь войдите в систему.");
        } catch (Exception e) {
            printError(e.getMessage());
        }
    }

    private void printMenu() {
        linkService.checkAndDeleteInactiveLinks();
        printHeader("Main Menu");
        System.out.println("1. Create short link");
        System.out.println("2. Redirect by short link");
        System.out.println("3. List my links");
        System.out.println("4. Update link");
        System.out.println("5. Delete link");
        System.out.println("6. Logout user");
        System.out.println("7. Help");
        System.out.println("8. Exit");
        System.out.print("Choose action: ");
    }

    private void handleChoice(String choice) {
        switch (choice) {
            case "1":
                createShortLink();
                break;
            case "2":
                redirectToLink();
                break;
            case "3":
                listUserLinks();
                break;
            case "4":
                updateLink();
                break;
            case "5":
                deleteLink();
                break;
            case "6":
                logout();
                break;
            case "7":
                HelpPrinter.printHelp();
                break;
            case "8":
                exit();
                break;
            default :
                printError("Invalid choice. Type '7' for help.");
        }
    }

    private void createShortLink() {
        printHeader("Create Short Link");

        System.out.print("Enter URL to shorten: ");
        String url = scanner.nextLine().trim();

        System.out.print("Title (optional): ");
        String title = scanner.nextLine().trim();
        if (title.isEmpty()) {
            title = null;
        }

        System.out.print("Description (optional): ");
        String description = scanner.nextLine().trim();
        if (description.isEmpty()) {
            description = null;
        }

        System.out.print("Max clicks (default " + AppConfig.DEFAULT_MAX_CLICKS + "): ");
        String maxClicksStr = scanner.nextLine().trim();
        Integer maxClicks = null;
        if (!maxClicksStr.isEmpty()) {
            try {
                maxClicks = Integer.parseInt(maxClicksStr);
            } catch (NumberFormatException e) {
                printError("Invalid number. Using default.");
            }
        }

        System.out.print("Expiration date (yyyy-MM-dd HH:mm, optional): ");
        String expStr = scanner.nextLine().trim();
        LocalDateTime expirationDate = null;
        if (!expStr.isEmpty()) {
            try {
                expirationDate = LocalDateTime.parse(expStr, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                printError("Invalid date format. Using default (24h).");
            }
        }

        try {
            ShortLink link = linkService.createShortLink(url, title, description, maxClicks, expirationDate);

            printSuccess("Link created successfully!");
            System.out.println("Short URL: " + link.getShortUrl());
            System.out.println("Original URL: " + link.getOriginalUrl());
            System.out.println("Expires: " + link.getExpirationDate().format(DATE_FORMATTER));
            System.out.println("Max clicks: " + link.getMaxClicks());

        } catch (ValidationException e) {
            printError("Validation error: " + e.getMessage());
        } catch (AccessDeniedException e) {
            printError("Access denied: " + e.getMessage());
        }
    }

    private void redirectToLink() {
        printHeader("Redirect");

        System.out.print("Enter short URL (e.g., " + AppConfig.SHORT_URL_PREFIX + "ABC123): ");
        String shortUrl = scanner.nextLine().trim();

        try {
            String originalUrl = linkService.redirect(shortUrl);

            printSuccess("Redirecting to: " + originalUrl);

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {

                System.out.print("Open in browser? (y/n): ");
                String open = scanner.nextLine().trim().toLowerCase();

                if (open.equals("y") || open.equals("yes")) {
                    try {
                        Desktop.getDesktop().browse(new URI(originalUrl));
                        System.out.println("Browser opened successfully.");
                    } catch (Exception e) {
                        printError("Could not open browser: " + e.getMessage());
                    }
                }
            } else {
                System.out.println("Desktop browsing not supported on this system.");
                System.out.println("Manual URL: " + originalUrl);
            }

        } catch (LinkNotFoundException e) {
            printError("Link not found: " + e.getMessage());
        } catch (LinkExpiredException e) {
            printError("Link expired: " + e.getMessage());
        } catch (LinkLimitExceededException e) {
            printError("Link limit exceeded: " + e.getMessage());
        }
    }

    private void listUserLinks() {
        printHeader("Your Links");

        List<ShortLink> links = linkService.getUserLinks();

        if (links.isEmpty()) {
            System.out.println("No links found.");
            return;
        }

        System.out.println("Total links: " + links.size());
        System.out.println();

        for (ShortLink link : links) {
            if (link.getTitle() != null) {
                System.out.println("  Title: " + link.getTitle());
            }
            System.out.println("  Short: " + link.getShortUrl());
            System.out.println("  Original: " + link.getOriginalUrl());
            System.out.println("  Clicks: " + link.getClickCount() + "/" + link.getMaxClicks());
            System.out.println("  Created: " + link.getCreationDate().format(DATE_FORMATTER));
            System.out.println("  Expires: " + link.getExpirationDate().format(DATE_FORMATTER));
            System.out.println();
        }
    }

    private void updateLink() {
        printHeader("Update Link");

        System.out.print("Enter short URL to update: ");
        String shortUrl = scanner.nextLine().trim();

        try {
            ShortLink link = linkService.getLink(shortUrl);

            System.out.println("Current values:");
            System.out.println("  Title: " + (link.getTitle() != null ? link.getTitle() : "(none)"));
            System.out.println("  Description: " + (link.getDescription() != null ? link.getDescription() : "(none)"));
            System.out.println("  Max clicks: " + link.getMaxClicks());
            System.out.println("  Expires: " + link.getExpirationDate().format(DATE_FORMATTER));
            System.out.println();

            System.out.print("New title (press Enter to keep current): ");
            String title = scanner.nextLine().trim();
            if (title.isEmpty()) {
                title = null;
            }

            System.out.print("New description (press Enter to keep current): ");
            String description = scanner.nextLine().trim();
            if (description.isEmpty()) {
                description = null;
            }

            System.out.print("New max clicks (press Enter to keep current): ");
            String maxClicksStr = scanner.nextLine().trim();
            Integer maxClicks = null;
            if (!maxClicksStr.isEmpty()) {
                try {
                    maxClicks = Integer.parseInt(maxClicksStr);
                } catch (NumberFormatException e) {
                    printError("Invalid number. Keeping current.");
                }
            }

            System.out.print("New expiration date (yyyy-MM-dd HH:mm, press Enter to keep current): ");
            String expStr = scanner.nextLine().trim();
            LocalDateTime expirationDate = null;
            if (!expStr.isEmpty()) {
                try {
                    expirationDate = LocalDateTime.parse(expStr, DATE_FORMATTER);
                } catch (DateTimeParseException e) {
                    printError("Invalid date format. Keeping current.");
                }
            }
            linkService.updateLink(shortUrl, title, description, maxClicks, expirationDate);
            printSuccess("Link updated successfully!");

        } catch (LinkNotFoundException e) {
            printError("Link not found: " + e.getMessage());
        } catch (AccessDeniedException e) {
            printError("Access denied: " + e.getMessage());
        } catch (ValidationException e) {
            printError("Validation error: " + e.getMessage());
        }
    }

    private void deleteLink() {
        printHeader("Delete Link");
        System.out.print("Enter short URL to delete: ");
        String shortUrl = scanner.nextLine().trim();
        System.out.print("Are you sure? This cannot be undone. (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.equals("y") && !confirm.equals("yes")) {
            System.out.println("Deletion cancelled.");
            return;
        }
        try {
            linkService.deleteLink(shortUrl);
            printSuccess("Link deleted successfully!");
        } catch (LinkNotFoundException e) {
            printError("Link not found: " + e.getMessage());
        } catch (AccessDeniedException e) {
            printError("Access denied: " + e.getMessage());
        }
    }

    private void logout() {
        printHeader("Logout");
        linkService.logout();
        authenticateUser();
    }

    private void exit() {
        printHeader("Exit");
        running = false;
        System.out.println("Goodbye!");
    }

    private void printHeader(String title) {
        System.out.println();
        System.out.println("=== " + title + " ===");
    }

    private static void printSuccess(String message) {
        System.out.println("УСПЕХ: " + message);
    }

    private static void printError(String message) {
        System.out.println("ОШИБКА: " + message);
    }
}
