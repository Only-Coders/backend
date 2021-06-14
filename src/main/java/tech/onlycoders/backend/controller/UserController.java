package tech.onlycoders.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tech.onlycoders.backend.bean.auth.UserDetails;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.SortContactsBy;
import tech.onlycoders.backend.dto.SortUsersBy;
import tech.onlycoders.backend.dto.contactrequest.request.CreateContactRequestDto;
import tech.onlycoders.backend.dto.contactrequest.request.ResponseContactRequestDto;
import tech.onlycoders.backend.dto.contactrequest.response.ReadContactRequestDto;
import tech.onlycoders.backend.dto.institute.request.CreateInstituteDto;
import tech.onlycoders.backend.dto.institute.request.UpdateDegreeDto;
import tech.onlycoders.backend.dto.institute.response.ReadDegreeDto;
import tech.onlycoders.backend.dto.language.request.UpdateUserLanguageDto;
import tech.onlycoders.backend.dto.language.response.ReadLanguageDto;
import tech.onlycoders.backend.dto.pagination.*;
import tech.onlycoders.backend.dto.post.response.ReadPostDto;
import tech.onlycoders.backend.dto.skill.request.CreateSkillDto;
import tech.onlycoders.backend.dto.skill.response.ReadSkillDto;
import tech.onlycoders.backend.dto.tag.response.ReadTagDto;
import tech.onlycoders.backend.dto.user.request.*;
import tech.onlycoders.backend.dto.user.response.ReadUserDto;
import tech.onlycoders.backend.dto.user.response.ReadUserLiteDto;
import tech.onlycoders.backend.dto.user.response.ReadUserToDeleteDto;
import tech.onlycoders.backend.dto.workplace.request.CreateWorkplaceDto;
import tech.onlycoders.backend.dto.workposition.request.UpdateWorkPositionDto;
import tech.onlycoders.backend.dto.workposition.response.ReadWorkPositionDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.service.*;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

  private final UserService userService;
  private final WorkplaceService workplaceService;
  private final TagService tagService;
  private final InstituteService instituteService;
  private final SkillService skillService;
  private final PostService postService;

  public UserController(
    UserService userService,
    WorkplaceService workplaceService,
    TagService tagService,
    InstituteService instituteService,
    SkillService skillService,
    PostService postService
  ) {
    this.userService = userService;
    this.workplaceService = workplaceService;
    this.tagService = tagService;
    this.instituteService = instituteService;
    this.skillService = skillService;
    this.postService = postService;
  }

  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ReadUserDto.class)) }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  @GetMapping("/{canonicalName}")
  @Operation(summary = "Gets user profile")
  ResponseEntity<ReadUserDto> getProfile(@PathVariable String canonicalName) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var sourceCanonicalName = userDetails.getCanonicalName();
    var persistedPerson = this.userService.getProfile(sourceCanonicalName, canonicalName);
    return ResponseEntity.ok(persistedPerson);
  }

  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = { @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedUsers.class)) }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  @GetMapping
  @Operation(summary = "Search users by full name")
  ResponseEntity<PaginateDto<ReadUserDto>> findUsersByName(
    @RequestParam(defaultValue = "") String partialName,
    @RequestParam(defaultValue = "") String countryName,
    @RequestParam(defaultValue = "") String skillName,
    @RequestParam(defaultValue = "FIRSTNAME") SortUsersBy orderBy,
    @RequestParam(defaultValue = "0") @Min(0) Integer page,
    @RequestParam(defaultValue = "20") @Min(1) Integer size
  ) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var canonicalName = userDetails.getCanonicalName();
    var persistedPerson =
      this.userService.findByPartialName(canonicalName, page, size, partialName, countryName, skillName, orderBy);
    return ResponseEntity.ok(persistedPerson);
  }

  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ReadWorkPositionDto.class))
        }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  @PostMapping("/workplaces")
  @Operation(summary = "Adds a working experience.")
  ResponseEntity<ReadWorkPositionDto> addWorkingExperience(@RequestBody @Valid WorkExperienceDto workExperienceDto)
    throws ApiException {
    if (workExperienceDto.getId() == null) {
      var newOrganization =
        this.workplaceService.createWorkplace(CreateWorkplaceDto.builder().name(workExperienceDto.getName()).build());
      workExperienceDto.setId(newOrganization.getId());
    }
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    var result = this.workplaceService.addWorkExperience(email, workExperienceDto);
    return ResponseEntity.ok(result);
  }

  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  @PostMapping("/institutes")
  @Operation(summary = "Adds a school experience.")
  ResponseEntity<?> addEducationExperience(@RequestBody @Valid EducationExperienceDto educationExperienceDto)
    throws ApiException {
    if (educationExperienceDto.getId() == null) {
      var newOrganization =
        this.instituteService.createInstitute(
            CreateInstituteDto.builder().name(educationExperienceDto.getName()).build()
          );
      educationExperienceDto.setId(newOrganization.getId());
    }
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    var result = this.instituteService.addUserDegree(email, educationExperienceDto);
    return ResponseEntity.ok(result);
  }

  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  @PostMapping("/skills")
  @Operation(summary = "Adds a skill to the user.")
  ResponseEntity<?> addSkill(@RequestBody AddSkillDto addSkillDto) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    if (addSkillDto.getCanonicalName() == null) {
      var readSkillDto = this.skillService.createSkill(CreateSkillDto.builder().name(addSkillDto.getName()).build());
      addSkillDto.setCanonicalName(readSkillDto.getCanonicalName());
    }
    this.skillService.addSkillToUser(email, addSkillDto.getCanonicalName());
    return ResponseEntity.ok().build();
  }

  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  @DeleteMapping("/skills/{skillCanonicalName}")
  @Operation(summary = "Remove a skill from the user.")
  ResponseEntity<?> removeUserSkill(@PathVariable String skillCanonicalName) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var canonicalName = userDetails.getCanonicalName();
    this.skillService.removeUserSkill(skillCanonicalName, canonicalName);
    return ResponseEntity.ok().build();
  }

  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = { @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedTags.class)) }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  @GetMapping("/{canonicalName}/tags")
  @Operation(summary = "Get the tags that the user follows")
  ResponseEntity<PaginateDto<ReadTagDto>> getUserTags(
    @PathVariable @NotBlank String canonicalName,
    @RequestParam(defaultValue = "") String tagCanonicalName,
    @RequestParam(defaultValue = "0") @Min(0) Integer page,
    @RequestParam(defaultValue = "20") @Min(1) Integer size
  ) throws ApiException {
    var tags = this.tagService.getFollowedTags(canonicalName, tagCanonicalName, page, size);
    return ResponseEntity.ok(tags);
  }

  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  @PostMapping("/tags/{canonicalName}")
  @Operation(summary = "Adds a tag to the user.")
  ResponseEntity<?> followTag(@PathVariable @NotBlank String canonicalName) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    this.tagService.addTagToUser(email, canonicalName);
    return ResponseEntity.ok().build();
  }

  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  @DeleteMapping("/tags/{canonicalName}")
  @Operation(summary = "Remove a tag from the user.")
  ResponseEntity<?> unFollowTag(@PathVariable @NotBlank String canonicalName) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    this.tagService.removeTagFromUser(email, canonicalName);
    return ResponseEntity.ok().build();
  }

  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  @PostMapping("/contact-request")
  @Operation(summary = "sends a contact request.")
  ResponseEntity<?> sendContactRequest(@RequestBody CreateContactRequestDto contactRequestDto) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    this.userService.sendContactRequest(email, contactRequestDto);
    return ResponseEntity.ok().build();
  }

  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  @DeleteMapping("/contact-request/{canonicalName}")
  @Operation(summary = "deletes a  contact request.")
  ResponseEntity<?> deleteContactRequest(@PathVariable String canonicalName) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    this.userService.deleteContactRequest(email, canonicalName);
    return ResponseEntity.ok().build();
  }

  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  @PostMapping("/following/{canonicalName}")
  @Operation(summary = "Follows a user.")
  ResponseEntity<?> followUser(@PathVariable @NotBlank String canonicalName) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    this.userService.followUser(email, canonicalName);
    return ResponseEntity.ok().build();
  }

  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  @DeleteMapping("/following/{canonicalName}")
  @Operation(summary = "Unfollows a user.")
  ResponseEntity<?> unfollowUser(@PathVariable @NotBlank String canonicalName) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    this.userService.unfollowUser(email, canonicalName);
    return ResponseEntity.ok().build();
  }

  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  @PostMapping("/favorite-posts/{postId}")
  @Operation(summary = "Saves a favorite post to the user.")
  ResponseEntity<?> saveFavoritePost(@PathVariable @NotBlank String postId) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    this.userService.addFavoritePost(email, postId);
    return ResponseEntity.ok().build();
  }

  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  @DeleteMapping("/favorite-posts/{postId}")
  @Operation(summary = "Saves a favorite post to the user.")
  ResponseEntity<?> deleteFavoritePost(@PathVariable @NotBlank String postId) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    this.userService.removeFavoritePost(email, postId);
    return ResponseEntity.ok().build();
  }

  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = { @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedPosts.class)) }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  @GetMapping("/favorite-posts")
  @Operation(summary = "Get favorite posts from the user.")
  ResponseEntity<PaginateDto<ReadPostDto>> getFavoritePosts(
    @RequestParam(defaultValue = "0") @Min(0) Integer page,
    @RequestParam(defaultValue = "20") @Min(1) Integer size
  ) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var canonicalName = userDetails.getCanonicalName();
    var posts = this.postService.getFavoritePosts(canonicalName, page, size);
    return ResponseEntity.ok(posts);
  }

  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedContactRequests.class))
        }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  @GetMapping("/received-contact-requests")
  @Operation(summary = "Get received contact requests")
  ResponseEntity<PaginateDto<ReadContactRequestDto>> getReceivedContactRequests(
    @RequestParam(defaultValue = "0") @Min(0) Integer page,
    @RequestParam(defaultValue = "20") @Min(1) Integer size
  ) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    return ResponseEntity.ok(this.userService.getReceivedContactRequests(email, page, size));
  }

  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  @PutMapping("/received-contact-requests")
  @Operation(summary = "Response received contact requests")
  ResponseEntity<PaginateDto<ReadContactRequestDto>> acceptContactRequest(
    @RequestBody ResponseContactRequestDto response
  ) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    this.userService.acceptContactRequest(email, response);
    return ResponseEntity.ok().build();
  }

  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = { @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedUsers.class)) }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  @GetMapping("/contacts")
  @Operation(summary = "Get my contacts.")
  ResponseEntity<PaginateDto<ReadUserLiteDto>> getContacts(
    @RequestParam(defaultValue = "") String partialName,
    @RequestParam(defaultValue = "") String countryName,
    @RequestParam(defaultValue = "") String skillName,
    @RequestParam(defaultValue = "FULLNAME") SortContactsBy orderBy,
    @RequestParam(defaultValue = "0") @Min(0) Integer page,
    @RequestParam(defaultValue = "20") @Min(1) Integer size
  ) {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var canonicalName = userDetails.getCanonicalName();
    var contacts =
      this.userService.getMyContacts(canonicalName, page, size, partialName, countryName, skillName, orderBy);
    return ResponseEntity.ok(contacts);
  }

  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = { @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedUsers.class)) }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  @GetMapping("/follows")
  @Operation(summary = "Get my follows.")
  ResponseEntity<PaginateDto<ReadUserLiteDto>> getMyFollows(
    @RequestParam(defaultValue = "") String partialName,
    @RequestParam(defaultValue = "") String countryName,
    @RequestParam(defaultValue = "FULLNAME") SortContactsBy orderBy,
    @RequestParam(defaultValue = "") String skillName,
    @RequestParam(defaultValue = "0") @Min(0) Integer page,
    @RequestParam(defaultValue = "20") @Min(1) Integer size
  ) {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var canonicalName = userDetails.getCanonicalName();
    var contacts =
      this.userService.getMyFollows(canonicalName, page, size, partialName, countryName, skillName, orderBy);
    return ResponseEntity.ok(contacts);
  }

  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('ADMIN')")
  @PatchMapping("/{canonicalName}/blocked")
  @Operation(summary = "update if a user is blocked")
  ResponseEntity<?> setUserBlockedStatus(
    @PathVariable @NotBlank String canonicalName,
    @RequestBody UpdateUserBlockedStatusDto blockedStatusDto
  ) throws ApiException {
    this.userService.setUserBlockedStatus(canonicalName, blockedStatusDto);
    return ResponseEntity.ok().build();
  }

  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ReadUserToDeleteDto.class))
        }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  @PutMapping("elimination")
  @Operation(summary = "Set my own user to be eliminated")
  ResponseEntity<ReadUserToDeleteDto> deleteMyUser() throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    return ResponseEntity.ok(this.userService.setEliminationDate(email));
  }

  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  @DeleteMapping("/contacts/{canonicalName}")
  @Operation(summary = "Remove a contact.")
  ResponseEntity<?> getContacts(@PathVariable String canonicalName) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var requesterCanonicalName = userDetails.getCanonicalName();
    this.userService.removeContact(requesterCanonicalName, canonicalName);
    return ResponseEntity.ok().build();
  }

  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  @DeleteMapping("elimination")
  @Operation(summary = "Cancel my own user elimination")
  ResponseEntity<?> cancelMyUserElimination() throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    this.userService.cancelEliminationDate(email);
    return ResponseEntity.ok().build();
  }

  @PreAuthorize("hasAuthority('USER')")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedWorkPosition.class))
        }
      )
    }
  )
  @GetMapping("/{canonicalName}/institutes")
  @Operation(summary = "Search User Institutes")
  ResponseEntity<PaginateDto<ReadDegreeDto>> getUserDegrees(
    @PathVariable String canonicalName,
    @RequestParam(defaultValue = "0", required = false) @Min(0) Integer page,
    @RequestParam(defaultValue = "20", required = false) @Min(1) Integer size
  ) {
    var pagination = this.instituteService.getUserDegrees(canonicalName, page, size);
    return ResponseEntity.ok(pagination);
  }

  @PreAuthorize("hasAuthority('USER')")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedWorkPosition.class))
        }
      )
    }
  )
  @GetMapping("/{canonicalName}/workplaces")
  @Operation(summary = "Search Workplaces User")
  ResponseEntity<PaginateDto<ReadWorkPositionDto>> getUserJobs(
    @PathVariable String canonicalName,
    @RequestParam(defaultValue = "0", required = false) @Min(0) Integer page,
    @RequestParam(defaultValue = "20", required = false) @Min(1) Integer size
  ) {
    var pagination = this.workplaceService.getUserJobs(canonicalName, page, size);
    return ResponseEntity.ok(pagination);
  }

  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedSkillsDto.class))
        }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  @GetMapping("/{canonicalName}/skills")
  @Operation(summary = "Get the skils that the user has")
  ResponseEntity<PaginateDto<ReadSkillDto>> getUserSkills(
    @PathVariable @NotBlank String canonicalName,
    @RequestParam(defaultValue = "0") @Min(0) Integer page,
    @RequestParam(defaultValue = "20") @Min(1) Integer size
  ) throws ApiException {
    var skills = this.skillService.getUserSkills(canonicalName, page, size);
    return ResponseEntity.ok(skills);
  }

  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  @DeleteMapping("/workplaces/{id}")
  @Operation(summary = "Remove a working experience.")
  ResponseEntity<?> removeWorkingExperience(@PathVariable String id) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    this.workplaceService.removeWorkExperience(email, id);
    return ResponseEntity.ok().build();
  }

  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  @PutMapping("/workplaces/{id}")
  @Operation(summary = "Updates a user work experience")
  ResponseEntity<?> updateWorkingExperience(@PathVariable String id, UpdateWorkPositionDto updateWorkPositionDto)
    throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    this.workplaceService.updateWorkExperience(email, id, updateWorkPositionDto);
    return ResponseEntity.ok().build();
  }

  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  @DeleteMapping("/institutes/{id}")
  @Operation(summary = "Remove a user institute.")
  ResponseEntity<?> removeDegree(@PathVariable String id) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    this.instituteService.removeDegree(email, id);
    return ResponseEntity.ok().build();
  }

  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ReadUserDto.class)) }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  @PutMapping
  @Operation(summary = "Updates user profile")
  ResponseEntity<ReadUserDto> getProfile(@RequestBody @Valid UpdateUserDto updateUserDto) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var canonicalName = userDetails.getCanonicalName();
    var persistedPerson = this.userService.updateProfile(canonicalName, updateUserDto);
    return ResponseEntity.ok(persistedPerson);
  }

  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  @PutMapping("/institutes/{id}")
  @Operation(summary = "Updates a user degree")
  ResponseEntity<?> updateDegree(@PathVariable String id, UpdateDegreeDto updateDegreeDto) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    this.instituteService.updateDegree(email, id, updateDegreeDto);
    return ResponseEntity.ok().build();
  }

  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ReadLanguageDto.class)) }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  @GetMapping("/language")
  @Operation(summary = "Get my language.")
  ResponseEntity<ReadLanguageDto> getMyLanguage() {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var canonicalName = userDetails.getCanonicalName();
    return ResponseEntity.ok(this.userService.getUserLanguage(canonicalName));
  }

  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  @PutMapping("/language")
  @Operation(summary = "Set my language.")
  ResponseEntity<ReadLanguageDto> setMyLanguage(@RequestBody UpdateUserLanguageDto languageDto) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var canonicalName = userDetails.getCanonicalName();
    this.userService.setUserLanguage(canonicalName, languageDto);
    return ResponseEntity.ok().build();
  }

  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('ADMIN')")
  @DeleteMapping("{canonicalName}")
  @Operation(summary = "Delete and Ban a user.")
  ResponseEntity<?> deleteAndBanUser(@PathVariable String canonicalName) throws ApiException {
    this.userService.deleteAndBanUser(canonicalName);
    return ResponseEntity.ok().build();
  }

  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  @PostMapping("fcm-token")
  @Operation(summary = "Add a FCM token to the user account.")
  ResponseEntity<?> addFCMToken(@RequestBody AddFCMTokenDto addFCMTokenDto) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var canonicalName = userDetails.getCanonicalName();
    this.userService.addFCMToken(canonicalName, addFCMTokenDto);
    return ResponseEntity.ok().build();
  }
}
