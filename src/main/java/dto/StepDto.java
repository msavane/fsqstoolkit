package dto;

public class StepDto {

    private String action;
    private String locatorType;
    private String property;
    private String value;

    // Constructor (for compact creation)
    public StepDto(String action, String locatorType, String property, String value) {
        this.action = action;
        this.locatorType = locatorType;
        this.property = property;
        this.value = value;
    }

    // No-arg constructor
    public StepDto() {}

    // Getters
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

    // Setters
    public void setAction(String action) {
        this.action = action;
    }

    public void setLocatorType(String locatorType) {
        this.locatorType = locatorType;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
