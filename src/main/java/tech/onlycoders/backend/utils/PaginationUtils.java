package tech.onlycoders.backend.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PaginationUtils {

  public static int getPagesQuantity(int totalQuantity, int pageSize) {
    var bd_totalQuantity = BigDecimal.valueOf(totalQuantity);
    var bd_pageSize = BigDecimal.valueOf(pageSize);

    var bd_pageQuantity = bd_totalQuantity.divide(bd_pageSize);
    return bd_pageQuantity.setScale(0, RoundingMode.UP).intValue();
  }
}
