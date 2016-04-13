package com.meidusa.venus.io.asm.test;

import java.util.List;
import java.util.Map;

import com.meidusa.fastbson.ASMSerializerFactory;
import com.meidusa.fastbson.parse.BSONScanner;
import com.meidusa.fastbson.parse.BSONWriter;
import com.meidusa.fastbson.serializer.AbstractObjectSerializer;
import com.meidusa.fastbson.serializer.ObjectSerializer;
import com.meidusa.fastbson.util.BSON;

public class HelloDeserializer extends AbstractObjectSerializer implements ObjectSerializer {

    public byte name_prefix[];
    public byte greeting_prefix[];
    public byte age_prefix[];
    public byte ppp_prefix[];
    public byte cost_prefix[];
    public byte map_prefix[];
    public byte list_prefix[];
    public byte today_prefix[];
    public byte numbers_prefix[];
    public byte innerMap_prefix[];
    public byte inner_prefix[];
    public ObjectSerializer map_serializer;
    public ObjectSerializer map_sub_serializer[];
    public ObjectSerializer list_serializer;
    public ObjectSerializer list_sub_serializer[];
    public ObjectSerializer numbers_serializer;
    public ObjectSerializer numbers_sub_serializer[];
    public ObjectSerializer innerMap_serializer;
    public ObjectSerializer innerMap_sub_serializer[];
    public ObjectSerializer inner_serializer;

    public HelloDeserializer() {
        numbers_prefix = new byte["numbers".length() + 2];
        numbers_prefix[0] = BSON.ARRAY;
        System.arraycopy("numbers".getBytes(), 0, numbers_prefix, 1, "numbers".length());
        numbers_serializer = ASMSerializerFactory.getSerializer(int[].class);
        numbers_sub_serializer = new ObjectSerializer[1];
        numbers_sub_serializer[0] = ASMSerializerFactory.getSerializer(int.class);
        list_prefix = new byte["list".length() + 2];
        list_prefix[0] = BSON.ARRAY;
        System.arraycopy("list".getBytes(), 0, list_prefix, 1, "list".length());
        list_serializer = ASMSerializerFactory.getSerializer(List.class);
        list_sub_serializer = new ObjectSerializer[1];
        list_sub_serializer[0] = ASMSerializerFactory.getSerializer(String.class);
        today_prefix = new byte["today".length() + 2];
        today_prefix[0] = BSON.DATE;
        System.arraycopy("today".getBytes(), 0, today_prefix, 1, "today".length());
        ppp_prefix = new byte["ppp".length() + 2];
        ppp_prefix[0] = BSON.NUMBER;
        System.arraycopy("ppp".getBytes(), 0, ppp_prefix, 1, "ppp".length());
        map_prefix = new byte["map".length() + 2];
        map_prefix[0] = BSON.OBJECT;
        System.arraycopy("map".getBytes(), 0, map_prefix, 1, "map".length());
        map_serializer = ASMSerializerFactory.getSerializer(Map.class);
        map_sub_serializer = new ObjectSerializer[1];
        map_sub_serializer[0] = ASMSerializerFactory.getSerializer(Long.class);
        age_prefix = new byte["age".length() + 2];
        age_prefix[0] = BSON.NUMBER_INT;
        System.arraycopy("age".getBytes(), 0, age_prefix, 1, "age".length());
        cost_prefix = new byte["cost".length() + 2];
        cost_prefix[0] = BSON.NUMBER;
        System.arraycopy("cost".getBytes(), 0, cost_prefix, 1, "cost".length());
        greeting_prefix = new byte["greeting".length() + 2];
        greeting_prefix[0] = BSON.STRING;
        System.arraycopy("greeting".getBytes(), 0, greeting_prefix, 1, "greeting".length());
        name_prefix = new byte["name".length() + 2];
        name_prefix[0] = BSON.STRING;
        System.arraycopy("name".getBytes(), 0, name_prefix, 1, "name".length());
        ;

    }

