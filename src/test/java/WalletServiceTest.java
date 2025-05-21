import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.yakov.dto.WalletOperationDTO;
import org.yakov.exception.ConcurrentModificationException;
import org.yakov.exception.InsufficientFundsException;
import org.yakov.exception.WalletNotFoundException;
import org.yakov.model.OperationType;
import org.yakov.model.Wallet;
import org.yakov.repository.WalletRepository;
import org.yakov.service.WalletServiceImpl;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    @Test
    public void testProcessDeposit_Success() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(new BigDecimal("1000.00"));
        wallet.setVersion(0L);

        WalletOperationDTO dto = new WalletOperationDTO();
        dto.setWalletId(walletId);
        dto.setOperationType(OperationType.DEPOSIT);
        dto.setAmount(new BigDecimal("500.00"));

        Wallet updatedWallet = new Wallet();
        updatedWallet.setId(walletId);
        updatedWallet.setBalance(new BigDecimal("1500.00"));
        updatedWallet.setVersion(1L);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(updatedWallet);

        Wallet result = walletService.processOperation(dto);

        assertEquals(new BigDecimal("1500.00"), result.getBalance());
        assertEquals(1L, result.getVersion());
        verify(walletRepository).findById(walletId);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    public void testProcessWithdraw_Success() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(new BigDecimal("1000.00"));
        wallet.setVersion(0L);

        WalletOperationDTO dto = new WalletOperationDTO();
        dto.setWalletId(walletId);
        dto.setOperationType(OperationType.WITHDRAW);
        dto.setAmount(new BigDecimal("500.00"));

        Wallet updatedWallet = new Wallet();
        updatedWallet.setId(walletId);
        updatedWallet.setBalance(new BigDecimal("500.00"));
        updatedWallet.setVersion(1L);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(updatedWallet);

        Wallet result = walletService.processOperation(dto);

        assertEquals(new BigDecimal("500.00"), result.getBalance());
        verify(walletRepository).findById(walletId);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    public void testProcessWithdraw_InsufficientFunds() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(new BigDecimal("100.00"));
        wallet.setVersion(0L);

        WalletOperationDTO dto = new WalletOperationDTO();
        dto.setWalletId(walletId);
        dto.setOperationType(OperationType.WITHDRAW);
        dto.setAmount(new BigDecimal("500.00"));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        assertThrows(InsufficientFundsException.class, () -> walletService.processOperation(dto));
        verify(walletRepository).findById(walletId);
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    public void testProcessOperation_WalletNotFound() {
        UUID walletId = UUID.randomUUID();
        WalletOperationDTO dto = new WalletOperationDTO();
        dto.setWalletId(walletId);
        dto.setOperationType(OperationType.DEPOSIT);
        dto.setAmount(new BigDecimal("500.00"));

        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> walletService.processOperation(dto));
        verify(walletRepository).findById(walletId);
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    public void testProcessOperation_ConcurrentModification() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(new BigDecimal("1000.00"));
        wallet.setVersion(0L);

        WalletOperationDTO dto = new WalletOperationDTO();
        dto.setWalletId(walletId);
        dto.setOperationType(OperationType.DEPOSIT);
        dto.setAmount(new BigDecimal("500.00"));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenThrow(ObjectOptimisticLockingFailureException.class);

        assertThrows(ConcurrentModificationException.class, () -> walletService.processOperation(dto));
        verify(walletRepository).findById(walletId);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    public void testGet_Success() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(new BigDecimal("1000.00"));
        wallet.setVersion(0L);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        Wallet result = walletService.get(walletId);

        assertEquals(walletId, result.getId());
        assertEquals(new BigDecimal("1000.00"), result.getBalance());
        verify(walletRepository).findById(walletId);
    }

    @Test
    public void testGet_WalletNotFound() {
        UUID walletId = UUID.randomUUID();
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> walletService.get(walletId));
        verify(walletRepository).findById(walletId);
    }
}