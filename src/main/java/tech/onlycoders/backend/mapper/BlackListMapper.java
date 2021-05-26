package tech.onlycoders.backend.mapper;

import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import tech.onlycoders.backend.dto.blacklist.response.ReadBlackListDto;
import tech.onlycoders.backend.model.BlackList;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface BlackListMapper {
  List<ReadBlackListDto> blackListToReadBlackLists(List<BlackList> blacklists);
}
