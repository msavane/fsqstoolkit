package parser;

import dto.StepDto;
import dto.TestCaseDto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestCaseParser {

    public TestCaseDto loadFromScriptFile(String fileName) throws IOException {
        // Load file from resources/testcases/ using classloader as InputStream
        InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("testcases/" + fileName);

        if (inputStream == null) {
            throw new IOException("❌ File not found in resources/testcases/: " + fileName);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        List<String> lines = new ArrayList<>();
        String line;

        while ((line = reader.readLine()) != null) {
            lines.add(line.trim());
        }

        TestCaseDto testCase = new TestCaseDto();
        List<StepDto> steps = new ArrayList<>();

        for (String ln : lines) {
            if (ln.isEmpty()) continue;

            if (ln.startsWith("🧪 Feature:")) {
                testCase.setFeatureName(ln.substring("🧪 Feature:".length()).trim());
                continue;
            }

            if (ln.startsWith("🌐 Target URL:")) {
                testCase.setTargetUrl(ln.substring("🌐 Target URL:".length()).trim());
                continue;
            }

            if (ln.startsWith("🎯 Event Trigger:")) {
                testCase.setEventListener(ln.substring("🎯 Event Trigger:".length()).trim());
                continue;
            }

            Matcher summaryStep = Pattern.compile("\\d+\\. \\[(.*?)\\] using \\[(.*?)=(.*?)\\] => ?(.*)").matcher(ln);
            if (summaryStep.find()) {
                steps.add(new StepDto(
                        summaryStep.group(1).trim(),
                        summaryStep.group(2).trim(),
                        summaryStep.group(3).trim(),
                        summaryStep.group(4).trim()
                ));
                continue;
            }

            if (ln.toLowerCase().startsWith("feature:")) {
                testCase.setFeatureName(ln.substring("feature:".length()).trim());
                continue;
            }

            if (ln.toLowerCase().startsWith("target url:")) {
                testCase.setTargetUrl(ln.substring("target url:".length()).trim());
                continue;
            }

            if (ln.toLowerCase().startsWith("event trigger:")) {
                testCase.setEventListener(ln.substring("event trigger:".length()).trim());
                continue;
            }

            if (ln.toLowerCase().startsWith("navigate to ")) {
                testCase.setTargetUrl(ln.substring("navigate to ".length()).trim());
                continue;
            }

            Matcher actionMatcher = Pattern.compile(
                    "Action:\\s*(\\w+),\\s*Locator Type:\\s*([^,]+),\\s*Locator Value:\\s*([^,]+),\\s*Value:\\s*(.*)"
            ).matcher(ln);
            if (actionMatcher.find()) {
                steps.add(new StepDto(
                        actionMatcher.group(1).trim(),
                        actionMatcher.group(2).trim(),
                        actionMatcher.group(3).trim(),
                        actionMatcher.group(4).trim()
                ));
                continue;
            }

            Matcher pressMatcher = Pattern.compile("keypress\\s+\"(.*?)\"\\s+key\\s+in\\s+\"(.*?)\"").matcher(ln);
            if (pressMatcher.find()) {
                steps.add(new StepDto("keypress", "id", pressMatcher.group(2).trim(), pressMatcher.group(1).trim()));
                continue;
            }

            Matcher enterMatcher = Pattern.compile("enter\\s+\"(.*?)\"\\s+into\\s+\"(.*?)\"").matcher(ln);
            if (enterMatcher.find()) {
                steps.add(new StepDto("type", "id", enterMatcher.group(2).trim(), enterMatcher.group(1).trim()));
                continue;
            }

            Matcher assertMatcher = Pattern.compile("assert\\s+\"(.*?)\"").matcher(ln);
            if (assertMatcher.find()) {
                String raw = assertMatcher.group(1).trim();
                String locatorType = "id";
                String locatorValue = raw;

                if (raw.contains("=")) {
                    String[] parts = raw.split("=", 2);
                    locatorType = parts[0].trim();
                    locatorValue = parts[1].trim();
                }

                steps.add(new StepDto("assert", locatorType, locatorValue, ""));
                continue;
            }

            Matcher clickMatcher = Pattern.compile("click\\s+\"(.*?)\"").matcher(ln);
            if (clickMatcher.find()) {
                String raw = clickMatcher.group(1).trim();
                String locatorType = raw.startsWith("alt=") ? "alt" : "id";
                String locatorValue = raw.startsWith("alt=") ? raw.substring(4).trim() : raw;

                steps.add(new StepDto("click", locatorType, locatorValue, ""));
            }
        }

        if (testCase.getFeatureName() == null || testCase.getFeatureName().isEmpty()) {
            testCase.setFeatureName("Auto-parsed test case");
        }
        if (testCase.getEventListener() == null) {
            testCase.setEventListener("");
        }

        testCase.setSteps(steps);
        return testCase;
    }
}
