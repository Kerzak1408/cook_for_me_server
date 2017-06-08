package WebSockets;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

	  @Override
	    public void configureMessageBroker(MessageBrokerRegistry config) {
		  config.enableSimpleBroker("/topic", "/queue", "/exchange");
//        config.enableStompBrokerRelay("/topic", "/queue", "/exchange"); // Uncomment for external message broker (ActiveMQ, RabbitMQ)
        config.setApplicationDestinationPrefixes("/topic", "/queue"); // prefix in client queries
        config.setUserDestinationPrefix("/user");
	    }

	    @Override
	    public void registerStompEndpoints(StompEndpointRegistry registry) {
	    	registry.addEndpoint("/example-endpoint").withSockJS();
	    }

}