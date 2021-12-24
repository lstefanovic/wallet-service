package com.playtomic.tests.wallet.repository;

import com.playtomic.tests.wallet.repository.model.Transaction;
import com.playtomic.tests.wallet.repository.model.TransactionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

@DataMongoTest
@ExtendWith(SpringExtension.class)
public class TransactionRepositoryTest {

    private static final String TRANSACTION_ID1 = "topUp";
    private static final String TRANSACTION_ID2 = "purchase1";
    private static final String TRANSACTION_ID3 = "purchase2";
    private static final String WALLET_ID = "id1";
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    public void checkTransactionAlreadyRestoredTest() {
	try {
	    this.transactionRepository.checkTransactionRestore(WALLET_ID, TRANSACTION_ID3);
	    fail("Expected");
	} catch (Exception e) {
	    assertEquals(TransactionCustomRepositoryImpl.TRANSACTION_IS_ALREADY_RESTORED, e.getMessage());
	}
    }

    @Test
    public void checkTransactionRestoreTest() {

	this.transactionRepository.checkTransactionRestore(WALLET_ID, TRANSACTION_ID2);
	Query query = new Query();
	query.addCriteria(Criteria.where("walletId").is(WALLET_ID));
	query.addCriteria(Criteria.where("paymentId").is(TRANSACTION_ID2));
	query.addCriteria(Criteria.where("transactionType").is(TransactionType.RESTORE.name()));
	Transaction transaction = this.mongoTemplate.findOne(query, Transaction.class);
	assertNull(transaction);
    }

    @Test
    public void getAllTransactions() {
	List<Transaction> transactions = this.transactionRepository.getAllTransactions(WALLET_ID);
	assertEquals(4, transactions.size());

    }

    @Test
    public void getTransactionTest() {
	Transaction transaction = this.transactionRepository.findTransaction(WALLET_ID, TRANSACTION_ID1);

	assertEquals(WALLET_ID, transaction.getWalletId());
	assertEquals(TRANSACTION_ID1, transaction.getPaymentId());
	assertEquals(TransactionType.TOP_UP.name(), transaction.getTransactionType());
	assertEquals(BigDecimal.valueOf(50), transaction.getAmount());
    }

    @Test
    public void transactionNotFound() {

	try {
	    this.transactionRepository.findTransaction(WALLET_ID, "unknown");
	} catch (Exception e) {
	    assertEquals(TransactionCustomRepositoryImpl.REQUESTED_TRANSACTION_DOESN_T_EXIST, e.getMessage());
	}
    }

    @AfterEach
    private void after() {
	this.mongoTemplate.dropCollection(Transaction.class);
    }

    @BeforeEach
    private void before() {
	Transaction transactionTopUp = Transaction.builder().walletId(WALLET_ID)
		.transactionType(TransactionType.TOP_UP.name())
		.paymentId(TRANSACTION_ID1)
		.creditCardNumber("222 444 555").amount(BigDecimal.valueOf(50)).build();
	Transaction transactionPurchase1 = Transaction.builder().walletId(WALLET_ID)
		.transactionType(TransactionType.PURCHASE.name())
		.paymentId(TRANSACTION_ID2).amount(BigDecimal.valueOf(10)).build();
	Transaction transactionPurchase2 = Transaction.builder().walletId(WALLET_ID)
		.transactionType(TransactionType.PURCHASE.name())
		.paymentId(TRANSACTION_ID3).amount(BigDecimal.valueOf(14)).build();
	Transaction transactionRestore = Transaction.builder().walletId(WALLET_ID)
		.transactionType(TransactionType.RESTORE.name())
		.paymentId(TRANSACTION_ID3).amount(BigDecimal.valueOf(14)).build();
	this.mongoTemplate.save(transactionTopUp);
	this.mongoTemplate.save(transactionPurchase1);
	this.mongoTemplate.save(transactionPurchase2);
	this.mongoTemplate.save(transactionRestore);
    }
}
