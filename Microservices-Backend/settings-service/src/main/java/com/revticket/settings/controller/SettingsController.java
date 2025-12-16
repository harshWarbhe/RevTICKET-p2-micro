package com.revticket.settings.controller;

import com.revticket.settings.dto.SettingsDTO;
import com.revticket.settings.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    @GetMapping("/settings")
    public ResponseEntity<SettingsDTO> getSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    @GetMapping("/settings/public")
    public ResponseEntity<SettingsDTO> getPublicSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    @GetMapping("/settings/{key}")
    public ResponseEntity<Map<String, String>> getSetting(@PathVariable String key) {
        String value = settingsService.getSetting(key);
        return ResponseEntity.ok(Map.of("key", key, "value", value));
    }

    @PutMapping("/admin/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SettingsDTO> updateSettings(@RequestBody SettingsDTO settings) {
        return ResponseEntity.ok(settingsService.updateSettings(settings));
    }

    @PutMapping("/admin/settings/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> updateSetting(@PathVariable String key, @RequestBody Map<String, String> request) {
        settingsService.saveSetting(key, request.get("value"));
        return ResponseEntity.ok(Map.of("key", key, "value", request.get("value")));
    }
}
