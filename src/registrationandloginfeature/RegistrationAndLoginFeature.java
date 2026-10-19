package registrationandloginfeature;

import java.util.Scanner;

public class RegistrationAndLoginFeature {

    static Scanner input = new Scanner(System.in);
    static String username = null;
    static String password = null;
    static String cellphone = null;
    static boolean isRegistered = false;

    public static void main(String[] args) {

        int i = 0;

        while (i < 1) {
            System.out.println("================Chat App================");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.println("");
            System.out.print("Enter choice: ");
            int choice = input.nextInt();
            input.nextLine();

            switch (choice) {
                case 1:
                    registerUser();
                    break;

                case 2:
                    String status = returnLoginStatus();
                    System.out.println(status);
                    break;

                case 3:
                    System.out.println("Thank you for using ChatApp.");
                    i = 2;
                    break;

                default:
                    System.out.println("Unknown option. Please enter 1, 2, or 3.");
                    break;
            }
        }
    }

    // ─── Validation methods ──────────────────────────────────────────────────

    public static boolean checkUserName(String userName) {
        // Must contain underscore AND be no more than 5 characters
        return userName.contains("_") && userName.length() <= 5;
    }

    public static boolean checkPasswordComplexity(String passWord) {
        // At least 8 chars, one capital, one digit, one special character.
        boolean longEnough   = passWord.length() >= 8;
        boolean hasUpper     = passWord.matches(".*[A-Z].*");
        boolean hasDigit     = passWord.matches(".*[0-9].*");
        boolean hasSpecial   = passWord.matches(".*[^A-Za-z0-9].*");
        return longEnough && hasUpper && hasDigit && hasSpecial;
    }

    public static boolean checkCellPhoneNumber(String cellPhone) {
        // Must have country code.
        return cellPhone.matches("\\+27[0-9]{9}");
    }

    // ─── Registration ────────────────────────────────────────────────────────

    public static void registerUser() {
        System.out.println("Welcome to registration.");
        System.out.println("");

        // ── Username ──────────────────────────────────────────────────────────
        String enteredUsername;
        while (true) {
            System.out.print("Enter username: ");
            enteredUsername = input.nextLine();
            if (checkUserName(enteredUsername)) {
                System.out.println("Username successfully captured.");
                System.out.println("");
                break;
            } else {
                System.out.println("Username is not correctly formatted, please ensure that your username contains an underscore and is no more than five characters in length.");
                System.out.println("");
            }
        }

        // ── Password ──────────────────────────────────────────────────────────
        String enteredPassword;
        while (true) {
            System.out.print("Enter password: ");
            enteredPassword = input.nextLine();
            if (checkPasswordComplexity(enteredPassword)) {
                System.out.println("Password successfully captured.");
                   break;
            } else {
                System.out.println("Password is not correctly formatted, please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.");
                System.out.println("");
            }
        }

        // ── Cell phone ────────────────────────────────────────────────────────
        String enteredCell;
        while (true) {
            System.out.print("Enter cellphone number (e.g. +27831234567): ");
            enteredCell = input.nextLine();
            if (checkCellPhoneNumber(enteredCell)) {
                System.out.println("Cell phone number successfully added.");
                System.out.println("");
                break;
            } else {
            System.out.println("");
                 System.out.println("Cell phone number incorrectly formatted " +
                        "or does not contain international code.");
                System.out.println("");
            }
        }

        // ── Save and confirm ──────────────────────────────────────────────────
        username     = enteredUsername;
        password     = enteredPassword;
        cellphone    = enteredCell;
        isRegistered = true;

        System.out.println("Successfully registered.");
        System.out.println("");
    }

    // ─── Login ───────────────────────────────────────────────────────────────

    public static boolean loginUser() {

        if (!isRegistered) {
            System.out.println("You have not yet registered. " +
                    "Please register first.");
            System.out.println("");
            return false;
        }

        // ── Username check ────────────────────────────────────────────────────
        System.out.print("Enter username: ");
        String enteredUsername = input.nextLine();
        if (!enteredUsername.equals(username)) {
            System.out.println("Username or password incorrect, please try again.");
            System.out.println("");
            return false;
        }

        // ── Password check ────────────────────────────────────────────────────
        System.out.print("Enter password: ");
        String enteredPassword = input.nextLine();
        if (!enteredPassword.equals(password)) {
            System.out.println("Username or password incorrect, please try again.");
            System.out.println("");
            return false;
        }

        return true;
    }

    public static String returnLoginStatus() {
        if (loginUser()) {
            // Brief requires: "Welcome <user first name>, <user last name> it is great to see you."
            // Using username in place of first/last name until those fields are added.
            return "Welcome " + username + " it is great to see you.";
        } else {
            return "Username or password incorrect, please try again.";
        }
    }
}