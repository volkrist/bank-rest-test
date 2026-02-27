package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    @NotNull
    private Long fromCardId;
    @NotNull
    private Long toCardId;
    @NotNull
    @Positive(message = "Amount must be positive")
    private Long amountCents;
}
