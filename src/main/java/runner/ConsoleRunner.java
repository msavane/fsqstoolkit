package runner;

import builder.TestCaseBuilder;
import dto.TestCaseDto;
import parser.TestCaseParser;
import service.TestCaseService;
import util.FileDiscoveryUtil;
import io.cucumber.core.cli.Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConsoleRunner {

    private final Scanner scanner = new Scanner(System.in);
    private final TestCaseBuilder builder = new TestCaseBuilder();
    private final TestCaseService service = new TestCaseService();
    String featureNameTc;

    public void run(TestCaseDto initialTestCase) {
        TestCaseDto testCase = initialTestCase;

        while (true) {
            if (testCase == null) {
                testCase = handleTestCaseSelection();
                if (testCase == null) continue;
            }

            service.printTestCaseSummary(testCase);

            if (promptYesNo("Run this test case now?").equals("y")) {
                runBasedOnStyle(testCase);
            }

            if (promptYesNo("Would you like to save this test case?").equals("y")) {
                saveTestCase(testCase);
            }

            if (!promptYesNo("Create another test case?").equals("y")) {
                break;
            }

            testCase = null;
        }


        System.out.println("👋 Exiting FSQS Toolkit. Goodbye!");
        scanner.close();
    }

    private TestCaseDto handleTestCaseSelection() {
        System.out.println("🎉 Welcome to FSQS Toolkit!");
        System.out.println("How would you like to create a test?");
        System.out.println("1. Load from file");
        System.out.println("2. Create new test case");

        String inputMethod = promptChoice("Select option [1/2]:", "1", "2");

        return switch (inputMethod) {
            case "1" -> loadTestCaseFromFile();
            case "2" -> createNewTestCase();
            default -> null;
        };
    }

    private TestCaseDto loadTestCaseFromFile() {
        try {
            System.out.println("Select test type to load:");
            System.out.println("1. FSQS Legacy Script (*.txt)");
            System.out.println("2. Gherkin Cucumber Feature (*.feature)");

            String styleChoice = promptChoice("Choose [1/2]:", "1", "2");
            String extension = styleChoice.equals("2") ? ".feature" : ".txt";

            // Use filesystem relative paths - no classloader resource!
            Path folderPath = getFolderPathForStyle(styleChoice);

            if (folderPath == null || !Files.exists(folderPath)) {
                System.out.println("❌ Folder not found: " + folderPath);
                return null;
            }

            List<Path> availableFiles = discoverTestFiles(folderPath, extension);
            if (availableFiles.isEmpty()) {
                System.out.println("⚠ No test cases found for that type.");
                return null;
            }

            System.out.println("Available test cases:");
            for (int i = 0; i < availableFiles.size(); i++) {
                System.out.println((i + 1) + ". " + availableFiles.get(i).getFileName());
                featureNameTc = availableFiles.get(i).getFileName().toString();
            }

            int choice = Integer.parseInt(promptForInput("Enter number to load:"));
            Path selected = availableFiles.get(choice - 1);

            TestCaseParser parser = new TestCaseParser();
            // Pass full file path string to parser
            return parser.loadFromScriptFile(selected.toString());

        } catch (IOException | NumberFormatException e) {
            System.err.println("❌ Failed to load test case: " + e.getMessage());
            return null;
        }
    }

    private Path getFolderPathForStyle(String styleChoice) {
        if ("2".equals(styleChoice)) {
            // Gherkin features path (from project root)
            return Paths.get("src/test/resources/features");
        } else {
            // Legacy test cases path
            return Paths.get("src/main/resources/testcases");
        }
    }

    private List<Path> discoverTestFiles(Path folderPath, String extension) throws IOException {
        try (Stream<Path> stream = Files.list(folderPath)) {
            return stream
                    .filter(path -> path.toString().endsWith(extension))
                    .collect(Collectors.toList());
        }
    }

    private TestCaseDto createNewTestCase() {
        System.out.println("Which type of test case do you want to create?");
        System.out.println("1. Standard UI Test");
        System.out.println("2. Gherkin Feature Test");
        System.out.println("3. REST API Test");

        String typeChoice = promptChoice("Select [1/2/3]:", "1", "2", "3");

        return switch (typeChoice) {
            case "1" -> builder.buildFromInput();
            case "2" -> builder.buildGherkinFromInput();
            case "3" -> builder.buildApiTestFromInput();
            default -> {
                System.out.println("❌ Invalid choice.");
                yield null;
            }
        };
    }

    private void runBasedOnStyle(TestCaseDto testCase) {
        String style = promptChoice("Choose execution style: (1) Standard  (2) Gherkin  (3) REST", "1", "2", "3");

        switch (style) {
            case "1" -> service.runTestCase(testCase);
           /* case "2" -> {
                //Path featureFile = Paths.get("src/test/resources/features", testCase.getClass() + ".feature");
                Path featureFile = Paths.get("src/test/resources/features", featureNameTc );
                if (!Files.exists(featureFile)) {
                    System.out.println("❌ Feature file not found: " + featureFile);
                } else {
                    runFeatureSmart(featureFile.toString());
                }*/
            case "2" -> {
                String featureFilePath;

                // If user loaded from file, we already have a filename
                if (featureNameTc != null && !featureNameTc.isEmpty()) {
                    featureFilePath = Paths.get("src/test/resources/features", featureNameTc).toString();
                } else {
                    // Otherwise dynamically generate and write a temp file
                    featureFilePath = writeTempFeatureFile(testCase);
                }

                if (featureFilePath == null || !Files.exists(Paths.get(featureFilePath))) {
                    System.out.println("❌ Unable to run Gherkin test case. Feature file not found or invalid.");
                } else {
                    runFeatureSmart(featureFilePath);
                }

            }
            case "3" -> service.runRestTestCase(testCase);
        }
    }

    private void runFeatureSmart(String featurePath) {
        Path pom = Paths.get("pom.xml");

        if (Files.exists(pom)) {
            System.out.println("🧪 Running Cucumber Feature Tests via Maven...");
            try {
                ProcessBuilder pb = new ProcessBuilder("mvn", "test");
                pb.inheritIO();
                Process process = pb.start();
                int exitCode = process.waitFor();
                System.out.println("✅ Maven test completed with exit code: " + exitCode);
            } catch (Exception e) {
                System.err.println("❌ Maven execution failed. Falling back to direct feature run.");
                runFeatureDirectly(featurePath);
            }
        } else {
            System.out.println("⚠ No pom.xml found. Running feature directly...");
            runFeatureDirectly(featurePath);
        }
    }

    private void runFeatureDirectly(String featurePath) {
        String[] argv = new String[]{
                "--plugin", "pretty",
                "--glue", "stepdefinitions",
                featurePath
        };
        byte exitStatus = Main.run(argv, Thread.currentThread().getContextClassLoader());
        if (exitStatus != 0) {
            System.out.println("❌ Feature failed: " + featurePath);
        } else {
            System.out.println("✅ Feature completed successfully: " + featurePath);
        }
    }

    private void saveTestCase(TestCaseDto testCase) {
        String style = promptChoice("Choose save format: (1) Standard  (2) Gherkin", "1", "2");
        String filename = promptForInput("Enter filename to save to (e.g., test.txt):");
        boolean saveAsGherkin = style.equals("2");
        service.saveTestCaseToFile(testCase, filename, saveAsGherkin);
        System.out.println("💾 Test case saved to: " + filename);
    }

    private String promptYesNo(String message) {
        String input;
        do {
            System.out.println(message + " [y/n]");
            input = scanner.nextLine().trim().toLowerCase();
        } while (!input.equals("y") && !input.equals("n"));
        return input;
    }


    private String promptChoice(String message, String... validOptions) {
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

    private String promptForInput(String message) {
        System.out.println(message);
        return scanner.nextLine().trim();
    }

    private String writeTempFeatureFile(TestCaseDto testCase) {
        StringBuilder sb = new StringBuilder();
        sb.append("Feature: ").append(testCase.getFeatureName()).append("\n\n");

        for (var step : testCase.getSteps()) {
            sb.append("  ").append(step.getLocatorType()) // e.g., Given, When, Then
                    .append(" ").append(step.getProperty())    // e.g., I open the Wikipedia page
                    .append("\n");
        }

        try {
            Path dir = Paths.get("src/test/resources/features");
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            Path tempFile = dir.resolve("temp_dynamic.feature");
            Files.write(tempFile, sb.toString().getBytes());
            return tempFile.toString();
        } catch (IOException e) {
            System.err.println("❌ Failed to write temp Gherkin feature file: " + e.getMessage());
            return null;
        }
    }
}

/*package runner;

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
*/