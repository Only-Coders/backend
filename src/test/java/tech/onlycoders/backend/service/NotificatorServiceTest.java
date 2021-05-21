package tech.onlycoders.backend.service;

import static org.hamcrest.Matchers.any;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tech.onlycoders.notificator.dto.MessageDTO;

@ExtendWith(MockitoExtension.class)
public class NotificatorServiceTest {

  @InjectMocks
  private NotificatorService service;

  @Mock
  private RabbitTemplate template;

  @Mock
  private Queue queue;

  private final EasyRandom ezRandom = new EasyRandom();

  @Test
  @MockitoSettings(strictness = Strictness.LENIENT)
  public void ShouldSendMessage() {
    var message = ezRandom.nextObject(MessageDTO.class);
    Mockito.doNothing().when(this.template).convertAndSend("onlycoders-notificator", any(MessageDTO.class));
    Mockito.when(this.queue.getName()).thenReturn("onlycoders-notificator");
    this.service.send(message);
  }
}
