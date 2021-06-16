package tech.onlycoders.backend.service;

import java.util.Optional;
import java.util.stream.Collectors;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.onlycoders.backend.dto.notificationConfiguration.request.NotificationConfigDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.NotificationMapper;
import tech.onlycoders.backend.model.NotificationConfig;
import tech.onlycoders.backend.model.User;
import tech.onlycoders.backend.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

  @InjectMocks
  private NotificationService service;

  @Mock
  private NotificationRepository notificationRepository;

  private final EasyRandom ezRandom = new EasyRandom();

  @Spy
  private NotificationMapper notificationMapper;

  @Test
  public void ShouldConfigureNotification() throws ApiException {
    var notificationConfigDto = ezRandom.nextObject(NotificationConfigDto.class);
    var id = ezRandom.nextObject(String.class);
    var user = ezRandom.nextObject(User.class);

    Mockito
      .when(notificationRepository.findById(Mockito.any(), Mockito.any()))
      .thenReturn(Optional.of(NotificationConfig.builder().build()));

    service.updateStatus(user.getCanonicalName(), notificationConfigDto, id);
  }

  @Test
  public void ShouldReturnUserNotificationConfig() {
    var notificationConfigs = ezRandom.objects(NotificationConfig.class, 10).collect(Collectors.toList());
    var user = ezRandom.nextObject(User.class);
    Mockito
      .when(notificationRepository.getUserNotificationConfig(user.getCanonicalName()))
      .thenReturn(notificationConfigs);
    service.getUserNotificationConfig(user.getCanonicalName());
  }
}
