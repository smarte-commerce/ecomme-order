package com.winnguyen1905.order.persistance.entity;

import java.time.LocalDate;
import java.util.List;

import com.winnguyen1905.order.common.constant.OrderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@Table(name = "orders")
public class EOrder extends EBaseAudit {
  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @Column(name = "order_number", nullable = false, unique = true)
  private String orderNumber;

  @Column(name = "subtotal", nullable = false)
  private Double subtotal;

  @Column(name = "discount_amount", nullable = false)
  private Double discountAmount;

  @Column(name = "tax_amount", nullable = false)
  private Double taxAmount;

  @Column(name = "shipping_amount", nullable = false)
  private Double shippingAmount;

  @Column(name = "total_amount", nullable = false)
  private Double totalAmount;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private OrderStatus status;

  @Column(name = "shipping_address", columnDefinition = "json")
  private String shippingAddress;

  @Column(name = "billing_address", columnDefinition = "json")
  private String billingAddress;

  @Column(name = "estimated_delivery_date")
  private LocalDate estimatedDeliveryDate;

  @Column(name = "special_instructions")
  private String specialInstructions;

  @OneToMany(mappedBy = "order")
  private List<EOrderItem> orderItems;

  @OneToMany(mappedBy = "order")
  private List<EOrderDiscount> orderDiscounts;

  @OneToMany(mappedBy = "order")
  private List<EOrderStatusHistory> statusHistories;

  @OneToMany(mappedBy = "order")
  private List<EVendorOrder> vendorOrders;

  @OneToMany(mappedBy = "order")
  private List<EOrderExternalRef> externalRefs;
}
