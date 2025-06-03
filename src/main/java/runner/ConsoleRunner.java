package runner;

import builder.TestCaseBuilder;
import dto.TestCaseDto;
import service.TestCaseService;

import java.util.Scanner;

public class ConsoleRunner {

    public void run() {
        Scanner scanner = new Scanner(System.in);
        TestCaseBuilder builder = new TestCaseBuilder();
        TestCaseService service = new TestCaseService();

        boolean continueRunning = true;

        while (continueRunning) {
            TestCaseDto testCase = builder.buildFromInput();
            service.printTestCaseSummary(testCase);

            String runNow = promptYesNo(scanner, "Run this test case now? (y/n)");
            if (runNow.equalsIgnoreCase("y")) {
                System.out.println("Running the test case...");
                service.runTestCase(testCase); // Primary test execution method
                System.out.println("Test run complete!");
            }

            String again = promptYesNo(scanner, "Create another test case? (y/n)");
            continueRunning = again.equalsIgnoreCase("y");
        }

        System.out.println("Exiting FSQS Toolkit. Goodbye!");
        scanner.close();
    }

    private String promptYesNo(Scanner scanner, String message) {
        String input;
        do {
            System.out.println(message);
            input = scanner.nextLine().trim().toLowerCase();
        } while (!input.equals("y") && !input.equals("n"));
        return input;
    }

}
