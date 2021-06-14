package tech.onlycoders.notificator.dto;

import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDTO implements Serializable {

  private EventType eventType;
  private String message;
  private String to;
  private String from;
  private String imageURI;

  @Builder.Default
  private Date createdAt = new Date();
}
