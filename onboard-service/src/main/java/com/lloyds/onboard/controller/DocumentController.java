package com.lloyds.onboard.controller;

import com.lloyds.onboard.entity.ApplicationDocument;
import com.lloyds.onboard.service.ApplicationDocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for managing application documents.
 * Provides endpoints to retrieve, upload, and update document content via multipart form data.
 */
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final ApplicationDocumentService documentService;

    /**
     * Constructs a new DocumentController.
     *
     * @param documentService the service used to handle document operations
     */
    public DocumentController(ApplicationDocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * Retrieves an application document by its unique ID.
     *
     * @param documentId the ID of the document to retrieve
     * @return ResponseEntity containing the ApplicationDocument
     */
    @GetMapping("/{documentId}")
    public ResponseEntity<ApplicationDocument> getDocument(@PathVariable Long documentId) {
        ApplicationDocument document = documentService.getDocument(documentId);
        return ResponseEntity.ok(document);
    }

    /**
     * Uploads a new document for a specific application.
     *
     * @param id         the application ID
     * @param pageNumber the page number associated with the document
     * @param fieldName  the field name describing the document type (e.g. PAN, ID Proof)
     * @param file       the multipart file to upload
     * @return ResponseEntity containing a success or error message
     */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadDocument(
            @RequestParam("id") Long id,
            @RequestParam("currentPage") int pageNumber,
            @RequestParam("fieldName") String fieldName,
            @RequestParam("file") MultipartFile file) {
        try {
            ApplicationDocument document = documentService.saveDocument(id, pageNumber, fieldName, file);
            return ResponseEntity.ok("Document uploaded successfully with ID: " + document.getId());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uploading document: " + e.getMessage());
        }
    }

    /**
     * Updates an existing document by document ID.
     *
     * @param documentId the ID of the document to update
     * @param file       the new file to associate with the document
     * @param fieldName  the updated field name
     * @param pageNumber the updated page number
     * @return ResponseEntity containing a success or error message
     */
    @PutMapping(value = "/{documentId}", consumes = "multipart/form-data")
    public ResponseEntity<String> updateDocument(
            @PathVariable Long documentId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("fieldName") String fieldName,
            @RequestParam("pageNumber") int pageNumber) {
        try {
            ApplicationDocument updatedDocument = documentService.updateDocument(documentId, file, fieldName, pageNumber);
            return ResponseEntity.ok("Document updated successfully with ID: " + updatedDocument.getId());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating document: " + e.getMessage());
        }
    }
}