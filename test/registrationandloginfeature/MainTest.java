package registrationandloginfeature;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * Tests main() by feeding it scripted keyboard input that always finishes with
 * Exit (3) or Quit (3), so the menu loop ends and the test completes.
 */
public class MainTest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream captured;
    private Path storeFile;

    @Before
    public void setUp() throws IOException {
        // clean slate
        RegistrationAndLoginFeature.username = null;
        RegistrationAndLoginFeature.password = null;
        RegistrationAndLoginFeature.cellphone = null;
        RegistrationAndLoginFeature.isRegistered = false;
        RegistrationAndLoginFeature.isLoggedIn = false;
        Message.resetSession();
        storeFile = Files.createTempFile("quickchat-main-test", ".json");
        Files.deleteIfExists(storeFile);
        Message.setStoreFile(storeFile.toString());

        // capture everything main() prints
        captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
    }

    @After
    public void tearDown() throws IOException {
        System.setOut(originalOut);
        Files.deleteIfExists(storeFile);
    }

    /** Feeds the given lines to the app as if typed, runs main(), returns what was printed. */
    private String runMainWith(String... lines) {
        String script = String.join("\n", lines) + "\n";
        RegistrationAndLoginFeature.input = new Scanner(new ByteArrayInputStream(script.getBytes()));
        RegistrationAndLoginFeature.main(new String[0]);
        return captured.toString();
    }

    @Test
    public void exit_endsTheProgram() {
        String out = runMainWith("3");
        assertTrue(out.contains("Thank you for using ChatApp."));
    }

    @Test
    public void unknownOption_isReported_thenExit() {
        String out = runMainWith("9", "3");
        assertTrue(out.contains("Unknown option. Please enter 1, 2, or 3."));
        assertTrue(out.contains("Thank you for using ChatApp."));
    }

    @Test
    public void login_beforeRegistering_isRefused_andQuickChatStaysClosed() {
        String out = runMainWith("2", "3");
        assertTrue(out.contains("You have not yet registered."));
        assertTrue(out.contains("Username or password incorrect, please try again."));
        assertFalse(out.contains("Welcome to QuickChat."));
    }

    @Test
    public void login_withWrongPassword_doesNotOpenQuickChat() {
        String out = runMainWith(
                "1", "kt_kn", "Ab1!defg", "+27831234567",   // register
                "2", "kt_kn", "WrongPass1!",                 // bad login
                "3");                                        // exit
        assertTrue(out.contains("Successfully registered."));
        assertFalse(out.contains("Welcome to QuickChat."));
        assertEquals(0, Message.returnTotalMessages());
    }

    @Test
    public void fullFlow_register_login_sendOne_discardOne_quit() {
        String out = runMainWith(
                "1", "kt_kn", "Ab1!defg", "+27831234567",   // register
                "2", "kt_kn", "Ab1!defg",                    // login
                "2",                                         // QuickChat: enter 2 messages
                "2",                                         // menu: Show recent -> Coming Soon.
                "1",                                         // menu: Send Messages
                "+27718693002",
                "Hi Mike, can you join us for dinner tonight?",
                "1",                                         // Send
                "08575975889",                               // rejected (no international code)
                "+27831112222",
                "Hi Keegan, did you receive the payment?",
                "2",                                         // Disregard
                "0",                                         // press 0 to delete
                "3");                                        // menu: Quit

        assertTrue(out.contains("Welcome kt_kn it is great to see you."));
        assertTrue(out.contains("Welcome to QuickChat."));
        assertTrue(out.contains("Coming Soon."));
        assertTrue(out.contains("Message ID generated:"));
        assertTrue(out.contains("Message successfully sent."));
        assertTrue(out.contains("HITONIGHT"));
        assertTrue(out.contains("Cell phone number is incorrectly formatted or does not contain "
                + "an international code. Please correct the number and try again."));
        assertTrue(out.contains("Press 0 to delete the message."));
        assertTrue(out.contains("Total messages sent: 1"));
        assertTrue(out.contains("Thank you for using QuickChat."));
        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    public void quickChat_onlyAllowsTheChosenNumberOfMessages() {
        String out = runMainWith(
                "1", "kt_kn", "Ab1!defg", "+27831234567",
                "2", "kt_kn", "Ab1!defg",
                "1",                                         // only 1 message allowed
                "1",                                         // Send Messages
                "+27718693002", "Hi Mike, thanks", "1",      // the one message
                "1",                                         // try to send again
                "3");                                        // quit
        assertTrue(out.contains("You have already entered all 1 messages."));
        assertEquals(1, Message.returnTotalMessages());
    }
}