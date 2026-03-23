package me.link.bootstrap.core.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PageReq {
    @Schema(description = "页码", example = "1")
    private Integer pageNo = 1;

    @Schema(description = "每页条数", example = "10")
    private Integer pageSize = 10;
}
