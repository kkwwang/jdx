package cn.yiidii.jdx.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Set;

@Getter
@Setter
@Accessors(chain = true)
public class AcountLinkDto {

    private String mainAcount;
    private Set<String> otherAcount;

}
