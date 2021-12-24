package com.playtomic.tests.wallet.service.impl;

import com.playtomic.tests.wallet.repository.TransactionRepository;
import com.playtomic.tests.wallet.repository.WalletRepository;
import com.playtomic.tests.wallet.repository.exception.WalletException;
import com.playtomic.tests.wallet.repository.model.Transaction;
import com.playtomic.tests.wallet.repository.model.TransactionType;
import com.playtomic.tests.wallet.repository.model.Wallet;
import com.playtomic.tests.wallet.service.StripeService;
import com.playtomic.tests.wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class WalletServiceTest {

    private static final String WALLET_ID = "wallet1";
    @MockBean
    private StripeService stripeService;
    @MockBean
    private TransactionRepository transactionRepository;
    @MockBean
    private WalletRepository walletRepository;
    @Autowired
    private WalletService walletService;

    @Test
    public void getWalletFailedTest() {

	WalletException ex = new WalletException("error occurred");
	when(this.walletRepository.findById(WALLET_ID)).thenThrow(ex);

	try {
	    Wallet result = this.walletService.getWallet(WALLET_ID);
	    fail("expected");
	} catch (Exception e) {
	    assertNotNull(e);
	    assertEquals(ex.getMessage(), e.getMessage());
	}
	verify(this.walletRepository).findById(WALLET_ID);
    }

    @Test
    public void getWalletTest() {

	Wallet wallet = Wallet.builder().id(WALLET_ID).balance(BigDecimal.valueOf(305)).build();
	when(this.walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(wallet));
	Wallet result = this.walletService.getWallet(WALLET_ID);

	assertNotNull(result);
	assertEquals(wallet.getId(), result.getId());
	assertEquals(wallet.getBalance(), result.getBalance());

	verify(this.walletRepository).findById(WALLET_ID);
    }

    @Test
    public void makePurchaseFailedTest() {
	BigDecimal amount = BigDecimal.valueOf(10);

	WalletException ex = new WalletException("Insufficient balance");
	doThrow(ex).when(this.walletRepository).emptyWallet(WALLET_ID, amount);

	try {
	    this.walletService.makePurchase(WALLET_ID, amount);
	    fail("excpected");
	} catch (Exception e) {
	    assertNotNull(e);
	    assertEquals(ex.getMessage(), e.getMessage());
	}
	verify(this.walletRepository).emptyWallet(WALLET_ID, amount);
    }

    @Test
    public void makePurchaseTest() {
	BigDecimal amount = BigDecimal.valueOf(10);
	String paymentId = "12345";

	doNothing().when(this.walletRepository).emptyWallet(WALLET_ID, amount);

	Transaction transaction = Transaction.builder().paymentId(paymentId).build();
	when(this.transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

	String result = this.walletService.makePurchase(WALLET_ID, amount);
	assertEquals(paymentId, result);

	verify(this.walletRepository).emptyWallet(WALLET_ID, amount);
	verify(this.transactionRepository).save(any(Transaction.class));
    }

    @Test
    public void restorePurchaseFailedTest() {
	String paymentId = "1234";
	WalletException ex = new WalletException("Purchase already restored.");
	doThrow(ex).when(this.transactionRepository).checkTransactionRestore(WALLET_ID, paymentId);

	try {
	    this.walletService.restorePurchase(WALLET_ID, paymentId);
	    fail("Expected");
	} catch (Exception e) {
	    assertNotNull(e);
	    assertEquals(ex.getMessage(), e.getMessage());
	}
	verify(this.transactionRepository).checkTransactionRestore(WALLET_ID, paymentId);
    }

    @Test
    public void restorePurchaseTest() {
	String paymentId = "1234";

	doNothing().when(this.transactionRepository).checkTransactionRestore(WALLET_ID, paymentId);

	Transaction transaction = Transaction.builder().walletId(WALLET_ID).paymentId(paymentId)
		.transactionType(TransactionType.PURCHASE.name()).build();
	when(this.transactionRepository.findTransaction(WALLET_ID, paymentId)).thenReturn(transaction);
	doNothing().when(this.walletRepository).topUpWallet(WALLET_ID, transaction.getAmount());
	when(this.transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

	this.walletService.restorePurchase(WALLET_ID, paymentId);

	verify(this.transactionRepository).checkTransactionRestore(WALLET_ID, paymentId);
	verify(this.transactionRepository).findTransaction(WALLET_ID, paymentId);
	verify(this.walletRepository).topUpWallet(WALLET_ID, transaction.getAmount());
	verify(this.transactionRepository).save(any(Transaction.class));
    }

    @Test
    public void topUpWalletFailedTest() {

	String creditCardNumber = "2222 3333 4444";
	BigDecimal amount = BigDecimal.valueOf(9);

	try {
	    this.walletService.topUpWallet(WALLET_ID, creditCardNumber, amount);
	} catch (Exception e) {
	    assertNotNull(e);
	    assertEquals(WalletService.AMOUNT_CAN_T_BE_LESS_THAN_10, e.getMessage());
	}
    }

    @Test
    public void topUpWalletTest() {

	String creditCardNumber = "2222 3333 4444";
	BigDecimal amount = BigDecimal.valueOf(10);
	doNothing().when(this.stripeService).charge(creditCardNumber, amount);
	doNothing().when(this.walletRepository).topUpWallet(WALLET_ID, amount);
	Transaction transaction = Transaction.builder().paymentId("payment2").build();
	when(this.transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

	this.walletService.topUpWallet(WALLET_ID, creditCardNumber, amount);

	verify(this.stripeService).charge(creditCardNumber, amount);
	verify(this.walletRepository).topUpWallet(WALLET_ID, amount);
	verify(this.transactionRepository).save(any(Transaction.class));
    }

    @Test
    public void transactionHistoryTest() {

	Transaction transaction1 = Transaction.builder().walletId(WALLET_ID).amount(BigDecimal.valueOf(150))
		.transactionType(TransactionType.TOP_UP.name()).build();
	Transaction transaction2 = Transaction.builder().walletId(WALLET_ID).amount(BigDecimal.valueOf(60))
		.transactionType(TransactionType.PURCHASE.name()).build();
	List<Transaction> transactions = List.of(transaction1, transaction2);

	when(this.transactionRepository.getAllTransactions(WALLET_ID)).thenReturn(transactions);

	List<Transaction> result = this.walletService.transactionHistory(WALLET_ID);
	assertNotNull(result);
	assertEquals(transaction1, result.get(0));
	assertEquals(transaction2, result.get(1));

	verify(this.transactionRepository).getAllTransactions(WALLET_ID);
    }

}
