package tech.onlycoders.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.onlycoders.backend.bean.FirebaseService;
import tech.onlycoders.backend.dto.admin.request.CreateAdminDto;
import tech.onlycoders.backend.dto.admin.response.ReadAdminDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.AdminMapper;
import tech.onlycoders.backend.repository.AdminRepository;
import tech.onlycoders.backend.repository.PersonRepository;

@Service
public class AdminService {

  private final AdminMapper adminMapper;
  private final PersonRepository personRepository;
  private final AdminRepository adminRepository;
  private final FirebaseService firebaseService;

  public AdminService(
    PersonRepository personRepository,
    AdminMapper adminMapper,
    AdminRepository adminRepository,
    FirebaseService firebaseService
  ) {
    this.personRepository = personRepository;
    this.adminMapper = adminMapper;
    this.adminRepository = adminRepository;
    this.firebaseService = firebaseService;
  }

  public ReadAdminDto createAdmin(CreateAdminDto createAdminDto) throws ApiException {
    var optionalPerson = this.personRepository.findByEmail(createAdminDto.getEmail());
    if (optionalPerson.isPresent()) {
      throw new ApiException(HttpStatus.CONFLICT, "error.email-taken");
    } else {
      this.firebaseService.createUser(createAdminDto.getEmail());
      var admin = this.adminMapper.createAdminDtoToPerson(createAdminDto);
      adminRepository.save(admin);
      return adminMapper.adminToReadAdminDto(admin);
    }
  }
}
