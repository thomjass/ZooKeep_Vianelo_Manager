package zookeeperProject.manager;


import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import java.util.concurrent.CountDownLatch;

public class WatcherConn implements Watcher{
    public static CountDownLatch connectionLatch = new CountDownLatch(1);
   
    public void process(WatchedEvent arg0) {
        // TODO Auto-generated method stub
        if(arg0.getState() == KeeperState.SyncConnected){
             connectionLatch.countDown();
        }
       
    }
   

}