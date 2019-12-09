import java.io.Serializable;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Class que ira ter toda a informacao procurada pelo Jsoup
 * o titulo de um site, url deste, o texto
 * o hashmap com palavras e a quantidade de vezes que se repete no body
 * Hashset com o numero de Sites que refere esta pagina
 */

public class Site implements Serializable {
    private String url;
    private String titulo;
    private String texto;
    private HashMap<String , Integer> conta_palavras;
    private HashSet<String> urlPointing = new HashSet<>();
    private int total_ind;

    public Site(String url, String titulo, String texto , HashMap<String, Integer> mapa)
    {
        this.url = url;
        this.titulo = titulo;
        this.texto = texto;
        this.conta_palavras = mapa;
    }

    public HashMap<String, Integer> getConta_palavras()
    {
        return conta_palavras;
    }

    public HashSet<String> getUrlPointing() {
        return urlPointing;
    }

    public String getUrl(){
        return url;
    }

    public String getTitulo(){
        return titulo;
    }

    public String getTexto(){
        return texto;
    }

    public void addReference(String url)
    {
        urlPointing.add(url);
        total_ind = urlPointing.size();
    }

    public int getTotal_ind() {
        return total_ind;
    }
}
