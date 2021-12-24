package com.playtomic.tests.wallet.service;

import com.playtomic.tests.wallet.repository.TransactionRepository;
import com.playtomic.tests.wallet.repository.WalletRepository;
import com.playtomic.tests.wallet.repository.model.Transaction;
import com.playtomic.tests.wallet.repository.model.TransactionType;
import com.playtomic.tests.wallet.repository.model.Wallet;
import com.playtomic.tests.wallet.service.exception.WalletServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class WalletService {

    public static final String AMOUNT_CAN_T_BE_LESS_THAN_10 = "Amount can't be less than 10.";
    public static final String THERE_IS_NO_WALLET_WITH_ID = "There is no wallet with id: %s";
    @Autowired
    private StripeService stripeService;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private WalletRepository walletRepository;


    public Wallet getWallet(@NonNull String walletId) {
	return this.walletRepository.findById(walletId).orElseThrow(
		() -> new WalletServiceException(String.format(THERE_IS_NO_WALLET_WITH_ID, walletId)));
    }

    public String makePurchase(@NonNull String walletId, @NonNull BigDecimal amount) {
	try {
	    this.walletRepository.emptyWallet(walletId, amount);

	    String paymentId = UUID.randomUUID().toString();
	    Transaction transaction = Transaction.builder().walletId(walletId).paymentId(paymentId)
		    .transactionType(TransactionType.PURCHASE.name()).amount(amount).build();
	    return this.transactionRepository.save(transaction).getPaymentId();
	} catch (Exception e) {
	    log.error(e.getMessage(), e);
	    throw new WalletServiceException(e.getMessage(), e);
	}
    }

    public void restorePurchase(@NonNull String walletId, @NonNull String paymentId) {
	try {
	    this.transactionRepository.checkTransactionRestore(walletId, paymentId);

	    Transaction transaction = this.transactionRepository.findTransaction(walletId, paymentId);
	    this.walletRepository.topUpWallet(walletId, transaction.getAmount());

	    Transaction refundTransaction = Transaction.builder().walletId(walletId).paymentId(paymentId)
		    .transactionType(TransactionType.RESTORE.name()).build();
	    this.transactionRepository.save(refundTransaction);
	} catch (Exception e) {
	    log.error(e.getMessage(), e);
	    throw new WalletServiceException(e.getMessage(), e);
	}
    }

    public void topUpWallet(@NonNull String walletId, @NonNull String creditCardNumber, @NonNull BigDecimal amount) {
	try {
	    checkAmount(amount);
	    String paymentId = UUID.randomUUID().toString();
	    Transaction transaction = Transaction.builder().walletId(walletId).paymentId(paymentId)
		    .creditCardNumber(creditCardNumber).transactionType(TransactionType.TOP_UP.name())
		    .amount(amount).build();
	    this.stripeService.charge(creditCardNumber, amount);
	    this.walletRepository.topUpWallet(walletId, amount);
	    this.transactionRepository.save(transaction);
	} catch (Exception e) {
	    log.error(e.getMessage(), e);
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

    private void checkAmount(BigDecimal amount) {
	int amountDiff = amount.compareTo(BigDecimal.valueOf(10));

	if (amountDiff < 0) {
	    throw new WalletServiceException(AMOUNT_CAN_T_BE_LESS_THAN_10);
	}
    }
}
