package p002;

import com.google.gson.Gson;

class Person {
    String name;
    int age;
    boolean employed;

    public Person(String name, int age, boolean employed) {
        this.name = name;
        this.age = age;
        this.employed = employed;
    }
}

public class Main {
    public static void main(String[] args) {
        Person person = new Person("Sweet", 50, true);

        Gson gson = new Gson();
        String json = gson.toJson(person);

        System.out.println("Serialized JSON:");
        System.out.println(json);
    }
}
