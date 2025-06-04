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

            System.out.print("Locator type (e.g. id, name, cssSelector, xpath, alt): ");
            String locatorType = scanner.nextLine();

            System.out.print("Locator value (e.g. field name, CSS selector, XPath): ");
            String locatorValue = scanner.nextLine();

            System.out.print("Action value (e.g. text to type or key to press, or leave blank): ");
            String actionValue = scanner.nextLine();

            steps.add(new StepDto(action, locatorType, locatorValue, actionValue));

            System.out.print("Add another property? (y/n): ");
            addMore = scanner.nextLine();
        } while (addMore.equalsIgnoreCase("y"));

        testCase.setSteps(steps);

        printSummary(testCase);

        System.out.print("Run this test case now? (y/n): ");
        return testCase;
    }

    private void printSummary(TestCaseDto testCase) {
        System.out.println("\n======= TEST CASE SUMMARY =======");
        System.out.printf("🧪 Feature:         %s%n", testCase.getFeatureName());
        System.out.printf("🌐 Target URL:      %s%n%n", testCase.getTargetUrl());

        System.out.println("🔁 Steps:");
        int i = 1;
        for (StepDto step : testCase.getSteps()) {
            System.out.printf("  %d. [%s] using [%s=%s] => %s%n",
                    i++, step.getAction(), step.getLocatorType(), step.getProperty(), step.getValue());
        }

        System.out.printf("%n🎯 Event Trigger: %s%n", testCase.getEventListener());
        System.out.println("=================================\n");
    }
}
