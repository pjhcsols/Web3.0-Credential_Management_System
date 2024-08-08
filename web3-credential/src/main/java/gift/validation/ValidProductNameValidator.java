package gift.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidProductNameValidator implements ConstraintValidator<ValidProductName, String> {

    @Override
    public void initialize(ValidProductName constraintAnnotation) {

    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        if (name == null) {
            return true; // Null values are handled separately if needed
        }

        return !name.contains("카카오") || isApprovedByMD(name);
    }

    // Implement your logic for MD approval
    private boolean isApprovedByMD(String name) {
        // Your logic to check if the product name is approved by MD
        return false; // Replace with your actual logic
    }
}
