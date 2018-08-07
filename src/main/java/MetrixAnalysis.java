import com.mongodb.BasicDBObject;

import com.mongodb.MongoClient;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.*;

public class MetrixAnalysis
{
    private static int falseNegativeCount = 0,falsePositiveCount = 0,faceDetectedCount =0,successCount = 0;
    public static void main(String args[]) throws Exception
    {
        StreamExecutionEnvironment env=StreamExecutionEnvironment.getExecutionEnvironment();

        String clientId = MqttConstant.CLIENT_ID;
        String serverUrl =MqttConstant.URL;

        String password =MqttConstant.PASSWORD;
        String USERNAME =MqttConstant.USERNAME;

        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setUserName(USERNAME);
        connectOptions.setPassword(password.toCharArray());

        MqttClient client = new MqttClient(serverUrl, clientId);
        client.connect(connectOptions);

        System.out.println("SUBSCRIBED_TOPICS:-");
        for (Map.Entry<String,String> topic:MqttConstant.TOPICS.entrySet())
        {
            System.out.println(topic.getValue());
            client.subscribe(topic.getValue()) ;
        }
        System.out.println("\n"+"PUBLISHING_TOPIC:-"+MqttConstant.TOPICS.get("PUBLISHING_TOPIC")+"\n");

        DataStream<Integer> dataStream= env.fromElements(1,2,3,4,5,6);

        client.setTimeToWait(1000000);

        System.out.println("Client Connected :-  "+client.isConnected());


        client.setCallback(new MqttCallback()
        {
            @Override
            public void connectionLost(Throwable throwable)
            { System.out.println("Connnection Lost!!!");
                try {
                    main(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");
                Date date = new Date();

                //mongo db connection with database and collection
                System.out.println("\nMESSAGE_ARRIVED:-\n"+mqttMessage+"\n");
                MongoClient mongoClient = new MongoClient(MqttConstant.LOCAL_DB_ADDRESS, 27017);
                DB database = mongoClient.getDB("Faces");
                DBCollection fetchCollection = database.getCollection("DeviceAnalysis");

                //getting stream coming from mqtt.fx in payload
                String payload=new String(mqttMessage.getPayload());
                System.out.println("\nPAYLOAD:-\n"+payload+"\n");

                try
                {
                    //creating json object for stream
                    JSONObject obj = new JSONObject(payload);
                    System.out.println("JSON_OBJECT:-\n" + obj + "\n");

                    //fetching fields from jsonobject
                    String device_id = obj.getString("device_id");
                    Long timestamp = obj.getLong("timestamp");
                    String version = obj.getString("version");
                    String model;
                    Date resultdate = new Date(timestamp);
                    String streamDate=sdf.format(resultdate);

                    //creating BasicDBObject for document
                    BasicDBObject facialStream= new BasicDBObject();

                    //check model value and insert in model variable
                    //if(obj.has("model"))
                        model = obj.getString("model");
                   //else
                       // model="";

                    //for type field
                    String streamType=null ;

                    if(obj.has("face"))
                    {
                        if(obj.getString("face").equals(""))
                        {
                            streamType="snapshot";
                        }
                        else
                        {
                            streamType="faceDetected";
                        }
                    }
                    else
                    {
                       streamType=obj.getString("type");
                    }

                    //for and query on the basis of device_id,version,model,date
                    BasicDBObject andQuery=new BasicDBObject();
                    List<BasicDBObject> obj1= new ArrayList<BasicDBObject>();
                    obj1.add(new BasicDBObject("device_id",device_id));
                    obj1.add(new BasicDBObject("version",version));
                    obj1.add(new BasicDBObject("date",streamDate));
                    obj1.add(new BasicDBObject("model",model));

                    andQuery.put("$and",obj1);

                    //count of fetched document
                    Cursor fetchRecords= fetchCollection.find(andQuery);
                    int  recordsCount = fetchCollection.find(andQuery).count();
                    System.out.print("FetchedRecordsCount: "+recordsCount+"\n");

                    Boolean flag =false;
                    if(recordsCount>0)
                    {
                        System.out.println("When Fetched Records>0\n");
                        falseNegativeCount = 0;falsePositiveCount = 0;faceDetectedCount =0;successCount = 0; //,snapshotsCount = 0;

                        while (fetchRecords.hasNext())
                        {
                            BasicDBObject fetchedRecord = (BasicDBObject) fetchRecords.next();
                            String recordType=fetchedRecord.getString("type");
                            int recordCount=fetchedRecord.getInt("count");

                            if (recordType.equals(streamType))
                            {
                                flag=true;
                                System.out.println("Fetched Records type matches with Input Facial Stream\n");
                                BasicDBObject updateQuerry1=new BasicDBObject().append("$set",new BasicDBObject().append("count",++recordCount).append("timestamp",timestamp));
                                fetchCollection.update(fetchedRecord,updateQuerry1);
                            }
                            calculatingSuccessCount(recordType,recordCount);

                        }

                        if(!flag)
                        {
                            System.out.println("Fetched Records type does not match with Input Facial Stream\n");
                            saveNewRecord(facialStream,device_id,version,model,streamType,timestamp,streamDate,fetchCollection);
                            calculatingSuccessCount(streamType,1);
                        }
                        successCount = faceDetectedCount - falsePositiveCount - falseNegativeCount;

                        fetchRecords= fetchCollection.find(andQuery);
                        while (fetchRecords.hasNext())
                        {
                            BasicDBObject fetchedRecord = (BasicDBObject) fetchRecords.next();
                            BasicDBObject updateQuerry2=new BasicDBObject().append("$set",new BasicDBObject().append("successCount",successCount));
                            fetchCollection.update(fetchedRecord,updateQuerry2);
                        }
                    }
                    else
                    {
                        System.out.println("When Fetched Records=0\n");
                        saveNewRecord(facialStream,device_id,version,model,streamType,timestamp,streamDate,fetchCollection);
                    }

                 System.out.println("\n session ended ====================================================================================");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken)
            { }
        });
        dataStream.print();
        env.execute();

        System.out.println("Client Connected :-  "+client.isConnected());
        Thread.sleep(120000);
        System.out.println("Reached Here");
        main(null);
    }


    private static void calculatingSuccessCount(String recordType, int recordCount)
    {
        switch (recordType)
        {
            case "faceDetected":faceDetectedCount=recordCount;
                break;
            case "falsepositive":falsePositiveCount=recordCount;
                break;
            case "falsenegative":falseNegativeCount=recordCount;
                break;
            default:System.out.println("Incorrect Record Type For calculating successCount!!!\n");
        }
    }

    private static void saveNewRecord( BasicDBObject facialStream, String device_id,String version,String model,String streamType,Long timestamp,String streamDate,DBCollection fetchCollection)
    {
        facialStream.put("device_id",device_id);
        facialStream.put("version",version);
        facialStream.put("model",model);
        facialStream.put("type",streamType);
        facialStream.put("count",1);
        facialStream.put("timestamp",timestamp);
        facialStream.put("date",streamDate);

        switch (streamType)
        {
            case "snapshot": facialStream.put("successCount",0);
                              break;
            case "faceDetected": facialStream.put("successCount",1);
                                 break;
            case "falsepositive":facialStream.put("successCount",-1);
                                 break;
            case "falsenegative":facialStream.put("successCount",-1);
                                 break;
            default:System.out.println("Incorrect Record Type!!! For initial success count\n");
        }
        System.out.println("GENERATED_DOCUMENT:-\n"+facialStream+"\n");
        fetchCollection.insert(facialStream);
    }
}



