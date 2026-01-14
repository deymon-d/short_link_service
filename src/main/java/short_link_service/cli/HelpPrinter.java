package short_link_service.cli;

public class HelpPrinter {
    public static void printWelcome() {
        System.out.println("\n" + "╔══════════════════════════════════════════════════════════╗\n"
                + "║                 SHORT LINK SERVICE                       ║\n"
                + "║           Advanced URL shortening with limits            ║\n"
                + "╚══════════════════════════════════════════════════════════╝\n");
    }

    public static void printHelp() {
        System.out.println("\n" + "╔══════════════════════════════════════════════════════════╗\n"
                + "║                         HELP MENU                        ║\n"
                + "╚══════════════════════════════════════════════════════════╝\n");

        System.out.println("Available commands in main menu:");
        System.out.println();
        System.out.println("  1. Create short link");
        System.out.println("     - Shorten any valid URL (http/https)");
        System.out.println("     - Set custom title, description, click limit, expiration");
        System.out.println("     - Default: 24h expiration, 10 click limit");
        System.out.println();
        System.out.println("  2. Redirect by short link");
        System.out.println("     - Enter short URL (e.g., clck.ru/ABC123)");
        System.out.println("     - Prints a link that can be opened in a browser");
        System.out.println("     - Tracks clicks and enforces limits");
        System.out.println();
        System.out.println("  3. List my links");
        System.out.println("     - View all your created links");
        System.out.println("     - Shows status, clicks, expiration");
        System.out.println();
        System.out.println("  4. Update link");
        System.out.println("     - Modify link properties (title, description, limits)");
        System.out.println("     - Only link owner can update");
        System.out.println();
        System.out.println("  5. Delete link");
        System.out.println("     - Permanently remove a link");
        System.out.println("     - Requires ownership");
        System.out.println();
        System.out.println("  6. Logout");
        System.out.println();
        System.out.println("  7. Help");
        System.out.println("     - Show this help message");
        System.out.println();
        System.out.println("  8. Exit");
        System.out.println("     - Gracefully shutdown the application");
        System.out.println();
    }
}
