package builder;

import dto.StepDto;
import dto.TestCaseDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TestCaseBuilder {

    public TestCaseDto buildFromInput() {
        Scanner scanner = new Scanner(System.in);
        TestCaseDto testCase = new TestCaseDto();
        List<StepDto> steps = new ArrayList<>();

       // System.out.println("\nWelcome to FSQS Toolkit!");

        System.out.print("\nFeature name: ");
        testCase.setFeatureName(scanner.nextLine());

        System.out.print("Target URL: ");
        testCase.setTargetUrl(scanner.nextLine());

        System.out.print("Form listener (submit, update, delete.): ");
        testCase.setEventListener(scanner.nextLine());

        String addMore;
        do {
            System.out.print("Action (e.g. type, click, select, etc...): ");
            String action = scanner.nextLine();
            System.out.print("Property name: ");
            String prop = scanner.nextLine();
            System.out.print("Property value: ");
            String val = scanner.nextLine();

            steps.add(new StepDto(action, prop, val));

            System.out.print("Add another property? (y/n): ");
            addMore = scanner.nextLine();
        } while (addMore.equalsIgnoreCase("y"));

        testCase.setSteps(steps);

        // Summary output
        printSummary(testCase);

        System.out.print("Run this test case now? (y/n): ");
        return testCase;
    }

    private void printSummary(TestCaseDto testCase) {
        System.out.println("\n======= TEST CASE SUMMARY =======");
        System.out.printf("🧪 Feature:         %s%n", testCase.getFeatureName());
        System.out.printf("🌐 Target URL:      http://%s%n%n", testCase.getTargetUrl());

        System.out.println("🔁 Steps:");
        int i = 1;
        for (StepDto step : testCase.getSteps()) {
            System.out.printf("  %d. [%s]    %-10s => %s%n", i++, step.getAction(), step.getProperty(), step.getValue());
        }

        System.out.printf("%n🎯 Event Trigger: %s%n", testCase.getEventListener());
        System.out.println("=================================\n");
    }
}
