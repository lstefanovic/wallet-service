package com.playtomic.tests.wallet.repository;

import com.playtomic.tests.wallet.repository.model.Wallet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@DataMongoTest
@ExtendWith(SpringExtension.class)
public class WalletCustomRepositoryImplTest {

    private static final BigDecimal START_BALANCE = BigDecimal.valueOf(50);
    private static final String WALLET_ID = "id1";
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private WalletRepository walletRepository;

    @Test
    public void emptyWalletTest() {
	BigDecimal amount = BigDecimal.valueOf(10);
	this.walletRepository.emptyWallet(WALLET_ID, amount);
	Wallet foundWallet = mongoTemplate.findById(WALLET_ID, Wallet.class);

	assertNotNull(foundWallet);
	assertEquals(WALLET_ID, foundWallet.getId());
	assertEquals(START_BALANCE.subtract(amount), foundWallet.getBalance());
    }

    @Test
    public void emptyWalletWithHigherAmountThanBalanceTest() {
	BigDecimal amount = BigDecimal.valueOf(51);
	try {
	    this.walletRepository.emptyWallet(WALLET_ID, amount);
	    fail();
	} catch (Exception e) {
	    assertEquals(WalletCustomRepositoryImpl.INSUFFICIENT_BALANCE, e.getMessage());
	}
	Wallet foundWallet = mongoTemplate.findById(WALLET_ID, Wallet.class);

	assertNotNull(foundWallet);
	assertEquals(WALLET_ID, foundWallet.getId());
	assertEquals(START_BALANCE, foundWallet.getBalance());
    }

    @Test
    public void topUpWallet() {
	BigDecimal amount = BigDecimal.valueOf(10);
	this.walletRepository.topUpWallet(WALLET_ID, amount);
	Wallet foundWallet = mongoTemplate.findById(WALLET_ID, Wallet.class);

	assertNotNull(foundWallet);
	assertEquals(WALLET_ID, foundWallet.getId());
	assertEquals(START_BALANCE.add(amount), foundWallet.getBalance());
    }

    @AfterEach
    private void after() {
	mongoTemplate.dropCollection(Wallet.class);
    }

    @BeforeEach
    private void before() {
	Wallet wallet = Wallet.builder().id(WALLET_ID).balance(START_BALANCE).build();
	this.mongoTemplate.save(wallet);
    }

}
