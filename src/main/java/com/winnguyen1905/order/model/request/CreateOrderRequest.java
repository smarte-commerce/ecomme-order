package com.winnguyen1905.order.model.request;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.Min;
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
public class CreateOrderRequest implements AbstractModel {
  private List<CheckoutItemRequest> checkoutItems;
  private UUID shippingDiscountId;
  private UUID globalProductDiscountId;

  // Customer Infomation
  @NotNull(message = "Shipping address is required")
  private String shippingAddress;

  private String billingAddress;

  private LocalDate estimatedDeliveryDate;

  private String specialInstructions;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CheckoutItemRequest implements AbstractModel {
    private UUID shopId;
    private String notes;
    private List<Item> items;
    private UUID shopProductDiscountId;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Item implements AbstractModel {
    private UUID productId;

    private UUID variantId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private Double weight;

    private String dimensions;

    private String taxCategory;
  }
}
