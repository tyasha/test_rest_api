package org.yakov.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yakov.dto.WalletOperationDTO;
import org.yakov.exception.ConcurrentModificationException;
import org.yakov.exception.InsufficientFundsException;
import org.yakov.exception.InvalidOperationTypeException;
import org.yakov.exception.WalletNotFoundException;
import org.yakov.model.OperationType;
import org.yakov.model.Wallet;
import org.yakov.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public Wallet processOperation(WalletOperationDTO dto) {
        Wallet wallet = walletRepository.findById(dto.getWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Кошелек с ID " + dto.getWalletId() + " не найден"));

        BigDecimal amount = dto.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительным числом");
        }

        if (dto.getOperationType() == OperationType.WITHDRAW) {
            if (wallet.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("Недостаточно средств на кошельке для снятия " + amount);
            }
            wallet.setBalance(wallet.getBalance().subtract(amount));
        } else if (dto.getOperationType() == OperationType.DEPOSIT) {
            wallet.setBalance(wallet.getBalance().add(amount));
        } else {
            throw new InvalidOperationTypeException("Недопустимый тип операции: " + dto.getOperationType());
        }

        try {
            return walletRepository.save(wallet);
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            throw new ConcurrentModificationException("Кошелек был модифицирован другой транзакцией. Пожалуйста, повторите операцию.");
        }
    }

    @Override
    public Wallet get(UUID uuid) {
        return walletRepository.findById(uuid)
                .orElseThrow(() -> new WalletNotFoundException("Кошелек с ID " + uuid + " не найден"));
    }
}
