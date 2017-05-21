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

package io.servicecomb.demo.server;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2016年12月6日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class TestResponse {
    private User user;

    /**
     * 获取user的值
     * @return 返回 user
     */
    public User getUser() {
        return user;
    }

    /**
     * 对user进行赋值
     * @param user user的新值
     */
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "TestResponse [user=" + user + "]";
    }

}
