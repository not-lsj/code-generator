package com.not_lsj.utils;

import freemarker.template.*;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 代码生成工具
 *
 * @author Administrator
 */
public class Generator {

    public Generator() {
    }

    private static final Map<String, String> jdbcToJavaType = new HashMap<>();

    static {//jdbc类型到java类型的转换 参考 myBatis 整理的，有问题再去调整把。
        //初始化jdbc类型转换。
        jdbcToJavaType.put("VARCHAR", "String");
        jdbcToJavaType.put("CHAR", "String");
        jdbcToJavaType.put("VARCHAR2", "String");
        jdbcToJavaType.put("NVARCHAR", "String");
        jdbcToJavaType.put("LONGNVARCHAR", "String");
        jdbcToJavaType.put("TEXT", "String");
        jdbcToJavaType.put("CLOB", "String");
        jdbcToJavaType.put("TINYLOB", "String");
        //数据库查出来的都是装箱类型，如果java类型用int，会存在值类型转换失败的问题
        jdbcToJavaType.put("INT", "Integer");
        jdbcToJavaType.put("INTEGER", "Integer");
        jdbcToJavaType.put("SMALLINT", "Integer");
        jdbcToJavaType.put("TINYINT", "Integer");
        jdbcToJavaType.put("BIGINT", "Long");

        jdbcToJavaType.put("NUMBERIC", "Long");
        jdbcToJavaType.put("NUMBER", "Long");
        jdbcToJavaType.put("DOUBLE", "Double");
        jdbcToJavaType.put("LONGBLOB", "String");
        jdbcToJavaType.put("FLOAT", "Float");

        jdbcToJavaType.put("DATE", "Date");
        jdbcToJavaType.put("DATETIME", "Date");//java.util.Date
        jdbcToJavaType.put("TIMESTAMP", "Timestamp");//java.sql.Timestamp

    }

    private List<String> primaryKeys;

    public void generate() throws Exception {
        Version version = new Version("2.3.27");
        // 这个版本号不知道是干吗的。https://freemarker.apache.org/docs/api/freemarker/template/Configuration.html#Configuration-freemarker.template.Version-
        URL url = Generator.class.getResource("/" + CGConstants.propLocation);

        Configuration conf = new Configuration(version);
        conf.setDefaultEncoding("UTF-8");
        conf.setObjectWrapper(new DefaultObjectWrapper(version));
        conf.setDirectoryForTemplateLoading(new File(url.getFile()));

        String tableName = PropLoader.getPlanProp("db.tableName");
        // freemarker测试
        // generateTest(conf,tableName);

        // 生成POJO类
        generatePOJO(conf, tableName);
        // 生成service接口类
        generateService(conf, tableName);
        // 生成serviceimpl实现类
        generateServiceImpl(conf, tableName);
        // 生成Controller
        generateController(conf, tableName);
        // 生成页面
        generatePage(conf, tableName);
        // 生成config.js的配置建议
        generatePageConf(conf, tableName);
        //TODO 辅助功能，帮忙实现一下Mybatis的mapper和xml
        //默认实现CRUD以后还是加到BaseServiceImpl里。这里只作为工具方法，生成一下。
        //generateMyBatisMapper(conf,tableName);
        //generateMyBatisXml(conf,tableName);
    }

    private void generatePageConf(Configuration conf, String tableName) throws Exception {
        // TODO Auto-generated method stub
        Template template = conf.getTemplate("configjs.ftl");
        Map<String, Object> paramMap = this.getCommonParam(tableName);
        Writer writer = new OutputStreamWriter(System.out, StandardCharsets.UTF_8);
        template.process(paramMap, writer);
        writer.flush();
        //writer.close();
    }

    private void generatePage(Configuration conf, String tableName) throws Exception {
        //生成Html页面，html页面是变化最多的部分。目前的实现逻辑麻烦的就在查询条件那一部分， 初期的最初实现就把POJO中配置了TableColumn属性的列全部生成为文本类型的查询条件， 以后再优化。
//		System.out.println("==== 开始生成 " + tableName + " 对应的管理页面 ====");
        Template template = conf.getTemplate("Html.ftl");
        // 拼凑参数
        Map<String, Object> paramMap = this.getCommonParam(tableName);
        String pojoName = paramMap.get("pojo_name").toString();
        String moduleName = paramMap.get("module_name").toString();
        //添加html相关参数。
        this.addHtmlInfo(paramMap, tableName);

        // 生成文件
        String folderLocation = CGConstants.HtmlLocation + "/" + moduleName;
        File filefolder = new File(folderLocation);
        if (!filefolder.exists()) {
            filefolder.mkdirs();
        }
        String filelocation = folderLocation + "/" + pojoName + ".html";
        Writer writer = new OutputStreamWriter(Files.newOutputStream(Paths.get(filelocation)), StandardCharsets.UTF_8);
        template.process(paramMap, writer);
        writer.flush();
        writer.close();
        System.out.println("==== " + tableName + " 对应的管理页面生成成功 生成地址：" + filelocation + " ====");
    }

