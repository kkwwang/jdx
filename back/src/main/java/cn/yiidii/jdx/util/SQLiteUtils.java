package cn.yiidii.jdx.util;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.sql.*;

public class SQLiteUtils {

    public static  JSONArray getLatestBean(){
        String sql = "SELECT  * FROM  bean WHERE  时间= ( SELECT MAX( 时间 ) FROM bean ) group by mobile, 时间 order by 时间, 序号;";
        return select(sql);
    }


    public static JSONArray getBeanByMobile(String mobile) {
        if (!StringUtils.hasText(mobile)) {
            return null;
        }
        String sql = "SELECT * FROM bean where mobile = ? group by mobile, 时间 order by 时间, 序号;";
        return select(sql, mobile);
    }

    private static JSONArray select(String sql, Serializable... params) {
        try {
            String datasourceUrl = SpringUtil.getProperty("spring.datasource.url");
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
}
