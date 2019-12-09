import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;

public class MulticastUser extends Thread {
    private String MULTICAST_ADDRESS = "224.0.224.0";
    private int UDP_PORT = 4000;
    private int JAVA_PORT = 5000;
    private HashMap<String,String> statusMulticast = new HashMap<>();
    private String username;
    private int numberOnline;

    public static void main(String[] args) {
        ///criar as arraylists pais, com synchronized e passar as cenas; de maneira a conseguir syncronizar;

        MulticastUser user = new MulticastUser();
        user.start();
    }

    public MulticastUser() {
        super("User " + (long) (Math.random() * 1000));
    }

    public void run() {
        MulticastSocket socketsend = null;
        MulticastSocket receiveSocket = null;
        System.out.println(this.getName() + " ready...");
        try {
            socketsend = new MulticastSocket();  // create socket without binding it (only for sending)
            receiveSocket = new MulticastSocket(JAVA_PORT); //porta que o multicast vai comunicar pa enviar

            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            receiveSocket.joinGroup(group);
            Scanner keyboardScanner = new Scanner(System.in);
            while (true) {
                //Sends message
                String readKeyboard = keyboardScanner.nextLine();
                byte[] buffer;
                String message = readKeyboard;
                buffer = message.getBytes();
                InetAddress group2 = InetAddress.getByName(MULTICAST_ADDRESS);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group2, UDP_PORT);
                socketsend.send(packet);
                if (!message.equals("type|broadcast")) {
                    Listener listener1 = new Listener(receiveSocket, UDP_PORT, socketsend, message);
                    listener1.start();
                }
                Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
                for (Thread t : threadSet) {
                    System.out.println("Thread :" + t + ":" + "state:" + t.getState());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socketsend.close();
        }
    }

    private class Listener extends Thread {
        private MulticastSocket receivesocket;
        private int send_PORT ;
        private MulticastSocket socketsend;
        private String type;

        public Listener(MulticastSocket receivesocket,int UDP_PORT,MulticastSocket socketsend,String type)
        {
            this.receivesocket = receivesocket;
            this.send_PORT = UDP_PORT;
            this.socketsend = socketsend;
            this.type = type;
        }

        public void run() {
            try {

                if(type.equals("type|ask")) //escolher a quem vai ligar ;
                {
                    byte[] buffer = new byte[100];
                    DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
                    receivesocket.receive(packet);

                    System.out.println("Recebi: "+packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    String [] auxmessage = message.split(";");
                    String [] tamanho = auxmessage[1].split("\\|");
                    int size = Integer.parseInt(tamanho[1]);
                    ArrayList<String> numeros = new ArrayList<>();

                    for(int i = 2 ; i < size+2;i++)
                    {
                        String[] id = auxmessage[i].split("\\|");
                        numeros.add(id[1]);
                    }

                    Random rn = new Random();
                    int numero_aleatorio = rn.nextInt(numeros.size());

                    for(int x = 0 ; x < numeros.size();x++)
                    {
                        if(x == numero_aleatorio)
                        {
                            statusMulticast.put(numeros.get(x),"main");
                        }
                        else{
                        statusMulticast.put(numeros.get(x),"online");
                        }
                    }

                    //eliminar as repetiçoes de info
                    for(int z = 0 ; z < size-1; z++)
                    {
                        byte[] buffer2 = new byte[500];
                        DatagramPacket packet2 = new DatagramPacket(buffer2,buffer2.length);
                        receivesocket.receive(packet2);
                    }

                    StringBuilder nova = new StringBuilder();
                    nova.append("type|status;size|"+size+";");
                    for(String key : statusMulticast.keySet())
                    {
                        nova.append("id|").append(key).append(";status|").append(statusMulticast.get(key)).append(";");
                    }

                    String sendMessage = nova.toString();
                    byte[] buff = sendMessage.getBytes();
                    InetAddress group2 = InetAddress.getByName(MULTICAST_ADDRESS);
                    DatagramPacket replay = new DatagramPacket(buff,buff.length,group2,send_PORT);
                    socketsend.send(replay);
                    Thread.currentThread().interrupt();

                }

                if(type.equals("type|size"))
                {
                    byte[] buf = new byte[500];
                    DatagramPacket packetsize = new DatagramPacket(buf,buf.length);
                    receivesocket.receive(packetsize);
                    String size = new String(packetsize.getData(), 0, packetsize.getLength());
                    System.out.println("Recebi: "+ size);
                    int tam = Integer.parseInt(size);
                    numberOnline = tam;
                    for(int z = 0 ; z < numberOnline-1; z++)
                    {
                        byte[] buffer2 = new byte[500];
                        DatagramPacket packet2 = new DatagramPacket(buffer2,buffer2.length);
                        receivesocket.receive(packet2);
                    }
                    Thread.currentThread().interrupt();
                }
                else{
                        byte[] buf = new byte[500];
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        receivesocket.receive(packet);
                        String answer = new String(packet.getData(), 0, packet.getLength());
                        System.out.println("Recebi outro: " + answer);
                        System.out.println(numberOnline);
                        for (int z = 0; z < numberOnline - 1; z++) {
                            byte[] buffer2 = new byte[500];
                            DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length);
                            receivesocket.receive(packet2);
                            System.out.println("LIMPEI TRASH");
                        }
                    Thread.currentThread().interrupt();
                    }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}



