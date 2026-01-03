package com.yyblcc.ecommerceplatforms.inteceptor;

import cn.hutool.core.util.DesensitizedUtil;
import com.yyblcc.ecommerceplatforms.annotation.Sensitive;
import com.yyblcc.ecommerceplatforms.domain.Enum.SensitiveType;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import java.lang.reflect.Field;
import java.sql.Statement;
import java.util.List;

@Slf4j
@Intercepts({@Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})})
public class DesensitizationInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object result = invocation.proceed();
        if (result instanceof List<?>){
            for (Object obj : (List<?>) result){
                desensitize(obj);
            }
        }else {
            desensitize(result);
        }
        return result;
    }

    private void desensitize(Object obj){
        if (obj == null){
            return;
        }


    Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields){
            if (field.isAnnotationPresent(Sensitive.class)){
                Sensitive sensitive = field.getAnnotation(Sensitive.class);
                field.setAccessible(true);
                try{
                    String value = (String) field.get(obj);
                    if (value != null){
                        field.set(obj, desensitizeValue(value, sensitive.type()));
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private String desensitizeValue(String value, SensitiveType type){
        switch (type){
            case REAL_NAME:
                return DesensitizedUtil.chineseName(value);
            case ID_CARD:
                return DesensitizedUtil.idCardNum(value, 6,0);
            case MOBILE_PHONE:
                return DesensitizedUtil.mobilePhone(value);
            case EMAIL:
                return DesensitizedUtil.email(value);
            case ADDRESS:
                return DesensitizedUtil.address(value, 8);
            default:
                return value;
        }
    }
}