    @SuppressWarnings("unchecked")
    public Object deserialize(BSONScanner scanner, ObjectSerializer[] subSerializer, int i) {
        scanner.skip(4); // length should not be included
        Hello hello = new Hello();
        boolean numbers_set = false;
        boolean list_set = false;
        boolean today_set = false;
        boolean ppp_set = false;
        boolean map_set = false;
        boolean age_set = false;
        boolean cost_set = false;
        boolean greeting_set = false;
        boolean name_set = false;
        boolean innerMap_set = false;
        boolean inner_set = false;

        while (scanner.readType() != BSON.EOO) {

            if (!numbers_set && scanner.match(numbers_prefix)) {
                hello.setNumbers((int[]) numbers_serializer.deserialize(scanner, null, 0));
                numbers_set = true;
                continue;
            }

            if (!list_set && scanner.match(list_prefix)) {
                hello.setList((List) list_serializer.deserialize(scanner, list_sub_serializer, 0));
                list_set = true;
                continue;
            }

            if (!today_set && scanner.match(today_prefix)) {
                hello.setToday(scanner.readDate());
                today_set = true;
                continue;
            }

            if (!ppp_set && scanner.match(ppp_prefix)) {
                hello.setPpp((float) scanner.readDouble());
                ppp_set = true;
                continue;
            }

            if (!map_set && scanner.match(map_prefix)) {
                hello.setMap((Map<String, Long>) map_serializer.deserialize(scanner, map_sub_serializer, 0));
                map_set = true;
                continue;
            }

            if (!age_set && scanner.match(age_prefix)) {
                hello.setAge(scanner.readInt());
                age_set = true;
                continue;
            }

            if (!cost_set && scanner.match(cost_prefix)) {
                hello.setCost(scanner.readDouble());

                cost_set = true;
                continue;
            }

            if (!greeting_set && scanner.match(greeting_prefix)) {
                hello.setGreeting(scanner.readBSONString());
                greeting_set = true;
                continue;
            }

            if (!name_set && scanner.match(name_prefix)) {
                hello.setName(scanner.readBSONString());
                name_set = true;
                continue;
            }
            if (!innerMap_set && scanner.match(innerMap_prefix)) {
                hello.setInnerMap((Map) innerMap_serializer.deserialize(scanner, innerMap_sub_serializer, 0));
                name_set = true;
                continue;
            }
            if (!inner_set && scanner.match(inner_prefix)) {
                hello.setInner((Inner) inner_serializer.deserialize(scanner, null, 0));
                name_set = true;
                continue;
            }

        }
        scanner.skip(1);
        return hello;
    }

    public void serialize(BSONWriter bsonwriter, Object value, ObjectSerializer[] subSerializer, int i) {
        Hello hello = (Hello) value;
        bsonwriter.begin();
        if (hello.getName() != null) {
            bsonwriter.writeBytes(name_prefix);
            bsonwriter.writeValue(hello.getName());
        }
        if (hello.getGreeting() != null) {
            bsonwriter.writeBytes(greeting_prefix);
            bsonwriter.writeValue(hello.getGreeting());
        }
        bsonwriter.writeBytes(age_prefix);
        bsonwriter.writeValue(hello.getAge());
        bsonwriter.writeBytes(ppp_prefix);
        bsonwriter.writeValue(hello.getPpp());
        if (hello.getCost() != null) {
            bsonwriter.writeBytes(cost_prefix);
            bsonwriter.writeValue(hello.getCost());
        }
        if (hello.getMap() != null) {
            bsonwriter.writeBytes(map_prefix);
            map_serializer.serialize(bsonwriter, hello.getMap(), map_sub_serializer, 0);
        }
        if (hello.getList() != null) {
            bsonwriter.writeBytes(list_prefix);
            list_serializer.serialize(bsonwriter, hello.getList(), list_sub_serializer, 0);
        }
        if (hello.getToday() != null) {
            bsonwriter.writeBytes(today_prefix);
            bsonwriter.writeValue(hello.getToday());
        }
        if (hello.getNumbers() != null) {
            bsonwriter.writeBytes(numbers_prefix);
            numbers_serializer.serialize(bsonwriter, hello.getNumbers(), numbers_sub_serializer, 0);
        }
        if (hello.getInnerMap() != null) {
            bsonwriter.writeBytes(innerMap_prefix);
            innerMap_serializer.serialize(bsonwriter, hello.getInnerMap(), innerMap_sub_serializer, 0);
        }
        if (hello.getInner() != null) {
            bsonwriter.writeBytes(inner_prefix);
            inner_serializer.serialize(bsonwriter, hello.getInner(), null, 0);
        }
        bsonwriter.end();
    }

    // byte[] numbers_prefix;
    // byte[] list_prefix;
    // byte[] today_prefix;
    // byte[] ppp_prefix;
    // byte[] map_prefix;
    // byte[] age_prefix;
    // byte[] cost_prefix;
    // byte[] greeting_prefix;
    // byte[] name_prefix;
    public Class<?> getSerializedClass() {
        return Hello.class;
    }

}
