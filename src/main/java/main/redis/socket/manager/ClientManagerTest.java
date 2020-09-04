package main.redis.socket.manager;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;

public class ClientManagerTest {
    private static final String EVENT_STATUS = "status";
    private static final String EVENT_CONNECTED = "onConnected";
    private static final String CLIENT_ID = "clientId1";

    public static void main(String[] args) {
        RedisClient redisClient = RedisClient.create("redis://192.168.143.192:6380");

        StatefulRedisPubSubConnection<String, String> subConnection
                = redisClient.connectPubSub();

        StatefulRedisPubSubConnection<String, String> pubConnection
                = redisClient.connectPubSub();

        RedisPubSubCommands<String, String> subscribeCommand;
        RedisPubSubCommands<String, String> publisherCommand;

        subConnection.addListener(new Listener());
        RedisCommands<String, String> command = redisClient.connect().sync();


        subscribeCommand = subConnection.sync();
        publisherCommand = pubConnection.sync();

        System.out.println("EKLENDI ABAB");

        subscribeCommand.subscribe(EVENT_CONNECTED + "/" + CLIENT_ID);
        publisherCommand.publish(EVENT_CONNECTED, CLIENT_ID);


        new Thread(() -> {
            while (true) {
//                publisherCommand.publish(EVENT_STATUS, CLIENT_ID);
                command.hset(CLIENT_ID, "status", "1");

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    static class Listener implements RedisPubSubListener<String, String> {

        public void message(String channel, String message) {
            System.out.println("CLIENT RECEIVED MESSAGE OVER REDIS ");
            System.out.println("Message: " + message);
        }

        public void message(String s, String k1, String s2) {

        }

        public void subscribed(String s, long l) {
            System.out.println();

        }

        public void psubscribed(String s, long l) {

        }

        public void unsubscribed(String s, long l) {

        }

        public void punsubscribed(String s, long l) {

        }
    }
}
