package tech.onlycoders.backend.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaginateDto<T> {

  private int currentPage;
  private int totalElements;
  private int totalPages;
  private List<T> content;
}
