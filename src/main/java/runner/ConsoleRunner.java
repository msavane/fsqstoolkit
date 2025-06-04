package runner;

import builder.TestCaseBuilder;
import dto.TestCaseDto;
import service.TestCaseService;

import java.util.Scanner;

public class ConsoleRunner {

    public void run(TestCaseDto testCase) {
        Scanner scanner = new Scanner(System.in);
        TestCaseBuilder builder = new TestCaseBuilder();
        TestCaseService service = new TestCaseService();

        boolean continueRunning = true;

        while (continueRunning) {
            if (testCase == null) {
                testCase = builder.buildFromInput(); // only prompt if no test case passed
            }

            service.printTestCaseSummary(testCase);

            String runNow = promptYesNo(scanner, "Run this test case now?");
            if (runNow.equals("y")) {
                String style = promptChoice(scanner, "Choose execution style: (1) Standard  (2) Gherkin", "1", "2");

                if (style.equals("1")) {
                    service.runTestCase(testCase);
                } else {
                    service.runGherkinStyleTest(testCase);
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
            testCase = null; // reset only if they want another one
        }

        System.out.println("👋 Exiting FSQS Toolkit. Goodbye!");
        scanner.close();
    }

    // ... [prompt methods remain the same] ...


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
