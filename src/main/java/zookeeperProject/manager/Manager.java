package zookeeperProject.manager;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class Manager 
{
    public static void main( String[] args )
    {
        ZooConnection connection = new ZooConnection();
        try {
			connection.connect("localhost");
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
