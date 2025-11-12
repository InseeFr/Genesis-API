package fr.insee.genesis.exceptions;

import com.networknt.schema.ValidationMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class SchemaValidationException extends Exception{

    private final String message;

    private Set<ValidationMessage> errors;
}
