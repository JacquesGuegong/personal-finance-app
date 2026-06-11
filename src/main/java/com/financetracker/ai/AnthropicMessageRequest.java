package com.financetracker.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * The JSON body we POST to Claude's Messages API.
 *
 * Shape Anthropic expects (text-only message):
 * <pre>
 * {
 *   "model": "claude-haiku-4-5-20251001",
 *   "max_tokens": 1024,
 *   "system": "You are a helpful finance assistant...",
 *   "messages": [ { "role": "user", "content": "..." } ]
 * }
 * </pre>
 *
 * The "content" field is special: the API accepts EITHER a plain string OR an
 * array of typed blocks. The array form is how images are sent:
 * <pre>
 *   "content": [
 *     { "type": "image", "source": { "type": "base64", "media_type": "image/jpeg", "data": "..." } },
 *     { "type": "text",  "text": "What is the total on this receipt?" }
 *   ]
 * </pre>
 *
 * That's why {@code Message.content} is declared as {@code Object}: Jackson
 * serializes whatever we put there — a String for text-only calls, a
 * {@code List<...>} of block records for image calls.
 *
 * Anthropic uses snake_case ({@code max_tokens}); Java prefers camelCase. The
 * {@code @JsonProperty} annotation bridges the two: the Java field is
 * {@code maxTokens}, but Jackson serializes it as {@code max_tokens}.
 */
public record AnthropicMessageRequest(
        String model,
        @JsonProperty("max_tokens") int maxTokens,
        String system,
        List<Message> messages
) {

    /** A single turn in the conversation. For us it's always one user message. */
    public record Message(String role, Object content) {

        /** Text-only message — content serializes as a plain JSON string. */
        public static Message user(String content) {
            return new Message("user", content);
        }

        /**
         * Image + text message — content serializes as a JSON array of blocks.
         * The image goes FIRST and the question about it second; that's the
         * ordering Anthropic recommends for best results.
         */
        public static Message userWithImage(String mediaType, String base64Data, String text) {
            return new Message("user", List.of(
                    new ImageBlock("image", new ImageSource("base64", mediaType, base64Data)),
                    new TextBlock("text", text)));
        }
    }

    /** {"type": "text", "text": "..."} */
    public record TextBlock(String type, String text) {}

    /** {"type": "image", "source": {...}} */
    public record ImageBlock(String type, ImageSource source) {}

    /** {"type": "base64", "media_type": "image/jpeg", "data": "<base64>"} */
    public record ImageSource(
            String type,
            @JsonProperty("media_type") String mediaType,
            String data
    ) {}
}
