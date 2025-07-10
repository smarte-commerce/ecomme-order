package com.winnguyen1905.order.persistance.repository;

import com.winnguyen1905.order.persistance.entity.EOrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<EOrderStatusHistory, UUID> {
} 
