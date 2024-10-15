package com.valer.rip.lab1.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConnectionRequestDTO {
    private int id;
    private String status;
    private String consumer;
    private String phoneNumber;
    private LocalDateTime creationDatetime;
    private LocalDateTime formationDatetime;
    private LocalDateTime completionDatetime;
    private Integer totalPrice;
    private String manager; 
    private String client;
    private List<ProviderDutyDTO> duties;
}
