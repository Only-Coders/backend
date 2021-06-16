package tech.onlycoders.backend.service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.onlycoders.backend.dto.notificationConfiguration.request.NotificationConfigDto;
import tech.onlycoders.backend.dto.notificationConfiguration.response.ReadNotificationConfigDto;
import tech.onlycoders.backend.mapper.NotificationMapper;
import tech.onlycoders.backend.model.NotificationConfig;
import tech.onlycoders.backend.repository.NotificationRepository;

@Service
@Slf4j
@Transactional
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;

  public NotificationService(NotificationRepository notificationRepository, NotificationMapper notificationMapper) {
    this.notificationRepository = notificationRepository;
    this.notificationMapper = notificationMapper;
  }

  public void updateStatus(String canonicalName, NotificationConfigDto notificationConfigDto) {
    var configOptional = notificationRepository.findByType(notificationConfigDto.getType().name(), canonicalName);

    if (configOptional.isPresent()) {
      var config = configOptional.get();
      config.setEmail(notificationConfigDto.getEmail());
      config.setPush(notificationConfigDto.getPush());
      config.setType(notificationConfigDto.getType());
      notificationRepository.save(config);
    } else {
      var notification = NotificationConfig
        .builder()
        .email(notificationConfigDto.getEmail())
        .type(notificationConfigDto.getType())
        .push(notificationConfigDto.getPush())
        .build();
      notificationRepository.save(notification);
      notificationRepository.createConfiguration(notification.getId(), canonicalName);
    }
  }

  public List<ReadNotificationConfigDto> getUserNotificationConfig(String canonicalName) {
    return this.notificationMapper.listNotificationConfigToReadNotificationConfig(
        this.notificationRepository.getUserNotificationConfig(canonicalName)
      );
  }
}
