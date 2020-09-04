package main.redis.stream;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.HashMap;
import java.util.Map;

public class RedisStreamProducer {
    public static void main(String[] args) {
        RedisClient redisClient = RedisClient.create("redis://192.168.143.192:6380"); // change to reflect your environment
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        Map<String, String> messageBody = new HashMap();
        messageBody.put( "speed", "15" );
        messageBody.put( "direction", "270" );
        messageBody.put( "sensor_ts", String.valueOf(System.currentTimeMillis()) );

        String messageId = syncCommands.xadd(
                "weather_sensor:wind",
                messageBody);


        System.out.println( String.format("Message %s : %s posted", messageId, messageBody) );

        connection.close();
        redisClient.shutdown();
    }
}
