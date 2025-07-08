package com.winnguyen1905.order.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExternalRefRequest {
  @NotBlank(message = "Service name is required")
  private String serviceName;

  @NotBlank(message = "External ID is required")
  private String externalId;

  @NotBlank(message = "Reference type is required")
  private String refType;

  private String status;
}
