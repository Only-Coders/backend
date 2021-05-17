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
import tech.onlycoders.backend.dto.ApiErrorResponse;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.contactrequest.request.CreateContactRequestDto;
import tech.onlycoders.backend.dto.post.response.ReadPostDto;
import tech.onlycoders.backend.dto.user.request.EducationExperienceDto;
import tech.onlycoders.backend.dto.user.request.WorkExperienceDto;
import tech.onlycoders.backend.dto.user.response.ReadUserDto;
import tech.onlycoders.backend.dto.workplace.request.CreateEducationalOrganizationDto;
import tech.onlycoders.backend.dto.workplace.request.CreateWorkplaceDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.service.EducationalOrganizationService;
import tech.onlycoders.backend.service.UserService;
import tech.onlycoders.backend.service.WorkplaceService;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

  private final UserService userService;
  private final WorkplaceService workplaceService;
  private final EducationalOrganizationService educationalOrganizationService;

  public UserController(
    UserService userService,
    WorkplaceService workplaceService,
    EducationalOrganizationService educationalOrganizationService
  ) {
    this.userService = userService;
    this.workplaceService = workplaceService;
    this.educationalOrganizationService = educationalOrganizationService;
  }

  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ReadUserDto.class)) }
      ),
      @ApiResponse(
        responseCode = "400",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "401",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "403",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "404",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  @GetMapping("/{canonicalName}")
  @Operation(summary = "Este endpoint obtiene una personas")
  ResponseEntity<ReadUserDto> getProfile(@PathVariable String canonicalName) throws ApiException {
    var persistedPerson = this.userService.getProfile(canonicalName);
    return ResponseEntity.ok(persistedPerson);
  }

  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = WorkExperienceDto.class))
        }
      ),
      @ApiResponse(
        responseCode = "400",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "401",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "403",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "404",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  @PostMapping("/works")
  @Operation(summary = "Adds a working experience.")
  ResponseEntity<WorkExperienceDto> addWorkingExperience(@RequestBody @Valid WorkExperienceDto workExperienceDto)
    throws ApiException {
    if (workExperienceDto.getId() == null) {
      var newOrganization =
        this.workplaceService.createWorkplace(CreateWorkplaceDto.builder().name(workExperienceDto.getName()).build());
      workExperienceDto.setId(newOrganization.getId());
    }
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    var result = this.userService.addWork(email, workExperienceDto);
    return ResponseEntity.ok(result);
  }

  @ApiResponses(
    value = {
      @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }),
      @ApiResponse(
        responseCode = "400",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "401",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "403",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "404",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  @PostMapping("/schools")
  @Operation(summary = "Adds a school.")
  ResponseEntity<?> addEducationExperience(@RequestBody @Valid EducationExperienceDto educationExperienceDto)
    throws ApiException {
    if (educationExperienceDto.getId() == null) {
      var newOrganization =
        this.educationalOrganizationService.createEducationalOrganization(
            CreateEducationalOrganizationDto.builder().name(educationExperienceDto.getName()).build()
          );
      educationExperienceDto.setId(newOrganization.getId());
    }
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    var result = this.userService.addSchool(email, educationExperienceDto);
    return ResponseEntity.ok(result);
  }

  @ApiResponses(
    value = {
      @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }),
      @ApiResponse(
        responseCode = "400",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "401",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "403",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "404",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  @PostMapping("/skills/{canonicalName}")
  @Operation(summary = "Adds a skill to the user.")
  ResponseEntity<?> addSkill(@PathVariable @NotBlank String canonicalName) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    this.userService.addSkill(email, canonicalName);
    return ResponseEntity.ok().build();
  }

  @ApiResponses(
    value = {
      @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }),
      @ApiResponse(
        responseCode = "400",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "401",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "403",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "404",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  @PostMapping("/tags/{canonicalName}")
  @Operation(summary = "Adds a tag to the user.")
  ResponseEntity<?> addTag(@PathVariable @NotBlank String canonicalName) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    this.userService.addTag(email, canonicalName);
    return ResponseEntity.ok().build();
  }

  @ApiResponses(
    value = {
      @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }),
      @ApiResponse(
        responseCode = "400",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "401",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "403",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "404",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  @PostMapping("/contact-request")
  @Operation(summary = "sends a contact request.")
  ResponseEntity<?> sendContactRequest(@RequestBody CreateContactRequestDto contactRequestDto) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    this.userService.sendContactRequest(email, contactRequestDto);
    return ResponseEntity.ok().build();
  }

  @ApiResponses(
    value = {
      @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }),
      @ApiResponse(
        responseCode = "400",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "401",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "403",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "404",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  @PostMapping("/following/{canonicalName}")
  @Operation(summary = "Follows a user.")
  ResponseEntity<?> followUser(@PathVariable @NotBlank String canonicalName) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    this.userService.followUser(email, canonicalName);
    return ResponseEntity.ok().build();
  }

  @ApiResponses(
    value = {
      @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }),
      @ApiResponse(
        responseCode = "400",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "401",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "403",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "404",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  @PostMapping("/favorite-posts/{postId}")
  @Operation(summary = "Saves a favorite post to the user.")
  ResponseEntity<?> saveFavoritePost(@PathVariable @NotBlank String postId) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    this.userService.addFavoritePost(email, postId);
    return ResponseEntity.ok().build();
  }

  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = { @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedPosts.class)) }
      ),
      @ApiResponse(
        responseCode = "400",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "401",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "403",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "404",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
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
}
