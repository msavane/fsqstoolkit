package generator;

import dto.TestCaseDto;
import dto.StepDto;

public class MarkdownGenerator {

    public static String toMarkdown(TestCaseDto dto) {
        StringBuilder sb = new StringBuilder();

        sb.append("## 🧪 Feature: ").append(dto.getFeatureName()).append("\n\n");
        sb.append("**🌐 Target URL:** [").append(dto.getTargetUrl()).append("](").append(dto.getTargetUrl()).append(")\n\n");

        sb.append("### 🔁 Steps:\n");
        int stepNum = 1;
        for (StepDto step : dto.getSteps()) {
            sb.append(stepNum++)
                    .append(". **")
                    .append(capitalize(step.getAction()))
                    .append("** into `")
                    .append(step.getProperty())
                    .append("` → `")
                    .append(step.getValue())
                    .append("`\n");
        }

        sb.append("\n**🎯 Trigger:** `").append(dto.getEventTrigger()).append("`\n");

        return sb.toString();
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
