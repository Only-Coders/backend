package tech.onlycoders.backend.mapper;

import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import tech.onlycoders.backend.dto.contactrequest.response.ReadContactRequestDto;
import tech.onlycoders.backend.model.ContactRequest;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses = { UserMapper.class })
public interface ContactRequestMapper {
  ReadContactRequestDto contactRequestToReadContactRequestDto(ContactRequest contactRequest);

  List<ReadContactRequestDto> contactRequestListToReadContactRequestDtoList(List<ContactRequest> contactRequests);
}
