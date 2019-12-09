import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class User  onde sera guardado a info de cada user, e os estados de login e logout, e assim como se e admin ou nao
 */

public class User implements Serializable {
    private int id;
    private String nome;
    private String password;
    private boolean Admin;
    private boolean state ;
    private ArrayList<String> searchHistory = new ArrayList<>();

    public User(int id,String nome, String password)
    {
        if(id == 0)
        {
            this.id = id;
            this.nome = nome;
            this.password = password;
            Admin = true ;
        }
        else{
            this.id = id;
            this.nome = nome;
            this.password = password;
            Admin = false ;
        }
    }

    public void login()
    {
        this.state = true;
    }

    public void logout(){this.state = false;}

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAdmin(){
        return Admin;
    }

    public void makeAdmin(){
        Admin = true;
    }

    public void insertSearch(String search)
    {
        searchHistory.add(search);
    }

    public ArrayList<String> getSearchHistory() {
        return searchHistory;
    }
}
