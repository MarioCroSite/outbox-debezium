package com.mario.inventoryservice.repositories;

import com.mario.inventoryservice.domain.entity.OutBox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OutBoxRepository extends JpaRepository<OutBox, UUID> {

}
