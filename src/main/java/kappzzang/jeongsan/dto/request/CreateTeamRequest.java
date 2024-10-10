package kappzzang.jeongsan.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateTeamRequest(
    @NotNull
    @Size(min = 1, max = 15)
    String name,
    @NotNull
    String subject,
    List<Long> members
) {

}
