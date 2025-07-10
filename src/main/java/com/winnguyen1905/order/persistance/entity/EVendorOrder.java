package com.winnguyen1905.order.persistance.entity;

import com.winnguyen1905.order.common.constant.VendorOrderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@Table(name = "vendor_orders")
public class EVendorOrder extends EBaseAudit {
  @ManyToOne
  @JoinColumn(name = "order_id", nullable = false)
  private EOrder order;

  @Column(name = "vendor_id", nullable = false)
  private Long vendorId;

  @Column(name = "vendor_order_number", nullable = false, unique = true)
  private String vendorOrderNumber;

  @Column(name = "subtotal", nullable = false)
  private Double subtotal;

  @Column(name = "commission_rate", nullable = false)
  private Double commissionRate;

  @Column(name = "commission_amount", nullable = false)
  private Double commissionAmount;

  @Column(name = "vendor_payout", nullable = false)
  private Double vendorPayout;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private VendorOrderStatus status;

  // created_at, updated_at handled by EBaseAudit
}
