package kappzzang.jeongsan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record Image(
    @NotBlank @Pattern(regexp = "^(jpg|jpeg|png|gif)$") String format,
    String url,
    @NotBlank @Size(max = 1048576) String data,
    @NotBlank @Size(max = 20) @Pattern(regexp = "^[a-zA-Z0-9_\\-. ]+$") String name) {

}
