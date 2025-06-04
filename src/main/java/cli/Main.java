package cli;

import dto.TestCaseDto;
import parser.TestCaseParser;
import runner.ConsoleRunner;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to FSQS Toolkit!");

        try {
            TestCaseParser parser = new TestCaseParser();
            TestCaseDto testCase = parser.loadFromScriptFile("testlogonplaymovie.txt");

            new ConsoleRunner().run(testCase); // run preloaded test

        } catch (IOException e) {
            System.err.println("❌ Failed to load test case: " + e.getMessage());
        }
    }
}
