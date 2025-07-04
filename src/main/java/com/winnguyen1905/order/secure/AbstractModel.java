package com.winnguyen1905.order.secure;

import java.io.Serial;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

// @JsonInclude(value = Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
@JsonFormat(pattern = "HH-mm-ss a dd-MM-yyyy", timezone = "GMT+7")
public interface AbstractModel extends Serializable {
  @Serial
  static final long serialVersionUID = 7213600440729202783L;
}
