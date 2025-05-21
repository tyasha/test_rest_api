import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.yakov.TestRestApiApplication;
import org.yakov.controller.WalletController;
import org.yakov.dto.WalletOperationDTO;
import org.yakov.exception.InsufficientFundsException;
import org.yakov.exception.WalletNotFoundException;
import org.yakov.model.OperationType;
import org.yakov.model.Wallet;
import org.yakov.service.WalletService;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestRestApiApplication.class)
@AutoConfigureMockMvc
public class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetWallet_Success() throws Exception {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(new BigDecimal("1000.00"));
        wallet.setVersion(1L);

        when(walletService.get(walletId)).thenReturn(wallet);

        mockMvc.perform(get("/api/v1/wallets/{uuid}", walletId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(walletId.toString()))
                .andExpect(jsonPath("$.balance").value("1000.0"))
                .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    public void testGetWallet_NotFound() throws Exception {
        UUID walletId = UUID.randomUUID();
        when(walletService.get(walletId)).thenThrow(new WalletNotFoundException("Кошелек не найден"));

        mockMvc.perform(get("/api/v1/wallets/{uuid}", walletId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("WALLET_NOT_FOUND"));
    }

    @Test
    public void testDepositOperation_Success() throws Exception {
        UUID walletId = UUID.randomUUID();
        WalletOperationDTO dto = new WalletOperationDTO();
        dto.setWalletId(walletId);
        dto.setOperationType(OperationType.DEPOSIT);
        dto.setAmount(new BigDecimal("500.00"));

        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(new BigDecimal("1500.0"));  // после внесения 500
        wallet.setVersion(1L);

        when(walletService.processOperation(any(WalletOperationDTO.class))).thenReturn(wallet);

        mockMvc.perform(post("/api/v1/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(walletId.toString()))
                .andExpect(jsonPath("$.balance").value("1500.0"));
    }

    @Test
    public void testWithdrawOperation_InsufficientFunds() throws Exception {
        UUID walletId = UUID.randomUUID();
        WalletOperationDTO dto = new WalletOperationDTO();
        dto.setWalletId(walletId);
        dto.setOperationType(OperationType.WITHDRAW);
        dto.setAmount(new BigDecimal("2000.00"));

        when(walletService.processOperation(any(WalletOperationDTO.class)))
                .thenThrow(new InsufficientFundsException("Недостаточно средств"));

        mockMvc.perform(post("/api/v1/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_FUNDS"));
    }

    @Test
    public void testInvalidJson() throws Exception {
        String invalidJson = "{\"walletId\": \"not-a-uuid\", \"operationType\": \"DEPOSIT\", \"amount\": 100}";

        mockMvc.perform(post("/api/v1/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}