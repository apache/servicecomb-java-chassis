package org.apache.servicecomb.governance.profileclasses.service;

import org.springframework.stereotype.Service;

@Service
public class MockProfileClassUserService {
    public String getUser() {
        return "bill";
    }
}
