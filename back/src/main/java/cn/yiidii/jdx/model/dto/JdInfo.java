package cn.yiidii.jdx.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;

/**
 * JdInfo
 *
 * @author ed w
 * @since 1.0
 */
@Data
@Builder
@Accessors(chain = true)
@RequiredArgsConstructor
@AllArgsConstructor
public class JdInfo {

    private String gsalt;
    private String guid;
    private String lsId;
    private String rsaModulus;

    private Long expireTime;

    private String preCookie;
    private String cookie;
    private String ptPin;

    private Set<String> qywxUserId = new HashSet<>();

}
