package org.yakov.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.yakov.model.OperationType;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletOperationDTO {
    @NotNull(message = "ID кошелька не может быть пустым")
    private UUID walletId;

    @NotNull(message = "Тип операции не может быть пустым")
    private OperationType operationType;

    @NotNull(message = "Сумма не может быть пустой")
    @DecimalMin(value = "0.01", message = "Сумма должна быть положительным числом")
    private BigDecimal amount;
}
