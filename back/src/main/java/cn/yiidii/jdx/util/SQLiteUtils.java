package cn.yiidii.jdx.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.yiidii.jdx.support.JWTFilter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.sql.*;
import java.util.*;
import java.util.Date;

@Slf4j
public class SQLiteUtils {

    private static final String BEAN_LOG_DB_NAME = "beanLog";
    private static final String LOGIN_LOG_DB_NAME = "loginLog";

    public static List getAllDate() {
        String sql = "SELECT DISTINCT SUBSTR( 时间, 0, 11 )  FROM bean";
        List result = new ArrayList();
        for (Object o : Objects.requireNonNull(select(BEAN_LOG_DB_NAME, sql))) {
            JSONObject item = (JSONObject) o;
            result.addAll(item.values());
        }

        return result;
    }

    public static JSONArray getLatestBean(String time) {
        if (!StringUtils.hasText(time)) {
            time = "";
        }
        time += "%";
        String sql = "SELECT  * FROM  bean WHERE  时间= ( SELECT MAX( 时间 ) FROM bean WHERE 时间 LIKE ?) group by mobile, 时间 order by 时间, 序号;";
        return select(BEAN_LOG_DB_NAME, sql, time);
    }


    public static JSONArray getBeanByMobile(String mobile, String startDate, String endDate) {
        if(null == startDate){
            // 7天前
            startDate = DateUtil.format(DateUtil.offsetDay(new Date(), -7), "yyyy-MM-dd");
        }
        if(null == endDate){
            endDate = DateUtil.format(new Date(), "yyyy-MM-dd");
        }

        if (StringUtils.pathEquals(mobile, "all")) {
            // 验证权限
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

            try {
                JWTFilter.checkToken(request);
                String sql = "SELECT * FROM bean where mobile is not null group by mobile, 时间 order by 时间, 序号;";
                return select(BEAN_LOG_DB_NAME, sql);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            if (!StringUtils.hasText(mobile)) {
                return null;
            }
            String[] split = mobile.split(",");

            StringBuilder param = new StringBuilder();
            for (int i = 0; i < split.length; i++) {
                param.append("?");
                if (i != split.length - 1) {
                    param.append(",");
                }
            }

            ArrayList<String> list = new ArrayList<>(Arrays.asList(split));
            list.add(startDate);
            list.add(endDate);


            String sql = "SELECT * FROM bean where mobile in (" + param + ") AND 时间 BETWEEN ? AND ? group by mobile, 时间 order by 时间, 序号;";
            return select(BEAN_LOG_DB_NAME, sql, list.toArray(new String[0]));
        }
    }

    private static JSONArray select(String dbName, String sql, Serializable... params) {
        try {
            String datasourceUrl = SpringUtil.getProperty("spring.datasource." + dbName);
            // 连接到SQLite数据库
            Connection connection = DriverManager.getConnection(datasourceUrl);

            // 查询数据
            PreparedStatement ps = connection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }


            ResultSet resultSet = ps.executeQuery();
            // 打印查询结果
            JSONArray result = new JSONArray();
            while (resultSet.next()) {
                JSONObject item = new JSONObject();
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String columnValue = resultSet.getString(columnName);
                    item.put(columnName, columnValue);
                }
                result.add(item);
            }


            // 关闭连接
            resultSet.close();
            ps.close();
            connection.close();

            return result;
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    public static void createLoginLogTable() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS \"login_log\" (  \"mobile\" TEXT,  \"login_day\" TEXT,  \"login_time\" TEXT, \"ip_addr\" TEXT,  \"type\" integer);";
            String datasourceUrl = SpringUtil.getProperty("spring.datasource." + LOGIN_LOG_DB_NAME);
            // 连接到SQLite数据库
            Connection connection = DriverManager.getConnection(datasourceUrl);
            // 查询数据
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.executeUpdate();

            ps.close();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void insertLoginLog(String mobile, String account, Date loginTime) {
        try {
            createLoginLogTable();
            String sql = "INSERT INTO login_log (\"mobile\", \"login_day\", \"login_time\", \"ip_addr\", \"type\") VALUES (?, ?, ?, ?, ?);";
            String datasourceUrl = SpringUtil.getProperty("spring.datasource." + LOGIN_LOG_DB_NAME);
            // 连接到SQLite数据库
            Connection connection = DriverManager.getConnection(datasourceUrl);
            // 查询数据
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, mobile);
            ps.setString(2, DateUtil.format(loginTime, "yyyy-MM-dd"));
            ps.setString(3, DateUtil.format(loginTime, "HH:mm"));
            ps.setString(4, HttpContextUtils.getRequestIpAddr());
            ps.setInt(5, 1);

            log.info(StrUtil.format("登录成功，手机号：{}，账号：{}", mobile, account));

            ps.executeUpdate();

            ps.close();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static JSONArray getLoginLog(String mobile) {
        createLoginLogTable();
        String sql = "SELECT * FROM login_log WHERE mobile = ? ORDER BY login_day DESC, login_time DESC;";
        return select(LOGIN_LOG_DB_NAME, sql, mobile);
    }
}
