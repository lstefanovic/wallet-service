package com.playtomic.tests.wallet.repository;

import com.playtomic.tests.wallet.repository.model.Wallet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Slf4j
@Repository
public class WalletCustomRepositoryImpl implements WalletCustomRepository {

    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public void updateWallet(String id, BigDecimal updatedBalance) {
	Query query = new Query();
	query.addCriteria(Criteria.where("_id").is(id));

	Update updateQuery = new Update();
	updateQuery.set("balance", updatedBalance);
	this.mongoTemplate.findAndModify(query, updateQuery, Wallet.class);
    }
}
