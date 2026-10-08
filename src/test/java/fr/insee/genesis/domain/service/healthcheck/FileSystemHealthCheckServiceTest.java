package fr.insee.genesis.domain.service.healthcheck;

import fr.insee.genesis.domain.model.healthcheck.FileSystemHealthCheckResult;
import fr.insee.genesis.domain.ports.api.FileSystemPort;
import fr.insee.genesis.infrastructure.adapter.LocalFileSystemAdapter;
import fr.insee.genesis.infrastructure.adapter.MinioFileSystemAdapter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.StandardOpenOption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileSystemHealthCheckServiceTest {

    private static final String TEST_FILE_NAME = "test.txt";
    private static final String TEST_STRING = "test";
    private static final String OK = "OK";

    private void mockNominalBehavior(FileSystemPort fileSystemPort) throws IOException {
        when(fileSystemPort.readAsString(TEST_FILE_NAME)).thenReturn(TEST_STRING + TEST_STRING);
        when(fileSystemPort.exists(TEST_FILE_NAME)).thenReturn(true);
    }

    @Test
    void check_shouldReturnAllOkWithLocalType_whenLocalAdapterAndEverythingWorks() throws IOException {
        //GIVEN
        //A local file system adapter working normally
        LocalFileSystemAdapter fileSystemPort = mock(LocalFileSystemAdapter.class);
        mockNominalBehavior(fileSystemPort);
        FileSystemHealthCheckService service = new FileSystemHealthCheckService(fileSystemPort);

        //WHEN
        //Running the health check
        FileSystemHealthCheckResult result = service.check();

        //THEN
        //All results are OK, the global status is OK and the type is Local
        assertThat(result.isOK()).isTrue();
        assertThat(result.fileSystemType()).isEqualTo("Local");
        assertThat(result.writeResult()).isEqualTo(OK);
        assertThat(result.appendResult()).isEqualTo(OK);
        assertThat(result.readResult()).isEqualTo(OK);
        assertThat(result.deleteResult()).isEqualTo(OK);
        verify(fileSystemPort).writeString(TEST_FILE_NAME, TEST_STRING, StandardOpenOption.CREATE);
        verify(fileSystemPort).writeString(TEST_FILE_NAME, TEST_STRING, StandardOpenOption.APPEND);
        verify(fileSystemPort).deleteIfExists(TEST_FILE_NAME);
    }

    @Test
    void check_shouldReturnMinioType_whenMinioAdapter() throws IOException {
        //GIVEN
        //A Minio file system adapter working normally
        MinioFileSystemAdapter fileSystemPort = mock(MinioFileSystemAdapter.class);
        mockNominalBehavior(fileSystemPort);
        FileSystemHealthCheckService service = new FileSystemHealthCheckService(fileSystemPort);

        //WHEN
        //Running the health check
        FileSystemHealthCheckResult result = service.check();

        //THEN
        //The type is Minio and the global status is OK
        assertThat(result.fileSystemType()).isEqualTo("Minio");
        assertThat(result.isOK()).isTrue();
    }

    @Test
    void check_shouldReturnUnexpectedValueType_whenUnknownFileSystemType() throws IOException {
        //GIVEN
        //A file system port which is neither Local nor Minio
        FileSystemPort fileSystemPort = mock(FileSystemPort.class);
        mockNominalBehavior(fileSystemPort);
        FileSystemHealthCheckService service = new FileSystemHealthCheckService(fileSystemPort);

        //WHEN
        //Running the health check
        FileSystemHealthCheckResult result = service.check();

        //THEN
        //The type describes the unexpected port and no exception is thrown
        assertThat(result.fileSystemType()).isEqualTo("Unexpected value: " + fileSystemPort);
        assertThat(result.isOK()).isTrue();
    }

    @Test
    void check_shouldReturnExceptionInWriteResult_whenWriteFails() throws IOException {
        //GIVEN
        //A file system port throwing on the write
        LocalFileSystemAdapter fileSystemPort = mock(LocalFileSystemAdapter.class);
        mockNominalBehavior(fileSystemPort);
        IOException exception = new IOException("write error");
        doThrow(exception).when(fileSystemPort)
                .writeString(TEST_FILE_NAME, TEST_STRING, StandardOpenOption.CREATE);
        FileSystemHealthCheckService service = new FileSystemHealthCheckService(fileSystemPort);

        //WHEN
        //Running the health check
        FileSystemHealthCheckResult result = service.check();

        //THEN
        //Only the write result contains the exception and the global status is not OK
        assertThat(result.isOK()).isFalse();
        assertThat(result.writeResult()).isEqualTo(exception.toString());
        assertThat(result.appendResult()).isEqualTo(OK);
        assertThat(result.readResult()).isEqualTo(OK);
        assertThat(result.deleteResult()).isEqualTo(OK);
    }

    @Test
    void check_shouldReturnExceptionInAppendResult_whenAppendFails() throws IOException {
        //GIVEN
        //A file system port throwing on the append write
        LocalFileSystemAdapter fileSystemPort = mock(LocalFileSystemAdapter.class);
        mockNominalBehavior(fileSystemPort);
        IOException exception = new IOException("append error");
        doThrow(exception).when(fileSystemPort)
                .writeString(TEST_FILE_NAME, TEST_STRING, StandardOpenOption.APPEND);
        FileSystemHealthCheckService service = new FileSystemHealthCheckService(fileSystemPort);

        //WHEN
        //Running the health check
        FileSystemHealthCheckResult result = service.check();

        //THEN
        //Only the append result contains the exception and the global status is not OK
        assertThat(result.isOK()).isFalse();
        assertThat(result.writeResult()).isEqualTo(OK);
        assertThat(result.appendResult()).isEqualTo(exception.toString());
        assertThat(result.readResult()).isEqualTo(OK);
        assertThat(result.deleteResult()).isEqualTo(OK);
    }

    @Test
    void check_shouldReturnUnexpectedContentInReadResult_whenContentIsWrong() throws IOException {
        //GIVEN
        //A file system port returning an unexpected content
        LocalFileSystemAdapter fileSystemPort = mock(LocalFileSystemAdapter.class);
        mockNominalBehavior(fileSystemPort);
        when(fileSystemPort.readAsString(TEST_FILE_NAME)).thenReturn("wrong");
        FileSystemHealthCheckService service = new FileSystemHealthCheckService(fileSystemPort);

        //WHEN
        //Running the health check
        FileSystemHealthCheckResult result = service.check();

        //THEN
        //The read result describes the unexpected content and the global status is not OK
        assertThat(result.isOK()).isFalse();
        assertThat(result.readResult()).isEqualTo("Unexpected content: wrong");
    }

    @Test
    void check_shouldReturnExceptionInReadResult_whenReadFails() throws IOException {
        //GIVEN
        //A file system port throwing on read
        LocalFileSystemAdapter fileSystemPort = mock(LocalFileSystemAdapter.class);
        mockNominalBehavior(fileSystemPort);
        IOException exception = new IOException("read error");
        when(fileSystemPort.readAsString(TEST_FILE_NAME)).thenThrow(exception);
        FileSystemHealthCheckService service = new FileSystemHealthCheckService(fileSystemPort);

        //WHEN
        //Running the health check
        FileSystemHealthCheckResult result = service.check();

        //THEN
        //Only the read result contains the exception and the global status is not OK
        assertThat(result.isOK()).isFalse();
        assertThat(result.writeResult()).isEqualTo(OK);
        assertThat(result.appendResult()).isEqualTo(OK);
        assertThat(result.readResult()).isEqualTo(exception.toString());
        assertThat(result.deleteResult()).isEqualTo(OK);
    }

    @Test
    void check_shouldReturnFileDoesNotExistInDeleteResult_whenFileDoesNotExist() throws IOException {
        //GIVEN
        //A file system port saying the test file does not exist
        LocalFileSystemAdapter fileSystemPort = mock(LocalFileSystemAdapter.class);
        mockNominalBehavior(fileSystemPort);
        when(fileSystemPort.exists(TEST_FILE_NAME)).thenReturn(false);
        FileSystemHealthCheckService service = new FileSystemHealthCheckService(fileSystemPort);

        //WHEN
        //Running the health check
        FileSystemHealthCheckResult result = service.check();

        //THEN
        //The delete result tells the file doesn't exist, nothing is deleted and the global status is not OK
        assertThat(result.isOK()).isFalse();
        assertThat(result.deleteResult()).isEqualTo("Test file doesn't exist");
        verify(fileSystemPort, never()).deleteIfExists(any());
    }

    @Test
    void check_shouldReturnExceptionInDeleteResult_whenDeleteFails() throws IOException {
        //GIVEN
        //A file system port throwing on delete
        LocalFileSystemAdapter fileSystemPort = mock(LocalFileSystemAdapter.class);
        mockNominalBehavior(fileSystemPort);
        IOException exception = new IOException("delete error");
        doThrow(exception).when(fileSystemPort).deleteIfExists(TEST_FILE_NAME);
        FileSystemHealthCheckService service = new FileSystemHealthCheckService(fileSystemPort);

        //WHEN
        //Running the health check
        FileSystemHealthCheckResult result = service.check();

        //THEN
        //Only the delete result contains the exception and the global status is not OK
        assertThat(result.isOK()).isFalse();
        assertThat(result.writeResult()).isEqualTo(OK);
        assertThat(result.appendResult()).isEqualTo(OK);
        assertThat(result.readResult()).isEqualTo(OK);
        assertThat(result.deleteResult()).isEqualTo(exception.toString());
    }
}