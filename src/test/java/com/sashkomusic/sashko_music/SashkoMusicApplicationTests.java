package com.sashkomusic.sashko_music;

import com.sashkomusic.config.AWSConfig;
import com.sashkomusic.domain.service.S3Service;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class SashkoMusicApplicationTests {

    @MockBean
    AWSConfig awsConfig;

    @MockBean
    S3Service s3Service;

    @Value("classpath:/docs/vm_catalog.pdf")
    Resource vmCatalogPdf;

    @Value("classpath:/docs/vm_catalog_formatted.pdf")
    Resource vmCatalogPdfFormatted;

    @Test
    void contextLoads() {
    }

    @Test
    void readDocWithTikaSpringAI() throws IOException {
        String outputPath = "vm_catalog_parsed.txt";

        TikaDocumentReader documentReader = new TikaDocumentReader(vmCatalogPdfFormatted);

        List<Document> documents1 = documentReader.get();

        var textSplitter = new TokenTextSplitter();
        List<Document> documents = textSplitter.apply(documentReader.get());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            for (int i = 0; i < documents.size(); i++) {
                Document doc = documents.get(i);
                writer.write("--- Document " + (i + 1) + " ---\n");
                writer.write(doc.getText());
                writer.write("\n\n");

                // Якщо є метадані, записуємо їх також
                if (!doc.getMetadata().isEmpty()) {
                    writer.write("--- Metadata ---\n");
                    doc.getMetadata().forEach((key, value) -> {
                        try {
                            writer.write(key + ": " + value + "\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    writer.write("\n");
                }
            }

            System.out.println("nice");

        }
    }

    @Test
    void readDocWithPdfReader() throws IOException {
        String outputPath = "vm_catalog_parsed_paragraph_formatted.txt";

        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(this.vmCatalogPdfFormatted,
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfBottomTextLinesToDelete(1)
                                .withNumberOfTopPagesToSkipBeforeDelete(6)
                                .build())
                        .withPagesPerDocument(1)
                        .build());


        var textSplitter = new TokenTextSplitter();
        List<Document> documents = textSplitter.apply(pdfReader.get());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            for (int i = 0; i < documents.size(); i++) {
                Document doc = documents.get(i);
                writer.write("--- Document " + (i + 1) + " ---\n");
                writer.write(doc.getText());
                writer.write("\n\n");

                // Якщо є метадані, записуємо їх також
                if (!doc.getMetadata().isEmpty()) {
                    writer.write("--- Metadata ---\n");
                    doc.getMetadata().forEach((key, value) -> {
                        try {
                            writer.write(key + ": " + value + "\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    writer.write("\n");
                }
            }

            System.out.println("nice");

        }
    }

    @Test
    void readDocWithClaudePDFAI() throws IOException {
        List<Resource> chunkResources = splitPdfIntoChunks(vmCatalogPdf, 5);

        System.out.println(chunkResources);

        
    }

    private List<Resource> splitPdfIntoChunks(Resource pdfResource, int pagesPerChunk) throws IOException {
        List<Resource> chunkResources = new ArrayList<>();

        // Створюємо тимчасову директорію для зберігання частин
        Path tempDir = Files.createTempDirectory("pdf_chunks");

        try (PDDocument document = Loader.loadPDF(pdfResource.getFile())) {
            int totalPages = document.getNumberOfPages();
            int totalChunks = (int) Math.ceil((double) totalPages / pagesPerChunk);

            for (int chunkIndex = 0; chunkIndex < totalChunks; chunkIndex++) {
                // Створюємо новий документ для частини
                try (PDDocument chunkDocument = new PDDocument()) {
                    // Визначаємо діапазон сторінок для поточної частини
                    int startPage = chunkIndex * pagesPerChunk;
                    int endPage = Math.min(startPage + pagesPerChunk, totalPages);

                    // Додаємо сторінки до нового документа
                    for (int pageIndex = startPage; pageIndex < endPage; pageIndex++) {
                        PDPage page = document.getPage(pageIndex);
                        chunkDocument.addPage(page);
                    }

                    // Зберігаємо частину як окремий PDF
                    String chunkFileName = String.format(
                            "%s_chunk_%d_of_%d.pdf",
                            pdfResource.getFilename().replace(".pdf", ""),
                            chunkIndex + 1,
                            totalChunks
                    );

                    // Зберігаємо в тимчасову директорію
                    Path chunkPath = tempDir.resolve(chunkFileName);
                    chunkDocument.save(chunkPath.toFile());

                    // Створюємо ресурс з файлу
                    Resource chunkResource = new FileSystemResource(chunkPath.toFile());
                    chunkResources.add(chunkResource);
                }
            }
        }
        return chunkResources;
    }
}
