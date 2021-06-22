package tech.onlycoders.backend.service;

import java.util.ArrayList;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.onlycoders.backend.bean.FirebaseService;
import tech.onlycoders.backend.dto.OrderBy;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.RoleEnum;
import tech.onlycoders.backend.dto.SortAllUsersBy;
import tech.onlycoders.backend.dto.admin.request.CreateAdminDto;
import tech.onlycoders.backend.dto.admin.response.ReadAdminDto;
import tech.onlycoders.backend.dto.admin.response.ReadGenericUserDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.AdminMapper;
import tech.onlycoders.backend.repository.AdminRepository;
import tech.onlycoders.backend.repository.GenericRepository;
import tech.onlycoders.backend.repository.PersonRepository;
import tech.onlycoders.backend.repository.RoleRepository;
import tech.onlycoders.backend.utils.PaginationUtils;
import tech.onlycoders.notificator.dto.EventType;
import tech.onlycoders.notificator.dto.MessageDTO;

@Service
@Transactional
public class AdminService {

  private final AdminMapper adminMapper;
  private final PersonRepository personRepository;
  private final AdminRepository adminRepository;
  private final FirebaseService firebaseService;
  private final RoleRepository roleRepository;
  private final NotificatorService notificatorService;
  private final GenericRepository genericRepository;

  public AdminService(
    PersonRepository personRepository,
    AdminMapper adminMapper,
    AdminRepository adminRepository,
    FirebaseService firebaseService,
    RoleRepository roleRepository,
    NotificatorService notificatorService,
    GenericRepository genericRepository
  ) {
    this.personRepository = personRepository;
    this.adminMapper = adminMapper;
    this.adminRepository = adminRepository;
    this.firebaseService = firebaseService;
    this.roleRepository = roleRepository;
    this.notificatorService = notificatorService;
    this.genericRepository = genericRepository;
  }

  public ReadAdminDto createAdmin(CreateAdminDto createAdminDto) throws ApiException {
    if (!createAdminDto.getEmail().endsWith("onlycoders.tech")) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "error.email-not-allowed");
    }
    var optionalPerson = this.personRepository.findByEmail(createAdminDto.getEmail());
    if (optionalPerson.isPresent()) {
      throw new ApiException(HttpStatus.CONFLICT, "error.email-taken");
    } else {
      var admin = this.adminMapper.createAdminDtoToPerson(createAdminDto);
      var role =
        this.roleRepository.findById("ADMIN")
          .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.500"));
      admin.setRole(role);
      adminRepository.save(admin);
      var activationLink = this.firebaseService.createUser(createAdminDto.getEmail());
      this.notificatorService.send(
          MessageDTO
            .builder()
            .eventType(EventType.NEW_ADMIN_ACCOUNT)
            .to(admin.getEmail())
            .message(activationLink)
            .build()
        );
      return adminMapper.adminToReadAdminDto(admin);
    }
  }

  public PaginateDto<ReadGenericUserDto> paginateAllUsers(
    String partialName,
    RoleEnum role,
    SortAllUsersBy sortBy,
    OrderBy orderBy,
    Integer page,
    Integer size
  ) {
    var totalQuantity = 0;

    var regex = "(?i).*" + partialName + ".*";
    var roleRegex = role == null ? "(?i).*" : role.name();

    var genericUserDtoList = new ArrayList<>(
      this.genericRepository.paginateAllPeople(regex, sortBy.label, orderBy.name(), roleRegex, page * size, size)
    );

    if (role == null) {
      totalQuantity = this.personRepository.countAllPeople(regex);
    } else if (role.equals(RoleEnum.ADMIN)) {
      totalQuantity = this.personRepository.countAllAdmins(regex);
    } else {
      totalQuantity = this.personRepository.countAllUsers(regex);
    }

    var pageQuantity = PaginationUtils.getPagesQuantity(totalQuantity, size);

    var dto = new PaginateDto<ReadGenericUserDto>();
    dto.setContent(genericUserDtoList);
    dto.setCurrentPage(page);
    dto.setTotalPages(pageQuantity);
    dto.setTotalElements(totalQuantity);

    return dto;
  }
}
