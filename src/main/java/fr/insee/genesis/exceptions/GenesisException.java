package fr.insee.genesis.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serial;

@Getter
public class GenesisException extends Exception {

    @Serial
    private static final long serialVersionUID = 3356078796351491095L;

    private final HttpStatus status;

    public GenesisException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
