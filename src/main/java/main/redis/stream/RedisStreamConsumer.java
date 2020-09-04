package main.redis.stream;

import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.List;

public class RedisStreamConsumer {

    public final static String STREAMS_KEY = "weather_sensor:wind";


    public static void main(String[] args) {
        RedisClient redisClient = RedisClient.create("redis://192.168.143.192:6380"); // change to reflect your environment
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        try {
            syncCommands.xgroupCreate(XReadArgs.StreamOffset.from(STREAMS_KEY, "0-0"), "application_1");
        } catch (RedisBusyException redisBusyException) {
            System.out.println(String.format("\t Group '%s' already exists", "application_1"));
        }


        System.out.println("Waiting for new messages");

        while (true) {

            List<StreamMessage<String, String>> messages = syncCommands.xreadgroup(
                    Consumer.from("application_1", "consumer_1"),
                    XReadArgs.StreamOffset.lastConsumed(STREAMS_KEY)
            );

            if (!messages.isEmpty()) {
                for (StreamMessage<String, String> message : messages) {
                    System.out.println(message);
                    // Confirm that the message has been processed using XACK
                    syncCommands.xack(STREAMS_KEY, "application_1", message.getId());
                }
            }


        }
    }
}
