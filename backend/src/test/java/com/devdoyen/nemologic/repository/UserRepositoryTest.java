package com.devdoyen.nemologic.repository;

import com.devdoyen.nemologic.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSaveAndFindByUuid() {
        User user = new User(null, "TestUser", 100, 1, "test-uuid-1234");
        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());

        User foundUser = userRepository.findByUuid("test-uuid-1234").orElse(null);
        assertNotNull(foundUser);
        // Red Phase: Expected to fail with AssertionFailedError
        assertEquals("RedPhaseUUID", foundUser.getUuid());
    }
}
