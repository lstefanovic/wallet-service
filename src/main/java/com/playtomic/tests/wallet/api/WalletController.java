package com.playtomic.tests.wallet.api;

import com.playtomic.tests.wallet.api.dto.PurchaseDto;
import com.playtomic.tests.wallet.api.dto.RestoreDto;
import com.playtomic.tests.wallet.api.dto.TopUpDto;
import com.playtomic.tests.wallet.api.dto.TransactionDto;
import com.playtomic.tests.wallet.api.dto.WalletDto;
import com.playtomic.tests.wallet.api.exception.WalletControllerException;
import com.playtomic.tests.wallet.repository.model.Transaction;
import com.playtomic.tests.wallet.repository.model.Wallet;
import com.playtomic.tests.wallet.service.WalletService;
import lombok.AccessLevel;
import lombok.Getter;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController()
@RequestMapping("/wallet")
public class WalletController {

    protected static final String ERROR_OCCURRED_ON_GETTING_TRANSACTION_HISTORY = "Error occurred on getting transaction history";
    protected static final String ERROR_OCCURRED_ON_GETTING_WALLET_INFO = "Error occurred on getting Wallet info.";
    protected static final String ERROR_OCCURRED_ON_WALLET_TOP_UP = "Error occurred on wallet top up.";
    protected static final String FAILED_TO_RESTORE_PURCHASE = "Failed to restore purchase.";
    protected static final String PURCHASE_FAILED = "Purchase failed.";
    private static final Logger log = LoggerFactory.getLogger(WalletController.class);
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    @Getter(AccessLevel.PRIVATE)
    private WalletService walletService;

    @GetMapping("/{walletId}/transactions")
    public List<TransactionDto> getTransactionHistory(@PathVariable String walletId) {
	try {
	    List<Transaction> transactions = getWalletService().transactionHistory(walletId);
	    return transactions.stream().map(transaction -> modelMapper.map(transaction, TransactionDto.class))
		    .collect(Collectors.toList());
	} catch (Exception e) {
	    throw new WalletControllerException(ERROR_OCCURRED_ON_GETTING_TRANSACTION_HISTORY, e);
	}
    }

    @GetMapping("/{walletId}")
    public WalletDto getWallet(@PathVariable String walletId) {
	try {
	    Wallet wallet = getWalletService().getWallet(walletId);
	    return WalletDto.builder().id(wallet.getId()).balance(wallet.getBalance()).build();
	} catch (Exception e) {
	    throw new WalletControllerException(ERROR_OCCURRED_ON_GETTING_WALLET_INFO, e);
	}
    }

    @PatchMapping("/{walletId}/purchase")
    public ResponseEntity<String> makePurchase(@PathVariable String walletId, @RequestBody PurchaseDto purchase) {
	try {
	    String paymentId = getWalletService().makePurchase(walletId, purchase.getAmount());
	    return ResponseEntity.ok(paymentId);
	} catch (Exception e) {
	    throw new WalletControllerException(PURCHASE_FAILED, e);
	}
    }

    @PatchMapping("/{walletId}/restore")
    public ResponseEntity<Object> restorePurchase(@PathVariable String walletId, @RequestBody RestoreDto restoreDto) {
	try {
	    getWalletService().restorePurchase(walletId, restoreDto.getPaymentId());
	    return ResponseEntity.ok().build();
	} catch (Exception e) {
	    throw new WalletControllerException(FAILED_TO_RESTORE_PURCHASE, e);
	}
    }

    @PatchMapping("/{walletId}/top-up")
    public void topUpWallet(@PathVariable String walletId, @RequestBody TopUpDto topUp) {
	try {
	    getWalletService().topUpWallet(walletId, topUp.getCreditCardNumber(), topUp.getAmount());
	} catch (Exception e) {
	    throw new WalletControllerException(ERROR_OCCURRED_ON_WALLET_TOP_UP, e);
	}
    }
}
