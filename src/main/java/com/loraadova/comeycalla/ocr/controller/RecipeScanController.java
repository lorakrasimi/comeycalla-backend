package com.loraadova.comeycalla.ocr.controller;

import com.loraadova.comeycalla.ocr.RecipeScanService;
import com.loraadova.comeycalla.ocr.dto.RecipeScanResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@CrossOrigin(origins = "http://localhost:4200")
public class RecipeScanController {

    @Autowired
    private RecipeScanService recipeScanService;

    @PostMapping("/scan")
    public ResponseEntity<RecipeScanResponseDto> scanRecipeImages(
            @RequestParam("images") List<MultipartFile> images,
            @RequestParam(value = "sections", required = false) List<String> sections
    ) {
        if (images.size() > 1 && (sections == null || sections.size() != images.size())) {
            return ResponseEntity.badRequest().build();
        }

        RecipeScanResponseDto result = recipeScanService.scanImages(images, sections);

        return ResponseEntity.ok(result);
    }
}