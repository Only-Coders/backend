package tech.onlycoders.backend.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.management.Notification;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import tech.onlycoders.backend.dto.comment.request.CreateCommentDto;
import tech.onlycoders.backend.dto.notificationConfiguration.request.NotificationConfigDto;
import tech.onlycoders.backend.dto.post.request.CreatePostDto;
import tech.onlycoders.backend.dto.post.request.CreateReactionDto;
import tech.onlycoders.backend.dto.report.request.CreatePostReportDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.*;
import tech.onlycoders.backend.model.*;
import tech.onlycoders.backend.repository.*;
import tech.onlycoders.backend.utils.PartialPostImpl;
import tech.onlycoders.backend.utils.PartialUserImpl;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

  @InjectMocks
  private NotificationService service;

  @Mock
  private NotificationRepository notificationRepository;

  private final EasyRandom ezRandom = new EasyRandom();

  @Spy
  private WorkPositionMapper workPositionMapper = new WorkPositionMapperImpl(new WorkplaceMapperImpl());

  @Test
  public void ShouldConfigureNotification() {
    var notificationConfigDto = ezRandom.nextObject(NotificationConfigDto.class);

    var user = ezRandom.nextObject(User.class);

    Mockito
      .when(notificationRepository.findByType(Mockito.any(), Mockito.any()))
      .thenReturn(Optional.of(NotificationConfig.builder().build()));

    service.updateStatus(user.getCanonicalName(), notificationConfigDto);
  }

  @Test
  public void ShouldCreateNotification() {
    var notificationConfigDto = ezRandom.nextObject(NotificationConfigDto.class);
    var user = ezRandom.nextObject(User.class);

    Mockito.when(notificationRepository.findByType(Mockito.any(), Mockito.any())).thenReturn(Optional.empty());

    service.updateStatus(user.getCanonicalName(), notificationConfigDto);
  }
}
