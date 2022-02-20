package com.playtomic.tests.wallet.service;

import com.playtomic.tests.wallet.repository.TransactionRepository;
import com.playtomic.tests.wallet.repository.WalletRepository;
import com.playtomic.tests.wallet.repository.exception.WalletException;
import com.playtomic.tests.wallet.repository.model.Transaction;
import com.playtomic.tests.wallet.repository.model.TransactionType;
import com.playtomic.tests.wallet.repository.model.Wallet;
import com.playtomic.tests.wallet.service.exception.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.service.exception.WalletServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class WalletService {

    public static final String AMOUNT_CAN_T_BE_LESS_THAN_10 = "Amount can't be less than 10.";
    public static final String THERE_IS_NO_WALLET_WITH_ID = "There is no wallet with id: %s";
    protected static final String INSUFFICIENT_BALANCE = "Insufficient balance.";
    @Autowired
    private StripeService stripeService;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private WalletRepository walletRepository;


    public Wallet getWallet(@NonNull String walletId) {
	return findWalletById(walletId);
    }

    @Transactional
    public String makePurchase(@NonNull String walletId, @NonNull BigDecimal amount) {
	try {
	    Wallet wallet = findWalletById(walletId);
	    checkBalance(wallet.getBalance(), amount);
	    BigDecimal updatedBalance = wallet.getBalance().subtract(amount);
	    this.walletRepository.updateWallet(walletId, updatedBalance);

	    String paymentId = UUID.randomUUID().toString();
	    Transaction transaction = Transaction.builder().walletId(walletId).paymentId(paymentId)
		    .transactionType(TransactionType.PURCHASE.name()).amount(amount).build();
	    return this.transactionRepository.save(transaction).getPaymentId();
	} catch (Exception e) {
	    log.error(e.getMessage(), e);
	    throw new WalletServiceException(e.getMessage(), e);
	}
    }

    @Transactional
    public void restorePurchase(@NonNull String walletId, @NonNull String paymentId) {
	try {
	    this.transactionRepository.checkTransactionRestore(walletId, paymentId);
	    Transaction transaction = this.transactionRepository.findTransaction(walletId, paymentId);

	    Wallet wallet = findWalletById(walletId);
	    BigDecimal updatedBalance = wallet.getBalance().add(transaction.getAmount());
	    this.walletRepository.updateWallet(walletId, updatedBalance);

	    Transaction refundTransaction = createTransaction(walletId, paymentId, null,
		    TransactionType.RESTORE.name(), transaction.getAmount());
	    this.transactionRepository.insert(refundTransaction);
	} catch (Exception e) {
	    log.error(e.getMessage(), e);
	    throw new WalletServiceException(e.getMessage(), e);
	}
    }

    @Transactional
    public void topUpWallet(@NonNull String walletId, @NonNull String creditCardNumber, @NonNull BigDecimal amount) {
	try {
	    Wallet wallet = findWalletById(walletId);
	    BigDecimal updatedBalance = wallet.getBalance().add(amount);
	    this.walletRepository.updateWallet(walletId, updatedBalance);

	    String paymentId = UUID.randomUUID().toString();
	    Transaction transaction = createTransaction(walletId, paymentId, creditCardNumber, TransactionType.TOP_UP.name(), amount);
	    this.transactionRepository.insert(transaction);

	    this.stripeService.charge(creditCardNumber, amount);
	} catch (Exception e) {
	    log.error(e.getMessage(), e);
	    if (e instanceof StripeAmountTooSmallException) {
		throw new WalletServiceException(AMOUNT_CAN_T_BE_LESS_THAN_10);
	    }
	    throw new WalletServiceException(e.getMessage(), e);
	}
    }

    public List<Transaction> transactionHistory(@NonNull String walletId) {
	try {
	    return this.transactionRepository.getAllTransactions(walletId);
	} catch (Exception e) {
	    log.error(e.getMessage(), e);
	    throw new WalletServiceException(e.getMessage(), e);
	}
    }

    private void checkBalance(BigDecimal balance, BigDecimal amount) {
	int diff = balance.compareTo(amount);
	if (diff < 0) {
	    throw new WalletException(INSUFFICIENT_BALANCE);
	}
    }

    private Transaction createTransaction(String walletId, String paymentId, String creditCardNumber,
					  String transactionType, BigDecimal amount) {
	return Transaction.builder().walletId(walletId).paymentId(paymentId)
		.creditCardNumber(creditCardNumber).transactionType(transactionType)
		.amount(amount).build();
    }

    private Wallet findWalletById(@NonNull String walletId) {
	return this.walletRepository.findById(walletId).orElseThrow(
		() -> new WalletServiceException(String.format(THERE_IS_NO_WALLET_WITH_ID, walletId)));

    }
}
