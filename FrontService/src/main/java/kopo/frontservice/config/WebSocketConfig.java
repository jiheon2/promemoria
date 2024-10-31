package kopo.frontservice.config;

import kopo.frontservice.interceptor.WebSocketChannelInterceptor;
import kopo.frontservice.interceptor.WebSocketInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// 웹 소켓 접속 설정(클라이언트), 메세지 주고 받을 때 어떤 형식으로 할 건지(url 설정)
@EnableWebSocketMessageBroker
@Configuration  // 웹소켓 메시지 브로커 설정과 STOMP 엔드포인트 설정
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override // 메시지 브로커 설정을 담당, 이 메서드는 클라이언트와 서버 간 메시지 라우팅을 관리함
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/videoCall"); // broker url 설정(클라이언트에게 메시지를 전달하기 위한 메시지 브로커의 엔드포인트, 이걸로 시작하면 브로커 -> 클라이언트)
        config.setApplicationDestinationPrefixes("/studyRoom");   // send url 설정(클라이언트가 메시지를 서버로 보낼 때 사용할 URL의 접두사 설정, 클라이언트는 이걸로 시작하는 주소로 메세지를 보냄)
        // 채팅방 생성 /studyRoom/createRoom, 입장 /studyRoom/enterRoom 이런 식으로
        // 클라이언트가 메세지를 서버로부터 받으려면 /videoCall/roomId 이렇게
    }

    @Override  // STOMP (Simple Text Oriented Messaging Protocol) 엔드포인트 설정을 담당(클라이언트가 웹 소켓에 연결하기 위해서)
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/signaling")  // webSocket 접속 시 endpoints 설정(여기로 클라이언트가 웹 소켓에 '접속' -> 처음에 웹 소켓에 접속 할 때만 씀(연결 설정), 메세지 보내는 건 /studyRoom 이거임)
                .setAllowedOriginPatterns("*")      // cors 설정(모두 허용)
                .addInterceptors(new WebSocketInterceptor())  // WebSocketInterceptor 추가
                .withSockJS(); // 브라우저에서 webSocket을 지원하지 않는 경우에 대한 대안으로 어플리케이션의 코드를 변경할 필요 없이 런타임에 필요할 때 대체하기 위해 설정
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new WebSocketChannelInterceptor()); // ChannelInterceptor 등록
    }

    /**
     * 메시지 브로커는 /videoCall로 시작하는 경로에서 클라이언트에게 메시지를 전달
     * 클라이언트는 /studyRoom으로 시작하는 경로를 통해 서버에 메시지를 전송
     * STOMP 엔드포인트는 /signaling로 설정되어 있으며, 모든 출처에서의 웹소켓 연결을 허용하고,
     * SockJS를 사용해 웹소켓을 지원하지 않는 브라우저에 대해 폴백(fallback) 기능을 제공
     */
}
