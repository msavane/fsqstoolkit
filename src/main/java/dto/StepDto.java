package dto;

public class StepDto {
    private String action;
    private String property;
    private String value;

    public StepDto(String action, String property, String value) {
        this.action = action;
        this.property = property;
        this.value = value;
    }

    public String getAction() {
        return action;
    }

    public String getProperty() {
        return property;
    }

    public String getValue() {
        return value;
    }
}
