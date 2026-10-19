package registrationandloginfeature;

import java.util.Scanner;

public class QuickChat {

    /** Runs QuickChat. Only works if the user has logged in successfully. */
    public static void run(Scanner in) {

        // Requirement 1: only logged-in users may send messages
        if (!RegistrationAndLoginFeature.isLoggedIn) {
            System.out.println("You must log in successfully before you can use QuickChat.");
            return;
        }

        // Requirement 2: welcome message
        System.out.println("Welcome to QuickChat.");
        System.out.println("");

        // Requirement 5: user decides how many messages they will enter
        int maxMessages = 0;
        while (maxMessages < 1) {
            maxMessages = readInt(in, "How many messages do you wish to enter? ");
            if (maxMessages < 1) {
                System.out.println("Please enter a number greater than 0.");
            }
        }
        System.out.println("");

        int entered = 0;
        boolean running = true;

        // Requirement 4: keep running until the user quits
        while (running) {
            System.out.println("============ QuickChat Menu ============");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("");

            int choice = readInt(in, "Enter choice: ");
            System.out.println("");

            switch (choice) {
                case 1:
                    if (entered >= maxMessages) {
                        System.out.println("You have already entered all " + maxMessages + " messages.");
                    } else {
                        entered = sendMessages(in, entered, maxMessages);
                    }
                    break;

                case 2:
                    System.out.println("Coming Soon.");
                    break;

                case 3:
                    printSummary();
                    System.out.println("Thank you for using QuickChat.");
                    running = false;
                    break;

                default:
                    System.out.println("Unknown option. Please enter 1, 2, or 3.");
                    break;
            }
            System.out.println("");
        }
    }

    /** Collects the remaining messages (up to the limit) and returns the new count entered. */
    private static int sendMessages(Scanner in, int entered, int maxMessages) {

        while (entered < maxMessages) {
            System.out.println("------ Message " + (entered + 1) + " of " + maxMessages + " ------");

            String recipient = askForRecipient(in);
            String text = askForMessage(in);

            Message message = new Message(entered, recipient, text);
            if (message.checkMessageID()) {
                System.out.println("Message ID generated: " + message.getMessageID());
            }

            String result = message.sentMessage(in);
            System.out.println(result);

            // Requirement 7: show full details once the message has been sent
            if (result.equals("Message successfully sent.")) {
                System.out.println("");
                System.out.println(message.details());
            }
            System.out.println("");

            entered++;
        }

        // Requirement 6: total shown once all messages have been entered
        printSummary();
        return entered;
    }

    private static String askForRecipient(Scanner in) {
        while (true) {
            System.out.print("Enter recipient cell number (e.g. +27718693002): ");
            String cell = in.nextLine().trim();
            String result = Message.checkRecipientCell(cell);
            System.out.println(result);
            if (result.equals("Cell phone number successfully captured.")) {
                return cell;
            }
        }
    }

    private static String askForMessage(Scanner in) {
        while (true) {
            System.out.print("Enter your message (max 250 characters): ");
            String text = in.nextLine();
            String result = Message.checkMessageLength(text);
            if (result.equals("Message ready to send.")) {
                System.out.println(result);
                return text;
            }
            System.out.println("Please enter a message of less than 250 characters.");
            System.out.println(result);
        }
    }

    private static void printSummary() {
        System.out.println("Messages sent this session:");
        System.out.println(Message.printMessages());
        System.out.println("");
        System.out.println("Total messages sent: " + Message.returnTotalMessages());
    }

    private static int readInt(Scanner in, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(in.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }
}