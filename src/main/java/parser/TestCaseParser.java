package parser;

import dto.StepDto;
import dto.TestCaseDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestCaseParser {

    public TestCaseDto loadFromScriptFile(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        TestCaseDto testCase = new TestCaseDto();
        List<StepDto> steps = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // Toolkit-style headers
            if (line.startsWith("🧪 Feature:")) {
                testCase.setFeatureName(line.substring("🧪 Feature:".length()).trim());
                continue;
            }

            if (line.startsWith("🌐 Target URL:")) {
                testCase.setTargetUrl(line.substring("🌐 Target URL:".length()).trim());
                continue;
            }

            if (line.startsWith("🎯 Event Trigger:")) {
                testCase.setEventListener(line.substring("🎯 Event Trigger:".length()).trim());
                continue;
            }

            // Toolkit-style step format
            Matcher summaryStep = Pattern.compile("\\d+\\. \\[(.*?)\\] using \\[(.*?)=(.*?)\\] => ?(.*)").matcher(line);
            if (summaryStep.find()) {
                String action = summaryStep.group(1).trim();
                String locatorType = summaryStep.group(2).trim();
                String locatorValue = summaryStep.group(3).trim();
                String value = summaryStep.group(4).trim();
                steps.add(new StepDto(action, locatorType, locatorValue, value));
                continue;
            }

            // Script-style: Feature, Target URL, Event Trigger
            if (line.toLowerCase().startsWith("feature:")) {
                testCase.setFeatureName(line.substring("feature:".length()).trim());
                continue;
            }

            if (line.toLowerCase().startsWith("target url:")) {
                testCase.setTargetUrl(line.substring("target url:".length()).trim());
                continue;
            }

            if (line.toLowerCase().startsWith("event trigger:")) {
                testCase.setEventListener(line.substring("event trigger:".length()).trim());
                continue;
            }

            // Gherkin-style: navigate to ...
            if (line.toLowerCase().startsWith("navigate to ")) {
                testCase.setTargetUrl(line.substring("navigate to ".length()).trim());
                continue;
            }

            // Structured Action line
            Pattern actionPattern = Pattern.compile(
                    "Action:\\s*(\\w+),\\s*Locator Type:\\s*([^,]+),\\s*Locator Value:\\s*([^,]+),\\s*Value:\\s*(.*)"
            );
            Matcher actionMatcher = actionPattern.matcher(line);
            if (actionMatcher.find()) {
                steps.add(new StepDto(
                        actionMatcher.group(1).trim(),
                        actionMatcher.group(2).trim(),
                        actionMatcher.group(3).trim(),
                        actionMatcher.group(4).trim()
                ));
                continue;
            }

            // Gherkin-style: press "KEY" key in "field"
            Matcher pressMatcher = Pattern.compile("keypress\\s+\"(.*?)\"\\s+key\\s+in\\s+\"(.*?)\"").matcher(line);
            if (pressMatcher.find()) {
                String key = pressMatcher.group(1).trim(); // e.g. ENTER
                String field = pressMatcher.group(2).trim(); // e.g. search
                steps.add(new StepDto("keypress", "id", field, key));
                continue;
            }


            // Gherkin-style: enter "value" into "field"
            Matcher enterMatcher = Pattern.compile("enter\\s+\"(.*?)\"\\s+into\\s+\"(.*?)\"").matcher(line);
            if (enterMatcher.find()) {
                steps.add(new StepDto("type", "id", enterMatcher.group(2).trim(), enterMatcher.group(1).trim()));
                continue;
            }

            // Gherkin-style: click "id" or click "alt=..."
            Matcher clickMatcher = Pattern.compile("click\\s+\"(.*?)\"").matcher(line);
            if (clickMatcher.find()) {
                String raw = clickMatcher.group(1).trim();
                String locatorType = "id";
                String locatorValue = raw;

                if (raw.startsWith("alt=")) {
                    locatorType = "alt";
                    //locatorValue = raw.substring("alt=".length()).trim();
                }

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