    private void generateController(Configuration conf, String tableName) throws Exception {
//		System.out.println("==== 开始生成 " + tableName + " 表对应的Controller实现类 ====");
        Template template = conf.getTemplate("Controller.ftl");
        // 拼凑参数
        Map<String, Object> paramMap = this.getCommonParam(tableName);
        String pojoName = paramMap.get("pojo_name").toString();
        String moduleName = paramMap.get("module_name").toString();
        // 生成文件
        String folderLocation = CGConstants.ControllerLocation + "/" + moduleName;
        File filefolder = new File(folderLocation);
        if (!filefolder.exists()) {
            filefolder.mkdirs();
        }
        String serviceLocation = folderLocation + "/" + pojoName + "Controller.java";
        Writer writer = new OutputStreamWriter(Files.newOutputStream(Paths.get(serviceLocation)), StandardCharsets.UTF_8);
        template.process(paramMap, writer);
        writer.flush();
        writer.close();
        System.out.println("==== " + tableName + " 表对应的Service接口类生成成功 生成地址：" + serviceLocation + " ====");
    }

    private void generateServiceImpl(Configuration conf, String tableName) throws Exception {
        System.out.println("==== 开始生成 " + tableName + " 表对应的serviceImpl实现类 ====");
        Template template = conf.getTemplate("ServiceImpl.ftl");
        // 拼凑参数
        Map<String, Object> paramMap = this.getCommonParam(tableName);
        String pojoName = paramMap.get("pojo_name").toString();
        String moduleName = paramMap.get("module_name").toString();
        // 生成文件
        String folderLocation = CGConstants.ServiceImplLocation + "/" + moduleName;
        File filefolder = new File(folderLocation);
        if (!filefolder.exists()) {
            filefolder.mkdirs();
        }
        String serviceLocation = folderLocation + "/" + pojoName + "ServiceImpl.java";
        Writer writer = new OutputStreamWriter(Files.newOutputStream(Paths.get(serviceLocation)), StandardCharsets.UTF_8);
        template.process(paramMap, writer);
        writer.flush();
        writer.close();
        System.out.println("==== " + tableName + " 表对应的Service接口类生成成功 生成地址：" + serviceLocation + " ====");
    }

    private void generateService(Configuration conf, String tableName) throws Exception {
        System.out.println("==== 开始生成 " + tableName + " 表对应的service接口 ====");
        Template template = conf.getTemplate("Service.ftl");
        // 拼凑参数
        Map<String, Object> paramMap = this.getCommonParam(tableName);
        String pojoName = paramMap.get("pojo_name").toString();
        String moduleName = paramMap.get("module_name").toString();
        // 生成文件
        String folderLocation = CGConstants.ServiceLocation + "/" + moduleName;
        File filefolder = new File(folderLocation);
        if (!filefolder.exists()) {
            filefolder.mkdirs();
        }
        String serviceLocation = folderLocation + "/" + pojoName + "Service.java";
        Writer writer = new OutputStreamWriter(Files.newOutputStream(Paths.get(serviceLocation)), StandardCharsets.UTF_8);
        template.process(paramMap, writer);
        writer.flush();
        writer.close();
        System.out.println("==== " + tableName + " 表对应的Service接口类生成成功 生成地址：" + serviceLocation + " ====");
    }

    private void generatePOJO(Configuration conf, String tableName)
            throws Exception {
        System.out.println("==== 开始生成 " + tableName + " 表对应的POJO ====");
        Template template = conf.getTemplate("Pojo.ftl");
        // 拼凑参数
        Map<String, Object> paramMap = this.getCommonParam(tableName);
        if (StringUtils.isNotBlank(PropLoader.getPlanProp("db.orderby"))) {
            paramMap.put("table_order_by", PropLoader.getPlanProp("db.orderby"));
        }
        if (StringUtils.isNotBlank(PropLoader.getPlanProp("db.order"))) {
            paramMap.put("table_order", PropLoader.getPlanProp("db.order"));
        }
        String pojoName = paramMap.get("pojo_name").toString();

        this.addTableInfo(paramMap, tableName);

        // 生成文件
        String pojoLocation = CGConstants.POJOLocation + "/" + pojoName + ".java";
        File pojoFile = new File(pojoLocation);
        Writer writer = new OutputStreamWriter(Files.newOutputStream(pojoFile.toPath()), StandardCharsets.UTF_8);
        template.process(paramMap, writer);
        writer.flush();
        writer.close();
        System.out.println("==== " + tableName + " 表对应的entity类生成成功 生成地址：" + pojoLocation + " ====");
    }

