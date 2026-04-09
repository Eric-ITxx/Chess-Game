package Driver;

import Frontend.Loginpage;
import gamelogic.ComputerPlayer;

import java.util.Scanner;

public class driverclass {
    public static void main(String[] args) {
        try {
            Scanner sc = new Scanner(System.in);
            System.out.println("Welcom to The Chess game");
            System.out.println("Please enter a number to play the " +
                    "desired gametype from the menu ");
            System.out.println("1.Play on The console ");
            System.out.println("2.Play on The GUI ");
            switch (sc.nextInt()) {
                case 1:
                    if (sc.nextInt() == 1) {
                        System.out.println("Please choose a gametype from the menu ");
                        System.out.println("1.Play PvsP ");
                        System.out.println("2.Play Vs Freind on LAN");
                        System.out.println("3.Play vs Computer");
                        sc.nextInt();
                        switch (sc.nextInt()) {
                            case 1 -> PvsPonConsole.main(args);
                            case 2 -> landriverclass.main(args);
                            case 3 -> landriverclass.main(args);
                            default -> throw new IllegalArgumentException("Invalid Input");
                        }

                    }
                    break;
                case 2:
                    Loginpage.main(args);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid Input");
            }
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


}
