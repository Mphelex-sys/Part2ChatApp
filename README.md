# QuickChat – Registration, Login & Messaging (Java)

A console chat application. Users register, log in, and then send messages through QuickChat.
Package: `registrationandloginfeature`

## Project structure

**Source Packages → `registrationandloginfeature`**

| File | Part | Purpose |
|---|---|---|
| `RegistrationAndLoginFeature.java` | 1 (edited in 2) | Registration, login, validation, and the main menu |
| `Message.java` | 2 | Message class: ID, recipient check, hash, send/store/disregard, JSON storage |
| `QuickChat.java` | 2 | QuickChat menu and message-entry flow (opens only after a successful login) |

**Test Packages → `registrationandloginfeature`** (JUnit 4)

| File | Tests | Covers |
|---|---|---|
| `RegistrationAndLoginFeatureTest.java` | 18 | Username, password, cell number checks; register; login; login status |
| `MessageTest.java` | 18 | Message length, recipient, ID, hashes, send/disregard/store, JSON, totals |
| `MainTest.java` | 6 | `main()` driven by scripted input that ends with Exit/Quit |

## How the app works

1. **Chat App menu:** `1. Register`, `2. Login`, `3. Exit`.
2. After a successful login the app shows "Welcome to QuickChat." and asks how many messages you want to enter.
3. **QuickChat menu:** `1) Send Messages`, `2) Show recently sent messages` ("Coming Soon."), `3) Quit`.
4. For each message you enter the recipient (`+27` followed by 9 digits) and the message (max 250 characters), then choose **Send**, **Disregard** or **Store**.
5. After a send, the app shows Message ID, Message Hash, Recipient and Message. When all messages have been entered, it shows the total sent.
6. Quit shows the summary and ends the application.

## Message rules (from the brief)

- **Message ID:** random ten-digit number (`checkMessageID()` ensures it is not more than 10 characters).
- **Recipient:** must have the international code (`+27` + 9 digits). Re-uses the Part 1 `checkCellPhoneNumber`.
- **Message length:** up to 250 characters.
  - OK: "Message ready to send."
  - Too long: "Message exceeds 250 characters by X; please reduce the size."
- **Message hash:** first two digits of the ID, `:`, message number, `:`, first and last word, in capitals. Example: `00:0:HITONIGHT`.
- **Send / Disregard / Store:** "Message successfully sent." / "Press 0 to delete the message." / "Message successfully stored."
- **Storage:** stored messages are appended to `storedMessages.json` in the folder the program runs from (written without any external library).

## Edits made to the Part 1 file

Three small edits in `RegistrationAndLoginFeature.java`, each marked `// PART 2`:

1. New field: `static boolean isLoggedIn = false;`
2. `returnLoginStatus()` saves the login result: `isLoggedIn = loginUser();`
3. `main`, menu option 2: if login succeeded, run `QuickChat.run(input)`, then log out and end the program.

Registration, validation and login logic are otherwise unchanged. These edits are what enforce "users can only send messages if they have logged in successfully".

## Assumptions

- Message numbering starts at 0 (matches the `00:0:` in the brief's example hash).
- "Total sent" counts only messages that were sent (discarded and stored messages are not counted). With the brief's test data (message 1 sent, message 2 discarded) the total is 1.
- Recipient numbers must be `+27` followed by 9 digits. `08575975889` from the test data is rejected for having no international code.
- Quit ends the whole application.

## Running the app in NetBeans

1. Put the three source files in Source Packages → `registrationandloginfeature`.
2. Right-click `RegistrationAndLoginFeature.java` → **Run File**.
3. Register, log in, then use the QuickChat menu.

## Running the tests in NetBeans

1. Right-click the project → Properties → Libraries → add **JUnit 4** under the test libraries.
2. Put the three test files in Test Packages → `registrationandloginfeature`.
3. Right-click a test file → **Test File**, or right-click the project → **Test** (Alt+F6) to run everything.
4. Green ticks in the Test Results panel mean the tests passed. A red entry names the failing test with expected vs. actual values.

## Verification

Compiled with JDK 21. All 42 tests pass (18 + 18 + 6), and a scripted end-to-end run (register → login → QuickChat with the brief's test data) behaved as expected. The test environment could not download the real JUnit jar, so the tests were run with a small JUnit-4-compatible harness. The test code itself is standard JUnit 4.
