package tech.onlycoders.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.blacklist.response.ReadBlackListDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.BlackListMapper;
import tech.onlycoders.backend.model.BlackList;
import tech.onlycoders.backend.repository.BlacklistRepository;
import tech.onlycoders.backend.utils.PaginationUtils;

@Service
public class BlackListService {

  private final BlacklistRepository blacklistRepository;
  private final BlackListMapper blackListMapper;

  public BlackListService(BlacklistRepository blacklistRepository, BlackListMapper blackListMapper) {
    this.blacklistRepository = blacklistRepository;
    this.blackListMapper = blackListMapper;
  }

  public PaginateDto<ReadBlackListDto> paginateBlackList(String partialEmail, Integer page, Integer size) {
    var regex = "(?i)" + partialEmail + ".*";
    var blackLists = this.blacklistRepository.paginateAllBlackListedUsers(regex, page * size, size);
    var totalQuantity = this.blacklistRepository.countAllBlackListedUsers(regex);

    var genericUserDtoList = this.blackListMapper.blackListToReadBlackLists(blackLists);
    var pageQuantity = PaginationUtils.getPagesQuantity(totalQuantity, size);

    var dto = new PaginateDto<ReadBlackListDto>();
    dto.setContent(genericUserDtoList);
    dto.setCurrentPage(page);
    dto.setTotalPages(pageQuantity);
    dto.setTotalElements(totalQuantity);
    return dto;
  }

  public void removeUser(String email) {
    blacklistRepository.removeByEmail(email);
  }

  public ReadBlackListDto addUser(String email) throws ApiException {
    var exist = blacklistRepository.findById(email);
    if (exist.isPresent()) {
      throw new ApiException(HttpStatus.CONFLICT, "error.user-already-exist");
    } else {
      var blackList = new BlackList();
      blackList.setEmail(email);

      blacklistRepository.save(blackList);

      var readBlackListDto = new ReadBlackListDto();
      readBlackListDto.setEmail(email);
      return readBlackListDto;
    }
  }
}
