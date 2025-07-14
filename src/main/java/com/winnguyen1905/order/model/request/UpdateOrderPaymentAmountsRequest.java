package com.winnguyen1905.order.model.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import com.winnguyen1905.order.secure.AbstractModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderPaymentAmountsRequest implements AbstractModel {
  
  @NotNull(message = "Paid amount is required")
  @DecimalMin(value = "0.0", message = "Paid amount must be non-negative")
  private BigDecimal paidAmount;

  @NotNull(message = "Amount to be paid is required")
  @DecimalMin(value = "0.0", message = "Amount to be paid must be non-negative")
  private BigDecimal amountToBePaid;
} 
