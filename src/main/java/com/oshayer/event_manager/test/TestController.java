package com.oshayer.event_manager.test;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;
@RestController
public class TestController {
    // ✅ Normal fast response
    @GetMapping("/api/test/ok")
    public ResponseEntity<String> ok() {
        return ResponseEntity.ok("Event Manager is working fine Oshayer 🚀");
    }
    // ✅ Slow response (to trigger TimeLimiter)
    @GetMapping("/api/test/slow")
    public ResponseEntity<String> slow() throws InterruptedException {
        TimeUnit.SECONDS.sleep(1); // delay 1s
        return ResponseEntity.ok("This response is slow ⏳");
    }

    // ✅ Error response (to trigger CircuitBreaker & Retry)
    @GetMapping("/api/test/error")
    public ResponseEntity<String> error() {
        return ResponseEntity.status(500).body("Simulated server error 💥");
    }


}
