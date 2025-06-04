package dto;

public class StepDto {
    private String action;        // e.g. type, click
    private String locatorType;   // e.g. name, id, css, xpath, alt
    private String property;      // the locator string itself
    private String value;         // value to input or use

    public StepDto(String action, String locatorType, String property, String value) {
        this.action = action;
        this.locatorType = locatorType;
        this.property = property;
        this.value = value;
    }

    public String getAction() {
        return action;
    }

    public String getLocatorType() {
        return locatorType;
    }

    public String getProperty() {
        return property;
    }

    public String getValue() {
        return value;
    }
}
