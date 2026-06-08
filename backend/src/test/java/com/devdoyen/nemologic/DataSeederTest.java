package com.devdoyen.nemologic;

import com.devdoyen.nemologic.config.DataSeeder;
import com.devdoyen.nemologic.repository.StageRepository;
import com.devdoyen.nemologic.repository.UserRepository;
import com.devdoyen.nemologic.scheduler.DailyPuzzleScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.devdoyen.nemologic.model.Stage;
import com.devdoyen.nemologic.model.User;
import static org.mockito.Mockito.*;

/**
 * DataSeeder CommandLineRunner 단위 테스트
 *
 * 검증 목표:
 * 1. run() 호출 시 DailyPuzzleScheduler.generateDailyPuzzle()이 반드시 1회 호출된다.
 * 2. DB가 비어있지 않은 상황(count > 0)에서도 스케줄러는 독립적으로 트리거된다.
 * 3. DB가 완전히 비어 있을 때 기본 User / Stage 시딩이 수행된다.
 */
@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StageRepository stageRepository;

    @Mock
    private DailyPuzzleScheduler dailyPuzzleScheduler;

    @InjectMocks
    private DataSeeder dataSeeder;

    // -----------------------------------------------------------------------
    // Red Phase: DailyPuzzleScheduler 트리거 검증
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("[Red] run() 호출 시 DailyPuzzleScheduler.generateDailyPuzzle()이 1회 실행된다")
    void run_shouldTriggerDailyPuzzleScheduler_once() throws Exception {
        // given — DB가 이미 채워진 상태로 시딩 분기를 건너뜀
        when(userRepository.count()).thenReturn(3L);
        when(stageRepository.count()).thenReturn(2L);

        // when
        dataSeeder.run();

        // then — 스케줄러가 정확히 1회 호출됐는지 검증
        verify(dailyPuzzleScheduler, times(1)).generateDailyPuzzle();
    }

    @Test
    @DisplayName("[Red] DB가 비어있을 때 기본 유저 3명이 저장된다")
    void run_whenUsersEmpty_shouldSeedThreeDefaultUsers() throws Exception {
        // given
        when(userRepository.count()).thenReturn(0L);
        when(stageRepository.count()).thenReturn(2L);

        // when
        dataSeeder.run();

        // then — UserRepository.save()가 정확히 3회 호출됐는지 검증
        verify(userRepository, times(3)).save(any(User.class));
        // 스케줄러도 함께 호출
        verify(dailyPuzzleScheduler, times(1)).generateDailyPuzzle();
    }

    @Test
    @DisplayName("[Red] DB가 비어있을 때 기본 스테이지 2개가 저장된다")
    void run_whenStagesEmpty_shouldSeedTwoDefaultStages() throws Exception {
        // given
        when(userRepository.count()).thenReturn(3L);
        when(stageRepository.count()).thenReturn(0L);

        // when
        dataSeeder.run();

        // then — StageRepository.save()가 정확히 2회 호출됐는지 검증
        verify(stageRepository, times(2)).save(any(Stage.class));
        // 스케줄러도 함께 호출
        verify(dailyPuzzleScheduler, times(1)).generateDailyPuzzle();
    }
}
