package topicmanager;

import apiREST.apiREST_Message;
import apiREST.apiREST_Publisher;
import apiREST.apiREST_Subscriber;
import apiREST.apiREST_Topic;
import entity.Message;
import util.Subscription_check;
import entity.Topic;
import util.Topic_check;
import entity.User;
import java.util.List;
import publisher.Publisher;
import publisher.PublisherStub;
import subscriber.Subscriber;
import webSocketService.WebSocketClient;

public class TopicManagerStub implements TopicManager 
{

  public User user;

  public TopicManagerStub(User user) 
  {
    WebSocketClient.newInstance();
    this.user = user;
  }

  public void close() 
  {
    WebSocketClient.close();
  }

  @Override
  public Publisher addPublisherToTopic(Topic topic) 
  {
    Publisher p = new PublisherStub(topic);
    entity.Publisher ep = new entity.Publisher();
    ep.setTopic(topic);
    ep.setUser(user);
    apiREST_Publisher.createPublisher(ep);
    return p;
  }

  @Override
  public void removePublisherFromTopic(Topic topic) 
  {
    entity.Publisher ep = new entity.Publisher();
    ep.setId(user.getId());
    ep.setTopic(topic);
    ep.setUser(user);
    apiREST_Publisher.deletePublisher(ep);
  }

  @Override
  public Topic_check isTopic(Topic topic) 
  {
    return apiREST_Topic.isTopic(topic);
  }

  @Override
  public List<Topic> topics() 
  {
    return apiREST_Topic.allTopics();
  }

  @Override
  public Subscription_check subscribe(Topic topic, Subscriber subscriber) 
  {
    if (apiREST_Topic.isTopic(topic).isOpen)
    {
        entity.Subscriber s = new entity.Subscriber();
        s.setUser(user);
        s.setTopic(topic);
        apiREST_Subscriber.createSubscriber(s);
        WebSocketClient.addSubscriber(topic, subscriber);
        return new Subscription_check(topic,Subscription_check.Result.OKAY);
    }
    else
        return new Subscription_check(topic,Subscription_check.Result.NO_TOPIC);  
  }

  @Override
  public Subscription_check unsubscribe(Topic topic, Subscriber subscriber) 
  {
    entity.Subscriber s = new entity.Subscriber();
    s.setUser(user);
    s.setTopic(topic);
    WebSocketClient.removeSubscriber(topic);
    return apiREST_Subscriber.deleteSubscriber(s);
  }

  @Override
  public Publisher publisherOf() 
  {
    entity.Publisher ep = apiREST_Publisher.PublisherOf(user);
    if (ep != null)
    {
        return new PublisherStub(ep.getTopic());
    }
    else
        return null;
    
  }

  @Override
  public List<entity.Subscriber> mySubscriptions() 
  {
    return apiREST_Subscriber.mySubscriptions(user);
  }

  @Override
  public List<Message> messagesFrom(Topic topic) 
  {
    return apiREST_Message.messagesFromTopic(topic);
  }

}
