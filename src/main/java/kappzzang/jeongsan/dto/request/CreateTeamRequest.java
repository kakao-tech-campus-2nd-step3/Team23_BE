package kappzzang.jeongsan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateTeamRequest(
    @NotBlank
    @Size(min = 1, max = 15)
    String name,
    @NotNull
    String subject,
    @NotNull
    List<Long> members
) {

}
