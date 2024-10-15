package com.valer.rip.lab1.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateConnectionRequestDTO {
    @NotNull
    private String consumer;
    @NotNull
    private String phoneNumber;
}
