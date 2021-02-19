package party.liyin.test;

import org.springframework.beans.BeanUtils;
import party.liyin.beanmapper.BeanMapper;
import party.liyin.beanmapper.BeanMapperException;
import party.liyin.beanmapper.BeanMapperInPlace;
import party.liyin.beanmapper.StaticBeanMapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Test {

    public static class BaseClass {
        private int one;
        private String two;
        public BaseClass() {}
        public BaseClass(int one, String two) {
            this.one = one;
            this.two = two;
        }

        public int getOne() {
            return one;
        }

        public void setOne(int one) {
            this.one = one;
        }

        public String getTwo() {
            return two;
        }

        public void setTwo(String two) {
            this.two = two;
        }
    }

    public static class TargetClass extends BaseClass {
        private double three;
        public TargetClass() {}
        public TargetClass(int one, String two, double three) {
            super(one, two);
            this.three = three;
        }

        public double getThree() {
            return three;
        }

        public void setThree(double three) {
            this.three = three;
        }
    }

    @org.junit.Test
    public void mapperTest() throws BeanMapperException {
        BaseClass base = new BaseClass(1, "A");
        TargetClass target = new TargetClass();
        assert base.getOne() == 1;
        assert base.getTwo().equals("A");
        assert target.getOne() != 1;
        assert target.getTwo() == null;
        assert target.getThree() == 0.0;
        StaticBeanMapper.copy(base, target);
        target.setThree(7.2);
        assert base.getOne() == 1;
        assert base.getTwo().equals("A");
        assert target.getOne() == 1;
        assert target.getTwo().equals("A");
        assert target.getThree() == 7.2;
    }

    @org.junit.Test
    public void springTest() {
        BaseClass base = new BaseClass(1, "A");
        TargetClass target = new TargetClass();
        assert base.getOne() == 1;
        assert base.getTwo().equals("A");
        assert target.getOne() != 1;
        assert target.getTwo() == null;
        assert target.getThree() == 0.0;
        BeanUtils.copyProperties(base, target);
        target.setThree(7.2);
        assert base.getOne() == 1;
        assert base.getTwo().equals("A");
        assert target.getOne() == 1;
        assert target.getTwo().equals("A");
        assert target.getThree() == 7.2;
    }

    @org.junit.Test
    public void conflictTest() throws BeanMapperException {
        BaseClass base = new BaseClass(1, "A");
        TargetClass target = new TargetClass();
        BeanMapper mapper = new BeanMapper(BaseClass.class, TargetClass.class);
        BeanMapper mapperConflict = new BeanMapper(BaseClass.class, TargetClass.class);
        mapper.copy(base, target);
        mapperConflict.copy(base, target);
    }

    @org.junit.Test
    public void beanInPlaceTest() throws BeanMapperException {
        BaseClass base = new BaseClass(1, "A");
        ArrayList<BeanMapperInPlace.TypePair> typePairs = new ArrayList<>();
        typePairs.add(new BeanMapperInPlace.TypePair("test", int.class));
        BeanMapperInPlace virtual = new BeanMapperInPlace(BaseClass.class, typePairs);
        ArrayList<BeanMapperInPlace.DataPair> dataPairs = new ArrayList<>();
        dataPairs.add(new BeanMapperInPlace.DataPair("test", 10));
        BaseClass nbC = virtual.copy(base, dataPairs);
        System.out.println(nbC.getOne());
        System.out.println(nbC.getTwo());
        Arrays.stream(nbC.getClass().getDeclaredFields()).forEach(u -> {
            u.setAccessible(true);
            try {
                System.out.println(u.getName());
                System.out.println(u.get(nbC));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        ArrayList<BeanMapperInPlace.TypePair> typePairs2 = new ArrayList<>();
        typePairs2.add(new BeanMapperInPlace.TypePair("test", String.class));
        BeanMapperInPlace virtual2 = new BeanMapperInPlace(BaseClass.class, typePairs2);
        ArrayList<BeanMapperInPlace.DataPair> dataPairs2 = new ArrayList<>();
        dataPairs2.add(new BeanMapperInPlace.DataPair("test", "Hello"));
        BaseClass nbC2 = virtual2.copy(base, dataPairs2);
        System.out.println(nbC2.getOne());
        System.out.println(nbC2.getTwo());
        Arrays.stream(nbC2.getClass().getDeclaredFields()).forEach(u -> {
            u.setAccessible(true);
            try {
                System.out.println(u.getName());
                System.out.println(u.get(nbC2));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    @org.junit.Test
    public void timeTest() throws BeanMapperException {
        final BaseClass base = new BaseClass(1, "A");
        System.out.println("Test for 10000000 times bean copy.");
        long startTime = System.currentTimeMillis();
        BeanMapper mapper = new BeanMapper(BaseClass.class, TargetClass.class);
        System.out.println("BeanMapper.copy Compile took " + (System.currentTimeMillis() - startTime) + " ms.");

        startTime = System.currentTimeMillis();
        for (int i = 0; i <= 10000000; i++) {
            TargetClass target = new TargetClass();
            mapper.copy(base, target);
            target.setThree(1.2);
            use(target.toString());
        }
        System.out.println("BeanMapper.copy took " + (System.currentTimeMillis() - startTime) + " ms.");

        startTime = System.currentTimeMillis();
        {
            TargetClass target = new TargetClass();
            StaticBeanMapper.copy(base, target);
            target.setThree(1.2);
            use(target.toString());
        }
        System.out.println("StaticBeanMapper.copy Compile took " + (System.currentTimeMillis() - startTime) + " ms.");

        startTime = System.currentTimeMillis();
        for (int i = 0; i <= 10000000; i++) {
            TargetClass target = new TargetClass();
            StaticBeanMapper.copy(base, target);
            target.setThree(1.2);
            use(target.toString());
        }
        System.out.println("StaticBeanMapper.copy took " + (System.currentTimeMillis() - startTime) + " ms.");

        startTime = System.currentTimeMillis();
        for (int i = 0; i <= 10000000; i++) {
            TargetClass target = new TargetClass();
            BeanUtils.copyProperties(base, target);
            target.setThree(1.2);
            use(target.toString());
        }
        System.out.println("BeanUtils.copyProperties took " + (System.currentTimeMillis() - startTime) + " ms.");

        startTime = System.currentTimeMillis();
        BeanMapperInPlace vMapper = new BeanMapperInPlace(BaseClass.class, Stream.of(new BeanMapperInPlace.TypePair("three", double.class)).collect(Collectors.toList()));
        System.out.println("BeanMapperInPlace compile took " + (System.currentTimeMillis() - startTime) + " ms.");
        startTime = System.currentTimeMillis();
        for (int i = 0; i <= 10000000; i++) {
            List<BeanMapperInPlace.DataPair> dataPairs = new ArrayList<>();
            dataPairs.add(new BeanMapperInPlace.DataPair("three", 1.2));
            BaseClass target = vMapper.copy(base, dataPairs);
            use(target.toString());
        }
        System.out.println("BeanMapperInPlace took " + (System.currentTimeMillis() - startTime) + " ms.");

        startTime = System.currentTimeMillis();
        for (int i = 0; i <= 10000000; i++) {
            TargetClass target = new TargetClass();
            target.setOne(base.getOne());
            target.setTwo(base.getTwo());
            target.setThree(1.2);
            use(target.toString());
        }
        System.out.println("Bean Code Setter took " + (System.currentTimeMillis() - startTime) + " ms.");

    }

    private void use(String e) {}
}
