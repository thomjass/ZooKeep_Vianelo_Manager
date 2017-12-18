package zookeeperProject.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.Watcher.Event.EventType;


import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class WatcherMaster implements Watcher {
	List<String> id_to_enroll;
	List<String> listChildren;

	public void process(WatchedEvent event) {
		/*
		try {
			TimeUnit.MILLISECONDS.sleep(300);
		} catch (InterruptedException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		*/
		if (event.getType() == Event.EventType.None) {
            // We are are being told that the state of the
            // connection has changed
            switch (event.getState()) {
            case SyncConnected:
                // In this particular example we don't need to do anything
                // here - watches are automatically re-registered with 
                // server and any watches triggered while the client was 
                // disconnected will be delivered (in order of course)
            	System.out.println("New client connected");
                break;
            case Expired:
                // It's all over
            	System.out.println("Client deconnected.");
                break;
            }
        }
		// TODO Auto-generated method stub
		if (event.getType() == EventType.NodeCreated) {
			System.out.println("Noeud créé: "+event.getPath());
		}
		else if (event.getType() == EventType.NodeDeleted) {
			System.out.println(event.getPath()+" deleted");
			if(event.getPath().contains("/request/enroll")) {
				try {
					ZooConnection.zoo.getChildren("/request/enroll", new WatcherMaster());
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}else if(event.getPath().contains("/request/quit")) {
				try {
					ZooConnection.zoo.getChildren("/request/quit", new WatcherMaster());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else if(event.getPath().contains("/online/")) {
				List<String> msg = new ArrayList<String>();
				String id_to_delete = event.getPath().split("/")[2];
				System.out.println("id_to_delete: "+id_to_delete);
				try {
					msg = ZooConnection.zoo.getChildren("/queue/"+id_to_delete, false);
					System.out.println("Size of msg:" +msg.size());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(!msg.isEmpty()) {
					try {
						ZooConnection.zoo.create("/backup/"+id_to_delete,"1".getBytes(),ZooDefs.Ids.CREATOR_ALL_ACL, CreateMode.PERSISTENT);
						System.out.println("Backup créé");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					for(String str:msg) {
						byte[] bdata;
						try {
							bdata = ZooConnection.zoo.getData("/queue/"+id_to_delete+"/"+str, null,null);
							ZooConnection.zoo.create("/backup/"+id_to_delete+"/msg", bdata, ZooDefs.Ids.CREATOR_ALL_ACL, CreateMode.PERSISTENT_SEQUENTIAL);
							ZooConnection.zoo.delete("/queue/"+id_to_delete+"/"+str, -1);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
							ZooConnection.zoo.delete("/queue/"+id_to_delete, -1);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 

					}
					
				}else {
					try {
						ZooConnection.zoo.delete("/queue/"+id_to_delete,-1);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
				}
				
			}
		}
		else if (event.getType() == EventType.NodeChildrenChanged) {
			System.out.println("Master: " + event.getPath() +" NodeChildrenChange triggered");
			if(event.getPath().equals("/request/enroll")) {
				try {
					id_to_enroll = ZooConnection.zoo.getChildren("/request/enroll", false);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					for(int i=0;i<id_to_enroll.size();i++) {
							
							try {
								ZooConnection.zoo.create("/registry/"+id_to_enroll.get(i), "1".getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL, CreateMode.PERSISTENT);
								ZooConnection.zoo.setData("/request/enroll/"+id_to_enroll.get(i), "1".getBytes(), -1);
								System.out.println(id_to_enroll.get(i)+" has been registered successfully");
							} catch (InterruptedException e) {
								try {
									ZooConnection.zoo.setData("/request/enroll/"+id_to_enroll.get(i), "0".getBytes(), -1);
									System.out.println(id_to_enroll.get(i)+" error Connection");
								} catch (Exception e1) {
									// TODO Auto-generated catch block
									System.out.println("LINE 49 WATCHER MASTER");
									e1.printStackTrace();
								}
								 
							} catch (KeeperException e) {
								try {
									e.printStackTrace();
									 ZooConnection.zoo.setData("/request/enroll/"+id_to_enroll.get(i), "2".getBytes(), -1);
									 System.out.println(id_to_enroll.get(i)+" is already registered");
									} catch (Exception e2) {
										try {

											ZooConnection.zoo.setData("/request/enroll/"+id_to_enroll.get(i), "0".getBytes(), -1);
											System.out.println(id_to_enroll.get(i)+" error Connection");
										} catch (Exception e1) {
											e1.printStackTrace();
										} 
									}
							}
					}
					
					try {
						ZooConnection.zoo.getChildren("/request/enroll", new WatcherMaster());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}else if(event.getPath().equals("/request/quit")) {
				
				try {
					List<String> id_to_quit = ZooConnection.zoo.getChildren(event.getPath(), false);
					Stat version_stat = ZooConnection.zoo.exists("/registry/"+id_to_quit.get(0),false);
					
					if(version_stat!=null) {
						int version = version_stat.getVersion();
						try {
							ZooConnection.zoo.delete("/registry/"+id_to_quit.get(0), version);
							ZooConnection.zoo.setData("/request/quit/"+id_to_quit.get(0), "1".getBytes(), -1);
							System.out.println(id_to_quit.get(0) + " has been deleted successfully");
						} catch (InterruptedException e) {
							ZooConnection.zoo.setData("/request/quit/"+id_to_quit.get(0), "0".getBytes(), -1);
						}
					}else {
						ZooConnection.zoo.setData("/request/quit/"+id_to_quit.get(0), "2".getBytes(), -1);
						System.out.println(id_to_quit.get(0) + " doesn't exist in registry");
					}
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				try {
					ZooConnection.zoo.getChildren("/request/quit", new WatcherMaster());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else if(event.getPath().equals("/online")) {
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
		else if (event.getType() == EventType.NodeDataChanged) {
			System.out.println(event.getPath()+"'s data has been changed");
		}
		else {
			System.out.println(event.getPath());
		}
	}
	
}

