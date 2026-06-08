package com.bct.bct_godfather.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.bct.bct_godfather.service.CoverService;


@Controller
public class CoverPageRoute {
    

    @Autowired private CoverService coverService;

    @GetMapping("/cover/{roll_no}")
public ResponseEntity<byte[]> getCoverPage(@PathVariable String roll_no)
        throws IOException, InterruptedException {

    String input = roll_no.trim();

    String formattedRollNo;

    if (input.startsWith("THA081BCT")) {
        formattedRollNo = input;
    } else {
        try {
            formattedRollNo = "THA081BCT" +
                    String.format("%03d", Integer.parseInt(input));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(("Invalid roll number: " + roll_no).getBytes());
        }
    }

    byte[] coverPdf = coverService.getCoverPdf(formattedRollNo);

    return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + formattedRollNo + "_cover.pdf\""
            )
            .body(coverPdf);
}
    

}
