package registrationandloginfeature;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MessageTest {

    // ── Test data from the brief ─────────────────────────────────────────────
    private static final String RECIPIENT_1 = "+27718693002";
    private static final String MESSAGE_1   = "Hi Mike, can you join us for dinner tonight?";
    private static final String RECIPIENT_2 = "08575975889";
    private static final String MESSAGE_2   = "Hi Keegan, did you receive the payment?";

    private Path storeFile;

    @Before
    public void setUp() throws IOException {
        Message.resetSession();
        storeFile = Files.createTempFile("quickchat-test", ".json");
        Files.deleteIfExists(storeFile);          // start with no file
        Message.setStoreFile(storeFile.toString());
    }

    @After
    public void tearDown() throws IOException {
        Files.deleteIfExists(storeFile);
    }

    // ─── Message length ──────────────────────────────────────────────────────

    @Test
    public void messageLength_success() {
        assertEquals("Message ready to send.", Message.checkMessageLength(MESSAGE_1));
    }

    @Test
    public void messageLength_exactly250_success() {
        assertEquals("Message ready to send.", Message.checkMessageLength("a".repeat(250)));
    }

    @Test
    public void messageLength_failure() {
        assertEquals("Message exceeds 250 characters by 10; please reduce the size.",
                Message.checkMessageLength("a".repeat(260)));
    }

    // ─── Recipient number ────────────────────────────────────────────────────

    @Test
    public void recipient_success() {
        Message m = new Message("0012345678", 0, RECIPIENT_1, MESSAGE_1);
        assertEquals("Cell phone number successfully captured.", m.checkRecipientCell());
    }

    @Test
    public void recipient_failure() {
        Message m = new Message("0012345678", 1, RECIPIENT_2, MESSAGE_2);
        assertEquals("Cell phone number is incorrectly formatted or does not contain "
                + "an international code. Please correct the number and try again.",
                m.checkRecipientCell());
    }

    // ─── Message ID ──────────────────────────────────────────────────────────

    @Test
    public void messageID_generated_isValid() {
        Message m = new Message(0, RECIPIENT_1, MESSAGE_1);
        assertEquals(10, m.getMessageID().length());
        assertTrue(m.getMessageID().matches("[0-9]{10}"));
        assertTrue(m.checkMessageID());
    }

    @Test
    public void messageID_tooLong_fails() {
        Message m = new Message("12345678901", 0, RECIPIENT_1, MESSAGE_1);
        assertFalse(m.checkMessageID());
    }

    // ─── Message hash ────────────────────────────────────────────────────────

    @Test
    public void hash_testCase1() {
        Message m = new Message("0012345678", 0, RECIPIENT_1, MESSAGE_1);
        assertEquals("00:0:HITONIGHT", m.createMessageHash());
        assertEquals("00:0:HITONIGHT", m.getMessageHash());
    }

    @Test
    public void hash_loopOverRemainingMessages() {
        String[][] data = {
            // id,          number, message,                                   expected hash
            {"0012345678", "0", "Hi Mike, thanks",                              "00:0:HITHANKS"},
            {"0098765432", "1", MESSAGE_2,                                      "00:1:HIPAYMENT"},
            {"5512345678", "2", "Hello",                                        "55:2:HELLOHELLO"},
            {"1234567890", "3", "  Meet me at the park!  ",                     "12:3:MEETPARK"},
        };

        for (String[] row : data) {
            Message m = new Message(row[0], Integer.parseInt(row[1]), RECIPIENT_1, row[2]);
            assertEquals(row[3], m.createMessageHash());
        }
    }

    // ─── Send / disregard / store ────────────────────────────────────────────

    @Test
    public void sentMessage_send() {
        Message m = new Message("0012345678", 0, RECIPIENT_1, MESSAGE_1);
        assertEquals("Message successfully sent.", m.sentMessage(1));
        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    public void sentMessage_disregard() {
        Message m = new Message("0098765432", 1, RECIPIENT_2, MESSAGE_2);
        assertEquals("Press 0 to delete the message.", m.sentMessage(2));
        assertEquals(0, Message.returnTotalMessages());
    }

    @Test
    public void sentMessage_store() throws IOException {
        Message m = new Message("0012345678", 0, RECIPIENT_1, MESSAGE_1);
        assertEquals("Message successfully stored.", m.sentMessage(3));
        assertTrue(Files.exists(storeFile));

        String json = Files.readString(storeFile);
        assertTrue(json.contains("\"messageHash\": \"00:0:HITONIGHT\""));
        assertTrue(json.contains("\"recipient\": \"+27718693002\""));
        assertEquals(0, Message.returnTotalMessages());   // stored, not sent
    }

    @Test
    public void storeMessage_twoMessages_appends() throws IOException {
        new Message("0012345678", 0, RECIPIENT_1, MESSAGE_1).storeMessage();
        new Message("0098765432", 1, RECIPIENT_1, MESSAGE_2).storeMessage();

        String json = Files.readString(storeFile).trim();
        assertTrue(json.startsWith("["));
        assertTrue(json.endsWith("]"));
        assertTrue(json.contains("00:0:HITONIGHT"));
        assertTrue(json.contains("00:1:HIPAYMENT"));
    }

    @Test
    public void toJson_escapesQuotes() {
        Message m = new Message("0012345678", 0, RECIPIENT_1, "He said \"hi\"");
        assertTrue(m.toJson().contains("He said \\\"hi\\\""));
    }

    @Test
    public void sentMessage_invalidChoice() {
        Message m = new Message("0012345678", 0, RECIPIENT_1, MESSAGE_1);
        assertEquals("Invalid option. Please choose 1 (Send), 2 (Disregard) or 3 (Store).",
                m.sentMessage(9));
    }

    // ─── Whole test-data scenario + printing ─────────────────────────────────

    @Test
    public void testData_totalMessages() {
        Message m1 = new Message("0012345678", 0, RECIPIENT_1, MESSAGE_1);
        Message m2 = new Message("0098765432", 1, RECIPIENT_2, MESSAGE_2);

        m1.sentMessage(1);   // Select Send
        m2.sentMessage(2);   // Select Discard

        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    public void printMessages_listsSentMessages() {
        new Message("0012345678", 0, RECIPIENT_1, MESSAGE_1).sentMessage(1);

        String output = Message.printMessages();
        assertTrue(output.contains("Message ID: 0012345678"));
        assertTrue(output.contains("Message Hash: 00:0:HITONIGHT"));
        assertTrue(output.contains("Recipient: +27718693002"));
        assertTrue(output.contains("Message: " + MESSAGE_1));
    }

    @Test
    public void printMessages_empty() {
        assertEquals("No messages have been sent yet.", Message.printMessages());
    }
}