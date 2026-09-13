package com.worklog.admin;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DirectoryClientTest {

    private static DirectoryProperties properties(String baseUrl, String apiKey) {
        DirectoryProperties p = new DirectoryProperties();
        p.setBaseUrl(baseUrl);
        p.setApiKey(apiKey);
        return p;
    }

    @Test
    @DisplayName("주소와 키가 모두 있어야 설정된 것으로 본다")
    void requiresBothBaseUrlAndKey() {
        assertThat(properties("http://directory.internal", "key").isConfigured()).isTrue();
        assertThat(properties("http://directory.internal", "").isConfigured()).isFalse();
        assertThat(properties("", "key").isConfigured()).isFalse();
        assertThat(properties(null, null).isConfigured()).isFalse();
    }

    @Test
    @DisplayName("설정이 없으면 호출하지 않고 빈 목록을 돌려준다 — 콘솔의 나머지가 같이 죽으면 안 된다")
    void returnsEmptyWhenNotConfigured() {
        DirectoryClient client = new DirectoryClient(properties(null, null));

        assertThat(client.isConfigured()).isFalse();
        assertThat(client.companies()).isEmpty();
        assertThat(client.employees(1L)).isEmpty();
    }

    @Test
    @DisplayName("사내망이 닿지 않아도 예외를 던지지 않는다")
    void survivesUnreachableHost() {
        // 존재하지 않는 주소 — 연결 실패해도 빈 목록이어야 한다
        DirectoryClient client = new DirectoryClient(properties("http://127.0.0.1:1/none", "key"));

        assertThat(client.employees(1L)).isEmpty();
    }
}
