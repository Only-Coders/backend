package tech.onlycoders.backend.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PaginationUtilsTest {

  @Test
  public void ShouldRoundUpDivisions() {
    var result = PaginationUtils.getPagesQuantity(500, 400);
    assertEquals(2, result);
    result = PaginationUtils.getPagesQuantity(10, 3);
    assertEquals(4, result);
    result = PaginationUtils.getPagesQuantity(10, 3);
    assertEquals(4, result);
  }

  @Test
  public void ShouldFailToRoundUpDivisions() {
    assertThrows(IllegalArgumentException.class, () -> PaginationUtils.getPagesQuantity(-1, 400));
    assertThrows(IllegalArgumentException.class, () -> PaginationUtils.getPagesQuantity(100, 0));
  }
}
