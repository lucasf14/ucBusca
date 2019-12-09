package action;

import com.opensymphony.xwork2.ActionSupport;
import model.HeyBean;
import org.apache.struts2.interceptor.SessionAware;

import java.rmi.RemoteException;
import java.util.Map;

public class sendMessage extends ActionSupport implements SessionAware {
    private Map<String, Object> session;
    private String message = null;

    @Override
    public String execute() throws RemoteException {
        if (this.message != null && !message.equals("")) {
            System.out.println(session);
            System.out.println(this.message);
            System.out.println(session);

            return SUCCESS;
        }
        return ERROR;

    }

    public void setMessage(String message) {
        this.message = message; // what about this input?
    }

    public String getMessage() {
        return message;
    }

    public HeyBean getHeyBean() {
        System.out.println(session.get("username"));
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
