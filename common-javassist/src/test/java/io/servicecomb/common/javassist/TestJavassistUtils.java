/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.common.javassist;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.huawei.paas.foundation.common.utils.ReflectUtils;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2017年3月10日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class TestJavassistUtils {
    @Test
    public void testInterface() throws Exception {
        ClassConfig classConfig = new ClassConfig();
        classConfig.setIntf(true);
        String intfName = "cse.ut.TestInterface";
        classConfig.setClassName(intfName);

        String source = "java.util.List method(java.util.Map map, java.util.Set set);";
        String genericSignature =
            "(Ljava/util/Map<Ljava/lang/String;Ljava/lang/String;>;Ljava/util/Set<Ljava/lang/String;>;)Ljava/util/List<Ljava/lang/String;>;";
        classConfig.addMethod(source, genericSignature);

        Class<?> intf = JavassistUtils.createClass(classConfig);

        Assert.assertEquals(intfName, intf.getName());
        Method method = ReflectUtils.findMethod(intf, "method");
        Assert.assertEquals("method", method.getName());
        Assert.assertEquals("java.util.List<java.lang.String>", method.getGenericReturnType().getTypeName());

        Type[] types = method.getGenericParameterTypes();
        Assert.assertEquals("java.util.Map<java.lang.String, java.lang.String>", types[0].getTypeName());
        Assert.assertEquals("java.util.Set<java.lang.String>", types[1].getTypeName());
    }

    @Test
    public void singleWrapperInt() throws Exception {
        ClassConfig classConfig = new ClassConfig();
        classConfig.setClassName("cse.ut.single.IntWrapper");
        classConfig.addField("intField", TypeFactory.defaultInstance().constructType(int.class));

        JavassistUtils.genSingleWrapperInterface(classConfig);

        Class<?> wrapperClass = JavassistUtils.createClass(classConfig);

        SingleWrapper instance = (SingleWrapper) wrapperClass.newInstance();
        instance.writeField(100);
        int intFieldValue = (int) instance.readField();
        Assert.assertEquals(100, intFieldValue);
    }

    @Test
    public void multiWrapper() throws Exception {
        ClassConfig classConfig = new ClassConfig();
        classConfig.setClassName("cse.ut.multi.Wrapper");
        classConfig.addField("intField", (Type) int.class);
        classConfig.addField("strField", String.class);

        JavassistUtils.genMultiWrapperInterface(classConfig);

        Class<?> wrapperClass = JavassistUtils.createClass(classConfig);

        MultiWrapper instance = (MultiWrapper) wrapperClass.newInstance();
        instance.writeFields(new Object[] {100, "test"});
        Object[] fieldValues = (Object[]) instance.readFields();
        Assert.assertEquals(100, fieldValues[0]);
        Assert.assertEquals("test", fieldValues[1]);
    }

    @Test
    public void testEnum() throws Exception {
        @SuppressWarnings("rawtypes")
        Class<? extends Enum> cls = JavassistUtils.createEnum("cse.ut.EnumAbc", "a", "b");
        Method method = cls.getMethod("values");
        Enum<?>[] values = (Enum<?>[]) method.invoke(null);

        Assert.assertEquals("cse.ut.EnumAbc", cls.getName());
        Assert.assertEquals(2, values.length);
        Assert.assertEquals("a", values[0].name());
        Assert.assertEquals(0, values[0].ordinal());
        Assert.assertEquals("b", values[1].name());
        Assert.assertEquals(1, values[1].ordinal());
    }

    @Test
    public void testGetNameForGenerateCode() {
        JavaType jt = TypeFactory.defaultInstance().constructType(byte[].class);
        String name = JavassistUtils.getNameForGenerateCode(jt);
        Assert.assertEquals("byte[]", name);
        
        jt = TypeFactory.defaultInstance().constructType(Byte[].class);
        name = JavassistUtils.getNameForGenerateCode(jt);
        Assert.assertEquals("java.lang.Byte[]", name);
        
        jt = TypeFactory.defaultInstance().constructType(Object[].class);
        name = JavassistUtils.getNameForGenerateCode(jt);
        Assert.assertEquals("java.lang.Object[]", name);
    }
}
