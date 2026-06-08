package com.devdoyen.nemologic.repository;

import com.devdoyen.nemologic.model.History;
import com.devdoyen.nemologic.model.Stage;
import com.devdoyen.nemologic.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class HistoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private HistoryRepository historyRepository;

    @Test
    public void testSaveAndFindByUserId() {
        User user = new User(null, "TestUser", 100, 1);
        Stage stage = new Stage(null, "TestStage", 5, 5, new int[][]{{0}});
        
        user = entityManager.persist(user);
        stage = entityManager.persist(stage);
        
        History history = new History(user, stage, LocalDateTime.now(), 50, 120);
        entityManager.persist(history);
        entityManager.flush();
        
        List<History> found = historyRepository.findByUserId(user.getId());
        assertEquals(1, found.size());
        assertEquals(user.getId(), found.get(0).getUser().getId());
        assertEquals(120, found.get(0).getElapsedTime());
    }

}
