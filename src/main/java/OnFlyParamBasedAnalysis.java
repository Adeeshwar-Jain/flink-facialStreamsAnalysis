import com.google.gson.JsonObject;
import com.mongodb.*;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
public class OnFlyParamBasedAnalysis
{
    public static void main(String args[])
    {
        MongoClient mongoClient = new MongoClient(MqttConstant.LOCAL_DB_ADDRESS, 27017);
        DB database = mongoClient.getDB("Faces");

        DBCollection fetchCollection = database.getCollection("DeviceAnalysis");

        StreamExecutionEnvironment env=StreamExecutionEnvironment.getExecutionEnvironment();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");
        Long timestamp=System.currentTimeMillis();
        Date resultdate = new Date(timestamp);
        String streamDate=sdf.format(resultdate);

        System.out.println("Find Metrics\n");

        List distinctDevice_ids = fetchCollection.distinct("device_id");
        List distinctVersions=fetchCollection.distinct("version");
        List distinctModels=fetchCollection.distinct("model");
        List distinctDates=fetchCollection.distinct("date");

        BasicDBObject andQuery;

        DBCursor findRecords;
        DataStream<JsonObject> dataStream ;

        ArrayList<BasicDBObject> querries;

        System.out.println("1. Find metrics based on Device_id and current Date");
        for (int i=0 ;i<distinctDevice_ids.size();i++)
        {
            querries= new ArrayList<>();
            andQuery=new BasicDBObject();

            querries.add(new BasicDBObject("device_id",distinctDevice_ids.get(i)));
            querries.add(new BasicDBObject("date",streamDate));
            andQuery.put("$and",querries);
            findRecords=fetchCollection.find(andQuery);
            System.out.println();
            System.out.println("Device-id- "+ distinctDevice_ids.get(i)+":-");
            int count=0;
            while(findRecords.hasNext())
            {
                System.out.println(findRecords.next());
            }
        }

        System.out.println();

        System.out.println("2. Find metrics based on Version and current Date");
        for (int i=0 ;i<distinctVersions.size();i++)
        {
            querries= new ArrayList<>();
            andQuery=new BasicDBObject();

            querries.add(new BasicDBObject("version",distinctVersions.get(i)));
            querries.add(new BasicDBObject("date",streamDate));

            andQuery.put("$and",querries);
            findRecords=fetchCollection.find(andQuery);
            System.out.println();
            System.out.println("Version- "+ distinctVersions.get(i)+":-");
            while(findRecords.hasNext())
            {
                System.out.println(findRecords.next());
            }
        }
        System.out.println();

        System.out.println("3. Find metrics based on Model and current Date");
        for (int i=0 ;i<distinctModels.size();i++)
        {
            querries= new ArrayList<>();
            andQuery=new BasicDBObject();

            querries.add(new BasicDBObject("model",distinctModels.get(i)));
            querries.add(new BasicDBObject("date",streamDate));

            andQuery.put("$and",querries);
            findRecords=fetchCollection.find(andQuery);
            System.out.println();
            System.out.println("Model- "+ distinctModels.get(i)+":-");
            while(findRecords.hasNext())
            {
                System.out.println(findRecords.next());
            }
        }
        System.out.println();

        System.out.println("4. Find metrics based on Date");
        for (int i=0 ;i<distinctDates.size();i++)
        {
            BasicDBObject query = new BasicDBObject();
            query.put("date",distinctDates.get(i));
            findRecords=fetchCollection.find(query);

            System.out.println();
            System.out.println("Date- "+ distinctDates.get(i)+":-");
            while(findRecords.hasNext())
            {
                System.out.println(findRecords.next());
            }
        }

    }        
    
}
