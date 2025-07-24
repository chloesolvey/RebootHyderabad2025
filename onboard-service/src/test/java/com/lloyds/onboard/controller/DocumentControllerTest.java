package com.lloyds.onboard.controller;

import com.lloyds.onboard.config.ErrorConfig;
import com.lloyds.onboard.entity.ApplicationDocument;
import com.lloyds.onboard.service.ApplicationDocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationDocumentService documentService;

    @MockBean
    private ErrorConfig errorConfig;

    @Test
    void shouldGetDocumentById() throws Exception {
        ApplicationDocument doc = new ApplicationDocument();
        doc.setId(100L);

        when(documentService.getDocument(100L)).thenReturn(doc);

        mockMvc.perform(get("/api/documents/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void shouldUploadDocumentSuccessfully() throws Exception {
        ApplicationDocument doc = new ApplicationDocument();
        doc.setId(101L);

        MockMultipartFile mockFile = new MockMultipartFile("file", "test.pdf", "application/pdf", "Mock PDF content".getBytes());

        when(documentService.saveDocument(1L, 1, "passport", mockFile)).thenReturn(doc);

        mockMvc.perform(multipart("/api/documents/upload")
                        .file(mockFile)
                        .param("id", "1")
                        .param("currentPage", "1")
                        .param("fieldName", "passport"))
                .andExpect(status().isOk())
                .andExpect(content().string("Document uploaded successfully with ID: 101"));
    }

    @Test
    void shouldReturnErrorOnUploadFailure() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("file", "fail.pdf", "application/pdf", "corrupt".getBytes());

        when(documentService.saveDocument(anyLong(), anyInt(), anyString(), any())).thenThrow(new RuntimeException("File failed"));

        mockMvc.perform(multipart("/api/documents/upload")
                        .file(mockFile)
                        .param("id", "2")
                        .param("currentPage", "1")
                        .param("fieldName", "passport"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error uploading document: File failed"));
    }

    @Test
    void shouldUpdateDocumentSuccessfully() throws Exception {
        ApplicationDocument updated = new ApplicationDocument();
        updated.setId(200L);

        MockMultipartFile mockFile = new MockMultipartFile("file", "updated.pdf", "application/pdf", "updated content".getBytes());

        when(documentService.updateDocument(200L, mockFile, "license", 2)).thenReturn(updated);

        mockMvc.perform(multipart("/api/documents/200")
                        .file(mockFile)
                        .param("fieldName", "license")
                        .param("pageNumber", "2")
                        .with(request -> {
                            request.setMethod("PUT"); // Multipart PUT trick
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(content().string("Document updated successfully with ID: 200"));
    }

    @Test
    void shouldReturnErrorOnUpdateFailure() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("file", "error.pdf", "application/pdf", "bad file".getBytes());

        when(documentService.updateDocument(anyLong(), any(), anyString(), anyInt()))
                .thenThrow(new RuntimeException("Update failed"));

        mockMvc.perform(multipart("/api/documents/999")
                        .file(mockFile)
                        .param("fieldName", "license")
                        .param("pageNumber", "2")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error updating document: Update failed"));
    }
}
