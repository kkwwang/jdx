package cn.yiidii.jdx.util;

import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Objects;

/**
 * request 帮助类
 *
 * @author wangkai
 * @version V 1.0
 * @since JDK1.8
 */
public class HttpContextUtils {


    public static final ThreadLocal<Map<String, String>> THREAD_LOCAL = new ThreadLocal<>();
    private static final int IP_MAX_LENGTH = 15;
    private static final String IP_SEPARATOR = ",";

    public static HttpServletRequest getHttpServletRequest() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
    }

    /**
     * 获取HttpServletResponse
     *
     * @return HttpServletResponse
     * @author yangyi
     */
    public static HttpServletResponse getHttpServletResponse() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
    }

    /**
     * @return
     */
    public static String getDomain() {
        HttpServletRequest request = getHttpServletRequest();
        StringBuffer url = request.getRequestURL();
        return url.delete(url.length() - request.getRequestURI().length(), url.length()).toString();
    }

    public static String getOrigin() {
        HttpServletRequest request = getHttpServletRequest();
        return request.getHeader("Origin");
    }

    public static String getRequestIpAddr() {
        return getIp(getHttpServletRequest());
    }

    public static String getRequestIpAddr(HttpServletRequest request) {
        return getIp(request);
    }

    private static String getIp(HttpServletRequest request){
        String ip = request.getHeader("X-Forwarded-For");
        String unknown = "unknown";
        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-real-ip");
        }
        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ip != null && ip.length() > IP_MAX_LENGTH) {
            if (ip.indexOf(IP_SEPARATOR) > 0) {
                ip = ip.substring(0, ip.indexOf(IP_SEPARATOR));
            }
        }
        return ip;
    }

    /**
     * 获取请求的columnValue
     */
    public static String getRequestColumn(String column) {

        HttpServletRequest httpRequest = HttpContextUtils.getHttpServletRequest();
        String columnValue = httpRequest.getHeader(column);
        if (StringUtils.isEmpty(columnValue)) {
            columnValue = httpRequest.getParameter(column);
        }
        return columnValue;
    }

    public static Map<String, String> getHeaderMap() {
        return THREAD_LOCAL.get();
    }

    public static void setHeaderMap(Map<String, String> headerMap) {
        THREAD_LOCAL.set(headerMap);
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }

}
