import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Example program to list links from a URL.
 */

public class testenet {
    ArrayList<Site> listaSites = new ArrayList<>();
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        //List<Integer> mano = Collections.synchronizedList(new ArrayList<>());
   //     mano.add(10);
      //  mano.add(14);
     //   mano.add(16);
       // Search mak = new Search();
     //   mak.leObjecto();
        //mak.printurlMap();
       // mak.printSites();
      //  Fodase nova = new Fodase(mano);
       // nova.start();

        //Fodase nova2 = new Fodase(mano);
        //nova2.start();


       /* String novo = "mamas;nigga123";
        String[] nunca = novo.split(";");
        //String[] palavras = nunca[0].split( "\\|");
        System.out.println(nunca[0]);
        System.out.println(nunca[1]);

        UserRead novoU = new UserRead();
        String error = novoU.insereUser(nunca[0],nunca[1]);
        System.out.println(error);
        ArrayList<User>index1 = novoU.users;
        for(User peps: index1)
        {
            System.out.println("id:"+peps.getId()+" Nome:"+peps.getNome()+" "+peps.getPassword());
        }
*/

       /* String minha = "word|penis;word|vagina;word|cona";
        String[] fixe = minha.split(";");
        int z = fixe.length;
        ArrayList<String> wordList = new ArrayList<>();

        for(int i = 0 ; i < fixe.length;i++)
        {
            String[] sub = fixe[i].split("\\|");
            if(sub[0].equals("word"));
            {
                wordList.add(sub[1]);
            }
        }

        for(String word : wordList)
        {
            System.out.println(word);
        }*/
    //    Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("MAN CRASHEI ME TODO EHEHEHEH")));
    //    ola nvovo = new ola();
   //     nvovo.start();
        Search novo = new Search();
        novo.SearchUrls("www.sapo.pt");
        novo.SearchUrls("https://en.wikipedia.org/wiki/Main_Page");
        novo.SearchRecursive(10);

        List<Site> listaSites = new ArrayList<>();
        List<String> urlList = new ArrayList<>();
        Map<String, HashSet> urlMap = new HashMap<>();
        Map<String ,HashSet<String>>  pais = new HashMap<>();
        listaSites = novo.getListaSites();
        urlList = novo.getUrls();
        urlMap = novo.getUrlMap();
        pais = novo.getPais();
        novo.printSites();

/*
       Search novo = new Search();
        listaSites = novo.getListaSites();
        urlList = novo.getUrls();
        urlMap = novo.getUrlMap();

        novo.escreveObjeto(listaSites,urlList,urlMap);
     */

       //  novo.leObjecto();
       // System.out.println("DONE READY....");
        //novo.printurlMap();
        //novo.escreveObjeto();

       // novo.printSites();

    /*    HashMap<String , HashSet<String>> mapaclass ;
        String url = "https://www.reddit.com/";
        print("Fetching %s...", url);

        Document doc = Jsoup.connect(url).get();
        Elements links = doc.select("a[href]");

        String title = doc.title();
        print("\nTitle: %s", title);


        print("\nLinks: (%d)", links.size());

       /* for (Element link : links) {
///            print("<%s>  (%s)", link.attr("abs:href"), trim(link.text(), 35));
            ///System.out.println("Link: "+link.attr("abs:href"));
            String cena = link.attr("abs:href").toString();
            System.out.println(cena);
        }*/
/*
       for(int i = 0 ; i < 10 ; i ++)
       {
           String cena = links.get(i).attr("abs:href").toString();
           System.out.println(cena);
       }

        String text = doc.body().text();
///        print("\nBody : %s",text);

        HashMap<String, Integer> mapa = new HashMap<>();

        mapa = countWords(text);

        Site novo = new Site(url,title,text,mapa);

        mapaclass = new HashMap<>();

        for(String word : mapa.keySet())
        {
            String key = word.toString();
            if(mapaclass.containsKey(key))
            {
                mapaclass.get(key).add(novo.getUrl());
            }
            else{
                HashSet<String> new_hash = new HashSet<>();
                mapaclass.put(key,new_hash);
                mapaclass.get(key).add(novo.getUrl());
            }
        }
/*
        for(String word : mapaclass.keySet())
        {
            String key = word.toString();
            String valor = mapaclass.get(key).toString();
            System.out.println(key + " "+ valor);

        }*/

       // Arraylist<Integer> mano = new ArrayList<>();

    }
    private static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

    private static String trim(String s, int width) {
        if (s.length() > width)
            return s.substring(0, width - 1) + ".";
        else
            return s;
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
                String[] array = linha.split("[ ,;:?!(){}<>+']");
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
        }
        for(String word : conta_palavras.keySet())
        {
            String key = word.toString();
            String valor = conta_palavras.get(word).toString();
            System.out.println(key + " "+ valor);
        }
        return conta_palavras;
    }

    private static class Fodase extends Thread{
        List<Integer> mano2;

        public Fodase(List<Integer> mano)
        {
            this.mano2 = mano;
        }

        public void run(){
            for(int x: mano2)
            {
                System.out.println(x);
            }
            mano2.add(156);
            Reader novo = new Reader(mano2);
            novo.start();

            Fucker nova = new Fucker(mano2);
            nova.start();
        }
    }

    private static class Reader extends Thread{
        List<Integer> mano2;

        public Reader(List<Integer> mano)
        {
            this.mano2 = mano;
        }

        public void run(){
            mano2.add(1234);
            List<Integer> mano = Collections.synchronizedList(new ArrayList<>());
            mano.add(1);
            mano.add(2);
            mano2 = mano;
            for(int i = 0 ; i < mano2.size();i++)
            {
                System.out.println(mano2.get(i));
            }
        }
    }

    private static class Fucker extends Thread{
        List<Integer> mano2;
        public Fucker(List<Integer> mano)
        {
            this.mano2 = mano;
        }

        public void run() {
            for (int i = 0; i < mano2.size(); i++) {
                System.out.println("ENTOU: "+mano2.get(i));
            }
        }
    }

    private static class ola extends Thread {
        public void run(){
            try{
                for(int i = 0 ; i < 10000000; i++)
                {
                    System.out.println(i);
                }
            }catch (Exception e)
            {
                System.out.println("WE DIED MOFOCKAS");
            }
        }
    }

   /* public class KillTest {

        public static void main(String args[]) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    System.out.println("Running Shutdown Hook");
                }
            });

            try{
                TimeUnit.MINUTES.sleep(10);
            }catch(Exception e){
                System.out.println("Error thrown");
            }finally {
                System.out.println("How awesome is finally?");
            }
        }*/
}



