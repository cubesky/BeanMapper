# BeanMapper
a tiny speedy BeanUtils.copyProperties replacement.

[中文说明](README.zh.md)

## Why
Beacuse `copyProperties` in `BeanUtils` （Spring or Apache Commons） is achieved by java reflection feature, it has serious performance issue when it is called a lot.

BeanMapper using javassist library to achieve transform code on the fly, by directly generate bytecode, it can provide performance near pure getter - setter code after first "compiled".

## Limitation
 * Class must named as lower camel case JavaBean setter and getter.
 * Class must be public.

## Import
Prebuilt package is provide by CI, it will automatically release it to Maven Central, import this library from Maven Central is enough.

## Usage
BeanMapper library procide three BeanMapper to you.

### StaticBeanMapper
StaticBeanMapper will automatically transform data by calling it using source bean class and target bean class instance.

```java
StaticBeanMapper.copy(source, target);
```

StaticBeanMapper using HashMap and class full name to maintain a list, call the right transform method automatically. This will cause some performance issue, but it performace is much higher than using reflect.

### BeanMapper
BeanMapper will create and maintain transform class. You need an instance of this class to transform code. One BeanMapper is related to one Source - Target Bean class pair.

BeanMapper has constructor to generate transform class.
```java
BeanMapper mapper = new BeanMapper(Source.class, Target.class);
mapper.copy(source, target)
```

### BeanMapperInPlace
BeanMapperInPlace is a magic usage to add fields and method on the fly. You only need source class and the type which you want to add.
```java
BeanMapperInPlace vMapper = new BeanMapperInPlace(Source.class, Stream.of(new BeanMapperInPlace.TypePair("three", double.class)).collect(Collectors.toList()));
List<BeanMapperInPlace.DataPair> dataPairs = new ArrayList<>();
dataPairs.add(new BeanMapperInPlace.DataPair("three", 1.2));
Source target = vMapper.copy(base, dataPairs);
```
Then, you will get a return object cast to your original class type. It will contain these new field and data.

## Performance
In 10000000 times copy test.

BeanUtils.copyProperties took 9809ms

StaticBeanMapper.copy took 100ms (First Compile) + 3633ms (10000000 times copy test)

BeanMapper.copy took 98ms (First Compile) + 1641ms (10000000 times copy test)

manually calling getter and setter took 1479ms