package com.alimmit.golf.scorecard;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Paginated list of scorecards")
public record ScorecardPageDto(
    @ArraySchema(schema = @Schema(implementation = ScorecardDto.class)) List<ScorecardDto> data,
    @Schema(
            description =
                "Opaque cursor to pass as `cursor` on the next request; null on last page",
            nullable = true)
        String nextCursor,
    @Schema(description = "Whether more results are available after this page") boolean hasNext) {}
