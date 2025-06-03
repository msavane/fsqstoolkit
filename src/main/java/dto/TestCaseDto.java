package dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DTO representing a test case with metadata and a list of test steps.
 */
public class TestCaseDto {

    private String featureName;
    private String targetUrl;
    private String eventListener;
    private Map<String, String> properties; // Optional metadata
    private List<StepDto> steps;

    // --- Getters and Setters ---

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getEventListener() {
        return eventListener;
    }

    public void setEventListener(String eventListener) {
        this.eventListener = eventListener;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public List<StepDto> getSteps() {
        return steps;
    }

    public void setSteps(List<StepDto> steps) {
        this.steps = steps;
    }

    /**
     * Alias for getEventListener(), used for Markdown readability.
     */
    public String getEventTrigger() {
        return eventListener;
    }

    /**
     * Converts the test case steps to Gherkin-style readable text.
     */
    public List<String> getStepsAsText() {
        List<String> lines = new ArrayList<>();
        lines.add("navigate to " + targetUrl);

        for (StepDto step : steps) {
            String action = step.getAction().toLowerCase();
            String property = step.getProperty();
            String value = step.getValue();

            switch (action) {
                case "type":
                    lines.add(String.format("enter \"%s\" into \"%s\"", value, property));
                    break;
                case "click":
                    lines.add(String.format("click \"%s\"", property));
                    break;
                case "select":
                    lines.add(String.format("select \"%s\" from \"%s\"", value, property));
                    break;
                default:
                    lines.add(String.format("# Unknown action \"%s\" for \"%s\"", action, property));
            }
        }

        if (eventListener != null && !eventListener.isBlank()) {
            lines.add("click \"" + eventListener + "\" button");
        }

        return lines;
    }
}
