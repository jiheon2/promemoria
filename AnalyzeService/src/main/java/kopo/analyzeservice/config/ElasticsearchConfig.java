package kopo.analyzeservice.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.Transport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.host}")
    private String host;

    @Value("${spring.elasticsearch.port}")
    private int port;

    @Value("${spring.elasticsearch.username}")
    private String username;

    @Value("${spring.elasticsearch.password}")
    private String password;

    @Bean
    public RestClient restClient() {
        // 기본 인증을 위한 CredentialsProvider 설정
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY, // 모든 호스트와 포트에 적용
                new UsernamePasswordCredentials(username, password) // 사용자 이름과 비밀번호 설정
        );

        // HTTP 프로토콜로 RestClient 빌더 생성
        RestClientBuilder builder = RestClient.builder(
                        new HttpHost(host, port, "http")) // 보안 통신을 위한 HTTP 사용
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider) // 인증 제공자 설정
                );

        // RestClient 빌드 및 반환
        return builder.build();
    }

    @Bean
    public Transport transport(RestClient restClient) {
        // RestClient와 JSON 매퍼를 사용하여 전송 계층 생성
        return new RestClientTransport(
                restClient, new JacksonJsonpMapper()
        );
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(Transport transport) {
        // 전송 계층을 사용하여 ElasticsearchClient 생성 및 반환
        return new ElasticsearchClient((ElasticsearchTransport) transport);
    }
}