    // 一些公用的参数就一起封装一下。
    private Map<String, Object> getCommonParam(String tableName) {
        Map<String, Object> paramMap = new HashMap<>();
        String pojoName = CGUtil.getPOJONameByTableName(tableName);
        paramMap.put("pojo_name", pojoName);
        String moduleName = PropLoader.getPlanProp("plan.moduleName");
        paramMap.put("module_name", moduleName);
        String genTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        paramMap.put("gen_time", genTime);
        return paramMap;
    }

    //获取表结构信息 主要是表名、表注释、字段名，字段类型， 字段注释。
    private void addTableInfo(Map<String, Object> paramMap, String tableName) throws Exception {
        // TODO Auto-generated method stub
        Connection con = DBUtil.getDBConn();
        DatabaseMetaData meta = con.getMetaData();
        //获取表信息
        String schema = PropLoader.getProp().contains("jdbc.schema") ? PropLoader.getProp("jdbc.schema") : null;
        ResultSet tableSet = meta.getTables(schema, "%", tableName, new String[]{"TABLE"});
        if (tableSet.next()) {//只取查到的第一条数据
            paramMap.put("table_name", tableName);
            paramMap.put("table_name_small", tableName.toLowerCase());
            paramMap.put("table_remark", tableSet.getString("REMARKS"));
            System.out.println("获取到表信息 TABLE_NAME => " + tableSet.getString("TABLE_NAME") + ";TABLE_REMARK => " + tableSet.getString("REMARKS"));
        } else {
            System.err.println("数据库中没有查到表 " + tableName);
            throw new Exception("数据库中没有查到表 " + tableName);
        }
        //加载主键。
        if (primaryKeys == null || primaryKeys.isEmpty()) {
            primaryKeys = new ArrayList<>();//主键列名
            ResultSet primaryKeySet = meta.getPrimaryKeys(schema, "%", tableName);
            while (primaryKeySet.next()) {
                primaryKeys.add(primaryKeySet.getString("COLUMN_NAME"));
            }
        }
        //获取字段信息
        ResultSet columnSet = meta.getColumns(schema, "%", tableName, "%");
        List<Map<String, Object>> columns = new ArrayList<>();
        while (columnSet.next()) {
            Map<String, Object> columnInfo = new HashMap<>();
            String columnName = columnSet.getString("COLUMN_NAME");
            String columnType = columnSet.getString("TYPE_NAME");
            int datasize = columnSet.getInt("COLUMN_SIZE");
            int digits = columnSet.getInt("DECIMAL_DIGITS");
            int nullable = columnSet.getInt("NULLABLE");
            String remarks = columnSet.getString("REMARKS");
            System.out.println("获取到字段信息 ： columnName =>" + columnName + ";columnType => " + columnType + ";datasize=>" + datasize + "=>" + digits + ";nullable => " + nullable + ";remarks => " + remarks);
            //只对JDBC几种常见的数据类型做下匹配，其他不常用的就暂时不生成了。 健壮的类型映射还是需要看下别的框架是怎么做的。
            if (StringUtils.isNotBlank(columnType)
                    && jdbcToJavaType.containsKey(columnType.toUpperCase())) {
                columnInfo.put("columnName", columnName);
                columnInfo.put("columnType", columnType);
                columnInfo.put("javaType", jdbcToJavaType.get(columnType.toUpperCase()));
                String javaName = CGUtil.getCamelName(columnName);
                columnInfo.put("javaName", javaName);
                columnInfo.put("getterName", "get" + javaName.substring(0, 1).toUpperCase() + javaName.substring(1));
                columnInfo.put("setterName", "set" + javaName.substring(0, 1).toUpperCase() + javaName.substring(1));
                columnInfo.put("remarks", StringUtils.isNotBlank(remarks) ? remarks : "");
                columnInfo.put("isPK", primaryKeys.contains(columnName) ? "true" : "");
                columns.add(columnInfo);
            } else {
                System.out.println("字段信息 ： columnName =>" + columnName + " 类型 columnType => " + columnType + " 暂无法处理，待以后进行扩展 ;");
                throw new Exception("字段信息 ： columnName =>" + columnName + " 类型 columnType => " + columnType + " 暂无法处理，待以后进行扩展 ;");
            }

        }
        paramMap.put("COLUMNS", columns);
        //获取表主键信息
        //ResultSet primaryKeys = meta.getPrimaryKeys(null, schema, tableName);

    }

