package main.redis.pubsub;

import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;

public class RedisSubscriber {
    public static void main(String[] args) throws InterruptedException {
        RedisClient redisClient = RedisClient.create("redis://192.168.143.192:6380"); // change to reflect your environment

        StatefulRedisPubSubConnection<String, String> connection
                = redisClient.connectPubSub();
        connection.addListener(new RedisPubSubListener<String, String>() {
            public void message(String s, String s2) {
                System.out.println("Message is " + s + " on channel name is " + s2);
            }

            public void message(String s, String k1, String s2) {

            }

            public void subscribed(String s, long l) {

            }

            public void psubscribed(String s, long l) {

            }

            public void unsubscribed(String s, long l) {

            }

            public void punsubscribed(String s, long l) {

            }
        });

        RedisPubSubCommands<String, String> async
                = connection.sync();
        async.subscribe("channel");

        new Thread(() -> {
            while (true) {

            }
        }).run();

    }


    static class Listener implements RedisPubSubListener<String, String> {

        public void message(String channel, String message) {
            System.out.println("Message is " + message + " on channel name is " + channel);
        }

        public void message(String s, String k1, String s2) {

        }

        public void subscribed(String s, long l) {

        }

        public void psubscribed(String s, long l) {

        }

        public void unsubscribed(String s, long l) {

        }

        public void punsubscribed(String s, long l) {

        }
    }
}
