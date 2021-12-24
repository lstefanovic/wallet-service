package com.playtomic.tests.wallet.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playtomic.tests.wallet.api.dto.PurchaseDto;
import com.playtomic.tests.wallet.api.dto.RestoreDto;
import com.playtomic.tests.wallet.api.dto.TopUpDto;
import com.playtomic.tests.wallet.api.dto.WalletDto;
import com.playtomic.tests.wallet.api.exception.handler.ExceptionDetails;
import com.playtomic.tests.wallet.repository.model.Transaction;
import com.playtomic.tests.wallet.repository.model.TransactionType;
import com.playtomic.tests.wallet.repository.model.Wallet;
import com.playtomic.tests.wallet.service.WalletService;
import com.playtomic.tests.wallet.service.exception.WalletServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static com.playtomic.tests.wallet.api.WalletController.ERROR_OCCURRED_ON_GETTING_TRANSACTION_HISTORY;
import static com.playtomic.tests.wallet.api.WalletController.ERROR_OCCURRED_ON_GETTING_WALLET_INFO;
import static com.playtomic.tests.wallet.api.WalletController.ERROR_OCCURRED_ON_WALLET_TOP_UP;
import static com.playtomic.tests.wallet.api.WalletController.FAILED_TO_RESTORE_PURCHASE;
import static com.playtomic.tests.wallet.api.WalletController.PURCHASE_FAILED;
import static com.playtomic.tests.wallet.api.exception.handler.ResponseExceptionHandler.WALLET_ERROR_TITTLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(value = WalletController.class)
public class WalletControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @SpyBean
    private ModelMapper modelMapper;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private WalletService walletService;

    @Test
    public void getTransactionsFailedTest() throws Exception {
	doThrow(new WalletServiceException("Transactions not found."))
		.when(this.walletService).transactionHistory(any(String.class));

	mockMvc.perform(get("/wallet/id/transactions"))
		.andDo(mvcResult -> {
		    String responseAsString = mvcResult.getResponse().getContentAsString();
		    ExceptionDetails exceptionDetails = objectMapper.readValue(responseAsString, ExceptionDetails.class);
		    assertEquals(WALLET_ERROR_TITTLE, exceptionDetails.getTitle());
		    assertEquals(ERROR_OCCURRED_ON_GETTING_TRANSACTION_HISTORY, exceptionDetails.getMessage());
		    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exceptionDetails.getStatus());
		});
	verify(this.walletService).transactionHistory(any(String.class));
    }

    @Test
    public void getTransactionsTest() throws Exception {
	String walletId = "12345";
	Transaction transaction1 = Transaction.builder().walletId(walletId)
		.paymentId("payment1").creditCardNumber("54321")
		.transactionType(TransactionType.TOP_UP.name()).amount(BigDecimal.valueOf(50)).build();

	Transaction transaction2 = Transaction.builder().walletId(walletId)
		.paymentId("payment2").creditCardNumber(null)
		.transactionType(TransactionType.PURCHASE.name()).amount(BigDecimal.valueOf(30)).build();

	when(this.walletService.transactionHistory(walletId)).thenReturn(List.of(transaction1, transaction2));

	mockMvc.perform(get("/wallet/" + walletId + "/transactions"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$[0].paymentId").value(transaction1.getPaymentId()))
		.andExpect(jsonPath("$[0].creditCardNumber").value(transaction1.getCreditCardNumber()))
		.andExpect(jsonPath("$[0].transactionType").value(transaction1.getTransactionType()))
		.andExpect(jsonPath("$[0].amount").value(transaction1.getAmount()))
		.andExpect(jsonPath("$[1].paymentId").value(transaction2.getPaymentId()))
		.andExpect(jsonPath("$[1].creditCardNumber").value(transaction2.getCreditCardNumber()))
		.andExpect(jsonPath("$[1].transactionType").value(transaction2.getTransactionType()))
		.andExpect(jsonPath("$[1].amount").value(transaction2.getAmount()));
	verify(this.walletService).transactionHistory(walletId);
    }

    @Test
    public void getWalletNotFoundTest() throws Exception {
	doThrow(new WalletServiceException("Wallet not found."))
		.when(this.walletService).getWallet(any(String.class));

	mockMvc.perform(get("/wallet/" + "id"))
		.andDo(mvcResult -> {
		    String responseAsString = mvcResult.getResponse().getContentAsString();
		    ExceptionDetails exceptionDetails = objectMapper.readValue(responseAsString, ExceptionDetails.class);
		    assertEquals(WALLET_ERROR_TITTLE, exceptionDetails.getTitle());
		    assertEquals(ERROR_OCCURRED_ON_GETTING_WALLET_INFO, exceptionDetails.getMessage());
		    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exceptionDetails.getStatus());
		});
	verify(this.walletService).getWallet(any(String.class));
    }

    @Test
    public void getWalletTest() throws Exception {
	String walletId = "id1";
	BigDecimal balance = BigDecimal.valueOf(300);
	Wallet wallet = Wallet.builder().id(walletId).balance(balance).build();
	when(this.walletService.getWallet(walletId)).thenReturn(wallet);

	mockMvc.perform(get("/wallet/" + walletId))
		.andExpect(status().isOk())
		.andDo(mvcResult -> {
		    String responseAsString = mvcResult.getResponse().getContentAsString();
		    WalletDto walletResponse = objectMapper.readValue(responseAsString, WalletDto.class);
		    assertEquals(walletId, walletResponse.getId());
		    assertEquals(balance, walletResponse.getBalance());
		});
	verify(this.walletService).getWallet(walletId);
    }

    @Test
    public void makePurchaseFailedTest() throws Exception {
	BigDecimal amount = BigDecimal.valueOf(35);

	doThrow(new WalletServiceException("Purchase failed."))
		.when(this.walletService).makePurchase(any(String.class), any(BigDecimal.class));

	mockMvc.perform(patch("/wallet/id/purchase")
			.content(objectMapper.writeValueAsString(new PurchaseDto(amount)))
			.contentType(MediaType.APPLICATION_JSON_VALUE))
		.andDo(mvcResult -> {
		    String responseAsString = mvcResult.getResponse().getContentAsString();
		    ExceptionDetails exceptionDetails = objectMapper.readValue(responseAsString, ExceptionDetails.class);
		    assertEquals(WALLET_ERROR_TITTLE, exceptionDetails.getTitle());
		    assertEquals(PURCHASE_FAILED, exceptionDetails.getMessage());
		    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exceptionDetails.getStatus());
		});
	verify(this.walletService).makePurchase(any(String.class), any(BigDecimal.class));
    }

    @Test
    public void makePurchaseTest() throws Exception {
	String walletId = "id1";
	String paymentId = "pay1";
	BigDecimal amount = BigDecimal.valueOf(35);
	PurchaseDto purchaseDto = new PurchaseDto(amount);

	when(this.walletService.makePurchase(walletId, amount)).thenReturn(paymentId);

	mockMvc.perform(patch("/wallet/" + walletId + "/purchase")
			.content(objectMapper.writeValueAsString(purchaseDto))
			.contentType(MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().isOk())
		.andDo(mvcResult -> {
		    String responseAsString = mvcResult.getResponse().getContentAsString();
		    assertEquals(paymentId, responseAsString);
		});

	verify(this.walletService).makePurchase(walletId, amount);

    }

    @Test
    public void purchaseRestoreFailedTest() throws Exception {
	String paymentId = "pay1";
	RestoreDto restoreDto = new RestoreDto(paymentId);
	doThrow(new WalletServiceException("Purchase restore failed."))
		.when(this.walletService).restorePurchase(any(String.class), any(String.class));

	mockMvc.perform(patch("/wallet/id/restore")
			.content(objectMapper.writeValueAsString(restoreDto))
			.contentType(MediaType.APPLICATION_JSON_VALUE))
		.andDo(mvcResult -> {
		    String responseAsString = mvcResult.getResponse().getContentAsString();
		    ExceptionDetails exceptionDetails = objectMapper.readValue(responseAsString, ExceptionDetails.class);
		    assertEquals(WALLET_ERROR_TITTLE, exceptionDetails.getTitle());
		    assertEquals(FAILED_TO_RESTORE_PURCHASE, exceptionDetails.getMessage());
		    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exceptionDetails.getStatus());
		});
	verify(this.walletService).restorePurchase(any(String.class), any(String.class));
    }

    @Test
    public void restorePurchaseTest() throws Exception {
	String walletId = "id1";
	String paymentId = "pay1";
	RestoreDto restoreDto = new RestoreDto(paymentId);

	doNothing().when(this.walletService).restorePurchase(walletId, paymentId);

	mockMvc.perform(patch("/wallet/" + walletId + "/restore")
			.content(objectMapper.writeValueAsString(restoreDto))
			.contentType(MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().isOk());
	verify(this.walletService).restorePurchase(walletId, paymentId);
    }

    @Test
    public void topUpWalletFailedTest() throws Exception {
	String creditCard = "333-444-3333";
	BigDecimal amount = BigDecimal.valueOf(55);
	TopUpDto topUpDto = new TopUpDto(amount, creditCard);
	doThrow(new WalletServiceException("Wallet top up failed."))
		.when(this.walletService).topUpWallet(any(String.class), any(String.class), any(BigDecimal.class));

	mockMvc.perform(patch("/wallet/id/top-up")
			.content(objectMapper.writeValueAsString(topUpDto))
			.contentType(MediaType.APPLICATION_JSON_VALUE))
		.andDo(mvcResult -> {
		    String responseAsString = mvcResult.getResponse().getContentAsString();
		    ExceptionDetails exceptionDetails = objectMapper.readValue(responseAsString, ExceptionDetails.class);
		    assertEquals(WALLET_ERROR_TITTLE, exceptionDetails.getTitle());
		    assertEquals(ERROR_OCCURRED_ON_WALLET_TOP_UP, exceptionDetails.getMessage());
		    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exceptionDetails.getStatus());
		});
	verify(this.walletService).topUpWallet(any(String.class), any(String.class), any(BigDecimal.class));
    }

    @Test
    public void topUpWalletTest() throws Exception {
	String walletId = "id1";
	String creditCard = "333-444-3333";
	BigDecimal amount = BigDecimal.valueOf(55);
	TopUpDto topUpDto = new TopUpDto(amount, creditCard);

	doNothing().when(this.walletService).topUpWallet(walletId, creditCard, amount);

	mockMvc.perform(patch("/wallet/" + walletId + "/top-up")
			.content(objectMapper.writeValueAsString(topUpDto))
			.contentType(MediaType.APPLICATION_JSON_VALUE))
		.andExpect(status().isOk());

	verify(this.walletService).topUpWallet(walletId, creditCard, amount);
    }

}
