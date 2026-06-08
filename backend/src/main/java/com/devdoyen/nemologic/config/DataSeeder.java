package com.devdoyen.nemologic.config;

import com.devdoyen.nemologic.model.Stage;
import com.devdoyen.nemologic.model.User;
import com.devdoyen.nemologic.repository.StageRepository;
import com.devdoyen.nemologic.repository.UserRepository;
import com.devdoyen.nemologic.scheduler.DailyPuzzleScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 부팅 직후 실행되는 초기 데이터 시더.
 *
 * 역할:
 * 1. User / Stage 기본 데이터가 없을 경우 시딩한다.
 * 2. DailyPuzzleScheduler.generateDailyPuzzle()을 즉시 한 번 호출하여
 *    자정 크론 스케줄을 기다리지 않고도 AI 데일리 퍼즐이 DB에 적재되도록 한다.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final StageRepository stageRepository;
    private final DailyPuzzleScheduler dailyPuzzleScheduler;

    public DataSeeder(UserRepository userRepository,
                      StageRepository stageRepository,
                      DailyPuzzleScheduler dailyPuzzleScheduler) {
        this.userRepository = userRepository;
        this.stageRepository = stageRepository;
        this.dailyPuzzleScheduler = dailyPuzzleScheduler;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. 기본 유저 시딩
        if (userRepository.count() == 0) {
            userRepository.save(new User(null, "Player1", 200, 2));
            userRepository.save(new User(null, "Player2", 500, 3));
            userRepository.save(new User(null, "Player3", 1000, 5));
            log.info("[DataSeeder] 기본 유저 3명 시딩 완료");
        }

        // 2. 기본 스테이지 시딩
        if (stageRepository.count() == 0) {
            stageRepository.save(new Stage(
                null,
                "Heart Shape",
                5,
                5,
                new int[][]{
                    {0, 1, 0, 1, 0},
                    {1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1},
                    {0, 1, 1, 1, 0},
                    {0, 0, 1, 0, 0}
                }
            ));

            stageRepository.save(new Stage(
                null,
                "Checkerboard",
                5,
                5,
                new int[][]{
                    {1, 0, 1, 0, 1},
                    {0, 1, 0, 1, 0},
                    {1, 0, 1, 0, 1},
                    {0, 1, 0, 1, 0},
                    {1, 0, 1, 0, 1}
                }
            ));
            log.info("[DataSeeder] 기본 스테이지 2개 시딩 완료");
        }

        // 3. AI 데일리 퍼즐 강제 트리거 (부팅 시점 즉시 생성)
        log.info("[DataSeeder] AI 데일리 퍼즐 즉시 생성 트리거 시작...");
        dailyPuzzleScheduler.generateDailyPuzzle();
        log.info("[DataSeeder] AI 데일리 퍼즐 즉시 생성 트리거 완료");
    }
}
