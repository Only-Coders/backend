package tech.onlycoders.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.skill.request.CreateSkillDto;
import tech.onlycoders.backend.dto.skill.response.ReadSkillDto;
import tech.onlycoders.backend.service.SkillService;

@RestController
@RequestMapping("/api/skills")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class SkillController {

  private final SkillService skillService;

  public SkillController(SkillService skillService) {
    this.skillService = skillService;
  }

  @PreAuthorize("hasAuthority('USER')")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ReadSkillDto.class)) }
      )
    }
  )
  @PostMapping
  @Operation(summary = "Create a Skill")
  ResponseEntity<ReadSkillDto> createSkill(@RequestBody @Valid CreateSkillDto createSkillDto) {
    var skill = this.skillService.createSkill(createSkillDto);
    return ResponseEntity.ok(skill);
  }

  @PreAuthorize("hasAuthority('USER')")
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
  @GetMapping
  @Operation(summary = "Search skills by name")
  ResponseEntity<PaginateDto<ReadSkillDto>> getSkills(
    @RequestParam(defaultValue = "", required = false) String skillName,
    @RequestParam(defaultValue = "0", required = false) @Min(0) Integer page,
    @RequestParam(defaultValue = "20", required = false) @Min(1) Integer size
  ) {
    var pagination = this.skillService.listSkills(skillName, page, size);
    return ResponseEntity.ok(pagination);
  }
}

class PaginatedSkillsDto extends PaginateDto<ReadSkillDto> {}
