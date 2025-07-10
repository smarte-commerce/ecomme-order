package com.winnguyen1905.order.persistance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "order_external_refs")
public class EOrderExternalRef extends EBaseAudit {
  @ManyToOne
  @JoinColumn(name = "order_id", nullable = false)
  private EOrder order;

  @Column(name = "service_name", nullable = false)
  private String serviceName;

  @Column(name = "external_id", nullable = false)
  private String externalId;

  @Column(name = "ref_type", nullable = false)
  private String refType;

  @Column(name = "status")
  private String status;

  // created_at, updated_at handled by EBaseAudit
}
