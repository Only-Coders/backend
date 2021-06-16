package tech.onlycoders.backend.service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.onlycoders.backend.dto.notificationConfiguration.request.NotificationConfigDto;
import tech.onlycoders.backend.dto.notificationConfiguration.response.ReadNotificationConfigDto;
import tech.onlycoders.backend.exception.ApiException;
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

  public void updateStatus(String canonicalName, NotificationConfigDto notificationConfigDto, String id)
    throws ApiException {
    var config = notificationRepository
      .findById(id, canonicalName)
      .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.notification-not-found"));

    config.setEmail(notificationConfigDto.getEmail());
    config.setPush(notificationConfigDto.getPush());
    notificationRepository.save(config);
  }

  public List<ReadNotificationConfigDto> getUserNotificationConfig(String canonicalName) {
    return this.notificationMapper.listNotificationConfigToReadNotificationConfig(
        this.notificationRepository.getUserNotificationConfig(canonicalName)
      );
  }
}
