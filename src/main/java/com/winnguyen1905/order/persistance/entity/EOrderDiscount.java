package com.winnguyen1905.order.persistance.entity;

import com.winnguyen1905.order.common.constant.DiscountAppliesTo;
import com.winnguyen1905.order.common.constant.DiscountType;

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
@Table(name = "order_discounts")
public class EOrderDiscount extends EBaseAudit {
  @ManyToOne
  @JoinColumn(name = "order_id", nullable = false)
  private EOrder order;

  @Column(name = "discount_code", nullable = false)
  private String discountCode;

  @Column(name = "discount_name")
  private String discountName;

  @Enumerated(EnumType.STRING)
  @Column(name = "discount_type", nullable = false)
  private DiscountType discountType;

  @Column(name = "discount_value", nullable = false)
  private Double discountValue;

  @Column(name = "discount_amount", nullable = false)
  private Double discountAmount;

  @Enumerated(EnumType.STRING)
  @Column(name = "applies_to")
  private DiscountAppliesTo appliesTo;

  // applied_at handled by EBaseAudit.createdDate
}
