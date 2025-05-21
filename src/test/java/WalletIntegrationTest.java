import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.yakov.TestRestApiApplication;
import org.yakov.dto.WalletOperationDTO;
import org.yakov.model.OperationType;
import org.yakov.model.Wallet;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = TestRestApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WalletIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private final UUID TEST_WALLET_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");

    @Test
    public void testGetWallet() {
        ResponseEntity<Wallet> response = restTemplate.getForEntity(
                "/api/v1/wallets/" + TEST_WALLET_ID, Wallet.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(TEST_WALLET_ID, response.getBody().getId());
    }

    @Test
    public void testDepositAndWithdraw() {
        // 1. Получаем начальный баланс
        ResponseEntity<Wallet> initialResponse = restTemplate.getForEntity(
                "/api/v1/wallets/" + TEST_WALLET_ID, Wallet.class);

        BigDecimal initialBalance = initialResponse.getBody().getBalance();

        // 2. Делаем депозит
        WalletOperationDTO depositDto = new WalletOperationDTO();
        depositDto.setWalletId(TEST_WALLET_ID);
        depositDto.setOperationType(OperationType.DEPOSIT);
        depositDto.setAmount(new BigDecimal("500.00"));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<WalletOperationDTO> depositRequest = new HttpEntity<>(depositDto, headers);

        ResponseEntity<Wallet> depositResponse = restTemplate.postForEntity(
                "/api/v1/wallets", depositRequest, Wallet.class);

        assertEquals(HttpStatus.OK, depositResponse.getStatusCode());
        assertEquals(initialBalance.add(new BigDecimal("500.00")), depositResponse.getBody().getBalance());

        // 3. Делаем снятие
        WalletOperationDTO withdrawDto = new WalletOperationDTO();
        withdrawDto.setWalletId(TEST_WALLET_ID);
        withdrawDto.setOperationType(OperationType.WITHDRAW);
        withdrawDto.setAmount(new BigDecimal("200.00"));

        HttpEntity<WalletOperationDTO> withdrawRequest = new HttpEntity<>(withdrawDto, headers);

        ResponseEntity<Wallet> withdrawResponse = restTemplate.postForEntity(
                "/api/v1/wallets", withdrawRequest, Wallet.class);

        assertEquals(HttpStatus.OK, withdrawResponse.getStatusCode());
        assertEquals(initialBalance.add(new BigDecimal("300.00")), withdrawResponse.getBody().getBalance());
    }
}
