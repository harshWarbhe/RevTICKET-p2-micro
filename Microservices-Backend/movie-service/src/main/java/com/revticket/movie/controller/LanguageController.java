package com.revticket.movie.controller;

import com.revticket.movie.entity.Language;
import com.revticket.movie.service.LanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/languages")
public class LanguageController {

    @Autowired
    private LanguageService languageService;

    @GetMapping
    public ResponseEntity<List<Language>> getAllLanguages() {
        return ResponseEntity.ok(languageService.getAllActiveLanguages());
    }

    @PostMapping("/init")
    public ResponseEntity<String> initializeLanguages() {
        languageService.initializeDefaultLanguages();
        return ResponseEntity.ok("Languages initialized successfully");
    }
}
