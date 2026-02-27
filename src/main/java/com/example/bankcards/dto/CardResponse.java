package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardResponse {
    private Long id;
    /** Маска номера: **** **** **** 1234 */
    private String maskedNumber;
    private String holderName;
    private LocalDate expiryDate;
    private CardStatus status;
    private BigDecimal balance;
    private Instant createdAt;
}
