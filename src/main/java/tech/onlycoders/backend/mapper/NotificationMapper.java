package tech.onlycoders.backend.mapper;

import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import tech.onlycoders.backend.dto.notificationConfiguration.response.ReadNotificationConfigDto;
import tech.onlycoders.backend.model.NotificationConfig;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface NotificationMapper {
  List<ReadNotificationConfigDto> listNotificationConfigToReadNotificationConfig(
    List<NotificationConfig> notificationConfigs
  );
}
