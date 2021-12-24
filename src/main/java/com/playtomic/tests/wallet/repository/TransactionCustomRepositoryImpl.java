package com.playtomic.tests.wallet.repository;

import com.playtomic.tests.wallet.repository.exception.TransactionException;
import com.playtomic.tests.wallet.repository.model.Transaction;
import com.playtomic.tests.wallet.repository.model.TransactionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * persist information about every transaction
 */
@Slf4j
@Repository
public class TransactionCustomRepositoryImpl implements TransactionCustomRepository {

    protected static final String REQUESTED_TRANSACTION_DOESN_T_EXIST = "Requested transaction doesn't exist.";
    private static final String THERE_IS_NO_TRANSACTIONS_FOR_WALLET = "There is no transactions for wallet: %s";
    private static final String THERE_IS_NO_TRANSACTION_FOR_WALLET_AND_PAYMENT = "There is no transaction for wallet: %s, and payment: %s.";
    protected static final String TRANSACTION_IS_ALREADY_RESTORED = "Transaction is already restored.";
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void checkTransactionRestore(@NonNull String walletId, @NonNull String paymentId) {
	Query query = new Query();
	query.addCriteria(Criteria.where("walletId").is(walletId));
	query.addCriteria(Criteria.where("paymentId").is(paymentId));
	query.addCriteria(Criteria.where("transactionType").is(TransactionType.RESTORE.name()));
	Transaction transaction = this.mongoTemplate.findOne(query, Transaction.class);

	if (transaction != null) {
	    throw new TransactionException(TRANSACTION_IS_ALREADY_RESTORED);
	}
    }

    @Override
    public Transaction findTransaction(String walletId, String paymentId) {
	Query query = new Query();
	query.addCriteria(Criteria.where("walletId").is(walletId));
	query.addCriteria(Criteria.where("paymentId").is(paymentId));
	Transaction transaction = this.mongoTemplate.findOne(query, Transaction.class);

	if (transaction == null) {
	    log.error(String.format(THERE_IS_NO_TRANSACTION_FOR_WALLET_AND_PAYMENT, walletId, paymentId));
	    throw new TransactionException(REQUESTED_TRANSACTION_DOESN_T_EXIST);
	}
	return transaction;
    }

    @Override
    public List<Transaction> getAllTransactions(@NonNull String walletId) {
	Query query = new Query();
	query.addCriteria(Criteria.where("walletId").is(walletId));

	List<Transaction> transactions = this.mongoTemplate.find(query, Transaction.class);

	if (CollectionUtils.isEmpty(transactions)) {
	    log.error(String.format(THERE_IS_NO_TRANSACTIONS_FOR_WALLET, walletId));
	}
	return transactions;
    }
}
