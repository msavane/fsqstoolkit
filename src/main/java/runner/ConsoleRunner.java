package runner;

import builder.TestCaseBuilder;
import dto.TestCaseDto;
import parser.TestCaseParser;
import service.TestCaseService;
import util.FileDiscoveryUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class ConsoleRunner {

    public void run(TestCaseDto initialTestCase) {
        Scanner scanner = new Scanner(System.in);
        TestCaseBuilder builder = new TestCaseBuilder();
        TestCaseService service = new TestCaseService();
        TestCaseDto testCase = initialTestCase;

        boolean continueRunning = true;

        while (continueRunning) {
            if (testCase == null) {
                System.out.println("🎉 Welcome to FSQS Toolkit!");
                System.out.println("How would you like to create a test?");
                System.out.println("1. Load from file");
                System.out.println("2. Create new test case");

                String inputMethod = promptChoice(scanner, "Select option [1/2]:", "1", "2");

                if (inputMethod.equals("1")) {
                    try {
                        System.out.println("Select test type to load:");
                        System.out.println("1. FSQS Legacy Script (*.txt)");
                        System.out.println("2. Gherkin Cucumber Feature (*.feature)");

                        String styleChoice = promptChoice(scanner, "Choose [1/2]:", "1", "2");
                        String extension = styleChoice.equals("2") ? ".feature" : ".txt";

                        // ✅ Correct path to match your file location
                        String folderPath = "src/main/resources/testcases";
                        //List<Path> availableFiles = FileDiscoveryUtil.discoverTestFiles(folderPath, extension);
                        String resourceFolder = "testcases";
                        List<Path> availableFiles = FileDiscoveryUtil.discoverTestFiles(resourceFolder, extension);


                        if (availableFiles.isEmpty()) {
                            System.out.println("⚠ No test cases found for that type.");
                            continue;
                        }

                        System.out.println("Available test cases:");
                        for (int i = 0; i < availableFiles.size(); i++) {
                            System.out.println((i + 1) + ". " + availableFiles.get(i).getFileName());
                        }

                        int choice = Integer.parseInt(promptForInput(scanner, "Enter number to load:"));
                        Path selected = availableFiles.get(choice - 1);

                        TestCaseParser parser = new TestCaseParser();
                        testCase = parser.loadFromScriptFile(selected.getFileName().toString());

                    } catch (IOException | NumberFormatException e) {
                        System.err.println("❌ Failed to load test case: " + e.getMessage());
                        continue;
                    }

                } else {
                    System.out.println("Which type of test case do you want to create?");
                    System.out.println("1. Standard UI Test");
                    System.out.println("2. Gherkin Feature Test");
                    System.out.println("3. REST API Test");

                    String typeChoice = promptChoice(scanner, "Select [1/2/3]:", "1", "2", "3");

                    switch (typeChoice) {
                        case "1" -> testCase = builder.buildFromInput();
                        case "2" -> testCase = builder.buildGherkinFromInput();
                        case "3" -> testCase = builder.buildApiTestFromInput();
                        default -> {
                            System.out.println("❌ Invalid choice.");
                            continue;
                        }
                    }
                }
            }

            service.printTestCaseSummary(testCase);

            String runNow = promptYesNo(scanner, "Run this test case now?");
            if (runNow.equals("y")) {
                String style = promptChoice(scanner, "Choose execution style: (1) Standard  (2) Gherkin  (3) REST", "1", "2", "3");

                switch (style) {
                    case "1" -> service.runTestCase(testCase);
                    case "2" -> service.runGherkinStyleTest(testCase);
                    case "3" -> service.runRestTestCase(testCase);
                }

                System.out.println("✅ Test run complete.");
            }

            String saveNow = promptYesNo(scanner, "Would you like to save this test case?");
            if (saveNow.equals("y")) {
                String style = promptChoice(scanner, "Choose save format: (1) Standard  (2) Gherkin", "1", "2");
                String filename = promptForInput(scanner, "Enter filename to save to (e.g., test.txt):");

                boolean saveAsGherkin = style.equals("2");
                service.saveTestCaseToFile(testCase, filename, saveAsGherkin);
                System.out.println("💾 Test case saved to: " + filename);
            }

            String again = promptYesNo(scanner, "Create another test case?");
            continueRunning = again.equals("y");
            testCase = null; // reset for next iteration
        }

        System.out.println("👋 Exiting FSQS Toolkit. Goodbye!");
        scanner.close();
    }

    private String promptYesNo(Scanner scanner, String message) {
        String input;
        do {
            System.out.println(message + " [y/n]");
            input = scanner.nextLine().trim().toLowerCase();
        } while (!input.equals("y") && !input.equals("n"));
        return input;
    }

    private String promptChoice(Scanner scanner, String message, String... validOptions) {
        String input;
        boolean isValid;
        do {
            System.out.println(message);
            input = scanner.nextLine().trim();
            isValid = false;
            for (String option : validOptions) {
                if (option.equals(input)) {
                    isValid = true;
                    break;
                }
            }
        } while (!isValid);
        return input;
    }

    private String promptForInput(Scanner scanner, String message) {
        System.out.println(message);
        return scanner.nextLine().trim();
    }
}
