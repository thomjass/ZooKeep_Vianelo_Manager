package zookeeperProject.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.ZooKeeper;

public class ZooConnection {
		public static ZooKeeper zoo;
	   final CountDownLatch connectedSignal = new CountDownLatch(1);

	   // Method to connect zookeeper ensemble.
	   public ZooKeeper connect(String host) throws IOException,InterruptedException {	
	      zoo = new ZooKeeper(host,5000,new WatcherConn());
	      return zoo;
	   }
		
	   public static void create(String path, byte[] data) throws 
	      KeeperException,InterruptedException {
	      zoo.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE,
	      CreateMode.PERSISTENT);
		}
	
	   
	   public void initTree() {
		   String a = "1";
		   zoo.addAuthInfo("digest", "admin:admin".getBytes());
		   try {
			   create("/request",a.getBytes());
		   }catch(Exception e ) {
			   System.out.println(e.getMessage());
		   }
		   try {
			   create("/request/quit",a.getBytes());
		   }catch(Exception e ) {
			   System.out.println(e.getMessage());
		   }
		   try {
			   create("/request/enroll",a.getBytes());
		   }catch(Exception e ) {
			   System.out.println(e.getMessage());
		   }
		   try {
			   create("/registry",a.getBytes());
		   }catch(Exception e ) {
			   System.out.println(e.getMessage());
		   }
		   try {
			   create("/online",a.getBytes());
		   }catch(Exception e ) {
			   System.out.println(e.getMessage());
		   }
		   try {
			   create("/queue",a.getBytes());
		   }catch(Exception e ) {
			   System.out.println(e.getMessage());
		   }
		   try {
			   create("/backup",a.getBytes());
		   }catch(Exception e ) {
			   System.out.println(e.getMessage());
		   }
		   try {
			zoo.getChildren("/request/enroll", new WatcherMaster());
			zoo.getChildren("/request/quit", new WatcherMaster());
			zoo.getChildren("/online", new WatcherMaster());
		} catch (Exception e) {
			e.printStackTrace();
		} 
	   }

	   // Method to disconnect from zookeeper server
	   public void close() throws InterruptedException {
	      zoo.close();
	   }
	   public void checkEnroll() {
		   List<String> id_to_enroll=new ArrayList<String>();
		try {
				id_to_enroll = zoo.getChildren("/request/enroll", false);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				for(int i=0;i<id_to_enroll.size();i++) {
						
						try {
							zoo.create("/registry/"+id_to_enroll.get(i), "1".getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL, CreateMode.PERSISTENT);
							zoo.setData("/request/enroll/"+id_to_enroll.get(i), "1".getBytes(), -1);
							System.out.println(id_to_enroll.get(i)+" has been registered successfully");
						} catch (InterruptedException e) {
							try {
								zoo.setData("/request/enroll/"+id_to_enroll.get(i), "0".getBytes(), -1);
								System.out.println(id_to_enroll.get(i)+" error Connection");
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								System.out.println("LINE 49 WATCHER MASTER");
								e1.printStackTrace();
							}
							 
						} catch (KeeperException e) {
							try {
								 zoo.setData("/request/enroll/"+id_to_enroll.get(i), "2".getBytes(), -1);
								 System.out.println(id_to_enroll.get(i)+" is already registered");
								} catch (Exception e2) {
									try {

										zoo.setData("/request/enroll/"+id_to_enroll.get(i), "0".getBytes(), -1);
										System.out.println(id_to_enroll.get(i)+" error Connection");
									} catch (Exception e1) {
										e1.printStackTrace();
									} 
								}
						}
				}
				
				try {
					zoo.getChildren("/request/enroll", new WatcherMaster());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	   }
	   
	   public void checkQuit() {
		   List<String> id_to_quit = null;
		try {
			id_to_quit = ZooConnection.zoo.getChildren("/request/quit", false);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		for(int i=0; i<id_to_quit.size(); i++) {  
		   try {
				
				
					Stat version_stat = ZooConnection.zoo.exists("/registry/"+id_to_quit.get(i),false);
					if(version_stat!=null) {
						int version = version_stat.getVersion();
						try {
							ZooConnection.zoo.delete("/registry/"+id_to_quit.get(i), version);
							ZooConnection.zoo.setData("/request/quit/"+id_to_quit.get(i), "1".getBytes(), -1);
							System.out.println(id_to_quit.get(i) + " has been deleted successfully");
						} catch (InterruptedException e) {
							ZooConnection.zoo.setData("/request/quit/"+id_to_quit.get(i), "0".getBytes(), -1);
						}
					}else {
						ZooConnection.zoo.setData("/request/quit/"+id_to_quit.get(i), "2".getBytes(), -1);
						System.out.println(id_to_quit.get(i) + " doesn't exist in registry");
					}
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		   
			try {
				ZooConnection.zoo.getChildren("/request/quit", new WatcherMaster());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	   }
	   
	   public void checkOnline() {
			byte[] bdata;
			List<String> list_of_user = null;
			List<String> user_online = null;
			try {
				
				user_online = ZooConnection.zoo.getChildren("/online", false);
				list_of_user = ZooConnection.zoo.getChildren("/registry", false);
				for(int i = 0; i<user_online.size(); i++) {
					if(list_of_user.contains(user_online.get(i))) {
						try {
							System.out.println("Master: creation de "+ "/queue/"+user_online.get(i));
							ZooConnection.zoo.create("/queue/"+user_online.get(i), "-1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
							System.out.println("Master: setData de "+ "/online/"+user_online.get(i) +" à 1.");
							ZooConnection.zoo.setData("/online/"+user_online.get(i), "1".getBytes(), -1);
							//If we are here this is a new online user
							List<String> msg = new ArrayList<String>();
							try {
								System.out.println("Master: Getting all the children of "+"/backup/"+user_online.get(i));
								msg = ZooConnection.zoo.getChildren("/backup/"+user_online.get(i), false);
								System.out.println("Master: Liste des messages: "+ msg);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if(!msg.isEmpty()) {
								for(String str:msg) {
									try {
										System.out.println("Master: msg not empty so getData of "+"/backup/"+user_online.get(i)+"/"+str);
										bdata = ZooConnection.zoo.getData("/backup/"+user_online.get(i)+"/"+str, false ,null);
										ZooConnection.zoo.delete("/backup/"+user_online.get(i)+"/"+str, -1);
										System.out.println("Master: Création de " +"/queue/"+user_online.get(i)+"/msg");
										ZooConnection.zoo.create("/queue/"+user_online.get(i)+"/msg", bdata, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
								}
							}
							System.out.println("Master: pose watcher pouvant trigger NodeDeleted or NodeDataChanger sur "+"/online/"+user_online.get(i));
							ZooConnection.zoo.getData("/online/"+user_online.get(i), new WatcherMaster(), null);
							System.out.println(user_online.get(i) + " online.");
						}catch(Exception e) {
							e.printStackTrace();
							//System.out.println(user_online.get(i) + " already exists.");
						}
						
					}else {
						System.out.println("User not registred");
						ZooConnection.zoo.setData("/online/"+user_online.get(i), "2".getBytes(), -1);
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			try {
				ZooConnection.zoo.getChildren("/online", new WatcherMaster());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
	   }
	   
}
