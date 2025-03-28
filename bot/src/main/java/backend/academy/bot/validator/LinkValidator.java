package backend.academy.bot.validator;

import backend.academy.bot.model.Link;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

public class LinkValidator {

    private static final Validator validator;

    static {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    public static boolean isValid(Link link) {
        Set<ConstraintViolation<Link>> violations = validator.validate(link);
        return violations.isEmpty();
    }
}
