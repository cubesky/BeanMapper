package party.liyin.beanmapper;

public class BeanMapperException extends Exception {
    public BeanMapperException(Exception e) {
        this.addSuppressed(e);
    }
}
