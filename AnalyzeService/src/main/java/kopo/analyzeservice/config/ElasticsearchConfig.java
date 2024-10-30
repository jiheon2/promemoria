package kopo.analyzeservice.config;

import co.elastic.clients.transport.ElasticsearchTransport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.Transport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContexts;

import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import co.elastic.clients.json.jackson.JacksonJsonpMapper;

import org.springframework.beans.factory.annotation.Value;

import javax.net.ssl.SSLContext;

@Configuration
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.host}")
    private String host;
    // 쿠버네티스 배포시 host : elasticsearch

    @Value("${spring.elasticsearch.port}")
    private int port;

    @Value("${spring.elasticsearch.username}")
    private String username;

    @Value("${spring.elasticsearch.password}")
    private String password;

    @Bean
    public RestClient restClient() throws Exception {
        // 기본 인증을 위한 CredentialsProvider 설정
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY, // 모든 호스트와 포트에 적용
                new UsernamePasswordCredentials(username, password) // 사용자 이름과 비밀번호 설정
        );

        // 모든 인증서를 신뢰하는 SSL 컨텍스트 생성 (프로덕션 환경에서는 권장되지 않음)
        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, (certificates, authType) -> true) // 모든 인증서 신뢰
                .build();

        // 지정된 호스트, 포트 및 HTTPS 프로토콜로 RestClient 빌더 생성
        RestClientBuilder builder = RestClient.builder(
                        new HttpHost(host, port, "https")) // 보안 통신을 위한 HTTPS 사용
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider) // 인증 제공자 설정
                        .setSSLContext(sslContext) // SSL 컨텍스트 설정
                        .setSSLHostnameVerifier((hostname, session) -> true) // 호스트네임 검증 비활성화 (안전하지 않음)
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
