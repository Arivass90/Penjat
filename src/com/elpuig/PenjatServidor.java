package com.elpuig;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PenjatServidor {

    private static final int INACTIU = 0;
    private static final int JUGANT = 1;
    private static final int VICTORIA = 2;
    private static final int DERROTA = 3;
    private static final int NOU_JOC = 4;
    private static final int nroPort = 6112;
    private static boolean SocketDisponible = true;
    private static int conexionsRealitzades = 0;
    private static Socket conexio;
    private static int estatJoc = INACTIU;
    private static char lletra = '?';
    private static String paraula = "?";
    private static String paraulaEncriptada = "?";
    private static int nroIntents = 6;
    private static int nroMensaje = 0;
    static private final String[] paraulesPenjat = {"cinema", "musica","tecnologia", "art", "videojoc"};


    public static void main(String[] args) throws IOException {
        ServerSocket socketDeServicio = null;

        try {
            socketDeServicio = new ServerSocket(nroPort);
            BufferedReader entrada;
            DataOutputStream sortida;


            while (true) {

                try {
                    if (SocketDisponible) {
                        if (estatJoc == INACTIU) {
                            System.out.println("\nesperant client...");
                            //aceptem la conexio amb el client
                            conexio = socketDeServicio.accept();
                            System.out.println("connexio aceptada...\n");

                            sortida = new DataOutputStream(conexio.getOutputStream());
                            entrada = new BufferedReader(new InputStreamReader(conexio.getInputStream()));
                            conexionsRealitzades++;
                            System.out.println("SERVIDOR : connexio aceptada al client " + conexionsRealitzades);
                            llegirMissatge(entrada.readLine());
                            procesarMissatge();
                            sortida.writeBytes(respondreMissatge());
                        }
                    }

                    else {
                        entrada = new BufferedReader(new InputStreamReader(conexio.getInputStream()));
                        sortida = new DataOutputStream(conexio.getOutputStream());
                        if (estatJoc == JUGANT) {
                            llegirMissatge(entrada.readLine());
                            procesarMissatge();
                            sortida.writeBytes(respondreMissatge());
                            if (estatJoc == VICTORIA || estatJoc == DERROTA) {
                                estatJoc = INACTIU;
                                SocketDisponible = true;
                                System.out.println("Jugador " + conexionsRealitzades + " ha finalitzat la partida...");

                            }
                        }
                    }

                } catch (java.net.SocketException e) {
                    System.out.println("FINALITZADA CONEXIÓ AMB EL CLIENT.");
                    estatJoc = INACTIU;
                    SocketDisponible = true;
                    System.out.println("Jugador " + conexionsRealitzades + " ha finalitzat la partida...");
                }
            }
        } catch (IOException BindException) {

        }
    }

    private static void llegirMissatge(String sms) {
        // Recull les dades del metode llegirmissatge i les converteix a tokens dividits per el delimitador #
        StringTokenizer stk = new StringTokenizer(sms, "#");
        while (stk.hasMoreTokens()) {
            estatJoc = Integer.valueOf(stk.nextToken());
            nroIntents = Integer.valueOf(stk.nextToken());
            lletra = stk.nextToken().toUpperCase().charAt(0);
            paraulaEncriptada = stk.nextToken().toUpperCase();
            nroMensaje = Integer.valueOf(stk.nextToken());
        }
        nroMensaje++;
    }

    private static void procesarMissatge() {

        if (estatJoc == NOU_JOC) {
            setSocketDisponible(false);
            setEstatJoc(JUGANT);
            setNroIntents(6);
            setLletra('?');
            setParaula(esollirParaulaNouJoc());
            setParaulaEncriptada();
        } else {
            if (estatJoc == JUGANT) {
                //Comprova la informació dels mètodes i envia els missatges corresponents en cada cas
                if (Encert()) {
                    cambiaLletra();
                    if (guanyaJoc()) {
                        estatJoc = VICTORIA;
                        System.out.println("SERVIDOR : Jugador ha guanyat la partida");
                    } else {
                        System.out.println("SERVIDOR : Jugador ha esbrinat la paraula");
                    }
                } else {
                    nroIntents--;
                    System.out.println("SERVIDOR : Jugador ha perdut una vida");
                    if (nroIntents == 0) {
                        estatJoc = DERROTA;
                        System.out.println("SERVIDOR : Jugador ha perdut la partida");
                    }
                }
            } else {
                try {
                    //Tanquem la connexió
                    System.out.println("SERVIDOR : tancant conexió...");
                    conexio.shutdownOutput();
                    SocketDisponible = true;
                    System.out.println("SERVIDOR : conexió finalitzada.");
                } catch (IOException ex) {
                    Logger.getLogger(PenjatServidor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private static String respondreMissatge() {
        //Guardem totes les dades dins la variable A separades amb el mètode StringTokenizer
        String a = estatJoc + "#" + nroIntents + "#" + lletra + "#" + paraulaEncriptada + "#" + nroMensaje + "\n";
        return a;
    }

    public static void setSocketDisponible(boolean SocketDisponible) {
        //Alliberem el socket per a una nova connexio
        PenjatServidor.SocketDisponible = SocketDisponible;
    }

    // Actualitzem l'estat de les variables amb els setters
    public static void setEstatJoc(int estatJoc) {
        PenjatServidor.estatJoc = estatJoc;
    }

    public static void setLletra(char lletra) {
        PenjatServidor.lletra = lletra;
    }

    public static void setNroIntents(int nroIntents) {
        PenjatServidor.nroIntents = nroIntents;
    }

    public static void setParaula(String paraula) {
        PenjatServidor.paraula = paraula;
    }

    private static String esollirParaulaNouJoc() {
        //Selecciona una de les paraules assignades de manera aleatòria
        return paraulesPenjat[(int) (Math.random() * paraulesPenjat.length)];
    }

    private static void setParaulaEncriptada() {
        String p = "";
        // Recorre la paraula escollida i l'emmagatzema dins la variable paraula encriptada
        for (int i = 0; i < paraula.length(); i++) {
            p += "_";
        }
        paraulaEncriptada = p;
    }

    private static boolean Encert() {
        //Comprova que si lletra és part de la paraula encriptada, llavors retorna que es valida
        boolean encert = true;
        encert = !esRepeteix(lletra, paraulaEncriptada) && partDeLaParaula(lletra, paraula);
        return encert;
    }

    private static boolean esRepeteix(char l, String enProgres) {
        //Comprova que la variable es dins de les lletres encertades, comparant la paraula i manant-la a array de chars igualant la posició al número 1
        boolean repeteix = false;
        char[] prog = enProgres.toCharArray();
        for (int i = 0; i < prog.length; i++) {
            if (l == prog[i]) {
                repeteix = true;
            }
        }
        return repeteix;
    }

    private static boolean partDeLaParaula(char letra, String palabra) {
        boolean esPart = false;
        char[] pa = palabra.toUpperCase().toCharArray();
        for (int i = 0; i < pa.length; i++) {
            if (letra == pa[i]) {
                esPart = true;
            }
        }
        return esPart;
    }

    private static void cambiaLletra() {
        String[] enProg = paraulaEncriptada.split("");
        String[] pal = paraula.split("");
        String reemplazada = "";
        for (int i = 0; i < pal.length; i++) {
            if (String.valueOf(lletra).equalsIgnoreCase(pal[i])) {
                enProg[i] = String.valueOf(lletra);
            }
            reemplazada += enProg[i];
        }
        paraulaEncriptada = reemplazada;
    }

    private static boolean guanyaJoc() {
        if (paraulaEncriptada.equalsIgnoreCase(paraula)) {
            return true;
        } else {
            return false;
        }
    }
}
