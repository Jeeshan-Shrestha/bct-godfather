package com.bct.bct_godfather.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;

@Service
public class PptxToPdfService {
    
    public byte[] convert(byte[] pptxBytes) throws Exception {

    Path input = Files.createTempFile("input", ".pptx");
    Files.write(input, pptxBytes);

    Process process = new ProcessBuilder(
            "soffice",
            "--headless",
            "--convert-to", "pdf",
            input.toAbsolutePath().toString(),
            "--outdir", input.getParent().toString()
    ).start();

    process.waitFor();

    Path output = Paths.get(
            input.toString().replace(".pptx", ".pdf")
    );

    byte[] pdfBytes = Files.readAllBytes(output);

    Files.deleteIfExists(input);
    Files.deleteIfExists(output);

    return pdfBytes;
}

}
