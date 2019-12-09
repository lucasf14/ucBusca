import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Class responsavel pela escrita da info dos sites, lista de esperas em ficheiros objetos
 *
 */

public class Search implements Serializable {
    List<Site> listaSites = Collections.synchronizedList(new ArrayList<>());
    Map<String, HashSet> urlMap = Collections.synchronizedMap(new HashMap<>());
    Map<String ,HashSet<String>>  pais = Collections.synchronizedMap(new HashMap<>());
    List<String> urls = Collections.synchronizedList(new ArrayList<>());
    int maxSearch;


    public synchronized void SearchUrls(String url) {
       /// System.out.println("Fetching url: "+url);
        if (! url.startsWith("http://") && ! url.startsWith("https://"))
            url = "http://".concat(url);
        int flag = 0 ;
        for(Site site : listaSites) {
            if (site.getUrl().equals(url)) {
                flag = 1;
                System.out.println("ENTREI 2");
                for(String pai: pais.keySet())
                {
                    if(pais.get(pai).contains(url))
                    {
                        site.addReference(pai);
                    }
                }
            }
        }
        if(flag == 0) {
            try {
                Document doc = Jsoup.connect(url).get();
                Elements links = doc.select("a[href]");
                String title = doc.title();
                //System.out.println("Title: " + title);
                if (links.size() > 5) {

                    int z = 0;
                    int i = 0;

                    while (z < 5) {

                        if (links.get(i).attr("href").startsWith("#")) {
                            i = i + 1;
                        }
                        // Shall we ignore local links? Otherwise we have to rebuild them for future parsing
                        if (!links.get(i).attr("href").startsWith("http")) {
                            i = i + 1;
                        } else {
                            i = i + 1;
                            z = z + 1;
                            String urlsinLink = links.get(i).attr("abs:href").toString();
                            urls.add(urlsinLink);
                            if (pais.containsKey(url)) {
                                pais.get(url).add(urlsinLink);
                            } else {
                                HashSet<String> new_list = new HashSet<>();
                                pais.put(url, new_list);
                                pais.get(url).add(urlsinLink);
                            }
                            for (Site site : listaSites) {
                                if (site.getUrl().equals(url)) {
                                    site.addReference(urlsinLink);
                                }
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < links.size(); i++) {
                        if (links.get(i) == null) {
                            continue;
                        }
                        if (links.get(i).attr("href").startsWith("#")) {
                            continue;
                        }
                        if (!links.get(i).attr("href").startsWith("http")) {
                            continue;
                        } else {
                            String urlsinLink = links.get(i).attr("abs:href").toString();
                            urls.add(urlsinLink);
                            if (pais.containsKey(url)) {
                                pais.get(url).add(urlsinLink);
                            } else {
                                HashSet<String> new_list = new HashSet<>();
                                pais.put(url, new_list);
                                pais.get(url).add(urlsinLink);
                            }
                            for (Site site : listaSites) {
                                if (site.getUrl().equals(url)) {
                                    site.addReference(urlsinLink);
                                }
                            }
                        }
                    }
                }


                String text = doc.body().text();
                HashMap<String, Integer> mapa;

                mapa = countWords(text);
                Site novo = new Site(url, title, text, mapa);
                listaSites.add(novo);

                for(String pai: pais.keySet()) {
                    if (pais.get(pai).contains(url)) {
                        novo.addReference(pai);
                    }
                }

                for (String word : mapa.keySet()) {
                    String key = word.toString();
                    if (urlMap.containsKey(key)) {
                        urlMap.get(key).add(novo.getUrl());
                    } else {
                        HashSet<String> new_hash = new HashSet<>();
                        urlMap.put(key, new_hash);
                        urlMap.get(key).add(novo.getUrl());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void SearchRecursive(int maxSearch)
    {
        this.maxSearch = maxSearch;
        for(int i =0 ; i < maxSearch ; i++)
        {
            SearchUrls(urls.get(i));
            urls.remove(i);
        }
    }

    private static HashMap<String, Integer> countWords(String text) {
        HashMap<String, Integer> conta_palavras = new HashMap<String, Integer>();
        BufferedReader buffer = new BufferedReader(new InputStreamReader(new ByteArrayInputStream((text.getBytes(StandardCharsets.UTF_8)))));
        String linha;

        while (true)
        {
            try{
                if((linha = buffer.readLine()) == null)
                {
                    break;
                }
                String[] array = linha.split("[ ,;:.?!“”(){}\\\\[\\\\]<>']+");
                for(int i = 0; i < array.length;i++)
                {
                    String nova = array[i];
                    nova = nova.toLowerCase();
                    if("".equals(nova)){
                        continue;
                    }
                    if(!conta_palavras.containsKey(nova))
                    {
                        conta_palavras.put(nova,1);
                    }
                    else{
                        conta_palavras.put(nova,conta_palavras.get(nova)+1);
                    }
                }

            }catch(IOException e) {
                e.printStackTrace();
            }
        }

        // Close reader
        try {
            buffer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }/*
        for(String word : conta_palavras.keySet())
        {
            String key = word.toString();
            String valor = conta_palavras.get(word).toString();
            System.out.println(key + " "+ valor);
        }*/
        return conta_palavras;
    }

    public void printurlMap(){
        System.out.println("ENTREIIIIIIIIIIIII");
        for(String word : urlMap.keySet())
        {
            String key = urlMap.get(word).toString();
            System.out.println(word+" references : "+key);
        }
    }

    public synchronized void escreveObjeto(List<Site> listaSites, List<String> urls, Map<String, HashSet> mapa,Map<String ,HashSet<String>>  pais) {
        try {
            FileOutputStream fos = new FileOutputStream("baseObjeto.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(listaSites);
            oos.writeObject(urls);
            oos.writeObject(mapa);
            oos.writeObject(pais);
            oos.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch ( IOException e) {
            e.printStackTrace();
    }
    }

    public void leObjecto() throws IOException, ClassNotFoundException {

        FileInputStream fis = new FileInputStream("baseObjeto.ser");
        ObjectInputStream ois = new ObjectInputStream(fis);

        this.listaSites = (ArrayList<Site>)ois.readObject();
        this.urls = (List<String>)ois.readObject();
        this.urlMap = (HashMap<String, HashSet>)ois.readObject();
        this.pais = (HashMap<String, HashSet<String>>)ois.readObject();

        fis.close();
        ois.close();
    }

    public List<Site> getListaSites() {
        return listaSites;
    }

    public void printSites(){
        for(Site site : listaSites)
        {
            System.out.println("URL: "+site.getUrl());
            System.out.println("Nome: "+site.getTitulo());
            System.out.println("Texto: "+site.getTexto());
            System.out.println("REFERNCES: "+site.getUrlPointing());

        }
    }

    public Map<String, HashSet> getUrlMap() {
        return urlMap;
    }

    public List<String> getUrls() {
        return urls;
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

    public Map<String, HashSet<String>> getPais() {
        return pais;
    }
}



