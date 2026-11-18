package br.com.medisus.api.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "medisus.ai", name = "enabled", havingValue = "true")
public class AiConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "spring.ai.model", name = "chat", havingValue = "openai")
    PrescriptionModel prescriptionModel(ChatClient.Builder builder) {
        ChatClient client = builder.build();
        return (image, contentType) -> client.prompt()
                .user(user -> user.text("""
                        Read this prescription image and identify only clearly legible medication names and strengths.
                        Return a JSON array of strings and nothing else. Do not diagnose, recommend treatment, infer
                        missing text, or claim availability. If no medication can be read, return [].
                        """).media(org.springframework.util.MimeType.valueOf(contentType),
                        new org.springframework.core.io.ByteArrayResource(image)))
                .call()
                .content();
    }
}
