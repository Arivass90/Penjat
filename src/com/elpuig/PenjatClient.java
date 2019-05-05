package com.elpuig;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;

public class PenjatClient {

    private static final int INACTIU = 0;
    private static final int JUGANT = 1;
    private static final int VICTORIA = 2;
    private static final int DERROTA = 3;
    private static final int NOU_JOC = 4;
    private static final int JOC_ACABAT = 5;
    private static  int opcioClient ;
    private static BufferedReader in = null;
    private static DataOutputStream out = null;
    private static Socket conexio;
    private static String ip;
    private static int port;
    private static int estatJoc = INACTIU;
    private static int intentsRestants = 6;
    private static char lletra = '*';
    private static String paraulesEnProgres = "*";
    private static Integer missatge = 0;



    public static void main(String[] args) {
        while(opcioClient!=-1){
            // Demanem IP i port per pantalla
            Scanner scan = new Scanner(System.in);
            System.out.println("Introdueix la teva IP");
            ip = scan.nextLine();
            System.out.println("Introdueix el teu port");
            port=scan.nextInt();
            scan.nextLine();
            // Declarem el menu
            System.out.println("\n########### PENJAT ###########\n");
            System.out.println("------------ Menu ------------\n");
            System.out.println("1) Començar Partida");
            System.out.println("2) Sortir");

            opcioClient = scan.nextInt();
            scan.nextLine();

            switch (opcioClient) {

                case 1:

                    System.out.println("Començem el Joc");
                    iniciJoc();
                    break;

                case 2:
                    System.out.println(" Gracies per jugar");
                    opcioClient = -1;

                    break;
                default :
                    System.out.println("OPCIó INCORRECTA, ; Has d´elegir l´opció 1 o 2 !");
                    break;

            }

        }
    }
    public static void iniciJoc() {
        try {


            // Iniciem la connexió manant-li els paràmetres IP i PORT donat pel client
            conexio = new Socket(ip,port);
            // La variable entrada usuari guarda la lletra que posa l´usuari
            BufferedReader entradaUsuari = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("iniciant Conexió amb el servidor...");
            in = new BufferedReader(new InputStreamReader(conexio.getInputStream()));
            out = new DataOutputStream(conexio.getOutputStream());
            System.out.println("conexió realitzada...");
            boolean jugant = true;
            //Creem una variable booleana per controlar la sortida del bucle

            while (jugant == true) {
                // si l'estat del joc és inactiu o acabat canvia l'estat a Nou joc així sempre començarà de nou
                if (estatJoc == INACTIU || estatJoc == JOC_ACABAT) {
                    setEstatJoc(NOU_JOC);
                    enviarMissatge();
                }

                else {
                    llegirMissatge(in.readLine());
                    // si l'estat del joc es JUGANT demanarà lletra
                    if (estatJoc == JUGANT) {
                        // executa el metode
                        imprimirMissatgePerPantalla();
                        System.out.println("ingresa una lletra: ");
                        // Emmagatzema la lletra a la variable entradaUsuari
                        setLletra(entradaUsuari.readLine().charAt(0));
                        enviarMissatge();
                    }

                    else {
                        //Si l'estat del joc és victòria, canviem l'estat del joc a JOC ACABAT
                        if (estatJoc == VICTORIA) {
                            setEstatJoc(JOC_ACABAT);
                            imprimirMissatgePerPantalla();
                            //Canviem la variable booleana a false per sortir del bucle.
                            jugant = false;
                            System.out.println("HAS ENCERTAT LA PARAULA, HAS GUANYAT EL JOC DEL PENJAT!");
                        }
                        // Si l'estat del joc es derrota canviem l'estat del joc a ACABAT
                        if (estatJoc == DERROTA) {
                            setEstatJoc(JOC_ACABAT);
                            imprimirMissatgePerPantalla();
                            //Canviem la variable booleana a false per sortir del bucle.
                            jugant = false;
                            System.out.println("Quina llastima, No queden intents.\nHAS PERDUT EL JOC");
                        }
                        System.out.println("El joc ha finalitzat.");
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println("No es possible realitzar la connexio, el servidor es inactiu.");
        }
    }

    private static void enviarMissatge() {
        try {
            // Quan l'estat del lloc és nou joc, passem les dades del mètode crearMissatgeResposta a bytes
            if (estatJoc == NOU_JOC) {
                out.writeBytes(crearMissatgeResposta());
            }

            else {
                out.writeBytes(crearMissatgeResposta());
            }
        }
        catch (IOException iOException) {
            System.out.println("Error al enviar el missatge");
        }
    }



    public static void setEstatJoc(int estatJoc) {
        //Actualitza l'estat del joc
        PenjatClient.estatJoc = estatJoc;
    }

    public static void setLletra(char lletra) {
        //Carrega la lletra que ha posat el client
        PenjatClient.lletra = lletra;

        if (lletra != '*') {
            imprimirMissatgePerPantalla();
            System.out.println("introdueixi una lletra: ");
        }
    }


    public static void llegirMissatge(String sms) {
        StringTokenizer stk = new StringTokenizer(sms, "#");
        // Recull les dades del mètode llegirmissatge i els converteix a tokens dividits pel delimitador #
        while (stk.hasMoreTokens()) {
            estatJoc = Integer.valueOf(stk.nextToken());
            intentsRestants = Integer.valueOf(stk.nextToken());
            lletra = stk.nextToken().charAt(0);
            paraulesEnProgres = stk.nextToken();
            missatge = Integer.valueOf(stk.nextToken());
        }
    }
    public static String crearMissatgeResposta() {

        // Retorna el nou estat del joc + el número d'intents restants + la lletra de l'usuari + paraula encriptada + missatge
        return estatJoc + "#" + intentsRestants + "#" + lletra + "#" + paraulesEnProgres + "#" + missatge + "\n";
    }

    private static void imprimirMissatgePerPantalla() {
        //Retorna el mètode que imprimeix el joc - els intents restants + l'estat de la paraula xifrada + el número d'intents restants
        System.out.println("########### PENJAT ###########");
        imprimeixPenjat(intentsRestants);
        System.out.println("\nParaula actual: " + getParaulaActualXifrada());
        System.out.println("Intents restants: " + intentsRestants);
    }

    private static String getParaulaActualXifrada() {
        // Farem una array, després introduïm la paraula xifrada separada per espais en blanc i després les recorrem amb un for per retornar els caràcters únics
        String[] a = paraulesEnProgres.split("");
        String impr = "";
        for (int i = 0; i < a.length; i++) {
            impr += a[i] + " ";
        }
        return impr;
    }

    private static void imprimeixPenjat(int intentsRestants) {
        // Imprimeix el joc
        switch (intentsRestants) {

            case 6:
                System.out.println(" ---------------------");
                for (int j = 0; j < 15; j++) {
                    System.out.println(" |");
                }
                System.out.println("__");
                break;

            case 5:
                System.out.println(" ---------------------");
                System.out.println(" |                     |");
                System.out.println(" |                     |");
                System.out.println(" |                  -------");
                System.out.println(" |                 | -  -  |");
                System.out.println(" |                 |   o   |");
                System.out.println(" |                  -------");
                for (int j = 0; j < 10; j++) {
                    System.out.println(" |");

                }
                System.out.println("__");
                break;

            case 4:
                System.out.println(" ---------------------");
                System.out.println(" |                     |");
                System.out.println(" |                     |");
                System.out.println(" |                  -------");
                System.out.println(" |                 | -  -  |");
                System.out.println(" |                 |   o   |");
                System.out.println(" |                  -------");
                System.out.println(" |                     |   ");
                System.out.println(" |                     |   ");
                System.out.println(" |                     |   ");
                System.out.println(" |                     |   ");
                System.out.println(" |                     |   ");
                for (int j = 0; j < 5; j++) {
                    System.out.println(" |");

                }
                System.out.println("__");
                break;

            case 3:
                System.out.println(" ---------------------");
                System.out.println(" |                     |");
                System.out.println(" |                     |");
                System.out.println(" |                  -------");
                System.out.println(" |                 | -  -  |");
                System.out.println(" |                 |   o   |");
                System.out.println(" |                  -------");
                System.out.println(" |                     |   ");
                System.out.println(" |                   / |   ");
                System.out.println(" |                 /   |   ");
                System.out.println(" |                /    |   ");
                System.out.println(" |                     |   ");
                for (int j = 0; j < 5; j++) {
                    System.out.println(" |");

                }
                System.out.println("__");
                break;

            case 2:
                System.out.println(" ---------------------");
                System.out.println(" |                     |");
                System.out.println(" |                     |");
                System.out.println(" |                  -------");
                System.out.println(" |                 | -  -  |");
                System.out.println(" |                 |   o   |");
                System.out.println(" |                  -------");
                System.out.println(" |                     |   ");
                System.out.println(" |                   / | \\ ");
                System.out.println(" |                  /  |   \\ ");
                System.out.println(" |                 /   |     \\ ");
                System.out.println(" |                     |   ");
                for (int j = 0; j < 5; j++) {
                    System.out.println(" |");

                }
                System.out.println("__");
                break;

            case 1:
                System.out.println(" ---------------------");
                System.out.println(" |                     |");
                System.out.println(" |                     |");
                System.out.println(" |                  -------");
                System.out.println(" |                 | -  -  |");
                System.out.println(" |                 |   o   |");
                System.out.println(" |                  -------");
                System.out.println(" |                     |   ");
                System.out.println(" |                   / | \\ ");
                System.out.println(" |                  /  |   \\ ");
                System.out.println(" |                 /   |     \\ ");
                System.out.println(" |                     |   ");
                System.out.println(" |                    /  ");
                System.out.println(" |                   /      ");
                System.out.println(" |                  /       ");
                for (int j = 0; j < 2; j++) {
                    System.out.println(" |");

                }
                System.out.println("__");
                break;

            case 0:
                System.out.println(" ---------------------");
                System.out.println(" |                     |");
                System.out.println(" |                     |");
                System.out.println(" |                  -------");
                System.out.println(" |                 | X  X  |");
                System.out.println(" |                 |   o   |");
                System.out.println(" |                  -------");
                System.out.println(" |                     |   ");
                System.out.println(" |                   / | \\ ");
                System.out.println(" |                  /  |   \\ ");
                System.out.println(" |                 /   |     \\ ");
                System.out.println(" |                     |   ");
                System.out.println(" |                    / \\");
                System.out.println(" |                   /   \\  ");
                System.out.println(" |                  /     \\ ");
                for (int j = 0; j < 2; j++) {
                    System.out.println(" |");

                }
                System.out.println("__");
                System.out.println("Ha sigut penjat...");
                break;
        }
    }
}