package tech.onlycoders.backend.utils;

import java.text.Normalizer;

public class CanonicalFactory {

  public static String getCanonicalName(String name) {
    return Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("[^a-zA-Z0-9-_+]", "").toLowerCase();
  }
}
