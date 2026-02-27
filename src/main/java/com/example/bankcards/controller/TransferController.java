package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfers", description = "Transfer between own cards")
public class TransferController {

    private final TransferService transferService;
    private final com.example.bankcards.repository.UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Transfer between own cards (atomic)")
    public ResponseEntity<Map<String, Object>> transfer(@Valid @RequestBody TransferRequest request,
                                                        @AuthenticationPrincipal UserDetails user) {
        Long userId = userRepository.findByUsername(user.getUsername()).orElseThrow().getId();
        Transaction tx = transferService.transfer(
                request.getFromCardId(),
                request.getToCardId(),
                request.getAmountCents(),
                userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", tx.getId(),
                "fromCardId", tx.getFromCard().getId(),
                "toCardId", tx.getToCard().getId(),
                "amountCents", tx.getAmountCents(),
                "createdAt", tx.getCreatedAt()
        ));
    }
}
