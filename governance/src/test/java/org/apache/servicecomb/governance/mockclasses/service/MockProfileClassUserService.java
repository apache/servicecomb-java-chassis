package org.apache.servicecomb.governance.mockclasses.service;

import org.springframework.stereotype.Service;

@Service
public class MockProfileClassUserService {
    public String getUser() {
        return "bill";
    }
}
