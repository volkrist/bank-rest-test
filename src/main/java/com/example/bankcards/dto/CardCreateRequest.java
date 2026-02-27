package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardCreateRequest {
    /** Полный номер карты (16–19 цифр); при сохранении будет зашифрован */
    @NotBlank
    @Pattern(regexp = "\\d{16,19}", message = "Card number must be 16-19 digits")
    private String number;
    @NotBlank
    @Size(max = 255)
    private String holderName;
    @NotNull
    private LocalDate expiryDate;
}
