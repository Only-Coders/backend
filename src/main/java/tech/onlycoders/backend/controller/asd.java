package tech.onlycoders.backend.controller;

import org.springframework.web.bind.annotation.RestController;
import tech.onlycoders.backend.mapper.DumyMapper;

@RestController
public class asd {

  private final DumyMapper dumyMapper;

  public asd(DumyMapper dumyMapper) {
    this.dumyMapper = dumyMapper;
  }
}
