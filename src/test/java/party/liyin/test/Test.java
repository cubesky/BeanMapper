package party.liyin.test;

import org.springframework.beans.BeanUtils;
import party.liyin.beanmapper.BeanMapper;
import party.liyin.beanmapper.BeanMapperException;
import party.liyin.beanmapper.StaticBeanMapper;

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
            use(target.toString());
        }
        System.out.println("BeanMapper.copy took " + (System.currentTimeMillis() - startTime) + " ms.");

        startTime = System.currentTimeMillis();
        {
            TargetClass target = new TargetClass();
            StaticBeanMapper.copy(base, target);
            use(target.toString());
        }
        System.out.println("StaticBeanMapper.copy Compile took " + (System.currentTimeMillis() - startTime) + " ms.");

        startTime = System.currentTimeMillis();
        for (int i = 0; i <= 10000000; i++) {
            TargetClass target = new TargetClass();
            StaticBeanMapper.copy(base, target);
            use(target.toString());
        }
        System.out.println("StaticBeanMapper.copy took " + (System.currentTimeMillis() - startTime) + " ms.");

        startTime = System.currentTimeMillis();
        for (int i = 0; i <= 10000000; i++) {
            TargetClass target = new TargetClass();
            BeanUtils.copyProperties(base, target);
            use(target.toString());
        }
        System.out.println("BeanUtils.copyProperties took " + (System.currentTimeMillis() - startTime) + " ms.");

        startTime = System.currentTimeMillis();
        for (int i = 0; i <= 10000000; i++) {
            TargetClass target = new TargetClass();
            target.setOne(base.getOne());
            target.setTwo(base.getTwo());
            use(target.toString());
        }
        System.out.println("Bean Code Setter took " + (System.currentTimeMillis() - startTime) + " ms.");

    }

    private void use(String e) {}
}
