package com.winnguyen1905.order.secure;

public enum AccountType {
  ADMIN("ADMIN"), CUSTOMER("CUSTOMER"), VENDOR("VENDOR");

  String role;

  AccountType(String role) {
    this.role = role; 
  }
}
