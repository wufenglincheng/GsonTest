import com.google.gson.*;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.Excluder;
import com.google.gson.internal.bind.MyReflectiveTypeAdapterFactory;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GsonTest {
    private static final String JSON = "{\"id\":1," +
            "\"name\":\"liuting\"," +
            "\"number\":1000000000," +
            "\"isFast\":true," +
            "\"user\":{" +
            "\"userName\":\"liutingdasheng\"," +
            "\"userId\":123456," +
            "\"avatar\":\"http://www.pic.com/avatar.png\"" +
            "}}";

    private static final String NULL_JSON = "{\"id\":\"\"," +
            "\"name\":\"\"," +
            "\"number\":\"\"," +
            "\"isFast\":\"\"," +
            "\"user\":\"\"" +
            "}";

    public static void main(String args[]) {
        TestInfo info = buildGson().fromJson(JSON, TestInfo.class);
        System.out.println(new Gson().toJson(info));
    }

    public static Gson buildGson() {
        //这里构造Gson，注册自定义的int和long的解析器
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(TypeAdapters.newFactory(int.class, Integer.class, INTEGER))
                .registerTypeAdapterFactory(TypeAdapters.newFactory(long.class, Long.class, LONG))
                .create();
        //这里通过反射的方法把自定义的MyReflectiveTypeAdapterFactory替换进去
        try {
            Field field = gson.getClass().getDeclaredField("constructorConstructor");
            field.setAccessible(true);
            ConstructorConstructor constructorConstructor = (ConstructorConstructor) field.get(gson);
            Field factories = gson.getClass().getDeclaredField("factories");
            factories.setAccessible(true);
            List<TypeAdapterFactory> data = (List<TypeAdapterFactory>) factories.get(gson);
            List<TypeAdapterFactory> newData = new ArrayList<>(data);
            newData.remove(data.size() - 1);
            newData.add(new MyReflectiveTypeAdapterFactory(constructorConstructor, FieldNamingPolicy.IDENTITY, Excluder.DEFAULT));
            newData = Collections.unmodifiableList(newData);
            factories.set(gson, newData);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return gson;
    }


    private static TypeAdapter<Number> INTEGER = new TypeAdapter<Number>() {
        @Override
        public Number read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            try {
                return in.nextInt();
            } catch (NumberFormatException e) {
                in.nextString();
                return 0;
            }
        }

        @Override
        public void write(JsonWriter out, Number value) throws IOException {
            out.value(value);
        }
    };
    private static TypeAdapter<Number> LONG = new TypeAdapter<Number>() {
        @Override
        public Number read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            try {
                return in.nextLong();
            } catch (Exception e) {
                in.nextString();
            }
            return 0;
        }

        @Override
        public void write(JsonWriter out, Number value) throws IOException {
            out.value(value);

        }
    };


}
