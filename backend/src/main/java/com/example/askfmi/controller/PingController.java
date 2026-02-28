package com.example.askfmi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
public class PingController {

    @GetMapping("/ping")
    public ResponseEntity<HashMap<String, String>> ping() {
        // return pong in json format
        HashMap<String, String> response = new HashMap<>();
        response.put("response", "pong");

        // return the object as json
        return ResponseEntity.ok(response);
    }
}
