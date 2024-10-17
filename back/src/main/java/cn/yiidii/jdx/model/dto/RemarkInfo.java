package cn.yiidii.jdx.model.dto;

import lombok.*;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;

@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class RemarkInfo {

    private String wechat = "新账号";
    private String ptPin;
    private String mobile;
    private String notifyMobile;
    private String nickname;
    private String qywxUserId="";

    public String getQywxUserId() {
        return qywxUserId;
    }

    public RemarkInfo setQywxUserId(String qywxUserId) {
        this.qywxUserId = qywxUserId;
        return this;
    }

    public RemarkInfo setMobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    public RemarkInfo setNotifyMobile(String notifyMobile) {
        this.notifyMobile = notifyMobile;
        return this;
    }

    @SneakyThrows
    public RemarkInfo setPtPin(String ptPin) {
        this.ptPin = !StringUtils.hasText(ptPin) ? ptPin : URLDecoder.decode(ptPin,"utf-8");
        return this;
    }

    @SneakyThrows
    public RemarkInfo setWechat(String wechat) {
        this.wechat = !StringUtils.hasText(wechat) ? wechat :URLDecoder.decode(wechat,"utf-8");
        return this;
    }

    @SneakyThrows
    public RemarkInfo setNickname(String nickname) {
        this.nickname = !StringUtils.hasText(nickname) ? nickname : URLDecoder.decode(nickname,"utf-8");
        return this;
    }
}
