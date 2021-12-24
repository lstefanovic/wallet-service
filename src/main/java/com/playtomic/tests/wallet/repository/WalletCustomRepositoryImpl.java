package com.playtomic.tests.wallet.repository;

import com.playtomic.tests.wallet.repository.exception.WalletException;
import com.playtomic.tests.wallet.repository.model.Wallet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Slf4j
@Repository
public class WalletCustomRepositoryImpl implements WalletCustomRepository {

    protected static final String COULDN_T_FIND_REQUESTED_WALLET = "Couldn't find requested wallet.";
    protected static final String INSUFFICIENT_BALANCE = "Insufficient balance.";
    private static final String THERE_IS_NO_WALLET = "There is no wallet with id: %s";
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void emptyWallet(@NonNull String id, @NonNull BigDecimal amount) {
	Wallet wallet = findWallet(id);
	checkBalance(wallet.getBalance(), amount);

	BigDecimal updatedBalance = wallet.getBalance().subtract(amount);
	updateWallet(id, updatedBalance);
    }

    @Override
    public void topUpWallet(@NonNull String id, @NonNull BigDecimal amount) {
	Wallet wallet = findWallet(id);
	BigDecimal updatedBalance = wallet.getBalance().add(amount);
	updateWallet(id, updatedBalance);
    }

    private void checkBalance(BigDecimal balance, BigDecimal amount) {
	int diff = balance.compareTo(amount);
	if (diff < 0) {
	    throw new WalletException(INSUFFICIENT_BALANCE);
	}
    }

    private Wallet findWallet(String id) {
	Wallet wallet = this.mongoTemplate.findById(id, Wallet.class);
	if (wallet == null) {
	    log.error(String.format(THERE_IS_NO_WALLET, id));
	    throw new WalletException(COULDN_T_FIND_REQUESTED_WALLET);
	}
	return wallet;
    }

    private void updateWallet(String id, BigDecimal updatedBalance) {
	Query query = new Query();
	query.addCriteria(Criteria.where("_id").is(id));

	Update updateQuery = new Update();
	updateQuery.set("balance", updatedBalance);
	this.mongoTemplate.findAndModify(query, updateQuery, Wallet.class);
    }
}
