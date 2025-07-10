package P1;


	import java.sql.*;
	import java.util.*;
	import java.util.concurrent.*;
	import java.security.*;

	public class EventManagerApp {
	  /* DB CONFIG */
	  static class DB {
	    static Connection get() throws Exception {
	      String url = "jdbc:postgresql://localhost:5432/eventdb";
	      String user = "postgres", pass = "your_password";
	      Class.forName("org.postgresql.Driver");
	      return DriverManager.getConnection(url, user, pass);
	    }
	  }

	  /* MODELS */
	  static class User { int id; String username, password, role; }
	  static class Event { int id; String name, location; Timestamp time; int capacity; }
	  static class Registration { int id, userId, eventId; Timestamp when; }

	  /* DAOs */
	  static class UserDAO {
	    User findByUsername(String u) throws Exception {
	      var sql = "SELECT * FROM users WHERE username=?";
	      try (var c=DB.get(); var p=c.prepareStatement(sql)) {
	        p.setString(1,u); var rs=p.executeQuery();
	        if(rs.next()){ User user=new User();
	          user.id=rs.getInt("id");
	          user.username=rs.getString("username");
	          user.password=rs.getString("password");
	          user.role=rs.getString("role");
	          return user;
	        } return null;
	      }
	    }
	    boolean create(User u) throws Exception {
	      var sql="INSERT INTO users(username,password,role) VALUES(?,?,?)";
	      try(var c=DB.get(); var p=c.prepareStatement(sql)){
	        p.setString(1,u.username);
	        p.setString(2,u.password);
	        p.setString(3,u.role);
	        return p.executeUpdate()>0;
	      }
	    }
	  }

	  static class EventDAO {
	    boolean create(Event e) throws Exception {
	      var sql="INSERT INTO events(name,location,event_time,capacity) VALUES(?,?,?,?)";
	      try(var c=DB.get(); var p=c.prepareStatement(sql)){
	        p.setString(1,e.name); p.setString(2,e.location);
	        p.setTimestamp(3,e.time); p.setInt(4,e.capacity);
	        return p.executeUpdate()>0;
	      }
	    }
	    List<Event> listAll() throws Exception {
	      var list=new ArrayList<Event>();
	      var sql="SELECT * FROM events ORDER BY event_time";
	      try(var c=DB.get(); var p=c.prepareStatement(sql);
	          var rs=p.executeQuery()){
	        while(rs.next()){
	          Event e=new Event();
	          e.id=rs.getInt("id"); e.name=rs.getString("name");
	          e.location=rs.getString("location");
	          e.time=rs.getTimestamp("event_time");
	          e.capacity=rs.getInt("capacity");
	          list.add(e);
	        }
	      } return list;
	    }
	  }

	  static class RegistrationDAO {
	    boolean register(int uid,int eid) throws Exception {
	      var sql="INSERT INTO registrations(user_id,event_id) VALUES(?,?)";
	      try(var c=DB.get(); var p=c.prepareStatement(sql)){
	        p.setInt(1,uid); p.setInt(2,eid);
	        return p.executeUpdate()>0;
	      }
	    }
	    List<Registration> listForUser(int uid) throws Exception {
	      var list=new ArrayList<Registration>();
	      var sql="SELECT * FROM registrations WHERE user_id=?";
	      try(var c=DB.get(); var p=c.prepareStatement(sql)){
	        p.setInt(1,uid); var rs=p.executeQuery();
	        while(rs.next()){
	          Registration r=new Registration();
	          r.id=rs.getInt("id"); r.userId=uid;
	          r.eventId=rs.getInt("event_id");
	          r.when=rs.getTimestamp("registered_at");
	          list.add(r);
	        }
	      } return list;
	    }
	  }

	  /* SERVICES */
	  static class AuthService {
	    UserDAO ud = new UserDAO();
	    User login(String u,String p) throws Exception {
	      var user = ud.findByUsername(u);
	      if(user!=null && user.password.equals(p)) return user;
	      return null;
	    }
	    boolean register(String u,String p) throws Exception {
	      if(ud.findByUsername(u)!=null) return false;
	      User nu = new User(); nu.username=u; nu.password=p; nu.role="user";
	      return ud.create(nu);
	    }
	  }

	  static class NotificationService {
	    ExecutorService pool = Executors.newFixedThreadPool(2);
	    void remind(User u, Event e) {
	      pool.submit(() -> {
	        try { Thread.sleep(2000);
	          System.out.println("Reminder for " + u.username + ": Event \"" +
	            e.name + "\" at " + e.time);
	        } catch(Exception ignore){}
	      });
	    }
	    void shutdown() { pool.shutdown(); }
	  }

	  /* APP INTERFACE */
	  public static void main(String[] args) throws Exception {
	    Scanner sc=new Scanner(System.in);
	    AuthService auth=new AuthService();
	    EventDAO ed=new EventDAO();
	    RegistrationDAO rd=new RegistrationDAO();
	    NotificationService ns=new NotificationService();

	    System.out.println("Welcome to EventManager!");

	    User current=null;
	    while(current==null) {
	      System.out.print("1) Register  2) Login: ");
	      String opt=sc.nextLine();
	      if(opt.equals("1")) {
	        System.out.print("username: "); String u=sc.nextLine();
	        System.out.print("password: "); String p=sc.nextLine();
	        if(auth.register(u,p)) System.out.println("Registered! Please login.");
	        else System.out.println("Username taken.");
	      } else if(opt.equals("2")) {
	        System.out.print("username: "); String u=sc.nextLine();
	        System.out.print("password: "); String p=sc.nextLine();
	        current=auth.login(u,p);
	        if(current==null) System.out.println("Invalid.");
	      }
	    }

	    System.out.println("Hello, " + current.username + "!");
	    boolean admin = current.role.equals("admin");

	    while(true) {
	      System.out.println("\nMenu:");
	      System.out.println("1) List Events");
	      System.out.println("2) Register for Event");
	      System.out.println("3) My Registrations");
	      if(admin) System.out.println("4) Add Event");
	      System.out.println("0) Exit");
	      System.out.print("> ");
	      String o=sc.nextLine();

	      if(o.equals("1")) {
	        var evs = ed.listAll();
	        for(var e:evs) System.out.printf("%d) %s @ %s (cap %d)\n", e.id, e.name, e.time, e.capacity);
	      }
	      else if(o.equals("2")) {
	        System.out.print("Event ID: "); int id=Integer.parseInt(sc.nextLine());
	        if(rd.register(current.id,id)) {
	          System.out.println("Registered!");
	          var e = ed.listAll().stream().filter(x->x.id==id).findFirst().orElse(null);
	          if(e!=null) ns.remind(current,e);
	        }
	      }
	      else if(o.equals("3")) {
	        var regs = rd.listForUser(current.id);
	        for(var r:regs) System.out.printf("Event ID: %d at %s\n", r.eventId, r.when);
	      }
	      else if(admin && o.equals("4")) {
	        Event ne=new Event();
	        System.out.print("Name: "); ne.name=sc.nextLine();
	        System.out.print("Location: "); ne.location=sc.nextLine();
	        System.out.print("YYYY-MM-DD HH:MM: "); ne.time = Timestamp.valueOf(sc.nextLine()+":00");
	        System.out.print("Capacity: "); ne.capacity=Integer.parseInt(sc.nextLine());
	        if(ed.create(ne)) System.out.println("Added!");
	      }
	      else if(o.equals("0")) break;
	    }

	    ns.shutdown();
	    System.out.println("Bye!");
	  }
	}


}
