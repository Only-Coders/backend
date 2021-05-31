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
import tech.onlycoders.backend.dto.contactrequest.request.CreateContactRequestDto;
import tech.onlycoders.backend.dto.contactrequest.request.ResponseContactRequestDto;
import tech.onlycoders.backend.dto.contactrequest.response.ReadContactRequestDto;
import tech.onlycoders.backend.dto.institute.request.CreateInstituteDto;
import tech.onlycoders.backend.dto.post.response.ReadPostDto;
import tech.onlycoders.backend.dto.skill.request.CreateSkillDto;
import tech.onlycoders.backend.dto.tag.response.ReadTagDto;
import tech.onlycoders.backend.dto.user.request.AddSkillDto;
import tech.onlycoders.backend.dto.user.request.EducationExperienceDto;
import tech.onlycoders.backend.dto.user.request.UpdateUserBlockedStatusDto;
import tech.onlycoders.backend.dto.user.request.WorkExperienceDto;
import tech.onlycoders.backend.dto.user.response.ReadUserDto;
import tech.onlycoders.backend.dto.user.response.ReadUserLiteDto;
import tech.onlycoders.backend.dto.user.response.ReadUserToDeleteDto;
import tech.onlycoders.backend.dto.workplace.request.CreateWorkplaceDto;
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

  public UserController(
    UserService userService,
    WorkplaceService workplaceService,
    TagService tagService,
    InstituteService instituteService,
    SkillService skillService
  ) {
    this.userService = userService;
    this.workplaceService = workplaceService;
    this.tagService = tagService;
    this.instituteService = instituteService;
    this.skillService = skillService;
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
  ResponseEntity<PaginateDto<ReadUserLiteDto>> findUsersByName(
    @RequestParam String partialName,
    @RequestParam(defaultValue = "0") @Min(0) Integer page,
    @RequestParam(defaultValue = "20") @Min(1) Integer size
  ) throws ApiException {
    var persistedPerson = this.userService.findByPartialName(partialName, page, size);
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
    @RequestParam(defaultValue = "0") @Min(0) Integer page,
    @RequestParam(defaultValue = "20") @Min(1) Integer size
  ) throws ApiException {
    var tags = this.tagService.getFollowedTags(canonicalName, page, size);
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
    var email = userDetails.getEmail();
    var posts = this.userService.getFavoritePosts(email, page, size);
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
  ResponseEntity<PaginateDto<ReadContactRequestDto>> ResponseContactRequest(
    @RequestBody ResponseContactRequestDto response
  ) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    this.userService.responseContactRequest(email, response);
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
    @RequestParam(defaultValue = "0") @Min(0) Integer page,
    @RequestParam(defaultValue = "20") @Min(1) Integer size
  ) {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var canonicalName = userDetails.getCanonicalName();
    var contacts = this.userService.getMyContacts(canonicalName, page, size);
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
}

class PaginatedUsers extends PaginateDto<ReadUserLiteDto> {}

class PaginatedContactRequests extends PaginateDto<ReadContactRequestDto> {}
