import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by huawei on 5/18/16.
 */
public class Test {

    public static void main(String[] args) {
        System.out.println(Integer.MAX_VALUE);
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext-venus-client.xml");
    }
}
