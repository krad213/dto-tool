**Simple tool to generate DTO model from business-logic model or DB model, or any kind of classes containing valid getters**

Tool is used to create RAW copy of model, all excess data should be removed manually afterwards

Given immutable classes:
```java
public class TestClass {
    private Integer someNumber;
    private Float anotherNumber;
    private TestClass2 testClass2;

    public TestClass(Integer someNumber, Float anotherNumber, TestClass2 testClass2) {
        this.someNumber = someNumber;
        this.anotherNumber = anotherNumber;
        this.testClass2 = testClass2;
    }

    public Integer getSomeNumber() {
        return someNumber;
    }

    public Float getAnotherNumber() {
        return anotherNumber;
    }

    public TestClass2 getTestClass2() {
        return testClass2;
    }
}


public class TestClass2 {
    private String str;

    public TestClass2(String str) {
        this.str = str;
    }

    public String getStr() {
        return str;
    }
}
```

Fully serializable and json compatible copy will be generated containing default constructor and all getters and setters and ability to be copied from original class via method from():

```java
public class TestClassDto {
    private Integer someNumber;
    private Float anotherNumber;
    private TestClass2Dto testClass2;

    public static TestClassDto from(TestClass source) {
        if (source == null) {
            return null;
        }
        TestClassDto dto = new TestClassDto();
        dto.setSomeNumber(source.getSomeNumber());
        dto.setAnotherNumber(source.getAnotherNumber());
        dto.setTestClass2(TestClass2Dto.from(source.getTestClass2()));
        return dto;
    }

    public Integer getSomeNumber() {
        return someNumber;
    }

    public void setSomeNumber(Integer someNumber) {
        this.someNumber = someNumber;
    }

    public Float getAnotherNumber() {
        return anotherNumber;
    }

    public void setAnotherNumber(Float anotherNumber) {
        this.anotherNumber = anotherNumber;
    }

    public TestClass2Dto getTestClass2() {
        return testClass2;
    }

    public void setTestClass2(TestClass2Dto testClass2) {
        this.testClass2 = testClass2;
    }
}



public class TestClass2Dto {
    private String str;
    
    public static TestClass2Dto from(TestClass2 source) {
        if (source == null) {
            return null;
        }
        TestClass2Dto dto = new TestClass2Dto();
        dto.setStr(source.getStr());
        return dto;
    }
    
    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }
}
```
**Building:**

run

./gradlew build

executable jar will be placed to build/libs directory

**Usage:**

java -jar dto-tool.jar config.json

if "config" argument is not set "config.json" file will be used

**Configuration:**
```json
{
  "jarLocations": [
    "jarToLoadClassesFrom.jar"
  ],
  "entityPackages": [
    "packages.where.to.search.for.dependent.classes"
  ],
  "classNames": [
    "classes.to.create.dto.From"
  ],
  "targetPackage": "pack.age.for.generated.dtos",
  "targetDirectory": "generated"
}
```
jarLocations - Array containing paths to jar(s) with source model

entityPackages - Array Dependent classes will be searched only in this packages if class is outside of this array, original class will be used in DTO
 
classNames - Array of classes, should be used as "entry-point", DTOs will be created for given classes and all dependent classes

targetPackage - all created DTOs will be placed to this package

targetDirectory - output directory for generated DTOs

 
