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

        System.out.print("Choose test type: (1) UI Steps (2) API Steps: ");
        String testType = scanner.nextLine().trim();

        boolean addMore = true;

        while (addMore) {
            StepDto step = new StepDto();

            if ("2".equals(testType)) {
                // API test step input
                System.out.println("\n➕ Add an API step:");

                System.out.print("🔧 HTTP Method (GET, POST, PUT, DELETE): ");
                String method = scanner.nextLine().trim().toUpperCase();
                step.setAction(method);

                System.out.print("🌐 Target endpoint or URL: ");
                String target = scanner.nextLine().trim();
                step.setProperty(target);

                if ("POST".equals(method) || "PUT".equals(method)) {
                    System.out.print("📦 Request body (JSON or text) [optional]: ");
                    String body = scanner.nextLine().trim();
                    step.setValue(body);
                } else {
                    step.setValue("");
                }

                steps.add(step);

                System.out.print("🔎 Add assertion for expected text in response body? (y/n): ");
                String wantAssert = scanner.nextLine().trim();
                if (wantAssert.equalsIgnoreCase("y")) {
                    StepDto assertStep = new StepDto();
                    assertStep.setAction("ASSERT_BODY");

                    System.out.print("✍️ Expected text to assert: ");
                    String expectedText = scanner.nextLine().trim();

                    assertStep.setValue(expectedText);
                    steps.add(assertStep);
                }

            } else {
                // UI test step input
                System.out.println("\n➕ Add a UI step:");

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

                step.setAction(action);
                step.setLocatorType(locatorType);
                step.setProperty(locatorValue);
                step.setValue(value);

                steps.add(step);
            }

            System.out.print("➕ Add another step? (y/n): ");
            addMore = scanner.nextLine().trim().equalsIgnoreCase("y");
        }

        TestCaseDto testCase = new TestCaseDto();
        testCase.setFeatureName(featureName);
        testCase.setTargetUrl(targetUrl);
        testCase.setEventListener(eventTrigger);
        testCase.setSteps(steps);

        System.out.println("\n✅ Test case built successfully.");
        return testCase;
    }

    public TestCaseDto buildApiTestFromInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n🌐 Building REST API Test");

        System.out.print("🔤 Test name: ");
        String featureName = scanner.nextLine().trim();

        List<StepDto> steps = new ArrayList<>();
        boolean more = true;

        while (more) {
            StepDto step = new StepDto();

            System.out.print("🔧 HTTP Method (GET, POST, PUT, DELETE): ");
            String method = scanner.nextLine().trim().toUpperCase();
            step.setAction(method);

            System.out.print("🌐 Endpoint URL: ");
            String endpoint = scanner.nextLine().trim();
            step.setProperty(endpoint);

            if ("POST".equals(method) || "PUT".equals(method)) {
                System.out.print("📦 Body content (JSON or text): ");
                step.setValue(scanner.nextLine().trim());
            } else {
                step.setValue("");
            }

            steps.add(step);

            System.out.print("➕ Add expected response assertion? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                StepDto assertion = new StepDto();
                assertion.setAction("ASSERT_BODY");

                System.out.print("🔎 Expected response contains: ");
                assertion.setValue(scanner.nextLine().trim());

                steps.add(assertion);
            }

            System.out.print("➕ Add another API step? (y/n): ");
            more = scanner.nextLine().trim().equalsIgnoreCase("y");
        }

        TestCaseDto testCase = new TestCaseDto();
        testCase.setFeatureName(featureName);
        testCase.setTargetUrl("API_TEST");
        testCase.setSteps(steps);
        System.out.println("✅ API test case built.");
        return testCase;
    }

    public TestCaseDto buildGherkinFromInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n🌿 Building Gherkin-style Test Case");

        System.out.print("🔤 Feature name: ");
        String featureName = scanner.nextLine().trim();

        List<StepDto> steps = new ArrayList<>();
        boolean more = true;

        while (more) {
            StepDto step = new StepDto();

            System.out.print("🧩 Enter Gherkin step (Given/When/Then/And): ");
            String keyword = scanner.nextLine().trim();

            System.out.print("📝 Step description: ");
            String description = scanner.nextLine().trim();

            step.setAction("GHERKIN");
            step.setLocatorType(keyword);   // store keyword (e.g., "Given")
            step.setProperty(description);  // store step description

            steps.add(step);

            System.out.print("➕ Add another Gherkin step? (y/n): ");
            more = scanner.nextLine().trim().equalsIgnoreCase("y");
        }

        TestCaseDto testCase = new TestCaseDto();
        testCase.setFeatureName(featureName);
        testCase.setTargetUrl("GHERKIN");
        testCase.setSteps(steps);

        System.out.println("✅ Gherkin test case built.");
        return testCase;
    }
}