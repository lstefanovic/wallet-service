package com.playtomic.tests.wallet.repository;

import com.playtomic.tests.wallet.repository.model.Wallet;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WalletRepository extends MongoRepository<Wallet, String>, WalletCustomRepository {
}
