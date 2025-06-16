package cli;

import dto.TestCaseDto;
import parser.TestCaseParser;
import runner.ConsoleRunner;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("🎉 Welcome to FSQS Toolkit!");

        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose execution style:");
        System.out.println("1. FSQS Legacy Script (*.txt)");
        System.out.println("2. Gherkin Cucumber Feature (*.feature)");
        System.out.println("3. API Test Case (*.txt with GET/POST/etc)");
        System.out.print("Your choice [1/2/3]: ");

        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                try {
                    TestCaseParser parser = new TestCaseParser();
                    //TestCaseDto testCase = parser.loadFromScriptFile("assertmovie2.txt");
                    //TestCaseDto testCase = parser.loadFromScriptFile("getPostTest.txt");
                    TestCaseDto testCase = parser.loadFromScriptFile("pm1.txt");
                    new ConsoleRunner().run(testCase);
                } catch (IOException e) {
                    System.err.println("❌ Failed to load test case: " + e.getMessage());
                }
                break;

            case "2":
                try {
                    System.out.println("🧪 Running Cucumber Feature Tests...");
                    ProcessBuilder pb = new ProcessBuilder("mvn", "test");
                    pb.inheritIO();
                    Process process = pb.start();
                    int exitCode = process.waitFor();
                    System.out.println("✅ Cucumber test completed with exit code: " + exitCode);
                } catch (Exception e) {
                    System.err.println("❌ Failed to run Cucumber tests: " + e.getMessage());
                }
                break;

            case "3":
                try {
                    TestCaseParser parser = new TestCaseParser();
                    TestCaseDto apiTest = parser.loadFromScriptFile("apitestcase.txt");
                    new ConsoleRunner().run(apiTest); // Reuse existing runner if compatible
                } catch (IOException e) {
                    System.err.println("❌ Failed to load API test case: " + e.getMessage());
                }
                break;

            default:
                System.out.println("⚠️ Invalid choice. Exiting.");
        }
    }
}
