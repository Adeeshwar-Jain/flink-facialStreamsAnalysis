import java.util.HashMap;

public class  MqttConstant
{
        public static String DB_ADDRESS = "192.168.0.5";
        public static String LOCAL_DB_ADDRESS="127.0.0.1";
        public static String USERNAME = "orahi";
        public static String PASSWORD = "Orahi@321";
        public static String URL = "tcp://edge.orahi.com:8883";
        public static String CLIENT_ID = "31f11919f4aa452b8a3e0ed711a64303"+System.currentTimeMillis();

        public static HashMap<String,String> TOPICS;
        static
        {
            TOPICS = new HashMap<String, String>();
            TOPICS.put("CAM_STREAM1", "/mqtt/flink/camera");         //#facedetected, #snapshot input streams
            TOPICS.put("CAM_STREAM", "/camera/0001");                 //#facedetected, #snapshot input streamms
            // /camera/0001
            TOPICS.put("CAM_STREAM_ACTION", "/mqtt/flink");         //#falsepostives, #falsenegatives input streams
            TOPICS.put("PUBLISHING_TOPIC", "/flink/mqtt");          //#Publish input stream
        }
        public static String[]DEVICE_IDS={"cam_exit","cam_entry"};

}
