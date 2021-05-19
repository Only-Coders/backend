package tech.onlycoders.backend.utils;

public class PaginationUtils {

  public static int getPagesQuantity(int totalQuantity, int pageSize) throws IllegalArgumentException {
    if (pageSize < 1) throw new IllegalArgumentException("Page size cannot be lower than 1");
    if (totalQuantity < 0) throw new IllegalArgumentException("Total quantity cannot be lower than 0");
    return (totalQuantity + pageSize - 1) / pageSize;
  }
}
