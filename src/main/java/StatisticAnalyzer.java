import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatisticAnalyzer {
    static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMM");
    static DateTimeFormatter dateFormatForFile = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    static DecimalFormat decimalFormat = new DecimalFormat("#######.##",  new DecimalFormatSymbols(Locale.ENGLISH));
    public static void main(String[] args) throws InterruptedException {

        YearMonth startDate = YearMonth.parse(Configuration.STARTDATE,dateFormat);
        YearMonth endDate = YearMonth.parse(dateFormat.format(LocalDateTime.now()),dateFormat);

        ExecutorService executorService = Executors.newFixedThreadPool(Configuration.MAX_PARALLEL_THREADS);

        //listOfFuture we will store all our Callable tasks in this list. Then we will get results of execution.
        List<Future<JSONArray>> listOfFuture = new ArrayList<>();
        List<StatMonthEntity> listOfStatEntities = new LinkedList<>();

        while(startDate.isBefore(endDate)){
            String stringDate = startDate.format(dateFormat);
            listOfFuture.add(executorService.submit(
                    () -> JSONReader.readJsonArrayFromUrl(Configuration.URL + stringDate + "&json")));
            startDate = startDate.plusMonths(1);

        }

        executorService.shutdown();
        executorService.awaitTermination(1,TimeUnit.MINUTES);

        //Put all results of executions into one list of jsonArray
        for(Future<JSONArray> future : listOfFuture){
            try {
                //We don't need the list of full json arrays.
                //So, we will extract needed json objects from arrays and put them into new list.

                List<JSONObject> listOfJSONArrays = JSONReader.convertToList((JSONArray) future.get());

                //Here we are leaving only required objects in the list.
                for(JSONObject obj : listOfJSONArrays){
                    if(obj.get("id_api").equals(Configuration.REQUIRED_API_ID)) {
                        listOfStatEntities.add(new StatMonthEntity(obj.get("dt").toString(), Double.parseDouble(obj.get("value").toString())));

                    }
                }
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }

        }

        //Calculate the difference between 2 months
        //Sorry, I don't know how to do it by using Stream API...
        double prevValue = 0.0;
        for (StatMonthEntity listOfStatEntity : listOfStatEntities) {
            listOfStatEntity.calculateDifference(prevValue);
            prevValue = listOfStatEntity.getValue();
        }
        SaveToFile(Configuration.FILE_NAME, listOfStatEntities);
    }

    public static void SaveToFile (String fileName, List<StatMonthEntity> listToFile){
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))){

            //File header
            writer.write("Month,Value,Difference" );
            writer.newLine();

            //File body
            listToFile.stream()
                    .sorted(Comparator.comparing(StatMonthEntity::getDate).reversed())
                    .forEach(e -> {
                        try {
                            writer.write(
                                    String.format(LocalDate.parse(e.getDate(),DateTimeFormatter.ofPattern("yyyyMMdd")).toString() , DateTimeFormatter.ISO_LOCAL_DATE) + ","
                                            + decimalFormat.format(e.getValue()) + ","
                                            + decimalFormat.format(e.getDifference())
                                            + System.lineSeparator()
                            );
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

}

