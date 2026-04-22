package fr.insee.genesis.controller.validation.schedule;

import fr.insee.genesis.controller.dto.ScheduleRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ScheduleRequestValidator implements ConstraintValidator<ValidScheduleRequest, ScheduleRequestDto> {

    @Override
    public boolean isValid(ScheduleRequestDto value, ConstraintValidatorContext context) {

        if (value == null) {
            return true;
        }

        boolean valid = true;

        // Encryption rule
        if (value.isUseAsymmetricEncryption()) {
            if (value.getEncryptionVaultPath() == null || value.getEncryptionVaultPath().isBlank()) {
                addViolation(context, "encryptionVaultPath", "encryptionVaultPath is mandatory if useAsymetricEncryption=true");
                valid = false;
            }
        }

        // Date rule
        if (value.getScheduleBeginDate() != null &&
                value.getScheduleEndDate() != null &&
                value.getScheduleBeginDate().isAfter(value.getScheduleEndDate())) {

            addViolation(context, "scheduleEndDate",
                    "scheduleEndDate should be after scheduleBeginDate");
            valid = false;
        }

        return valid;
    }

    private void addViolation(ConstraintValidatorContext context, String field, String message) {
        context.disableDefaultConstraintViolation();

        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }

}
