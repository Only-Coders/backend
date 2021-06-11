package tech.onlycoders.backend.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.onlycoders.backend.bean.FirebaseService;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.RoleEnum;
import tech.onlycoders.backend.dto.admin.request.CreateAdminDto;
import tech.onlycoders.backend.dto.admin.response.ReadAdminDto;
import tech.onlycoders.backend.dto.admin.response.ReadGenericUserDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.AdminMapper;
import tech.onlycoders.backend.model.Person;
import tech.onlycoders.backend.repository.AdminRepository;
import tech.onlycoders.backend.repository.PersonRepository;
import tech.onlycoders.backend.repository.RoleRepository;
import tech.onlycoders.backend.utils.PaginationUtils;

@Service
@Transactional
public class AdminService {

  private final AdminMapper adminMapper;
  private final PersonRepository personRepository;
  private final AdminRepository adminRepository;
  private final FirebaseService firebaseService;
  private final RoleRepository roleRepository;

  public AdminService(
    PersonRepository personRepository,
    AdminMapper adminMapper,
    AdminRepository adminRepository,
    FirebaseService firebaseService,
    RoleRepository roleRepository
  ) {
    this.personRepository = personRepository;
    this.adminMapper = adminMapper;
    this.adminRepository = adminRepository;
    this.firebaseService = firebaseService;
    this.roleRepository = roleRepository;
  }

  public ReadAdminDto createAdmin(CreateAdminDto createAdminDto) throws ApiException {
    if (!createAdminDto.getEmail().endsWith("onlycoders.tech")) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "error.email-not-allowed");
    }
    var optionalPerson = this.personRepository.findByEmail(createAdminDto.getEmail());
    if (optionalPerson.isPresent()) {
      throw new ApiException(HttpStatus.CONFLICT, "error.email-taken");
    } else {
      this.firebaseService.createUser(createAdminDto.getEmail());
      var admin = this.adminMapper.createAdminDtoToPerson(createAdminDto);
      var role =
        this.roleRepository.findById("ADMIN")
          .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.500"));
      admin.setRole(role);
      adminRepository.save(admin);
      return adminMapper.adminToReadAdminDto(admin);
    }
  }

  public PaginateDto<ReadGenericUserDto> paginateAllUsers(
    String partialName,
    RoleEnum role,
    Integer page,
    Integer size
  ) {
    List<Person> people;
    var totalQuantity = 0;

    var regex = "(?i)" + partialName + ".*";

    if (role == null) {
      people = this.personRepository.paginateAllPeople(regex, page, size);
      totalQuantity = this.personRepository.countAllPeople(regex);
    } else if (role.equals(RoleEnum.ADMIN)) {
      people = this.personRepository.paginateAllAdmins(regex, page, size);
      totalQuantity = this.personRepository.countAllAdmins(regex);
    } else {
      people = this.personRepository.paginateAllUsers(regex, page, size);
      totalQuantity = this.personRepository.countAllUsers(regex);
    }

    var genericUserDtoList = this.adminMapper.peopleToReadGenericUsers(people);
    var pageQuantity = PaginationUtils.getPagesQuantity(totalQuantity, size);

    var dto = new PaginateDto<ReadGenericUserDto>();
    dto.setContent(genericUserDtoList);
    dto.setCurrentPage(page);
    dto.setTotalPages(pageQuantity);
    dto.setTotalElements(totalQuantity);

    return dto;
  }
}
