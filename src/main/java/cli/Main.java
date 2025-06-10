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
            TestCaseDto testCase = parser.loadFromScriptFile("standardWiki1.txt");//works with 2
            //TestCaseDto testCase = parser.loadFromScriptFile("pm2.txt");// works with 2


            new ConsoleRunner().run(testCase); // run preloaded test

        } catch (IOException e) {
            System.err.println("❌ Failed to load test case: " + e.getMessage());
        }
    }
}
