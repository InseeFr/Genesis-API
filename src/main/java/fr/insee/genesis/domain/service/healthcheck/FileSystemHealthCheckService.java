package fr.insee.genesis.domain.service.healthcheck;

import fr.insee.genesis.domain.model.healthcheck.FileSystemHealthCheckResult;
import fr.insee.genesis.domain.ports.api.FileSystemPort;
import fr.insee.genesis.infrastructure.adapter.LocalFileSystemAdapter;
import fr.insee.genesis.infrastructure.adapter.MinioFileSystemAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.StandardOpenOption;

@Service
@RequiredArgsConstructor
public class FileSystemHealthCheckService {
    private final static String TEST_FILE_NAME = "test.txt";
    private final static String TEST_STRING = "test";
    private final static String OK = "OK";

    private final FileSystemPort fileSystemPort;

    public FileSystemHealthCheckResult check(){
        String fileSystemType = switch (fileSystemPort){
            case LocalFileSystemAdapter _ -> "Local";
            case MinioFileSystemAdapter _-> "Minio";
            default -> "Unexpected value: " + fileSystemPort;
        };

        String writeTestResult = writeTest();
        String appendTestResult = appendTest();
        String readTestResult = readTest();
        String deleteTestResult = deleteTest();

        return FileSystemHealthCheckResult.builder()
                .isOK(isTestsOK(writeTestResult, appendTestResult, readTestResult, deleteTestResult))
                .fileSystemType(fileSystemType)
                .writeResult(writeTestResult)
                .appendResult(appendTestResult)
                .readResult(readTestResult)
                .deleteResult(deleteTestResult)
                .build();
    }

    private boolean isTestsOK(
            String writeTestResult,
            String appendTestResult,
            String readTestResult,
            String deleteTestResult
    ) {
        return writeTestResult.equals(OK)
                && appendTestResult.equals(OK)
                && readTestResult.equals(OK)
                && deleteTestResult.equals(OK);
    }

    private String writeTest() {
        try{
            fileSystemPort.writeString(TEST_FILE_NAME, TEST_STRING, StandardOpenOption.CREATE);
            return OK;
        } catch (Exception e) {
            return (e.toString());
        }
    }

    private String appendTest() {
        try{
            fileSystemPort.writeString(TEST_FILE_NAME, TEST_STRING, StandardOpenOption.APPEND);
            return OK;
        } catch (Exception e) {
            return (e.toString());
        }
    }

    private String readTest() {
        try{
            String fileContent = fileSystemPort.readAsString(TEST_FILE_NAME);
            return fileContent.equals(TEST_STRING + TEST_STRING) ?  OK :
                    "Unexpected content: %s".formatted(fileContent);
        } catch (Exception e) {
            return (e.toString());
        }
    }

    private String deleteTest() {
        try{
            if(!fileSystemPort.exists(TEST_FILE_NAME)){
                return "Test file doesn't exist";
            }
            fileSystemPort.deleteIfExists(TEST_FILE_NAME);
            return OK;
        } catch (Exception e) {
            return (e.toString());
        }
    }
}
