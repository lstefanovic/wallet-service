package com.playtomic.tests.wallet.repository;

import com.playtomic.tests.wallet.repository.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransactionRepository extends MongoRepository<Transaction, String>, TransactionCustomRepository {
}
