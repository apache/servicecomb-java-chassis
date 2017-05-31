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

import java.util.ArrayList;
import java.util.List;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @version  [版本号, 2016年12月6日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class TestRequest {
    private int index;

    private User user;

    private List<User> users = new ArrayList<>();

    private byte[] data;

    /**
     * 获取index的值
     * @return 返回 index
     */
    public int getIndex() {
        return index;
    }

    /**
    * 对index进行赋值
    * @param index index的新值
    */
    public void setIndex(int index) {
        this.index = index;
    }

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

    /**
     * 获取users的值
     * @return 返回 users
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * 对users进行赋值
     * @param users users的新值
     */
    public void setUsers(List<User> users) {
        this.users = users;
    }

    /**
     * 获取data的值
     * @return 返回 data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * 对data进行赋值
     * @param data data的新值
     */
    public void setData(byte[] data) {
        this.data = data;
    }
}
