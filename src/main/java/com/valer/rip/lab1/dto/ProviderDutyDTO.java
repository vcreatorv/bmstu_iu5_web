package com.valer.rip.lab1.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProviderDutyDTO {
    private int id;
    private String title;
    // private String description;
    private Boolean active;
    private int price;
    private Boolean monthlyPayment;
    private String unit;
    private String amountDescription;
    private int amount;
    
}