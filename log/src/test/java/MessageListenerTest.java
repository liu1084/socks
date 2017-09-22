import org.apache.activemq.command.ActiveMQObjectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.LoggingEvent;
import org.testng.annotations.Test;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * Created by jim on 2017/9/22.
 * This class is ...
 */

@Test
public class MessageListenerTest{

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageListenerTest.class);


    @Test
    public void outputLog(){
        int i = 0;
        while (i < 10000){
            try {
                LOGGER.debug("test " + i);
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class MyMessageListener implements MessageListener{

        @Override
        public void onMessage(Message message) {
            LoggingEvent event = null;
            try {
                event = (LoggingEvent)((ActiveMQObjectMessage)message).getObject();
                System.out.println("Received log [" + event.getLevel() + "]: "+ event.getMessage());
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
