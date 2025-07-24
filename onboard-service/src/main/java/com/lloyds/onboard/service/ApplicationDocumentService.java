package com.lloyds.onboard.service;

import com.lloyds.onboard.entity.ApplicationDocument;
import com.lloyds.onboard.exception.ServiceException;
import com.lloyds.onboard.model.Constants;
import com.lloyds.onboard.repository.ApplicationDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Service class for managing application documents.
 * <p>
 * Provides methods to save, retrieve, and update documents associated with applications.
 * </p>
 */
@Service
public class ApplicationDocumentService {

    private final ApplicationDocumentRepository repository;

    /**
     * Constructs an ApplicationDocumentService with the specified repository.
     *
     * @param repository the repository for accessing ApplicationDocument entities
     */
    public ApplicationDocumentService(ApplicationDocumentRepository repository) {
        this.repository = repository;
    }

    /**
     * Saves a new application document.
     * <p>
     * Creates an ApplicationDocument entity with the given parameters and file content,
     * then persists it in the repository.
     * </p>
     *
     * @param id         the application ID the document is associated with
     * @param pageNumber the page number of the document
     * @param fieldName  the field name related to the document
     * @param file       the multipart file containing the document data
     * @return the saved {@link ApplicationDocument} entity
     * @throws IOException if there is an error reading the file content
     */
    public ApplicationDocument saveDocument(Long id, int pageNumber, String fieldName, MultipartFile file) throws IOException {
        ApplicationDocument document = new ApplicationDocument();
        document.setApplicationId(id);
        document.setPageNumber(pageNumber);
        document.setFieldName(fieldName);
        document.setFileName(file.getOriginalFilename());
        document.setFileType(file.getContentType());
        document.setFileContent(file.getBytes());
        return repository.save(document);
    }

    /**
     * Retrieves an application document by its ID.
     *
     * @param documentId the ID of the document to retrieve
     * @return the {@link ApplicationDocument} if found
     * @throws ServiceException if no document is found with the given ID
     */
    public ApplicationDocument getDocument(Long documentId) {
        return repository.findById(documentId)
                .orElseThrow(() -> new ServiceException(Constants.DOCUMENT_ID_NOT_FOUND));
    }

    /**
     * Updates an existing application document with new file content and metadata.
     * <p>
     * Finds the document by ID, updates its file content, file name, type, field name, and page number,
     * and saves the changes.
     * </p>
     *
     * @param documentId the ID of the document to update
     * @param file       the new multipart file containing the updated document data
     * @param fieldName  the updated field name
     * @param pageNumber the updated page number
     * @return the updated {@link ApplicationDocument}
     * @throws IOException       if there is an error reading the file content
     * @throws ServiceException  if no document is found with the given ID
     */
    public ApplicationDocument updateDocument(Long documentId, MultipartFile file, String fieldName, int pageNumber) throws IOException {
        ApplicationDocument document = repository.findById(documentId)
                .orElseThrow(() -> new ServiceException(Constants.DOCUMENT_ID_NOT_FOUND));
        document.setFileName(file.getOriginalFilename());
        document.setFileType(file.getContentType());
        document.setFileContent(file.getBytes());
        document.setFieldName(fieldName);
        document.setPageNumber(pageNumber);

        return repository.save(document);
    }
}
