package utils; // À adapter selon votre structure

import javafx.scene.control.Control;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.Validator;

public class RegexValidator implements Validator<String> {
    private final String regexPattern;
    private final String errorMessage;

    public RegexValidator(String regexPattern, String errorMessage) {
        this.regexPattern = regexPattern;
        this.errorMessage = errorMessage;
    }

    @Override
    public ValidationResult apply(Control control, String value) {
        boolean isValid = value.matches(regexPattern);
        Severity severity = isValid ? Severity.ERROR : Severity.WARNING;
        return ValidationResult.fromMessageIf(
                control,
                errorMessage,
                severity,
                !isValid
        );
    }
}