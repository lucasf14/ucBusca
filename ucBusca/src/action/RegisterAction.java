package action;

import com.opensymphony.xwork2.ActionSupport;
import model.HeyBean;
import org.apache.struts2.interceptor.SessionAware;

import java.rmi.RemoteException;
import java.util.Map;

public class RegisterAction extends ActionSupport implements SessionAware {
    private Map<String, Object> session;
    private String username = null;
    private String password = null;


    @Override
    public String execute() throws RemoteException {

        if (!this.username.equals("") && !this.password.equals("")) {
            String data = this.getHeyBean().register(this.username,this.password);
            if(data.equals("Done")) {
                return SUCCESS;
            }
            else{
                return ERROR;
            }
        } else {

            return ERROR;
        }
    }

    public void setUsername(String username) {
        this.username = username; // what about this input?
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password; // what about this input?
    }

    public String getPassword() {
        return password;
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
