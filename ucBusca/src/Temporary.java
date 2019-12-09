import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Temporary implements Serializable {
    ArrayList<Site> listaSites = new ArrayList<>();
    HashMap<String, HashSet> urlMap = new HashMap<>();
    int id ;

    public Temporary(int id)
    {
        this.id = id;
    }

    public void escreveFicheiro(ArrayList<Site> listaSites,HashMap<String, HashSet> urlMap)
    {
        try {
            FileOutputStream fos = new FileOutputStream("temporaryFile"+this.id+".ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(listaSites);
            oos.writeObject(urlMap);
            oos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void leTemporary() throws IOException, ClassNotFoundException {

        FileInputStream fis = new FileInputStream("temporaryFile"+this.id+".ser");
        ObjectInputStream ois = new ObjectInputStream(fis);

        this.listaSites = (ArrayList<Site>)ois.readObject();
        this.urlMap = (HashMap<String, HashSet>)ois.readObject();

        fis.close();
        ois.close();
    }

    public HashSet<String> searchWord(String word)
    {
        for(String words : urlMap.keySet())
        {
            if(word.equals(words))
            {
                return urlMap.get(words);
            }
        }
        return null;
    }




}