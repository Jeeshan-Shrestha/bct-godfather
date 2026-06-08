package com.bct.bct_godfather.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class PdfToDocxService {

    public byte[] convert(byte[] pdfBytes) throws IOException {
        try (PDDocument pdf = PDDocument.load(pdfBytes);
             XWPFDocument docx = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            int pageCount = pdf.getNumberOfPages();
            PDFTextStripper stripper = new PDFTextStripper();

            for (int i = 1; i <= pageCount; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String pageText = stripper.getText(pdf);

                writeLines(docx, pageText);

                if (i < pageCount) {
                    addPageBreak(docx);
                }
            }

            docx.write(out);
            return out.toByteArray();
        }
    }

    private void writeLines(XWPFDocument docx, String pageText) {
        for (String line : pageText.split("\n")) {
            XWPFParagraph paragraph = docx.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText(line.stripTrailing());
        }
    }

    private void addPageBreak(XWPFDocument docx) {
        XWPFParagraph paragraph = docx.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.addBreak(BreakType.PAGE);
    }
}