    private void addHtmlInfo(Map<String, Object> paramMap, String tableName) throws Exception {
        List<List<Map<String, Object>>> allModules = new ArrayList<>();//所有控件
        Connection con = DBUtil.getDBConn();
        DatabaseMetaData meta = con.getMetaData();
        String schema = PropLoader.getProp().contains("jdbc.schema") ? PropLoader.getProp("jdbc.schema") : null;

        ResultSet columnSet = meta.getColumns(schema, "%", tableName, "%");
        int rowIndex = 0;
        List<Map<String, Object>> rowModules = null;//每一行的查询控件。 每行默认四个。

        while (columnSet.next()) {
            String remarks = columnSet.getString("REMARKS");
            String columnName = columnSet.getString("COLUMN_NAME");
            String columnTypeName = columnSet.getString("TYPE_NAME");
            String javaName = CGUtil.getCamelName(columnName);
            String moduleIsPk = primaryKeys.contains(columnName) ? "true" : "false";

            Map<String, Object> module = new HashMap<>();
            //页面每一行默认放四个查询控件。
            if (rowIndex % 4 == 0) {
                System.out.println("管理页面生成：查询条件换行");
                if (null != rowModules) {
                    allModules.add(rowModules);
                }
                rowModules = new ArrayList<>();
            }
            //查询控件的标签用字段的注释，如果没有注释就直接放字段名了。
            String condRemark = StringUtils.isBlank(remarks) ? columnName : remarks;
            module.put("remarks", condRemark);
            module.put("java_name", javaName);
            module.put("type_name", columnTypeName);
            module.put("isPK", moduleIsPk);
            rowModules.add(module);
            System.out.println("管理页面生成：加载查询条件： columnName => " + columnName + ";标签 =>" + condRemark + ";对应属性 => " + javaName);
            rowIndex++;
        }
        if (null != rowModules && !rowModules.isEmpty()) {
            allModules.add(rowModules);
        }
        paramMap.put("all_modules", allModules);
    }

    public static void main(String[] args) throws Exception {
        //mysql
        Class.forName("com.mysql.cj.jdbc.Driver");
        Properties props = new Properties();
        props.put("useInformationSchema", "true"); //mysql获取表注释需要加上这个属性 
        props.put("user", "root");
        props.put("password", "root");
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/genserver?serverTimezone=GMT%2B8&characterEncoding=utf-8&autoReconnect=true", props);
        //oracle
//		Class.forName("oracle.jdbc.driver.OracleDriver");
//		Connection con = DriverManager.getConnection("jdbc:oracle:thin:@172.16.49.65:1521/motion","swordrisk","risk#230");

//		 Properties props = new Properties();  
//         props.put("remarksReporting", "true");  //要获取注释，需要增加这个属性。
//         props.put("user", "swordrisk");  
//         props.put("password", "risk#230");  
//         Connection con = DriverManager.getConnection("jdbc:oracle:thin:@172.16.49.65:1521/motion",props);
        System.out.println("========映射表信息==============");
        DatabaseMetaData meta = con.getMetaData();
        ResultSet tables = meta.getTables("genserver", "%", "black_info", new String[]{"TABLE"});
        while (tables.next()) {
            ResultSetMetaData metaData = tables.getMetaData();
            System.out.println(metaData.getColumnCount());
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                System.out.println(metaData.getColumnName(i) + " ==> " + tables.getString(metaData.getColumnName(i)));
            }

            System.out.println(tables.getString("TABLE_NAME") + " --->>> " + tables.getString("REMARKS"));
        }
        System.out.println("========映射列信息==============");
        ResultSet columns = meta.getColumns("genserver", "%", "black_info", "%");
        System.out.println("columnName|columnType|datasize|digits|nullable|remarks");
        while (columns.next()) {
            String columnName = columns.getString("COLUMN_NAME");
            String columnType = columns.getString("TYPE_NAME");
            int datasize = columns.getInt("COLUMN_SIZE");
            int digits = columns.getInt("DECIMAL_DIGITS");
            int nullable = columns.getInt("NULLABLE");
            String remarks = columns.getString("REMARKS");
            System.out.println(columnName + "|" + columnType + "|" + datasize + "|" + digits + "|" + nullable + "|" + remarks);
        }
        System.out.println("========映射主键信息==============");
        ResultSet primaryKeys = meta.getPrimaryKeys("genserver", "%", "black_info");
        while (primaryKeys.next()) {
            ResultSetMetaData metaData = primaryKeys.getMetaData();
            System.out.println("主键个数：" + metaData.getColumnCount());
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                System.out.println(metaData.getColumnName(i) + " ==> " + primaryKeys.getString(metaData.getColumnName(i)));
            }
        }
    }
}
