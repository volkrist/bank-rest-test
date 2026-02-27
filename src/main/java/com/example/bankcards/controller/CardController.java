package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(name = "Cards", description = "My cards (USER) or all (ADMIN)")
public class CardController {

    private final CardService cardService;
    private final com.example.bankcards.repository.UserRepository userRepository;

    @GetMapping
    @Operation(summary = "List my cards with optional status filter and pagination")
    public Page<CardResponse> listMyCards(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(required = false) CardStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = userRepository.findByUsername(user.getUsername()).orElseThrow().getId();
        boolean admin = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (admin) {
            return cardService.getAllCardsForAdmin(pageable);
        }
        return cardService.getMyCards(userId, status, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get card by id")
    public CardResponse getById(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        Long userId = userRepository.findByUsername(user.getUsername()).orElseThrow().getId();
        boolean admin = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return cardService.getById(id, userId, admin);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create card (for self as USER, or for any user as ADMIN)")
    public CardResponse create(@Valid @RequestBody CardCreateRequest request,
                               @AuthenticationPrincipal UserDetails user,
                               @RequestParam(required = false) Long userId) {
        Long currentId = userRepository.findByUsername(user.getUsername()).orElseThrow().getId();
        boolean admin = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Long targetUserId = (admin && userId != null) ? userId : currentId;
        return cardService.createForUser(targetUserId, request, admin);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Set status: USER can request BLOCKED, ADMIN can set any")
    public CardResponse setStatus(@PathVariable Long id,
                                  @RequestParam CardStatus status,
                                  @AuthenticationPrincipal UserDetails user) {
        Long currentId = userRepository.findByUsername(user.getUsername()).orElseThrow().getId();
        boolean admin = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return cardService.setStatus(id, status, currentId, admin);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete card (ADMIN only)")
    public void delete(@PathVariable Long id) {
        cardService.deleteCard(id);
    }
}
