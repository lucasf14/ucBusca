import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


public class MulticastServer extends Thread {
    private String MULTICAST_ADDRESS = "224.0.224.0";
    public int no;
    private int SERVER_PORT = 4000;
    private int my_PORT = 8000;
    private int TCP_PORT = 6000;
    private String ip;

    //Vao guardar guardar nos ficheiros e abrir quando iniciam
    private List<Site> listaSite = Collections.synchronizedList(new ArrayList<>());
    private Map<String, HashSet> urlMap = Collections.synchronizedMap(new HashMap<>());
    private Map<String, HashSet<String>> pais = Collections.synchronizedMap(new HashMap<>());
    private List<String> urls_total = Collections.synchronizedList(new ArrayList<>());
    private List<User> users = Collections.synchronizedList(new ArrayList<>());

    //vao servir para ir buscar os atuais
    private ArrayList<Word> popular_search = new ArrayList<>();
    private Map<Integer,String> ids_mul = Collections.synchronizedMap(new HashMap<>());
    private List<String> auxiliar_ids = Collections.synchronizedList(new ArrayList<>());
    private Map<Integer,String> ips_mul = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) {
        //Pede para Criar server
        System.out.println("Server ID: ");
        Scanner ln = new Scanner(System.in);
        int x = ln.nextInt();

        MulticastServer server1 = new MulticastServer(x);
        server1.start();

    }

    public MulticastServer(int x) {
        this.no = x;
    }


    public void run() {
        MulticastSocket receiveSocket = null;
        MulticastSocket senderSocket = null;
        Search readingSites = new Search();
        try {
            readingSites.leObjecto();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        UserRead readingUsers = new UserRead();
        try {
            readingUsers.leUsers();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        //Le info dos Ficheiros objetos
        urls_total = readingSites.getUrls();
        users = readingUsers.getUsers();
        listaSite = readingSites.getListaSites();
        urlMap = readingSites.getUrlMap();
        pais = readingSites.getPais();

        //Cria tcp com base no ID dado ao server
        TCPServer tcpS = new TCPServer(TCP_PORT + no);
        tcpS.start();
        System.out.println("SERVER :" + no + " IS READY and size:" + urls_total.size());
        String numeroS = "" + no;

        //Coloca na lista dos servers e atribui a funcao de main ao servidor criado
        auxiliar_ids.add(numeroS);
        ids_mul.put(no, "main");

        //obtem ip para comunicacao tcp
        this.ip = getIP();

        try {

            //cria sockets um para receber e um para enviar
            receiveSocket = new MulticastSocket(SERVER_PORT);
            senderSocket = new MulticastSocket();

            //socket que recebe entra no grupo multicast
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            receiveSocket.joinGroup(group);


            //adiciona o ip na hashmap com ids e respetivos ips
            ips_mul.put(no,this.ip);
            String broadcast = "type|serverid;ID|" + this.no+";IP|"+this.ip+";";
            byte[] bufbroad = broadcast.getBytes();

            //envia para a rede o seu id e o IP para que os outro servers saibam que esta ativo
            DatagramPacket broadPacket = new DatagramPacket(bufbroad, broadcast.length(), group, SERVER_PORT);
            senderSocket.send(broadPacket);

            MulticastSocket finalSenderSocket = senderSocket;
            //em caso do server crashar por sigint manda mensagem para a rede para os servers removeres este server das listas
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    String killme = "type|kill;ID|" + no;
                    byte[] bufkill = killme.getBytes();
                    DatagramPacket broadPacket = new DatagramPacket(bufkill, bufkill.length, group, SERVER_PORT);
                    try {
                        System.out.println("KILL ME " + no);
                        finalSenderSocket.send(broadPacket);
                    } catch (IOException e) {
                        //e.printStackTrace();
                    }
                }
            });

            //thread de indexacao da start mas fica num sleep para quando receber mensagem dos outros tcp,so o server que ta main o faça
             RecursiveSearch startrec = new RecursiveSearch(10);
             startrec.start();

            while (true) {
                //wait for packet
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                receiveSocket.receive(packet);
                //receives message and processes it
                System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:" + "E O MEU ID E: " + this.no);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println(message);
                //passa mensagem para thread que vai enviar respostas com base na mensagem
                HandleProtocol novo = new HandleProtocol(no, packet, senderSocket, my_PORT);
                novo.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            receiveSocket.close();
            senderSocket.close();
        }
    }

    /**
     * Handle Protocol vai tratar das mensagens enviadas pelo RMI Server ,e  fazer as suas operações
     */


    private class HandleProtocol extends Thread {
        private int id;
        private DatagramPacket packet;
        private MulticastSocket socket;
        private int PORT;

        public HandleProtocol(int id, DatagramPacket packet, MulticastSocket socket, int PORT) {
            this.id = id;
            this.packet = packet;
            this.socket = socket;
            this.PORT = PORT;
        }

        public void run() {
            String message = new String(packet.getData(), 0, packet.getLength());
            String[] aux0 = message.split(";");
            if (aux0.length == 1) {
                switch (aux0[0]) {
                    case "type|info":
                        //info ordena a lista de sites pelo numero de referencias;
                        List<Site> provisorio_sites = listaSite;
                        Collections.sort(provisorio_sites, new Comparator<Site>() {
                            @Override
                            public int compare(Site site, Site t1) {
                                if (site.getTotal_ind() > t1.getTotal_ind())
                                    return 1;
                                if (site.getTotal_ind() < t1.getTotal_ind())
                                    return -1;
                                return 0;
                            }
                        });

                        //ordena a lista de palavras populares por ordem de mais procuradas
                        Collections.sort(popular_search, new Comparator<Word>() {
                            @Override
                            public int compare(Word word1, Word w2) {
                                if (word1.getContador() > w2.getContador())
                                    return 1;
                                if (word1.getContador() < w2.getContador())
                                    return -1;
                                return 0;
                            }
                        });
                        StringBuilder buildInfo = new StringBuilder();
                        buildInfo.append("type|popular;");

                        if (provisorio_sites.size() != 0) {
                            buildInfo.append("popular_list|url_list;");
                            int i = 0;
                            buildInfo.append("item_count|" + provisorio_sites.size() + ";");
                            for (Site site : provisorio_sites) {
                                System.out.println("Site:" + site.getUrl() + "refs para esta pagina :" + site.getTotal_ind());
                                buildInfo.append("site_" + i + "_title|").append(site.getTitulo()).append(";");
                                buildInfo.append("site_" + i + "_url|").append(site.getUrl()).append(";");
                                buildInfo.append("site_" + i + "_text|").append(site.getTexto().substring(0, 50)).append(";");
                                i = i + 1;
                            }
                            buildInfo.append("+");
                        }

                        if (provisorio_sites.size() == 0) {
                            buildInfo.append("popular_list|url_list;item_count|0;");
                            buildInfo.append("+");
                        }

                        if (popular_search.size() != 0) {
                            int i = 0;
                            buildInfo.append("item_count|" + popular_search.size() + ";");
                            for (Word word : popular_search) {
                                System.out.println("Word:" + word.getWord() + "numero de procuras: " + word.getContador());
                                buildInfo.append("word_" + i + "|").append(word.getWord());
                                buildInfo.append("procuras|").append(word.getContador()).append(";");
                                i = i + 1;
                            }
                            buildInfo.append("+");
                        }
                        if (popular_search.size() == 0) {
                            buildInfo.append("popular_list|word_list;item_count|0;");
                            buildInfo.append("+");
                        }

                        buildInfo.append("popular_list|server_list;");
                        for (int i = 0; i < auxiliar_ids.size(); i++) {
                            buildInfo.append("ids_" + i + "|" + auxiliar_ids.get(i) + ";");
                        }
                        buildInfo.append("+" + "popular_list|ports;port|" + SERVER_PORT + ";");

                        String infotoSend = buildInfo.toString();
                        byte[] bufferInfo = infotoSend.getBytes();
                        DatagramPacket replyInfo = new DatagramPacket(bufferInfo, bufferInfo.length, this.packet.getAddress(), my_PORT);
                        try {
                            this.socket.send(replyInfo);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;

                    case "type|size":
                        //devolve ao RMI o numero de servers ativos
                        String sizedalista = Integer.toString(auxiliar_ids.size());
                        byte[] b = sizedalista.getBytes();
                        DatagramPacket replaysize = new DatagramPacket(b, b.length, this.packet.getAddress(), my_PORT);
                        try {
                            this.socket.send(replaysize);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;

                    case "type|admin":
                        //devolve uma lista de users e se sao admins ou nao
                        StringBuilder userString = new StringBuilder();
                        if (users.size() != 0) {
                            for (int i = 0; i < users.size(); i++) {
                                userString.append("username|").append(users.get(i).getNome()).append(";admin|").append(users.get(i).isAdmin()).append(";");
                            }
                        } else {
                            userString.append("error|NoUsers");
                        }
                        String userMessage = userString.toString();
                        byte[] buffer = userMessage.getBytes();
                        DatagramPacket replayadmin = new DatagramPacket(buffer, buffer.length, this.packet.getAddress(), my_PORT);

                        try {
                            this.socket.send(replayadmin);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            } else {
                switch (aux0[0]) {
                    case "type|kill":
                        //servers removem o id recebido das suas listas
                        String[] killID = aux0[1].split("\\|");
                        if(ids_mul.containsKey(Integer.parseInt(killID[1]))) {
                            if (ids_mul.get(Integer.parseInt(killID[1])).equals("main")) {
                                auxiliar_ids.remove(killID[1]);
                                ids_mul.remove(Integer.parseInt(killID[1]));

                                int novomaine = Integer.parseInt(auxiliar_ids.get(0));
                                ids_mul.replace(novomaine, "main");

                                if (no == novomaine) {
                                    RecursiveSearch startingnew = new RecursiveSearch(10);
                                    startingnew.start();
                                }

                            } else {
                                auxiliar_ids.remove(killID[1]);
                                ids_mul.remove(Integer.parseInt(killID[1]));
                            }
                        }
                        break;

                    case "type|serverid":
                        //recebe id e o Ip do novo server e adicona na lista, e consequentemente envia toda as informaçoes para o novo server
                        String[] serverupID = aux0[1].split("\\|");
                        String[] serverIP = aux0[2].split("\\|");
                        if (!auxiliar_ids.contains(serverupID[1])) {
                            auxiliar_ids.add(serverupID[1]);
                            int z = Integer.parseInt(serverupID[1]);
                            if(!ips_mul.containsKey(z)){
                                ips_mul.put(z,serverIP[1]);
                            }
                            if (z != no) {
                                ids_mul.put(z, "online");
                            }
                            for (String x : auxiliar_ids) {
                                int y = Integer.parseInt(x);
                                if (y != no) {

                                    TCPclient broadtcp = new TCPclient(ips_mul.get(y), TCP_PORT + y, "BROADCAST");
                                    broadtcp.start();
                                }
                            }
                        }

                        break;

                    case "type|giveadmin":
                        //atribbui o estatudo admin ao novo user e guard no ficheiro objetos
                        String[] username = aux0[1].split("\\|");
                        String sendMessage = new String();
                        for (User user : users) {
                            if (user.getNome().equals(username[1])) {
                                if (user.isAdmin()) {
                                    sendMessage = "message|User already admin.";

                                } else {
                                    UserRead saveAdmin = new UserRead();
                                    user.makeAdmin();
                                    sendMessage = "message|User selected is now an admin.";
                                    saveAdmin.escreveUsers(users);
                                }
                            }
                        }

                        byte[] bufferuser = sendMessage.getBytes();
                        DatagramPacket replayusers = new DatagramPacket(bufferuser, bufferuser.length, this.packet.getAddress(), my_PORT);
                        try {
                            this.socket.send(replayusers);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;

                    case "type|newurl":
                        //no caso do server ser o main, adiciona na queue, para mais tarde enviar a todos os servers
                        System.out.println(no + " " + ids_mul.get(no));
                        if (ids_mul.get(no).equals("main")) {
                            System.out.println("Server :" + no + " searching url");
                            String[] url = aux0[1].split("\\|");
                            if (urls_total.isEmpty()) {
                                SearchUrls(url[1]);
                                RecursiveSearch main = new RecursiveSearch(10);
                                main.start();
                                String success = "type|confirm;message|Successfully indexed new URL!";
                                byte[] buffsucccess = success.getBytes();
                                DatagramPacket replysucces = new DatagramPacket(buffsucccess, buffsucccess.length, this.packet.getAddress(), my_PORT);
                                try {
                                    this.socket.send(replysucces);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                urls_total.add(url[1]);
                                System.out.println("Add na queue do " + no);
                                String success = "type|confirm;message|Successfully indexed new URL!";
                                byte[] buffsucccess = success.getBytes();
                                DatagramPacket replysucces = new DatagramPacket(buffsucccess, buffsucccess.length, this.packet.getAddress(), my_PORT);
                                try {
                                    this.socket.send(replysucces);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        break;

                    case "type|search":
                        //caso de login procura as palavras e adiciona no historico, caso nao esteja apenas devolve a lista de sites encontrados
                        String[] word = aux0[1].split("\\|");
                        if (word[0].equals("user")) {
                            ArrayList<String> wordList = new ArrayList<>();
                            ArrayList<String> listaUrls = new ArrayList<>();

                            //Cria Lista com palavras para procurar e adiciona no search history
                            for (int i = 2; i < aux0.length; i++) {
                                String[] sub = aux0[i].split("\\|");
                                if (sub[0].equals("word")) ;
                                {
                                    insertHistory(word[1], sub[1]);
                                    if (popular_search.size() != 0) {
                                        int flag = 0;
                                        for (Word x : popular_search) {
                                            if (x.getWord().equals(sub[1])) {
                                                x.addContador();
                                                flag = 1;
                                            }
                                        }
                                        if (flag == 0) {
                                            Word nova = new Word(sub[1]);
                                        }
                                    }

                                    if (popular_search.size() == 0) {
                                        Word nova = new Word(sub[1]);
                                    }
                                    wordList.add(sub[1]);
                                }
                            }
                            //Procura as palavras e recebe HashSet
                            for (String toSearch : wordList) {
                                HashSet<String> foundUrls = searchWord(toSearch);
                                if (foundUrls != null) {
                                    for (String i : foundUrls) {
                                        if (!listaUrls.contains(i)) {
                                            listaUrls.add(i);
                                        }
                                    }
                                }
                            }

                            ArrayList<Site> organized = new ArrayList<>();
                            //cria mensagem para mandar
                            StringBuilder auxiliarUrls = new StringBuilder();
                            for (String urls : listaUrls) {
                                for (Site site : listaSite) {
                                    if (site.getUrl().equals(urls)) {
                                        organized.add(site);
                                    }
                                }
                            }


                            Collections.sort(organized, new Comparator<Site>() {
                                @Override
                                public int compare(Site site, Site t1) {
                                    if (site.getTotal_ind() > t1.getTotal_ind())
                                        return 1;
                                    if (site.getTotal_ind() < t1.getTotal_ind())
                                        return -1;
                                    return 0;
                                }
                            });

                            if (listaUrls.size() != 0) {
                                int i = 0;
                                auxiliarUrls.append("type|url_list;item_count|" + organized.size() + ";");
                                for (Site site : organized) {
                                    System.out.println("Site:" + site.getUrl() + "refs para esta pagina :" + site.getTotal_ind());
                                    auxiliarUrls.append("site_" + i + "_title|").append(site.getTitulo()).append(";");
                                    auxiliarUrls.append("site_" + i + "_url|").append(site.getUrl()).append(";");
                                    auxiliarUrls.append("site_" + i + "_text|").append(site.getTexto().substring(0, 50)).append(";");
                                    i = i + 1;
                                }
                            }


                            if (listaUrls.size() == 0) {
                                auxiliarUrls.append("url|not found;");
                            }

                            //cria UDP e envia
                            String sendMessageurl = auxiliarUrls.toString();
                            System.out.println(sendMessageurl);
                            byte[] bufUrl2 = sendMessageurl.getBytes();
                            DatagramPacket replay4 = new DatagramPacket(bufUrl2, bufUrl2.length, this.packet.getAddress(), my_PORT);
                            try {
                                socket.send(replay4);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            ArrayList<String> wordList = new ArrayList<>();
                            ArrayList<String> listaUrls2 = new ArrayList<>();

                            //adiciona palavras a dar search numa lista
                            for (int i = 1; i < aux0.length; i++) {
                                String[] sub = aux0[i].split("\\|");
                                if (sub[0].equals("word")) ;
                                {
                                    if (popular_search.size() != 0) {
                                        int flag = 0;
                                        for (Word x : popular_search) {
                                            if (x.getWord().equals(sub[1])) {
                                                x.addContador();
                                                flag = 1;
                                            }
                                        }
                                        if (flag == 0) {
                                            Word nova = new Word(sub[1]);
                                        }
                                    }

                                    if (popular_search.size() == 0) {
                                        Word nova = new Word(sub[1]);
                                    }

                                    wordList.add(sub[1]);
                                }
                            }

                            //adiciona os urls numa lista
                            for (String toSearch : wordList) {
                                HashSet<String> foundUrls = searchWord(toSearch);
                                if (foundUrls != null) {
                                    for (String i : foundUrls) {
                                        if (!listaUrls2.contains(i)) {
                                            listaUrls2.add(i);
                                        }
                                    }
                                }
                            }
                            ArrayList<Site> organized = new ArrayList<>();
                            //transforma esses urls em mensagem e envia
                            StringBuilder aux = new StringBuilder();
                            for (String urls : listaUrls2) {
                                for (Site site : listaSite) {
                                    if (site.getUrl().equals(urls)) {
                                        organized.add(site);
                                    }
                                }
                            }

                            Collections.sort(organized, new Comparator<Site>() {
                                @Override
                                public int compare(Site site, Site t1) {
                                    if (site.getTotal_ind() > t1.getTotal_ind())
                                        return -1;
                                    if (site.getTotal_ind() < t1.getTotal_ind())
                                        return 1;
                                    return 0;
                                }
                            });

                            if (listaUrls2.size() != 0) {
                                int i = 0;
                                aux.append("type|url_list;item_count|" + organized.size() + ";");
                                for (Site site : organized) {
                                    System.out.println("Site:" + site.getUrl() + "refs para esta pagina :" + site.getTotal_ind());
                                    aux.append("site_" + i + "_title|").append(site.getTitulo()).append(";");
                                    aux.append("site_" + i + "_url|").append(site.getUrl()).append(";");
                                    aux.append("site_" + i + "_text|").append(site.getTexto().substring(0, 50)).append(";");
                                    i = i + 1;
                                }
                            }

                            if (listaUrls2.size() == 0) {
                                aux.append("url|not found;");
                            }

                            //envia UDP
                            String sendMessageurl = aux.toString();
                            System.out.println(sendMessageurl);
                            byte[] bufUrl2 = sendMessageurl.getBytes();
                            DatagramPacket replay4 = new DatagramPacket(bufUrl2, bufUrl2.length, this.packet.getAddress(), my_PORT);
                            try {
                                socket.send(replay4);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        break;

                    case "type|register":
                        //regista novo user , caso este nao exista
                        String[] nome1 = aux0[1].split("\\|");
                        String[] password1 = aux0[2].split("\\|");
                        UserRead novo = new UserRead();
                        String error = insereUser(nome1[1], password1[1]);
                        if (error.equals("Done")) {
                            for (User user : users) {
                                if (user.getNome().equals(nome1[1])) {
                                    user.login();
                                }
                            }
                            novo.escreveUsers(users);
                            String sendingM = "user|" + nome1[1] + ";Welcome to ucBusca";
                            byte[] buff1 = sendingM.getBytes();
                            DatagramPacket replay1 = new DatagramPacket(buff1, buff1.length, this.packet.getAddress(), my_PORT);
                            try {
                                socket.send(replay1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (error.equals("Error")) {
                            String sendingM = "error|Try again";
                            byte[] buff2 = sendingM.getBytes();
                            DatagramPacket replay2 = new DatagramPacket(buff2, buff2.length, this.packet.getAddress(), my_PORT);
                            try {
                                socket.send(replay2);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case "type|logout":
                        //muda o estado do user para false o que significa que deu logout
                        String[] logoutUser = aux0[1].split("\\|");
                        for (User user : users) {
                            if (user.getNome().equals(logoutUser[1])) {
                                user.logout();
                                UserRead aux = new UserRead();
                                aux.escreveUsers(users);
                            }
                        }
                        System.out.println("User: " + logoutUser[1] + " logout.....");
                        break;

                    case "type|login":
                        //muda o estado do user para login, em caso de nao existe ou passsword esteja errada envia para o RMI que deu erro
                        String[] nome2 = aux0[1].split("\\|");
                        String[] password2 = aux0[2].split("\\|");
                        String verify = checkUser(nome2[1], password2[1]);
                        System.out.println("CHECKING USER " + verify);
                        if (verify.equals("Done")) {
                            boolean state = false;
                            for (User useraux : users) {
                                if (useraux.getNome().equals(nome2[1])) {
                                    state = useraux.isAdmin();
                                    useraux.login();
                                }
                            }
                            String sendingM = "user|" + nome2[1] + ";admin|" + state + ";Welcome to ucBusca";
                            byte[] buff2 = sendingM.getBytes();
                            DatagramPacket replay2 = new DatagramPacket(buff2, buff2.length, this.packet.getAddress(), my_PORT);
                            try {
                                socket.send(replay2);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (verify.equals("Wrong Password")) {
                            String sendingM = "user|" + nome2[1] + ";error|Wrong password";
                            byte[] buff2 = sendingM.getBytes();
                            DatagramPacket replay2 = new DatagramPacket(buff2, buff2.length, this.packet.getAddress(), my_PORT);
                            try {
                                socket.send(replay2);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                        if (verify.equals("Error")) {
                            String sendingM = "user|" + nome2[1] + ";error|Not found";
                            byte[] buff2 = sendingM.getBytes();
                            DatagramPacket replay2 = new DatagramPacket(buff2, buff2.length, this.packet.getAddress(), my_PORT);
                            try {
                                socket.send(replay2);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case "type|mysearch": // checkar o historico do user
                        String[] usernameSearch = aux0[1].split("\\|");
                        ArrayList<String> history = new ArrayList<>();
                        if (usernameSearch[0].equals("user")) {
                            for (User user : users) {
                                if (user.getNome().equals(usernameSearch[1])) {
                                    history = user.getSearchHistory();
                                }
                            }
                        }

                        StringBuilder sendMessageSearch = new StringBuilder();
                        if (history.size() == 0) {
                            sendMessageSearch.append("mysearch|No search found;");
                        } else {
                            for (String search : history) {
                                sendMessageSearch.append("mysearch|" + search + ";");
                            }
                        }
                        String sendingMessage = sendMessageSearch.toString();
                        byte[] bufsearch = sendingMessage.getBytes();
                        DatagramPacket replay3 = new DatagramPacket(bufsearch, bufsearch.length, this.packet.getAddress(), my_PORT);
                        try {
                            socket.send(replay3);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;

                    case "type|connections":
                        //procura os sites que o referenciam

                        String[] getusername = aux0[1].split("\\|");
                        String[] getUrl = aux0[2].split("\\|");
                        insertHistory(getusername[1], getUrl[1]);
                        String nomeURL = "http://".concat(getUrl[1]);
                        HashSet<String> sendingUrls = new HashSet<>();
                        StringBuilder sendUrls = new StringBuilder();

                        //procura o site  e quem aponta para ele
                        for (Site site : listaSite) {
                            System.out.println(site.getUrl());
                                if(nomeURL.equals(site.getUrl())){
                                sendingUrls = site.getUrlPointing();
                                System.out.println(site.getUrlPointing());
                                break;
                            }
                        }
                        if (!sendingUrls.isEmpty()) {
                            //itera sobre os que apontam e contsroi mensagem
                            for (String i : sendingUrls) {
                                sendUrls.append("url|").append(i).append(";");
                            }
                        } else {
                            sendUrls.append("url|No Url Found");
                        }
                        String sendingUrl = sendUrls.toString();
                        byte[] bufUrl = sendingUrl.getBytes();
                        DatagramPacket replay4 = new DatagramPacket(bufUrl, bufUrl.length, this.packet.getAddress(), my_PORT);
                        try {
                            socket.send(replay4);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        }
    }

    /**
     *
     * @param word palavra procurada pelo user
     * @return lista de sites que contenham a palavra
     */

    private HashSet<String> searchWord(String word) {
        for (String words : urlMap.keySet()) {
            if (word.equals(words)) {
                return urlMap.get(words);
            }
        }
        return null;
    }

    /**
     * Thread Responsavel pelo a indexacao, caso o server seja o main
     * em caso de nao o ser, apenas faz sleep
     */


    private class RecursiveSearch extends Thread {
        private int id;
        private int PORT;

        public RecursiveSearch(int id) {
            this.id = id;
        }

        public void run() {
            try {
                //do something
                Thread.sleep(40000);
                if (ids_mul.get(no).equals("main")) {
                    System.out.println("Starting to Search...........");
                    SearchRecursive(10);
                    Search novo = new Search();
                    novo.escreveObjeto(listaSite, urls_total, urlMap, pais);
                    System.out.println("saving in text_file");
                    //cria a thread para mandar para os outros
                    ArrayList<Integer> ids = new ArrayList<>();
                    for (int key : ids_mul.keySet()) {
                        if (!ids_mul.get(key).equals("offline")) {
                            ids.add(key);
                            TCPclient tcp1 = new TCPclient(ips_mul.get(key), TCP_PORT + key, "UPDATE");
                            tcp1.start();
                        }
                    }
                    System.out.println("Server :" + no + " sending new main..........");
                    for (int i : ids) {
                        TCPclient tcp2 = new TCPclient(ips_mul.get(i), TCP_PORT + i, "NEW");
                        tcp2.start();
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("SHUTTING DOWN " + no);
                for (String x : auxiliar_ids) {
                    int y = Integer.parseInt(x);
                    if (y != no) {
                        TCPclient broadtcp = new TCPclient(ips_mul.get(y), TCP_PORT + y, "KILL");
                        broadtcp.start();
                    }
                }
            }
        }
    }

    /**
     * TCPServer ira processar Update no caso de indexacao, ou seja, o server main envia toda a informacao de urls, users, sites para estarem todos sincronizados,
     * NEW  escolha de novo server que ira ser main
     * BROADCAST quando server entra na rede, o mais antigo envia toda a sua informacao para estarem todos sincronizados
     */

    private class TCPServer extends Thread {
        ServerSocket socket = null;

        public TCPServer(int PORT) {
            try {
                this.socket = new ServerSocket(PORT);
            } catch (IOException e) {
                //e.printStackTrace();
                try {
                    int randomNum = ThreadLocalRandom.current().nextInt(40, 200 + 1);
                    no = randomNum + no;
                    TCPServer novo = new TCPServer(TCP_PORT + no);
                    novo.start();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }


        public void run() {
            if (socket.isBound()) {
                while (true) {
                    Socket s = null;
                    try {
                        s = this.socket.accept();
                        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                        Object message = ois.readUTF();
                        if (message.equals("UPDATE")) {
                            listaSite = (ArrayList<Site>) ois.readObject();
                            urlMap = (HashMap<String, HashSet>) ois.readObject();
                            urls_total = (List<String>) ois.readObject();
                            pais = (HashMap<String, HashSet<String>>)ois.readObject();
                            System.out.println("UPDATED INFO....." + no);
                        }
                        if (message.equals("NEW")) {
                            String IDFunction = ois.readUTF();
                            String[] id = IDFunction.split(":");
                            for (int keys : ids_mul.keySet()) {
                                ids_mul.put(keys, "online");
                            }
                            if (ids_mul.containsKey(Integer.parseInt(id[0]))) {
                                ids_mul.replace(Integer.parseInt(id[0]), id[1]);
                            }
                            if (no == Integer.parseInt(id[0])) {
                                System.out.println("Server: " + no + " starting to iterate");
                                RecursiveSearch recsearch = new RecursiveSearch(no);
                                recsearch.start();
                            }
                        }
                        if (message.equals("BROADCAST")) {
                            System.out.println("A receber:" + no);

                            List<String> temporary = (List<String>) ois.readObject();
                            Map<Integer, String> temp = (Map<Integer, String>) ois.readObject();
                            listaSite = (ArrayList<Site>) ois.readObject();
                            urlMap = (HashMap<String, HashSet>) ois.readObject();
                            urls_total = (List<String>) ois.readObject();
                            pais = (HashMap<String, HashSet<String>>)ois.readObject();
                            popular_search = (ArrayList<Word>)ois.readObject();
                            auxiliar_ids = temporary;
                            ids_mul = temp;
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        //e.printStackTrace();
                    }
                }
            }
        }
    }



    private class TCPclient extends Thread {
        Socket clientS = null;
        String info;

        public TCPclient(String host, int port, String info) {
            try {
                this.info = info;
                this.clientS = new Socket(host, port);
            } catch (ConnectException e) {
                // e.printStackTrace();
                //EM CASO DA PORT ESTAR INATIVA REMOVE O ID DAS LISTAS DOS SERVERS
                System.out.println("PORT " + port + " is offline");

                int id = port - 6000;
                String removeID = "" + id;

                for (String x : auxiliar_ids) {
                    if (x.equals(removeID)) {
                        if (ids_mul.get(id).equals("main")) {
                            auxiliar_ids.remove(removeID);
                            ids_mul.remove(id);

                            int novomaine = Integer.parseInt(auxiliar_ids.get(0));
                            ids_mul.replace(novomaine, "main");

                            if (no == novomaine) {
                                RecursiveSearch startingnew = new RecursiveSearch(10);
                                startingnew.start();
                            }


                        } else {
                            auxiliar_ids.remove(removeID);
                            ids_mul.remove(id);
                        }
                    }
                }
                currentThread().interrupt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            ObjectOutputStream oos = null;
            try {
                oos = new ObjectOutputStream(clientS.getOutputStream());
                if (this.info.equals("UPDATE")) {
                    //ATUALIZA AS LISTAS
                    oos.writeUTF("UPDATE");
                    oos.writeObject(listaSite);
                    oos.writeObject(urlMap);
                    oos.writeObject(urls_total);
                    oos.writeObject(pais);
                    System.out.println("SENDING new info......" + no);
                }
                if (this.info.equals("BROADCAST")) {
                    //SERVER MAIS VELHO MANDA PARA OS NOVOS
                    oos.writeUTF("BROADCAST");
                    oos.writeObject(auxiliar_ids);
                    oos.writeObject(ids_mul);
                    oos.writeObject(listaSite);
                    oos.writeObject(urlMap);
                    oos.writeObject(urls_total);
                    oos.writeObject(pais);
                    oos.writeObject(popular_search);
                    System.out.println("ENVIANDO");
                }

                if (this.info.equals("NEW")) {
                    oos.writeUTF("NEW");

                    ArrayList<Integer> ids = new ArrayList<>();
                    for (int key : ids_mul.keySet()) {
                        if (!ids_mul.get(key).equals("offline")) {
                            ids.add(key);
                        }
                    }
                    Random rn = new Random();
                    int numero = rn.nextInt(ids.size());
                    int id = ids.get(numero);
                    String nova = id + ":main";
                    oos.writeUTF(nova);

                    System.out.println("Server:" + no + " Sending new main.........");
                }
                oos.close();
            } catch (NullPointerException e) {
                currentThread().interrupt();
                // System.out.print("NullPointerException caught");
            } catch (ConnectException e) {
                e.printStackTrace();
                currentThread().interrupt();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     *
     * @param nome que ira ser inserido na lista de users
     * @param password que ira ser atribuida ao user
     * @return caso seja bem sucedida Done, em caso de ja existir um User com esse nome da Error
     */

    public String insereUser(String nome, String password) {
        int id = 0;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getNome().equals(nome)) {
                return ("Error");
            }
            id = i;
        }
        User novo = new User(id, nome, password);
        users.add(novo);
        return ("Done");
    }

    /**
     *
     * @param nome nome do user para dar login
     * @param password associada a esse user
     * @return em caso de nao existir Error, Password ser diferente Wrong Password, Done se for bem sucedido
     */

    public String checkUser(String nome, String password) {
        for (User checking : users) {
            if (nome.equals(checking.getNome())) {
                if (password.equals(checking.getPassword())) {
                    return "Done";
                }
                return "Wrong Password";
            }
        }
        return "Error";
    }

    /**
     *
     * @param nome do user para inserir no historico
     * @param word que ira ser inserido no historico
     */

    public void insertHistory(String nome, String word) {
        UserRead save = new UserRead();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getNome().equals(nome)) {
                users.get(i).insertSearch(word);
                save.escreveUsers(users);
            }
        }
    }

    /**
     *
     * @param maxSearch numero maximo de iteracoes de indexaçao
     */

    public synchronized void SearchRecursive(int maxSearch) {
        if (maxSearch < urls_total.size()) {
            for (int i = 0; i < maxSearch; i++) {
                SearchUrls(urls_total.get(i));
                urls_total.remove(i);
            }
        } else {
            for (int i = 0; i < urls_total.size(); i++) {
                SearchUrls(urls_total.get(i));
                urls_total.remove(i);
            }
        }

    }

    /**
     *
     * @param url que ira ser usado para fazer indexaçao, caso seja repetido adiciona quem o tenha referenciado, caso seja novo faz a indexacao normal
     */

    public synchronized void SearchUrls(String url) {
        /// System.out.println("Fetching url: "+url);
        if (!url.startsWith("http://") && !url.startsWith("https://"))
            url = "http://".concat(url);
        int flag = 0;
        for (Site site : listaSite) {
            if (site.getUrl().equals(url)) {
                flag = 1;
                for (String pai : pais.keySet()) {
                    if (pais.get(pai).contains(url)) {
                        site.addReference(pai);
                    }
                }
            }
        }
        if (flag == 0) {
            try {
                System.out.println("New Url processeding "+url+"...............");
                Document doc = Jsoup.connect(url).get();
                if (doc != null) {
                    Elements links = doc.select("a[href]");
                    String title = doc.title();
                    for (Element link : links) {
                        // Ignore bookmarks within the page
                        if (link.attr("href").startsWith("#")) {
                            continue;
                        }

                        // Shall we ignore local links? Otherwise we have to rebuild them for future parsing
                        if (!link.attr("href").startsWith("http")) {
                            continue;
                        }

                        String urlsinLink = links.attr("abs:href");
                        urls_total.add(urlsinLink);
                        if (pais.containsKey(url)) {
                            pais.get(url).add(urlsinLink);
                        } else {
                            HashSet<String> new_list = new HashSet<>();
                            pais.put(url, new_list);
                            pais.get(url).add(urlsinLink);
                        }
                        for (Site site : listaSite) {
                            if (site.getUrl().equals(url)) {
                                site.addReference(urlsinLink);
                            }
                        }
                    }
                    if (doc.body() != null) {
                        String text = doc.body().text();
                        HashMap<String, Integer> mapa;

                        mapa = countWords(text);
                        Site novo = new Site(url, title, text, mapa);
                        listaSite.add(novo);
                        System.out.println("Added new Site in Databse....................");

                        for (String pai : pais.keySet()) {
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
                    }
                }
            } catch (UnknownHostException e) {
                System.out.println("Wrong Website");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
        }
    }

    /**
     *
     * @return  IP da maquina
     */
    public String getIP(){
        try(final DatagramSocket socket = new DatagramSocket()){
            int randomNum = ThreadLocalRandom.current().nextInt(1500, 2000 + 1);
            socket.connect(InetAddress.getByName("8.8.8.8"), randomNum);
            return socket.getLocalAddress().getHostAddress();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     *
     * @param text texto obtido no body do url indexado
     * @return hashmap com as palavras e o numero de ocorrencias
     */

    private static HashMap<String, Integer> countWords(String text) {
        HashMap<String, Integer> conta_palavras = new HashMap<String, Integer>();
        BufferedReader buffer = new BufferedReader(new InputStreamReader(new ByteArrayInputStream((text.getBytes(StandardCharsets.UTF_8)))));
        String linha;

        while (true) {
            try {
                if ((linha = buffer.readLine()) == null) {
                    break;
                }
                String[] array = linha.split("[ ,;:.?!“”(){}\\\\[\\\\]<>']+");
                for (int i = 0; i < array.length; i++) {
                    String nova = array[i];
                    nova = nova.toLowerCase();
                    if ("".equals(nova)) {
                        continue;
                    }
                    if (!conta_palavras.containsKey(nova)) {
                        conta_palavras.put(nova, 1);
                    } else {
                        conta_palavras.put(nova, conta_palavras.get(nova) + 1);
                    }
                }

            } catch (IOException e) {
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
}