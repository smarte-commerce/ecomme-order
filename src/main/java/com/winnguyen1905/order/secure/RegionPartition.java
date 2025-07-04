package com.winnguyen1905.order.secure;

public enum RegionPartition {
  US("us"),
  EU("eu"),
  ASIA("asia");

  private final String region;

  RegionPartition(String region) {
    this.region = region;
  }

  public String getRegion() {
    return region;
  }

  public static final RegionPartition US_PARTITION = RegionPartition.US;
  public static final RegionPartition EU_PARTITION = RegionPartition.EU;
  public static final RegionPartition ASIA_PARTITION = RegionPartition.ASIA;
}
