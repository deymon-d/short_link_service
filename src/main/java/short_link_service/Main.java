package short_link_service;

import short_link_service.cli.CliController;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        try {
            PrintStream utf8PrintStreamOut = new PrintStream(System.out, true, StandardCharsets.UTF_8.name());
            PrintStream utf8PrintStreamErr = new PrintStream(System.err, true, StandardCharsets.UTF_8.name());
            System.setOut(utf8PrintStreamOut);
            System.setErr(utf8PrintStreamErr);
            CliController commandHandler = new CliController();
            commandHandler.start();
        } catch (Exception e) {
            System.err.println("Критическая ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
