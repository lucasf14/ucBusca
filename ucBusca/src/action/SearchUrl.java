package action;

import com.opensymphony.xwork2.ActionSupport;
import model.HeyBean;
import org.apache.struts2.interceptor.SessionAware;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

public class SearchUrl extends ActionSupport implements SessionAware {
    private Map<String, Object> session;
    private String message = null;
    private String search = null;
    private ArrayList data;
    private String username,password;

    @Override
    public String execute() throws RemoteException {
        if(session.containsKey("username")) {
            username = (String) session.get("username");
            password = (String) session.get("password");
        }
        data = this.getHeyBean().search(username,password,search);
        System.out.println(data);
        return SUCCESS;
    }

    public ArrayList getData() {
        return data;
    }

    public void setData(ArrayList data){
        this.data=data;
    }

    public void setUsername(String username){this.username=username;}

    public String getUsername(){return username;}

    public void setPassword(String password){this.password=password;}

    public String getPassword(){return password;}

    public void setSearch(String search) {
        this.search = search; // what about this input?
    }

    public String getSearch() {
        return search;
    }

    public HeyBean getHeyBean() {
        if(!session.containsKey("heyBean"))
            this.setHeyBean(new HeyBean());
        return (HeyBean) session.get("heyBean");
    }

    public void setHeyBean(HeyBean heyBean) {
        this.session.put("heyBean", heyBean);
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

}
