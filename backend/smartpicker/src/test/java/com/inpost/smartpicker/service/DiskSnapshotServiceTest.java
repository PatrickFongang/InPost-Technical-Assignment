package com.inpost.smartpicker.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inpost.smartpicker.model.Locker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiskSnapshotServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DiskSnapshotService diskSnapshotService;

    private static final String SNAPSHOT_FILE_PATH = "lockers_snapshot.json";

    @AfterEach
    void cleanup() {
        File file = new File(SNAPSHOT_FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void shouldSaveSnapshotSuccessfully() throws IOException {
        // given
        Map<String, List<Locker>> cache = Map.of("grid1", List.of(new Locker()));
        
        // when
        diskSnapshotService.saveSnapshot(cache);

        // then
        verify(objectMapper).writeValue(any(File.class), eq(cache));
    }

    @Test
    void shouldHandleIOExceptionDuringSave() throws IOException {
        // given
        Map<String, List<Locker>> cache = Map.of("grid1", List.of(new Locker()));
        doThrow(new IOException("Disk full")).when(objectMapper).writeValue(any(File.class), any());

        // when - should not throw exception
        diskSnapshotService.saveSnapshot(cache);

        // then
        verify(objectMapper).writeValue(any(File.class), eq(cache));
    }

    @Test
    void shouldLoadSnapshotSuccessfully() throws IOException {
        // given
        // Create a dummy file so exists() returns true
        new File(SNAPSHOT_FILE_PATH).createNewFile();
        Map<String, List<Locker>> expectedCache = Map.of("grid1", List.of(new Locker()));
        when(objectMapper.readValue(any(File.class), any(TypeReference.class))).thenReturn(expectedCache);

        // when
        Map<String, List<Locker>> result = diskSnapshotService.loadSnapshot();

        // then
        assertThat(result).isEqualTo(expectedCache);
        verify(objectMapper).readValue(any(File.class), any(TypeReference.class));
    }

    @Test
    void shouldReturnNullWhenSnapshotFileDoesNotExist() {
        // given
        File file = new File(SNAPSHOT_FILE_PATH);
        if (file.exists()) file.delete();

        // when
        Map<String, List<Locker>> result = diskSnapshotService.loadSnapshot();

        // then
        assertThat(result).isNull();
        verifyNoInteractions(objectMapper);
    }

    @Test
    void shouldReturnNullWhenLoadThrowsIOException() throws IOException {
        // given
        new File(SNAPSHOT_FILE_PATH).createNewFile();
        when(objectMapper.readValue(any(File.class), any(TypeReference.class))).thenThrow(new IOException("Corrupted"));

        // when
        Map<String, List<Locker>> result = diskSnapshotService.loadSnapshot();

        // then
        assertThat(result).isNull();
    }
}
