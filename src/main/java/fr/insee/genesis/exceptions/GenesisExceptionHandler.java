package fr.insee.genesis.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GenesisExceptionHandler {

    @ExceptionHandler(GenesisException.class)
    public ResponseEntity<String> handleGenesis(GenesisException exception) {
        log.error("Genesis error (Type: {}) : {}",
                exception.getClass().getSimpleName(),
                exception.getMessage());

        return ResponseEntity
                .status(exception.getStatus())
                .body(exception.getMessage());
    }

    @ExceptionHandler(QuestionnaireNotFoundException.class)
    public ResponseEntity<String> handleQuestionnaireNotFound(QuestionnaireNotFoundException exception) {
        log.error("Questionnaire not found (Type: {}) : {}",
                exception.getClass().getSimpleName(),
                exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(NoDataException.class)
    public ResponseEntity<String> handleNoData(NoDataException exception) {
        log.error("No data found (Type: {}) : {}",
                exception.getClass().getSimpleName(),
                exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(SpecificationNotFoundException.class)
    public ResponseEntity<String> handleSpec(SpecificationNotFoundException exception) {
        log.error("Specifications not available for collectionInstrumentId: {} (Type: {})",
                exception.getCollectionInstrumentId(),
                exception.getClass().getSimpleName());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(ReviewDisabledException.class)
    public ResponseEntity<String> handleReviewDisabled(ReviewDisabledException ex) {
        log.error("[{}] {}", ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAny(Exception ex) {
        log.error("Unexpected error (Type: {}) : {}", ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error");
    }
}
