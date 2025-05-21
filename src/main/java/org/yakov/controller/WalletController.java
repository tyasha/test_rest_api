package org.yakov.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yakov.dto.WalletOperationDTO;
import org.yakov.model.Wallet;
import org.yakov.service.WalletService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<Wallet> operate(@RequestBody @Valid WalletOperationDTO request) {
        Wallet wallet = walletService.processOperation(request);
        return ResponseEntity.ok(wallet);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<Wallet> getWallet(@PathVariable UUID uuid) {
        Wallet wallet = walletService.get(uuid);
        return ResponseEntity.ok(wallet);
    }
}
