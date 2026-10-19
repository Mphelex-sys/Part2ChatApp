package registrationandloginfeature;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Message {

    public static final int MAX_LENGTH = 250;

    // ─── Session-wide state (lives while the program is running) ─────────────
    private static final List<Message> sentMessages = new ArrayList<>();
    private static int totalSent = 0;
    private static String storeFile = "storedMessages.json";

    // ─── Per-message data ────────────────────────────────────────────────────
    private final String messageID;      // random ten-digit number
    private final int messageNumber;     // 0-based position of the message in this session
    private final String recipient;
    private final String text;
    private final String messageHash;

    /** Normal constructor: the Message ID is generated automatically. */
    public Message(int messageNumber, String recipient, String text) {
        this(generateMessageID(), messageNumber, recipient, text);
    }

    /** Constructor that accepts an ID (used by the unit tests so results are predictable). */
    public Message(String messageID, int messageNumber, String recipient, String text) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.text = text;
        this.messageHash = createMessageHash();
    }

    // ─── Required methods ────────────────────────────────────────────────────

    /** Ensures the message ID is not more than ten characters. */
    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= 10;
    }

    /** Validates this message's recipient (re-uses the Part 1 cell number rule). */
    public String checkRecipientCell() {
        return checkRecipientCell(recipient);
    }

    public static String checkRecipientCell(String cell) {
        if (cell != null && RegistrationAndLoginFeature.checkCellPhoneNumber(cell)) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain "
                + "an international code. Please correct the number and try again.";
    }

    /** Message length rule: no more than 250 characters. */
    public static String checkMessageLength(String text) {
        int length = text == null ? 0 : text.length();
        if (length <= MAX_LENGTH) {
            return "Message ready to send.";
        }
        return "Message exceeds 250 characters by " + (length - MAX_LENGTH)
                + "; please reduce the size.";
    }

    /**
     * Hash = first two digits of the ID, ":", message number, ":",
     * first word + last word of the message, all in capitals.
     * e.g. ID 0012345678, message 0, "Hi Mike, can you join us for dinner tonight?" -> 00:0:HITONIGHT
     */
    public String createMessageHash() {
        String idPart = messageID.substring(0, Math.min(2, messageID.length()));

        String[] words = text == null ? new String[]{""} : text.trim().split("\\s+");
        String first = cleanWord(words[0]);
        String last = cleanWord(words[words.length - 1]);

        return (idPart + ":" + messageNumber + ":" + first + last).toUpperCase();
    }

    /** Programmatic version: 1 = Send, 2 = Disregard, 3 = Store. */
    public String sentMessage(int choice) {
        switch (choice) {
            case 1:
                sentMessages.add(this);
                totalSent++;
                return "Message successfully sent.";
            case 2:
                return "Press 0 to delete the message.";
            case 3:
                return storeMessage() ? "Message successfully stored."
                                      : "Message could not be stored.";
            default:
                return "Invalid option. Please choose 1 (Send), 2 (Disregard) or 3 (Store).";
        }
    }

    /** Interactive version: asks the user whether to send, disregard or store. */
    public String sentMessage(Scanner in) {
        int choice = 0;
        while (choice < 1 || choice > 3) {
            System.out.println("What would you like to do with this message?");
            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            System.out.print("Enter choice: ");
            try {
                choice = Integer.parseInt(in.nextLine().trim());
            } catch (NumberFormatException e) {
                choice = 0;
            }
            if (choice < 1 || choice > 3) {
                System.out.println("Invalid option. Please choose 1, 2 or 3.");
            }
        }

        String result = sentMessage(choice);
        if (choice == 2) {
            // Keep asking until the user presses 0 to delete the message
            System.out.println(result);
            while (!in.nextLine().trim().equals("0")) {
                System.out.println(result);
            }
            return "Message deleted.";
        }
        return result;
    }

    /** Returns every message sent while the program has been running. */
    public static String printMessages() {
        if (sentMessages.isEmpty()) {
            return "No messages have been sent yet.";
        }
        StringBuilder sb = new StringBuilder();
        for (Message m : sentMessages) {
            sb.append(m.details()).append(System.lineSeparator());
        }
        return sb.toString().trim();
    }

    /** Total number of messages sent. */
    public static int returnTotalMessages() {
        return totalSent;
    }

    /** Appends this message to a JSON file (no external library needed). */
    public boolean storeMessage() {
        Path path = Paths.get(storeFile);
        String entry = toJson();
        try {
            String content = "[\n" + entry + "\n]";
            if (Files.exists(path)) {
                String existing = Files.readString(path, StandardCharsets.UTF_8).trim();
                if (existing.startsWith("[") && existing.endsWith("]")) {
                    String body = existing.substring(1, existing.length() - 1).trim();
                    if (!body.isEmpty()) {
                        content = "[\n" + body + ",\n" + entry + "\n]";
                    }
                }
            }
            Files.writeString(path, content, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /** Full details in the order required by the brief: ID, Hash, Recipient, Message. */
    public String details() {
        return String.format("Message ID: %s%nMessage Hash: %s%nRecipient: %s%nMessage: %s",
                messageID, messageHash, recipient, text);
    }

    public String toJson() {
        return "  {\n"
                + "    \"messageID\": \"" + escape(messageID) + "\",\n"
                + "    \"messageHash\": \"" + escape(messageHash) + "\",\n"
                + "    \"recipient\": \"" + escape(recipient) + "\",\n"
                + "    \"message\": \"" + escape(text) + "\"\n"
                + "  }";
    }

    public static String generateMessageID() {
        long number = ThreadLocalRandom.current().nextLong(0L, 10_000_000_000L);
        return String.format("%010d", number);
    }

    private static String cleanWord(String word) {
        return word.replaceAll("[^A-Za-z0-9]", "");
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    // Package-private hooks so unit tests start from a clean slate
    static void resetSession() {
        sentMessages.clear();
        totalSent = 0;
    }

    static void setStoreFile(String path) {
        storeFile = path;
    }

    public String getMessageID()   { return messageID; }
    public String getMessageHash() { return messageHash; }
    public String getRecipient()   { return recipient; }
    public String getText()        { return text; }
    public int getMessageNumber()  { return messageNumber; }
}