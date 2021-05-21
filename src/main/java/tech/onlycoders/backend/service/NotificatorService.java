package tech.onlycoders.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.onlycoders.notificator.dto.MessageDTO;

@Service
@Slf4j
@Transactional
public class NotificatorService {

  private final RabbitTemplate template;
  private final Queue queue;

  public NotificatorService(RabbitTemplate template, Queue queue) {
    this.template = template;
    this.queue = queue;
  }

  public void send(MessageDTO message) {
    this.template.convertAndSend(queue.getName(), message);
    log.info("~ notification sent {}", message);
  }
}
