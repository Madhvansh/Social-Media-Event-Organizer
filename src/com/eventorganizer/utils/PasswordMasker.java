package com.eventorganizer.utils;

import java.io.Console;
import java.util.Scanner;

public final class PasswordMasker {
    @SuppressWarnings("resource")
    private static final Scanner STDIN = new Scanner(System.in);
    private static boolean warnedNoConsole = false;

    private PasswordMasker() {}

    public static char[] read(String prompt) {
        Console console = System.console();
        if (console != null) {
            char[] pw = console.readPassword("%s", prompt);
            return pw == null ? new char[0] : pw;
        }
        if (!warnedNoConsole) {
            System.out.println("[warning] console not attached; password input is visible");
            warnedNoConsole = true;
        }
        System.out.print(prompt);
        String line = STDIN.nextLine();
        return line == null ? new char[0] : line.toCharArray();
    }
}
