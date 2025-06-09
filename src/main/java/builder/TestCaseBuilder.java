package builder;

import dto.StepDto;
import dto.TestCaseDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TestCaseBuilder {

    public TestCaseDto buildFromInput() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n🧱 Let's build your test case:");
        System.out.print("🔤 Feature name: ");
        String featureName = scanner.nextLine().trim();

        System.out.print("🌐 Target URL: ");
        String targetUrl = scanner.nextLine().trim();

        System.out.println("🎯 Event trigger (optional – leave blank if not needed):");
        System.out.print("   e.g. 'onLoginSubmit': ");
        String eventTrigger = scanner.nextLine().trim();

        List<StepDto> steps = new ArrayList<>();
        boolean addMore = true;

        while (addMore) {
            System.out.println("\n➕ Add a step:");

            System.out.print("🔧 Action (type, click, keypress): ");
            String action = scanner.nextLine().trim();

            System.out.print("📍 Locator type (id, name, css, xpath, tag, alt): ");
            String locatorType = scanner.nextLine().trim();

            System.out.print("🔑 Locator value: ");
            String locatorValue = scanner.nextLine().trim();

            String value = "";
            if (action.equalsIgnoreCase("type") || action.equalsIgnoreCase("keypress")) {
                System.out.print("💬 Value to input or key to press: ");
                value = scanner.nextLine().trim();
            }

            StepDto step = new StepDto();
            step.setAction(action);
            step.setLocatorType(locatorType); // <-- added to match your service code
            step.setProperty(locatorValue);   // <-- locator value
            step.setValue(value);             // <-- input value or key

            steps.add(step);

            System.out.print("➕ Add another step? (y/n): ");
            addMore = scanner.nextLine().trim().equalsIgnoreCase("y");
        }

        TestCaseDto testCase = new TestCaseDto();
        testCase.setFeatureName(featureName);
        testCase.setTargetUrl(targetUrl);
        testCase.setEventListener(eventTrigger);
        testCase.setSteps(steps);

        return testCase;
    }

}