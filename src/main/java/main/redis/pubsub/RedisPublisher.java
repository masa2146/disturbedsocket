package main.redis.pubsub;

import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;

public class RedisPublisher {

    public static void main(String[] args) {
        RedisClient redisClient = RedisClient.create("redis://192.168.143.192:6380"); // change to reflect your environment

        StatefulRedisPubSubConnection<String, String> connection
                = redisClient.connectPubSub();


        RedisPubSubAsyncCommands<String, String> async
                = connection.async();
        boolean channel = async.publish("channel", "Hello, Redis!").isDone();


    }
}
