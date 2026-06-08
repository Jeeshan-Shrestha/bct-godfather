package com.bct.bct_godfather.service;

import org.springframework.stereotype.Service;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class DocxToPdfService {

    public byte[] convert(byte[] docxBytes) throws IOException, InterruptedException {
        Path tempDir = Files.createTempDirectory("docx-convert");
        Path inputFile = tempDir.resolve("input.docx");
        Path outputFile = tempDir.resolve("input.pdf");

        try {
            Files.write(inputFile, docxBytes);

            ProcessBuilder pb = new ProcessBuilder(
                "libreoffice", "--headless", "--convert-to", "pdf",
                "--outdir", tempDir.toString(),
                inputFile.toString()
            );
            pb.redirectErrorStream(true);

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                String logs = new String(process.getInputStream().readAllBytes());
                throw new IOException("LibreOffice conversion failed: " + logs);
            }

            return Files.readAllBytes(outputFile);

        } finally {
            Files.deleteIfExists(inputFile);
            Files.deleteIfExists(outputFile);
            Files.deleteIfExists(tempDir);
        }
    }
}