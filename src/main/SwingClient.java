package main;

import entity.Message;
import util.Subscription_check;
import entity.Topic;
import subscriber.SubscriberImpl;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import publisher.Publisher;
import subscriber.Subscriber;
import topicmanager.TopicManager;
import topicmanager.TopicManagerStub;
import util.Topic_check;
import webSocketService.WebSocketClient;

public class SwingClient 
{

  TopicManager topicManager;
  public Map<Topic, Subscriber> my_subscriptions;
  Publisher publisher;
  Topic publisherTopic;
  Subscriber subscriber;

  JFrame frame;
  JTextArea topic_list_TextArea;
  public JTextArea messages_TextArea;
  public JTextArea my_subscriptions_TextArea;
  JTextArea publisher_TextArea;
  JTextField argument_TextField;

  public SwingClient(TopicManager topicManager) 
  {
    this.topicManager = topicManager;
    my_subscriptions = new HashMap<Topic, Subscriber>();
    publisher = null;
    publisherTopic = null;
  }

  public void createAndShowGUI() 
  {

    String login = ((TopicManagerStub) topicManager).user.getLogin();
    frame = new JFrame("Publisher/Subscriber demo, user : " + login);
    frame.setSize(300, 300);
    frame.addWindowListener(new CloseWindowHandler());

    topic_list_TextArea = new JTextArea(5, 10);
    messages_TextArea = new JTextArea(10, 20);
    my_subscriptions_TextArea = new JTextArea(5, 10);
    publisher_TextArea = new JTextArea(1, 10);
    argument_TextField = new JTextField(20);
    subscriber = new SubscriberImpl(SwingClient.this);


    JButton show_topics_button = new JButton("show Topics");
    JButton new_publisher_button = new JButton("new Publisher");
    JButton new_subscriber_button = new JButton("new Subscriber");
    JButton to_unsubscribe_button = new JButton("to unsubscribe");
    JButton to_post_an_event_button = new JButton("post an event");
    JButton to_close_the_app = new JButton("close app.");

    show_topics_button.addActionListener(new showTopicsHandler());
    new_publisher_button.addActionListener(new newPublisherHandler());
    new_subscriber_button.addActionListener(new newSubscriberHandler());
    to_unsubscribe_button.addActionListener(new UnsubscribeHandler());
    to_post_an_event_button.addActionListener(new postEventHandler());
    to_close_the_app.addActionListener(new CloseAppHandler());

    JPanel buttonsPannel = new JPanel(new FlowLayout());
    buttonsPannel.add(show_topics_button);
    buttonsPannel.add(new_publisher_button);
    buttonsPannel.add(new_subscriber_button);
    buttonsPannel.add(to_unsubscribe_button);
    buttonsPannel.add(to_post_an_event_button);
    buttonsPannel.add(to_close_the_app);

    JPanel argumentP = new JPanel(new FlowLayout());
    argumentP.add(new JLabel("Write content to set a new_publisher / new_subscriber / unsubscribe / post_event:"));
    argumentP.add(argument_TextField);

    JPanel topicsP = new JPanel();
    topicsP.setLayout(new BoxLayout(topicsP, BoxLayout.PAGE_AXIS));
    topicsP.add(new JLabel("Topics:"));
    topicsP.add(topic_list_TextArea);
    topicsP.add(new JScrollPane(topic_list_TextArea));
    topicsP.add(new JLabel("My Subscriptions:"));
    topicsP.add(my_subscriptions_TextArea);
    topicsP.add(new JScrollPane(my_subscriptions_TextArea));
    topicsP.add(new JLabel("I'm Publisher of topic:"));
    topicsP.add(publisher_TextArea);
    topicsP.add(new JScrollPane(publisher_TextArea));

    JPanel messagesPanel = new JPanel();
    messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.PAGE_AXIS));
    messagesPanel.add(new JLabel("Messages:"));
    messagesPanel.add(messages_TextArea);
    messagesPanel.add(new JScrollPane(messages_TextArea));

    Container mainPanel = frame.getContentPane();
    mainPanel.add(buttonsPannel, BorderLayout.PAGE_START);
    mainPanel.add(messagesPanel, BorderLayout.CENTER);
    mainPanel.add(argumentP, BorderLayout.PAGE_END);
    mainPanel.add(topicsP, BorderLayout.LINE_START);

    //this is where you restore the user profile:
    clientSetup();

    frame.pack();
    frame.setVisible(true);
  }

  // To rewrite current subscriptions in my_subscriptions_TextArea
  public void updateSubscriptions()
  {
    my_subscriptions_TextArea.setText("");
    for(Topic st : my_subscriptions.keySet())
    {
        my_subscriptions_TextArea.append(st.name + "\n");
    }
  }
  
  // To delete subscriptions to closed Topics
  public void refreshSubscriptions()
  {
    java.util.List<Topic> tl = new ArrayList<Topic>();
    for (Topic t : my_subscriptions.keySet())
    {
        Topic_check tc = topicManager.isTopic(t);
        if (!tc.isOpen)
        {
            tl.add(t);
            messages_TextArea.append("You have been unsubscribed from " 
                    + t.name + " because the topic is no longer available.\n");
        }
    }
    
    for (Topic t : tl)
    {
        my_subscriptions.remove(t);            
    }
    
    updateSubscriptions();
  }
  
  private void clientSetup() 
  {
    //Retrieve if it is publisher from some topic and the topic itself
    publisher = topicManager.publisherOf();
    if (publisher != null)
    {
        publisherTopic = publisher.topic();
        publisher_TextArea.setText(publisherTopic.name);
        //Retrieve previous messages from topic where the user is publisher
        List<Message> ml = topicManager.messagesFrom(publisherTopic);
            if (ml != null)
            {
                for (Message m : ml)
                {
                    messages_TextArea.append(m.topic.name + ": " + m.content + "\n");
                }
            }
    }
      
    //Retrieve its subscriptions
    List<entity.Subscriber> s = topicManager.mySubscriptions();
    if (s != null)
    {
        for (entity.Subscriber es : s)
        {
            my_subscriptions.put(es.getTopic(), subscriber);
            //Retrieve previous messages from topics where the user is subscribed
            List<Message> ml = topicManager.messagesFrom(es.getTopic());
            if (ml != null)
            {
                for (Message m : ml)
                {
                    messages_TextArea.append(m.topic.name + ": " + m.content + "\n");
                }
            }
        }
    }
    updateSubscriptions();
      
  }

  class showTopicsHandler implements ActionListener 
  {

    public void actionPerformed(ActionEvent e) 
    {
        refreshSubscriptions();
        topic_list_TextArea.setText("");
        for (Topic t : topicManager.topics())
        {
            topic_list_TextArea.append(t.name + "\n");
        }
    }
  }

  class newPublisherHandler implements ActionListener 
  {

    public void actionPerformed(ActionEvent e) 
    {
        refreshSubscriptions();
        String text = argument_TextField.getText();
        if (!text.isEmpty())
        {
            // If the publisher is changing its topic we have to remove it
            if (publisherTopic != null && !publisherTopic.name.equals(text))
            {
                topicManager.removePublisherFromTopic(publisherTopic);
                Topic_check tc = topicManager.isTopic(publisherTopic);
                if (tc.isOpen == false)
                {
                    topicManager.unsubscribe(publisherTopic, subscriber);
                    my_subscriptions.remove(publisherTopic);
                    messages_TextArea.append("You have been unsubscribed from " + 
                            publisherTopic.name + " because the topic is no "
                                    + "longer available.\n");
                }
            }
            // Create the topic
            publisherTopic = new Topic(text);   

            publisher = topicManager.addPublisherToTopic(publisherTopic);

            // I assume that when you are a publisher from one topic you 
            // immediatelly get subscribe to it.
            topicManager.subscribe(publisherTopic,subscriber);
            my_subscriptions.put(publisherTopic, subscriber);
            updateSubscriptions();

            publisher_TextArea.setText(text);
            
            argument_TextField.setText("");
        }
    }
  }

  class newSubscriberHandler implements ActionListener 
  {

    public void actionPerformed(ActionEvent e) 
    {
        refreshSubscriptions();
        String text = argument_TextField.getText();
        if (!text.isEmpty())
        {
            Topic t = new Topic(text);
            Subscription_check sc = topicManager.subscribe(t,subscriber);

            if (sc.result == Subscription_check.Result.OKAY)
            {
                my_subscriptions.put(t, subscriber);
                updateSubscriptions();
                messages_TextArea.append("You have been subscribed to " + text +"\n");
            }
            else
                messages_TextArea.append("There is no topic called " + text + "\n");

            argument_TextField.setText("");
        }
    }
  }

  class UnsubscribeHandler implements ActionListener 
  {

    public void actionPerformed(ActionEvent e) 
    {
        refreshSubscriptions();
        String text = argument_TextField.getText();
        if (!text.isEmpty())
        {
            Topic t = new Topic(text);

            // Is the topic within the subscriptions of the user?
            if (my_subscriptions.containsKey(t))
            {
                my_subscriptions.remove(t);
                topicManager.unsubscribe(t, subscriber);
                updateSubscriptions();
                messages_TextArea.append("You have been unsubscribed from " + text +"\n");
            }
            else
                messages_TextArea.append("You are not subscribed to " + text +"\n");

            argument_TextField.setText("");
        }
    }
  }

  class postEventHandler implements ActionListener 
  {

    public void actionPerformed(ActionEvent e) 
    {
        refreshSubscriptions();
        String text = argument_TextField.getText();
        if (!text.isEmpty())
        {
            // If you are publisher you can publish the message
            if(publisher != null)
            {
                Message msg = new Message(publisherTopic,text);
                publisher.publish(msg);
            }
            argument_TextField.setText("");
        }
    }
  }

  class CloseAppHandler implements ActionListener 
  {

    public void actionPerformed(ActionEvent e) 
    {
        System.out.println("one user closed");
        System.exit(0);
    }
  }

  class CloseWindowHandler implements WindowListener 
  {

    public void windowDeactivated(WindowEvent e) 
    {
    }

    public void windowActivated(WindowEvent e) 
    {
    }

    public void windowIconified(WindowEvent e) 
    {
    }

    public void windowDeiconified(WindowEvent e) 
    {
    }

    public void windowClosed(WindowEvent e) 
    {
    }

    public void windowOpened(WindowEvent e) 
    {
    }

    public void windowClosing(WindowEvent e) 
    {
        System.out.println("one user closed");
        System.exit(0);
    }
  }
}
