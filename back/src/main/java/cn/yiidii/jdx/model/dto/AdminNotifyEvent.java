package cn.yiidii.jdx.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 管理员通知事件
 *
 * @author ed w
 * @since 1.0
 */
@Getter
@Setter
@Accessors(chain = true)
public class AdminNotifyEvent extends ApplicationEvent {

    private static final Object NULL_OBJECT = new Object();

    private String title;
    private List<String> mobileList;
    private String content;
    private Boolean adminNotify;

    public AdminNotifyEvent(List<String> mobileList, String title, String content, Boolean adminNotify) {
        super(NULL_OBJECT);
        this.mobileList = mobileList;
        this.title = title;
        this.content = content;
        this.adminNotify = adminNotify;
    }
}
