package cn.yiidii.jdx.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class RemarkInfo {

    private String wechat;
    private String ptPin;
    private String mobile;
    private String nickname;

}
