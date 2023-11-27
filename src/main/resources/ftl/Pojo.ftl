package pojo;

import java.io.Serializable;
import java.util.Date;
import java.sql.Timestamp;

import com.alibaba.fastjson.annotation.JSONField;

import annotation.Table;
import annotation.TableColumn;


@Table(tableName = "${table_name_small}"<#if table_remark !="">,expFileName="${table_remark}"</#if>)
public class ${pojo_name} implements Serializable{
    private static final long serialVersionUID = 1L;
   
    <#if COLUMNS?exists>
        <#list COLUMNS as model>
            <#if model.remarks != "">//${model.remarks}</#if> <#if model.javaType=="Date">@JSONField(format="yyyy-MM-dd")
        <#elseif model.javaType=="Timestamp">@JSONField(format="yyyy-MM-dd hh:mm:ss")</#if><#if model.isPK == "true">
            @TableId</#if>
            @TableField("${model.columnName}")
            private ${model.javaType} ${model.javaName};

        </#list>
    </#if>
    
    <#if COLUMNS?exists>
        <#list COLUMNS as model>
	public ${model.javaType} ${model.getterName}() {
        return ${model.javaName};
    }
    
    public void ${model.setterName}(${model.javaType} ${model.javaName}) {
        <#if model.javaType == "String">
        this.${model.javaName} = ${model.javaName} == null ? null : ${model.javaName}.trim();
        <#else>
        this.${model.javaName} = ${model.javaName};
        </#if>
    }
        </#list>
    </#if>
    
    public String toString(){
    	StringBuilder sb = new StringBuilder();
    	sb.append("pojo.${pojo_name} ").append("[");
    	<#if COLUMNS?exists>
	        <#list COLUMNS as model>
	    sb.append(", ${model.javaName} = ").append(${model.javaName});
	        </#list>
	    </#if>
	    sb.append("]");
	    return sb.toString();
    }
    
}