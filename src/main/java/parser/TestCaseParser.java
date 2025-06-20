package parser;

import dto.StepDto;
import dto.TestCaseDto;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class TestCaseParser {

    public TestCaseDto loadFromScriptFile(String fileOrPath) throws IOException {
        List<String> lines;

        if (fileOrPath.contains("/") || fileOrPath.contains("\\")) {
            // Treat as path (absolute or relative)
            Path path = Paths.get(fileOrPath);
            if (!Files.exists(path)) {
                throw new IOException("❌ File not found: " + fileOrPath);
            }
            lines = Files.readAllLines(path);
        } else {
            // Treat as classpath resource
            InputStream inputStream = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream("testcases/" + fileOrPath);

            if (inputStream == null) {
                throw new IOException("❌ File not found in resources/testcases/: " + fileOrPath);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                lines = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line.trim());
                }
            }
        }

        if (fileOrPath.endsWith(".feature")) {
            return parseGherkin(lines);
        } else {
            return parseLegacyScript(lines);
        }
    }

    private TestCaseDto parseLegacyScript(List<String> lines) {
        TestCaseDto testCase = new TestCaseDto();
        List<StepDto> steps = new ArrayList<>();

        for (String ln : lines) {
            if (ln.isEmpty()) continue;

            if (ln.startsWith("🧪 Feature:") || ln.toLowerCase().startsWith("feature:")) {
                testCase.setFeatureName(ln.substring(ln.indexOf(":") + 1).trim());
                continue;
            }

            if (ln.startsWith("🌐 Target URL:") || ln.toLowerCase().startsWith("target url:") || ln.toLowerCase().startsWith("navigate to ")) {
                testCase.setTargetUrl(ln.substring(ln.indexOf(":") + 1).trim());
                continue;
            }
            /*if (ln.startsWith("🎯 Event Trigger:")) {
                testCase.setEventListener(ln.substring("🎯 Event Trigger:".length()).trim());
                continue;
            }*/
            if (ln.startsWith("🎯 Event Trigger:") || ln.toLowerCase().startsWith("event trigger:")) {
                //testCase.setEventListener(ln.substring("🎯 Event Trigger:".length()).trim());
                testCase.setEventListener(ln.substring(ln.indexOf(":") + 1).trim());
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

            Matcher actionMatcher = Pattern.compile("Action:\\s*(\\w+),\\s*Locator Type:\\s*([^,]+),\\s*Locator Value:\\s*([^,]+),\\s*Value:\\s*(.*)").matcher(ln);
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

        if (testCase.getFeatureName() == null) testCase.setFeatureName("Auto-parsed test case");
        if (testCase.getEventListener() == null) testCase.setEventListener("");

        testCase.setSteps(steps);
        return testCase;
    }

    private TestCaseDto parseGherkin(List<String> lines) {
        TestCaseDto testCase = new TestCaseDto();
        List<StepDto> steps = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("Feature:")) {
                testCase.setFeatureName(line.substring("Feature:".length()).trim());
            } else if (line.startsWith("Scenario:")) {
                // optionally handle scenarios later
            } else if (line.matches("^(Given|When|Then|And)\\b.*")) {
                StepDto parsed = parseGherkinStep(line);
                if (parsed != null) steps.add(parsed);
            }
        }

        testCase.setSteps(steps);
        if (testCase.getFeatureName() == null) testCase.setFeatureName("Gherkin Feature");

        return testCase;
    }

    private StepDto parseGherkinStep(String line) {
        // Normalize and extract step
        line = line.replaceAll("^(Given|When|Then|And)\\s*", "");

        Matcher enterMatcher = Pattern.compile("enter\\s+\"(.*?)\"\\s+into\\s+the\\s+\"(.*?)\"\\s+field", Pattern.CASE_INSENSITIVE).matcher(line);
        if (enterMatcher.find()) {
            return new StepDto("type", "name", enterMatcher.group(2).trim(), enterMatcher.group(1).trim());
        }

        Matcher pressMatcher = Pattern.compile("press\\s+the\\s+\"(.*?)\"\\s+key\\s+in\\s+the\\s+\"(.*?)\"\\s+field", Pattern.CASE_INSENSITIVE).matcher(line);
        if (pressMatcher.find()) {
            return new StepDto("keypress", "name", pressMatcher.group(2).trim(), pressMatcher.group(1).trim());
        }

        Matcher assertMatcher = Pattern.compile("see the\\s+\"(.*?)\"\\s+article page", Pattern.CASE_INSENSITIVE).matcher(line);
        if (assertMatcher.find()) {
            return new StepDto("assert", "title", assertMatcher.group(1).trim(), "");
        }

        return new StepDto("unknown", "text", "unknown", line); // fallback
    }
}

/*package parser;

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
*/