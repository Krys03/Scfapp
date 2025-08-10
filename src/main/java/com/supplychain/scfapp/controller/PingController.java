package com.supplychain.scfapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/ping")
public class PingController {

    // Public: pas d'auth, juste un OK rapide
    @GetMapping
    public ResponseEntity<Map<String, Object>> ping() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "time", Instant.now().toString()
        ));
    }
}
