package com.maning.expensecontrol.controller;

import com.maning.expensecontrol.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class PingController {

    @GetMapping("/ping")
    public Result<PingInfo> ping() {
        return Result.ok(new PingInfo("expense-control", LocalDateTime.now(), "morning-zy"));
    }

    public record PingInfo(String projectName, LocalDateTime currentTime, String author) {
    }
}
