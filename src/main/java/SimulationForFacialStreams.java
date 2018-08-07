import org.eclipse.paho.client.mqttv3.*;
import org.json.simple.JSONObject;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;

public class SimulationForFacialStreams
{
    public static void main(String[] args) throws Exception
    {
        String clientId=MqttConstant.CLIENT_ID;
        String serverUrl =MqttConstant.URL;

        String USERNAME = MqttConstant.USERNAME;
        String password=MqttConstant.PASSWORD;

        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setUserName(USERNAME);
        connectOptions.setPassword(password.toCharArray());

        MqttClient client = new MqttClient(serverUrl, clientId);
        client.connect(connectOptions);

        System.out.println("SUBSCRIBED_TOPICS:-");
        for (Map.Entry<String,String> topic:MqttConstant.TOPICS.entrySet())
        {
            System.out.println(topic.getValue());
            client.subscribe(topic.getValue());
        }
        System.out.println("\n"+"PUBLISHING_TOPIC:-"+MqttConstant.TOPICS.get("PUBLISHING_TOPIC")+"\n");

        int countForFacesDetected=0, countForSnapshots=0,countForFalseNegatives=0,countForFalsePositives=0;
        Random r = new Random();
        int SLow = 15;
        int SHigh = 20;
        int DLow = 13;
        int SSLow=5;
        int Failures;
        int PLow=3;

        int cfss=-1;
        int total=0;
        Boolean flag=false;
        try {
            countForSnapshots = r.nextInt(SHigh - SLow) + SLow;
            countForFacesDetected = r.nextInt(countForSnapshots - DLow) + DLow;

            total = countForFacesDetected + countForSnapshots;
//        int cfdNcfs=(total)/2;

            cfss = r.nextInt(countForFacesDetected - SSLow) + SSLow;
            System.out.println(cfss);

            Failures = countForFacesDetected - cfss;
            countForFalsePositives = r.nextInt(Failures - PLow) + PLow;
            countForFalseNegatives = Failures - countForFalsePositives;
//        int half_cfd=countForFacesDetected/2;
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        System.out.println("countForSnapshots: " + countForSnapshots + "\n" + "countForFacesDetected: " + countForFacesDetected);
        System.out.println("countForFalsePositives: "+countForFalsePositives+"\n"+"countForFalseNegatives: "+countForFalseNegatives+"\n");

        int cfd = 0,cfs = 0,cfp=0,cfn=0;;

//        FORMAT OF INPUT STREAMS:-

//        /mqtt/flink/camera----------------------#facesdetected, #snapshot input stream
//        {
//                "version" : "2",
//                "event_type" : "match",
//                "name" : "Renu Verma",
//                "timestamp" : "1531381899",
//                "faceid" : "57198e4b-2ae9-4b51-af25-fb3fee5a7a95",
//                "device_id" : "MqttConstant.DEVICE_IDS[1]",
//                "type" : "face",
//                "model" : "aws_rekog",
//                "face" : "" ---#snapshot, "face" :"HexCodedString" ---#facedetected
//        }

//        /mqtt/flink-----------------------------#falsepostives, #falsenegatives input stream
//        {
//            "version" : "v1.01",
//            "device_id" : "MqttConstant.DEVIC_IDS[0]",
//            "timestamp" : 1530534235,
//            "type" : "falsenegatives",
//            "faceid" : "73bef2a2-9a9a-4de6-9db4-abd97c4831e6".
//        }

        System.out.println("Gnerating #snapshots,#faceDetected......................");
        for (int index = 1; index <=total; index++)
        {
            JSONObject stream = new JSONObject();
            
            stream.put("version", "2");
            stream.put("event_type", "match");
            stream.put("name", "Renu Verma");
            stream.put("timestamp", System.currentTimeMillis());
            stream.put("faceid", "57198e4b-2ae9-4b51-af25-fb3fee5a7a95");
            System.out.println(index);
            if (flag==false)
                stream.put("device_id", MqttConstant.DEVICE_IDS[0]);
            else
                stream.put("device_id",MqttConstant.DEVICE_IDS[1]);
            flag=!flag;
            stream.put("type", "face");
            stream.put("model", "aws_recok");

            if (cfs != countForSnapshots) {
                cfs++;
                stream.put("face", "");
            } else if (cfd != countForFacesDetected) {
                cfd++;
                stream.put("face", "HexCodedString");
            }

            callSetCallBack(client);
            printStream_N_PublishMessage(client,stream);
        }
        System.out.println("Gnerating #falsepositives,#falsenegatives......................");
        for (int index=1;index<=countForFalseNegatives+countForFalsePositives;index++)
        {
            JSONObject stream=new JSONObject();

            stream.put("version","v1.01");
            stream.put("timestamp",System.currentTimeMillis());
            stream.put("faceid","57198e4b-2ae9-4b51-af25-fb3fee5a7a95");
            System.out.println(index);
            if (flag==false)
                stream.put("device_id",MqttConstant.DEVICE_IDS[0]);
            else
                stream.put("device_id",MqttConstant.DEVICE_IDS[1]);
            flag=!flag;
             stream.put("model","aws_recok");

            if(cfp!=countForFalsePositives)
            {
                cfp++;
                stream.put("type","falsepositive");
            }
            else if(cfn!=countForFalseNegatives)
            {
                cfn++;
                stream.put("type","falsenegative");
            }

            callSetCallBack(client);
            printStream_N_PublishMessage(client,stream);
        }
    }

    private static void callSetCallBack(MqttClient client)
    {
        client.setCallback(new MqttCallback()
        {
            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

                System.out.println(mqttMessage+"\n");
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

    private static void printStream_N_PublishMessage(MqttClient client,JSONObject stream)
    {
        System.out.println("Simulated Stream: " + stream.toString());
        try {
            client.publish(MqttConstant.TOPICS.get("PUBLISHING_TOPIC"), new MqttMessage(stream.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (MqttException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(1000);
        } catch (Exception ex) {
        }
    }
}

