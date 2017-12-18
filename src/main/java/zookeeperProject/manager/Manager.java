package zookeeperProject.manager;

import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Hello world!
 *
 */
public class Manager 
{
	public static ZooConnection connection = new ZooConnection();
	public static ZooKeeper zk;

    public static void main( String[] args )
    {

		try {
			zk = connection.connect("localhost");
			try {
				WatcherConn.connectionLatch.await(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(zk.getState());
			connection.initTree();
			connection.checkEnroll();
			connection.checkOnline();
			connection.checkQuit();
			while(true) {
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
        
    }
}
