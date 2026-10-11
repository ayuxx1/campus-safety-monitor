package com.campus.safety.config;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/settings")
public class AdminSettingsController {

    private final RuntimeSettingsService settingsService;

    public AdminSettingsController(RuntimeSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public CampusSettings getSettings() {
        return settingsService.getSettings();
    }

    @PutMapping
    public CampusSettings updateSettings(@Valid @RequestBody CampusSettings settings) {
        return settingsService.updateSettings(settings);
    }
}
