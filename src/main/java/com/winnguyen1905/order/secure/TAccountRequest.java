package com.winnguyen1905.order.secure;

import java.util.UUID;

import lombok.Builder;

@Builder
public record TAccountRequest(
    UUID id,
    String username,
    AccountType accountType,
    UUID socketClientId, RegionPartition region) implements AbstractModel {

  @Builder
  public TAccountRequest(
      UUID id,
      String username,
      AccountType accountType,
      UUID socketClientId, RegionPartition region) {
    this.id = id;
    this.username = username;
    this.accountType = accountType;
    this.region = region;
    this.socketClientId = socketClientId;
  }
}
