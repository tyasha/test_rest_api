package org.yakov.service;

import org.yakov.dto.WalletOperationDTO;
import org.yakov.exception.ConcurrentModificationException;
import org.yakov.exception.InsufficientFundsException;
import org.yakov.exception.InvalidOperationTypeException;
import org.yakov.exception.WalletNotFoundException;
import org.yakov.model.Wallet;

import java.util.UUID;


public interface WalletService {
    /**
     * Обрабатывает операции с кошельком (пополнение или снятие)
     *
     * @param dto объект, содержащий данные для операции
     * @return обновленный кошелек
     * @throws WalletNotFoundException если кошелек не найден
     * @throws InsufficientFundsException если недостаточно средств для снятия
     * @throws InvalidOperationTypeException если указан неверный тип операции
     * @throws ConcurrentModificationException если произошла конкурентная модификация кошелька
     */
    Wallet processOperation(WalletOperationDTO dto) throws WalletNotFoundException,
            InsufficientFundsException,
            InvalidOperationTypeException,
            ConcurrentModificationException;

    /**
     * Получает информацию о кошельке по его идентификатору
     *
     * @param uuid идентификатор кошелька
     * @return кошелек
     * @throws WalletNotFoundException если кошелек не найден
     */
    Wallet get(UUID uuid) throws WalletNotFoundException;
}
