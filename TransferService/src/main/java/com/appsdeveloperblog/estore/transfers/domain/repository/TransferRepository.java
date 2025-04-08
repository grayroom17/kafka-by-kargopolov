package com.appsdeveloperblog.estore.transfers.domain.repository;

import com.appsdeveloperblog.estore.transfers.domain.entity.TransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<TransferEntity, String> {

}
