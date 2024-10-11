package com.valer.rip.lab1.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "provider_duties")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProviderDuty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, length = 255, nullable = false)
    private String title;

    @Column(name = "img_url", length = 255)
    private String imgUrl;

    @Column(columnDefinition = "text", nullable = false)
    private String description;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(nullable = false)
    private int price;

    @Column(name = "monthly_payment", nullable = false)
    private Boolean monthlyPayment;

    @Column(length = 10, nullable = false)
    private String unit;

    @Column(name = "amount_description", length = 100, nullable = false)
    private String amountDescription;

    public Boolean getMonthlyPayment() {
        return monthlyPayment;
    }

    public Boolean getActive() {
        return active;
    }
}