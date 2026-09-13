package com.worklog.github;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/** 주소를 붙여 넣어 등록한다 (9/10). 사람이 실제로 복사해 오는 모양들을 모두 받는다. */
class RepoRefParseTest {

    @ParameterizedTest
    @CsvSource({
        "https://github.com/Ae-Ti/Devronicle, Ae-Ti/Devronicle",
        "http://github.com/Ae-Ti/Devronicle, Ae-Ti/Devronicle",
        "https://www.github.com/Ae-Ti/Devronicle, Ae-Ti/Devronicle",
        "github.com/Ae-Ti/Devronicle, Ae-Ti/Devronicle",
        "https://github.com/Ae-Ti/Devronicle.git, Ae-Ti/Devronicle",
        "git@github.com:Ae-Ti/Devronicle.git, Ae-Ti/Devronicle",
        "https://github.com/Ae-Ti/Devronicle/tree/main, Ae-Ti/Devronicle",
        "https://github.com/Ae-Ti/Devronicle/issues/12, Ae-Ti/Devronicle",
        "  https://github.com/Ae-Ti/Devronicle  , Ae-Ti/Devronicle",
        "Ae-Ti/Devronicle, Ae-Ti/Devronicle",
    })
    @DisplayName("주소·클론 주소·하위 경로·owner/repo 를 모두 받는다")
    void parses(String input, String expected) {
        assertThat(RepoService.parseRepoRef(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "Ae-Ti", "https://gitlab.com/a/b", "https://github.com/"})
    @DisplayName("GitHub 주소가 아니면 받지 않는다")
    void rejects(String input) {
        assertThat(RepoService.parseRepoRef(input)).isNull();
    }

    @org.junit.jupiter.api.Test
    @DisplayName("null 도 안전하게 거른다")
    void rejectsNull() {
        assertThat(RepoService.parseRepoRef(null)).isNull();
    }
}
