package com.lloyds.onboard.service;

import com.lloyds.onboard.entity.ApplicationDocument;
import com.lloyds.onboard.exception.ServiceException;
import com.lloyds.onboard.model.Constants;
import com.lloyds.onboard.repository.ApplicationDocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationDocumentServiceTest {

    @Mock
    private ApplicationDocumentRepository repository;

    @InjectMocks
    private ApplicationDocumentService service;

    @Mock
    private MultipartFile file;

    @Test
    void shouldSaveDocumentSuccessfully() throws IOException {
        ApplicationDocument document = new ApplicationDocument();

        when(file.getOriginalFilename()).thenReturn("doc.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getBytes()).thenReturn("PDF content".getBytes());
        when(repository.save(any())).thenReturn(document);

        ApplicationDocument result = service.saveDocument(1L, 1, "passport", file);

        assertNotNull(result);
        verify(repository).save(any(ApplicationDocument.class));
    }

    @Test
    void shouldGetDocumentById() {
        ApplicationDocument document = new ApplicationDocument();
        when(repository.findById(1L)).thenReturn(Optional.of(document));

        ApplicationDocument result = service.getDocument(1L);

        assertEquals(document, result);
        verify(repository).findById(1L);
    }

    @Test
    void shouldThrowExceptionIfDocumentNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.getDocument(99L));

        assertEquals(Constants.DOCUMENT_ID_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void shouldUpdateDocumentSuccessfully() throws IOException {
        ApplicationDocument existing = new ApplicationDocument();
        existing.setId(1L);

        when(file.getOriginalFilename()).thenReturn("updated.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getBytes()).thenReturn("Updated content".getBytes());
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenReturn(existing);

        ApplicationDocument result = service.updateDocument(1L, file, "license", 2);

        assertEquals("license", result.getFieldName());
        assertEquals(2, result.getPageNumber());
        assertEquals("updated.pdf", result.getFileName());
        verify(repository).save(existing);
    }
}