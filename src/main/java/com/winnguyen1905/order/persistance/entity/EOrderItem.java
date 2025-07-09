package com.winnguyen1905.order.persistance.entity;

import java.util.UUID;

import com.winnguyen1905.order.common.constant.OrderItemStatus;

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
@Table(name = "order_items")
public class EOrderItem extends EBaseAudit {
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private EOrder order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_sku")
    private String productSku;

    @Column(name = "product_category")
    private String productCategory;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "dimensions", columnDefinition = "json")
    private String dimensions;

    @Column(name = "tax_category")
    private String taxCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderItemStatus status;
}
