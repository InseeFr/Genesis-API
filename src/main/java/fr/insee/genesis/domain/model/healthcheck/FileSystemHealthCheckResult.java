package fr.insee.genesis.domain.model.healthcheck;

import lombok.Builder;
import org.jspecify.annotations.NonNull;

@Builder
public record FileSystemHealthCheckResult(
        boolean isOK,
        String fileSystemType,
        String writeResult,
        String appendResult,
        String readResult,
        String deleteResult
) {
    @Override
    public @NonNull String toString() {
        return """
                Type : %s
                Write : %s
                Append : %s
                Read : %s
                Delete : %s
                """.formatted(fileSystemType, writeResult, appendResult, readResult, deleteResult);
    }
}
