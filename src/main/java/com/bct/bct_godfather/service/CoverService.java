package com.bct.bct_godfather.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;

@Service
public class CoverService {

    private final byte[] templateBytes;
    private final Map<String, String[]> studentMap;

    public CoverService(
            @Value("${cover.template.path}") String templatePath,
            @Value("${cover.csv.path}") String csvPath,
            org.springframework.core.io.ResourceLoader resourceLoader
    ) throws IOException {
        this.templateBytes = resourceLoader.getResource(templatePath).getInputStream().readAllBytes();
        this.studentMap    = loadCsv(resourceLoader.getResource(csvPath).getInputStream());
    }

    /**
     * Returns a PDF byte array for the given roll number.
     * Throws IllegalArgumentException if the roll number is not found.
     */
    public byte[] getCoverPdf(String rollNo) throws IOException, InterruptedException {
        String[] student = studentMap.get(rollNo.trim());
        if (student == null) {
            throw new IllegalArgumentException("Roll number not found: " + rollNo);
        }

        String fullName = student[1].trim() + " " + student[2].trim();
        byte[] docxBytes = fillTemplate(templateBytes, fullName, rollNo.trim());

        return convertDocxToPdf(docxBytes, rollNo);
    }

    // -------------------------------------------------------------------------
    // CSV loading
    // -------------------------------------------------------------------------

    
    private Map<String, String[]> loadCsv(InputStream is) throws IOException {
    Map<String, String[]> map = new LinkedHashMap<>();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
        br.readLine();
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty()) {
                String[] parts = line.split(",", -1);
                map.put(parts[0].trim(), parts);
            }
        }
    }
    return map;
}

    // -------------------------------------------------------------------------
    // Template filling
    // -------------------------------------------------------------------------

    private byte[] fillTemplate(byte[] templateBytes, String name, String rollNo) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (ZipInputStream  zin  = new ZipInputStream(new ByteArrayInputStream(templateBytes));
             ZipOutputStream zout = new ZipOutputStream(out)) {

            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                byte[] data = zin.readAllBytes();

                if ("word/document.xml".equals(entry.getName())) {
                    String xml = new String(data, "UTF-8");
                    // Word splits {{key}} across 3 runs with <w:proofErr> tags in between.
                    // The prefix text before {{ differs per placeholder.
                    xml = replaceSplitPlaceholder(xml, "student_name", name,   " ");
                    xml = replaceSplitPlaceholder(xml, "roll_no",      rollNo, ": ");
                    data = xml.getBytes("UTF-8");
                }

                zout.putNextEntry(new ZipEntry(entry.getName()));
                zout.write(data);
                zout.closeEntry();
            }
        }

        return out.toByteArray();
    }

    /**
     * Word's spellchecker splits {{key}} across multiple XML runs like:
     *   <w:t>{prefix}{{</w:t></w:r>
     *   <w:proofErr w:type="spellStart"/>
     *   <w:r ...><w:t>{key}</w:t></w:r>
     *   <w:proofErr w:type="spellEnd"/>
     *   <w:r ...><w:t>}}</w:t></w:r>
     *
     * This method collapses all three runs into one text node with the value.
     * Also handles the simple merged {{key}} form as a fallback.
     */
    private String replaceSplitPlaceholder(String xml, String key, String value, String prefix) {
        String pattern =
            "(<w:t[^>]*>)" + Pattern.quote(prefix) + "\\{\\{</w:t></w:r>" +
            "<w:proofErr w:type=\"spellStart\"/>" +
            "<w:r[^>]*><w:rPr>.*?</w:rPr><w:t>" + Pattern.quote(key) + "</w:t></w:r>" +
            "<w:proofErr w:type=\"spellEnd\"/>" +
            "<w:r[^>]*><w:rPr>.*?</w:rPr><w:t>\\}\\}</w:t></w:r>";

        String escapedValue  = escapeXml(value);
        String escapedPrefix = escapeXml(prefix);

        String result = Pattern.compile(pattern, Pattern.DOTALL)
            .matcher(xml)
            .replaceAll("<w:t xml:space=\"preserve\">" +
                Matcher.quoteReplacement(escapedPrefix + escapedValue) + "</w:t></w:r>");

        return result.replace("{{" + key + "}}", escapedValue);
    }

    private String escapeXml(String v) {
        return v.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    // -------------------------------------------------------------------------
    // DOCX → PDF via LibreOffice headless (Linux)
    // -------------------------------------------------------------------------

    private byte[] convertDocxToPdf(byte[] docxBytes, String rollNo) throws IOException, InterruptedException {
        Path tmpDir  = Files.createTempDirectory("cover_" + rollNo + "_");
        Path docxFile = tmpDir.resolve(rollNo + ".docx");

        try {
            Files.write(docxFile, docxBytes);

            ProcessBuilder pb = new ProcessBuilder(
                "libreoffice", "--headless", "--convert-to", "pdf",
                "--outdir", tmpDir.toString(),
                docxFile.toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Drain stdout so the process doesn't block
            new Thread(() -> {
                try { process.getInputStream().transferTo(OutputStream.nullOutputStream()); }
                catch (IOException ignored) {}
            }).start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("LibreOffice conversion failed with exit code: " + exitCode);
            }

            Path pdfFile = tmpDir.resolve(rollNo + ".pdf");
            if (!Files.exists(pdfFile)) {
                throw new IOException("PDF not produced by LibreOffice for: " + rollNo);
            }

            return Files.readAllBytes(pdfFile);

        } finally {
            // Clean up temp files
            try (var stream = Files.walk(tmpDir)) {
                stream.sorted(Comparator.reverseOrder())
                      .map(Path::toFile)
                      .forEach(File::delete);
            }
        }
    }
}