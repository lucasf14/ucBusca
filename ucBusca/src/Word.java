import java.io.Serializable;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Class que ira guardar a quantidade vezes que a palavra e pesquisada para mais tarde uma lista com as palavras mais utilizadas seja pedida pelo o admin
 */


public class Word implements Serializable  {
    private String word;
    private int contador;

    public Word(String word)
    {
        this.word = word;
        this.contador = 1;
    }

    public void addContador()
    {
        contador = contador + 1;
    }

    public int getContador() {
        return contador;
    }

    public String getWord() {
        return word;
    }
}
