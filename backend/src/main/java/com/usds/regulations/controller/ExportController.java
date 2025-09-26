package com.usds.regulations.controller;

import com.usds.regulations.service.CSVExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/export")
@CrossOrigin(origins = "*")
public class ExportController {

    @Autowired
    private CSVExportService csvExportService;

    @GetMapping("/cfr-titles/csv")
    public ResponseEntity<String> exportCFRTitlesCSV() {
        try {
            String csvContent = csvExportService.exportCFRTitlesToCSV();
            
            String filename = "cfr_titles_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(csvContent, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error generating CSV export: " + e.getMessage());
        }
    }
}