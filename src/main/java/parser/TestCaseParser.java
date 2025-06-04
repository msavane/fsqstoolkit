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

            if (line.startsWith("navigate to ")) {
                testCase.setTargetUrl(line.replace("navigate to ", "").trim());

            } else if (line.startsWith("enter ")) {
                Pattern pattern = Pattern.compile("enter \\\"(.*)\\\" into \\\"(.*)\\\"");
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    steps.add(new StepDto("type", "id", matcher.group(2), matcher.group(1)));
                }

            } else if (line.startsWith("click ")) {
                Pattern pattern = Pattern.compile("click \\\"(.*)\\\"");
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String raw = matcher.group(1).trim();

                    if (raw.startsWith("alt=")) {
                        String locatorValue = raw.substring("alt=".length()).replaceAll("^\"|\"$", "").trim();
                        steps.add(new StepDto("click", "alt", locatorValue, ""));
                    } else {
                        steps.add(new StepDto("click", "id", raw, ""));
                    }
                }
            }
        }

        testCase.setFeatureName("Auto-parsed test case");
        testCase.setEventListener(""); // Optional: parse this if needed
        testCase.setSteps(steps);
        return testCase;
    }
}
