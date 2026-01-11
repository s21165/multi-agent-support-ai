package me.ather.support;

import java.util.Scanner;

/**
 * Main entry point for the AI Support System.
 * Handles the console-based user interface and communication loop.
 */
public class Main {
    public static void main(String[] args) {
        Orchestrator orchestrator = new Orchestrator();
        Scanner scanner = new Scanner(System.in);

        // ANSI escape codes for console styling
        String green = "\u001B[32m";
        String blue = "\u001B[34m";
        String reset = "\u001B[0m";

        System.out.println("==========================================");
        System.out.println("   Multi-Agent Support System Online");
        System.out.println("   Agents: Technical (A) & Billing (B)");
        System.out.println("==========================================\n");
        System.out.println("Type your message or 'exit' to quit.\n");

        while (true) {
            System.out.print(blue + "User: " + reset);
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Shutting down the system...");
                break;
            }

            if (input.isBlank()) continue;

            try {
                // The Orchestrator classifies intent and routes to the correct agent
                String response = orchestrator.handleUserQuery(input);

                System.out.println(green + "Assistant: " + reset + response);
                System.out.println("------------------------------------------");
            } catch (Exception e) {
                System.err.println("SYSTEM ERROR: " + e.getMessage());
            }
        }
        scanner.close();
    }
}