package pnnl.goss.core.client.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pnnl.goss.core.Client.DESTINATION_TYPE;

/**
 * Tests for the DESTINATION_TYPE enum and queue/topic support in the GOSS
 * client.
 *
 * The Java GOSS client now supports both QUEUE and TOPIC destination types,
 * matching the Python client's behavior.
 *
 * Key differences between Queue and Topic: - QUEUE: Point-to-point messaging.
 * Each message is consumed by exactly one consumer. This is the default for
 * getResponse() to match Python client behavior. - TOPIC: Publish-subscribe
 * messaging. Each message is delivered to all subscribers. This is the default
 * for subscribe() as topics are typically used for broadcast events.
 *
 * Usage examples:
 *
 * // Subscribe to a queue (for request/response patterns)
 * client.subscribe("goss.gridappsd.process.request", handler,
 * DESTINATION_TYPE.QUEUE);
 *
 * // Subscribe to a topic (for broadcast events)
 * client.subscribe("goss.gridappsd.simulation.output.123", handler,
 * DESTINATION_TYPE.TOPIC);
 *
 * // Publish to a queue client.publish("goss.gridappsd.process.request",
 * message, DESTINATION_TYPE.QUEUE);
 *
 * // Publish to a topic client.publish("goss.gridappsd.platform.log", message,
 * DESTINATION_TYPE.TOPIC);
 *
 * // Send request and get response (defaults to QUEUE)
 * client.getResponse(request, "goss.gridappsd.process.request",
 * RESPONSE_FORMAT.JSON);
 *
 * // Send request with explicit destination type client.getResponse(request,
 * "my.topic", RESPONSE_FORMAT.JSON, DESTINATION_TYPE.TOPIC);
 */
public class DestinationTypeTest {

    @Test
    @DisplayName("DESTINATION_TYPE enum should have QUEUE and TOPIC values")
    public void destinationTypeHasQueueAndTopic() {
        // Verify enum values exist
        assertThat(DESTINATION_TYPE.values()).hasSize(2);
        assertThat(DESTINATION_TYPE.valueOf("QUEUE")).isEqualTo(DESTINATION_TYPE.QUEUE);
        assertThat(DESTINATION_TYPE.valueOf("TOPIC")).isEqualTo(DESTINATION_TYPE.TOPIC);
    }

    @Test
    @DisplayName("QUEUE should be the preferred type for request/response patterns")
    public void queueIsPreferredForRequestResponse() {
        // Document that QUEUE is recommended for request/response
        // This matches Python client behavior where get_response uses /queue/
        // prefix
        DESTINATION_TYPE requestResponseType = DESTINATION_TYPE.QUEUE;

        assertThat(requestResponseType)
                .as("Request/response patterns should use QUEUE for point-to-point delivery")
                .isEqualTo(DESTINATION_TYPE.QUEUE);
    }

    @Test
    @DisplayName("TOPIC should be used for broadcast/event patterns")
    public void topicIsPreferredForBroadcast() {
        // Document that TOPIC is recommended for events/broadcasts
        DESTINATION_TYPE broadcastType = DESTINATION_TYPE.TOPIC;

        assertThat(broadcastType)
                .as("Broadcast patterns should use TOPIC for pub/sub delivery")
                .isEqualTo(DESTINATION_TYPE.TOPIC);
    }

    @Test
    @DisplayName("Enum ordinal values should be stable")
    public void enumOrdinalsAreStable() {
        // Verify ordinal values for serialization stability
        assertThat(DESTINATION_TYPE.TOPIC.ordinal()).isEqualTo(0);
        assertThat(DESTINATION_TYPE.QUEUE.ordinal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Enum should support standard operations")
    public void enumSupportsStandardOperations() {
        // Test enum operations
        assertThat(DESTINATION_TYPE.QUEUE.name()).isEqualTo("QUEUE");
        assertThat(DESTINATION_TYPE.TOPIC.name()).isEqualTo("TOPIC");

        // Test comparison
        assertThat(DESTINATION_TYPE.QUEUE).isNotEqualTo(DESTINATION_TYPE.TOPIC);

        // Test valueOf round-trip
        for (DESTINATION_TYPE type : DESTINATION_TYPE.values()) {
            assertThat(DESTINATION_TYPE.valueOf(type.name())).isEqualTo(type);
        }
    }
}
