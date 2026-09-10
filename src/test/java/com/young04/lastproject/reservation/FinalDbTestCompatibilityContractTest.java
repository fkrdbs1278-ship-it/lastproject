package com.young04.lastproject.reservation;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class FinalDbTestCompatibilityContractTest {

    @Test
    void 메인_DB설정은_팀원별_환경변수를_사용한다() throws Exception {
        String yaml = Files.readString(Path.of(
                "src/main/resources/application.yaml"
        ));

        assertThat(yaml).contains("url: ${DB_URL}");
        assertThat(yaml).contains("username: ${DB_USERNAME}");
        assertThat(yaml).contains("password: ${DB_PASSWORD}");

        /*
         * 과거 로컬 Oracle 설정을 주석으로 남겨둔 것은 실제 Spring 설정에
         * 영향을 주지 않는다. 따라서 주석을 제거한 '활성 설정'만 검사한다.
         */
        String activeYaml = yaml.lines()
                .filter(line -> !line.stripLeading().startsWith("#"))
                .collect(Collectors.joining("\n"));

        assertThat(activeYaml)
                .doesNotContain("url: jdbc:oracle:thin:@localhost:1521:XE");
        assertThat(activeYaml)
                .doesNotContain("username: orcltest");
    }

    @Test
    void 테스트_프로필은_특정_Oracle계정을_하드코딩하지_않는다()
            throws Exception {

        String yaml = Files.readString(Path.of(
                "src/test/resources/application-test.yml"
        ));

        String activeYaml = yaml.lines()
                .filter(line -> !line.stripLeading().startsWith("#"))
                .collect(Collectors.joining("\n"));

        assertThat(activeYaml).doesNotContain("jdbc:oracle:thin:@localhost");
        assertThat(activeYaml).doesNotContain("username: orcltest");
        assertThat(activeYaml).doesNotContain("password: 1234");
    }

    @Test
    void Phase2통합테스트는_공용DB의_사전_테스트데이터에_의존하지_않는다()
            throws Exception {

        String test = Files.readString(Path.of(
                "src/test/java/com/young04/lastproject/reservation/Phase2ServiceIntegrationTest.java"
        ));

        assertThat(test).doesNotContain(
                "MEMBER_ID = 'phase2_test_member'"
        );
        assertThat(test).doesNotContain(
                "NAME = 'PHASE2_TEST_CUT_30'"
        );

        assertThat(test).contains(
                "INSERT INTO MEMBER"
        );
        assertThat(test).contains(
                "INSERT INTO SERVICE_MENU"
        );
        assertThat(test).contains(
                "@Transactional"
        );
    }
}
