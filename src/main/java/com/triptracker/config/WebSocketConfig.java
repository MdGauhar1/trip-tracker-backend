
package com.triptracker.config;

import com.triptracker.auth.JwtService;
import com.triptracker.tripgroup.GroupMemberRepository;
import com.triptracker.user.User;
import com.triptracker.user.UserRepository;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    private static final Pattern LOCATION_TOPIC =
            Pattern.compile("^/topic/groups/(\\d+)/locations$");

    public WebSocketConfig(
            JwtService jwtService,
            UserRepository userRepository,
            GroupMemberRepository groupMemberRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                        "http://localhost:8100",
                        "http://localhost",
                        "https://localhost",
                        "capacitor://localhost"
                );
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {

            @Override
            public Message<?> preSend(
                    Message<?> message,
                    MessageChannel channel) {

                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(
                                message, StompHeaderAccessor.class);

                if (accessor == null || accessor.getCommand() == null) {
                    return message;
                }

                StompCommand command = accessor.getCommand();

                // Authenticate the WebSocket/STOMP session.
                if (command == StompCommand.CONNECT) {
                    String authorization =
                            accessor.getFirstNativeHeader("Authorization");

                    if (authorization == null
                            || !authorization.startsWith("Bearer ")) {
                        throw new IllegalArgumentException(
                                "Missing WebSocket bearer token");
                    }

                    String token = authorization.substring(7).trim();

                    if (token.isEmpty() || !jwtService.isTokenValid(token)) {
                        throw new IllegalArgumentException(
                                "Invalid or expired JWT");
                    }

                    String email = jwtService.extractEmail(token)
                            .trim()
                            .toLowerCase(Locale.ROOT);

                    User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Authenticated user was not found"));

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    email, null, List.of());

                    accessor.setUser(authentication);
                }

                // Only allow subscriptions to a location topic for a group
                // the authenticated user belongs to.
                if (command == StompCommand.SUBSCRIBE) {
                    Principal principal = accessor.getUser();
                    String destination = accessor.getDestination();

                    if (principal == null || destination == null) {
                        throw new IllegalArgumentException(
                                "Unauthenticated subscription");
                    }

                    Matcher matcher = LOCATION_TOPIC.matcher(destination);

                    if (!matcher.matches()) {
                        throw new IllegalArgumentException(
                                "Subscription destination is not allowed");
                    }

                    Long groupId = Long.parseLong(matcher.group(1));

                    String email = principal.getName()
                            .trim()
                            .toLowerCase(Locale.ROOT);

                    User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Authenticated user was not found"));

                    boolean isMember =
                            groupMemberRepository.existsByGroup_IdAndUser_Id(
                                    groupId, user.getId());

                    if (!isMember) {
                        throw new IllegalArgumentException(
                                "You are not a member of this trip");
                    }
                }

                return message;
            }
        });
    }
}
