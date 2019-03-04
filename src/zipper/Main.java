package zipper;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.*;
import javax.swing.*;

public class Main extends JFrame
{

    public Main()
    {
        this.setTitle("Zipper");
        this.setBounds(275, 300, 250, 250);
        this.setJMenuBar(pasekMenu);

        JMenu menuPlik = pasekMenu.add(new JMenu("Plik"));

        Action akcjaDodawania = new Akcja("Dodaj", "Dodaj nowy plik", "ctrl D");
        Action akcjaUsuwania = new Akcja("Usuń", "Usuń zaznaczony plik", "ctrl U");
        Action akcjaZipowania = new Akcja("Zip", "Utwórz ZIP", "ctrl Z");

        JMenuItem menuOtworz = menuPlik.add(akcjaDodawania);
        JMenuItem menuUsun = menuPlik.add(akcjaUsuwania);
        JMenuItem menuZip = menuPlik.add(akcjaZipowania);

        bDodaj = new JButton(akcjaDodawania);
        bUsun = new JButton(akcjaUsuwania);
        bZip = new JButton(akcjaZipowania);
        JScrollPane scroll = new JScrollPane(lista);
        lista.setBorder(BorderFactory.createEtchedBorder());

        GroupLayout layout = new GroupLayout((this.getContentPane()));

        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addComponent(scroll, 100, 150, Short.MAX_VALUE)
                        .addContainerGap(0, Short.MAX_VALUE)
                        .addGroup(
                                layout.createParallelGroup().addComponent(bDodaj).addComponent(bUsun).addComponent(bZip)
                        )
        );
        layout.setVerticalGroup(
                layout.createParallelGroup()
                        .addComponent(scroll, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup().addComponent(bDodaj).addComponent(bUsun).addGap(5, 40, Short.MAX_VALUE).addComponent(bZip))
        );


        this.getContentPane().setLayout(layout);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.pack();
    }

    private DefaultListModel modelListy = new DefaultListModel()
    {
        @Override
        public void addElement(Object obj) //dzięki temu lista wyświetla same nazwy, a nie całe ścieżki
        {
            lista.add(obj);
            super.addElement(((File)obj).getName());
        }

        @Override
        public Object get(int index) //dodawanie do ArrayList ścieżki plików
        {
            return lista.get(index);
        }

        @Override
        public Object remove(int index) //należy usunąć z ArrayList scieżki plików oraz nazwy z modelListy
        {
            lista.remove(index);
            return super.remove(index);
        }
        ArrayList lista = new ArrayList();
    };
    private JList lista = new JList(modelListy);
    private final JButton bDodaj;
    private final JButton bUsun;
    private final JButton bZip;
    private final JMenuBar pasekMenu = new JMenuBar();
    private JFileChooser chooser = new JFileChooser();

    public static void main(String[] args) {

        new Main().setVisible(true);

    }

    private class Akcja extends AbstractAction
    {
        public Akcja(String nazwa, String opis, String kSkrot)
        {
            this.putValue(Action.NAME, nazwa);
            this.putValue(Action.SHORT_DESCRIPTION, opis);
            this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(kSkrot));

        }
        public Akcja(String nazwa, String opis, String kSkrot, Icon ikona)
        {
            this(nazwa, opis, kSkrot);
            this.putValue(Action.SMALL_ICON, ikona);    //nadanie buttonom ikonek - do rozwinięcia potem
        }
        @Override
        public void actionPerformed(ActionEvent e)
        {
            switch (e.getActionCommand()) {
                case "Dodaj":
                    dodajWpis();
                    break;
                case "Usuń":
                    usuwanieWpisow();
                    break;
                case "Zip":
                    tworzenieArchiwumZip();
                    break;
                default:
                    break;
            }
        }

        private void dodajWpis()
        {
            chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setMultiSelectionEnabled(enabled);
            int tmp = chooser.showDialog(rootPane, "Dodaj do archiwum ");

            if (tmp == JFileChooser.APPROVE_OPTION)
            {
                File[] sciezki = chooser.getSelectedFiles();

                for (File sciezki1 : sciezki) {
                    if (!czyWpisSiePowtarza(sciezki1.getPath()))
                        modelListy.addElement(sciezki1);
                }
            }
        }

        private boolean czyWpisSiePowtarza(String testowanyWpis)
        {
            for (int i = 0; i < modelListy.getSize(); i++)
                if (((File)modelListy.get(i)).getPath().equals(testowanyWpis))
                    return true;
            return false;
        }

        private void usuwanieWpisow()   //usuwamy ścieżki z ArrayList i nazwy z modelListy
        {
            int[] tmp = lista.getSelectedIndices();

            for (int i = 0; i < tmp.length; i++)
            {
                modelListy.remove(tmp[i] - i);
            }
        }

        public static final int BUFFOR = 1024;
        private void tworzenieArchiwumZip()
        {
            chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            chooser.setSelectedFile(new File(System.getProperty("user.dir") + File.separator + "mojanazwa.zip"));
            int tmp = chooser.showDialog(rootPane, "Kompresuj");

            if (tmp == JFileChooser.APPROVE_OPTION)
            {
                byte tmpData[] = new byte[BUFFOR];
                try
                {
                    ZipOutputStream zOutS = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(chooser.getSelectedFile()),BUFFOR));

                    for (int i = 0; i < modelListy.getSize(); i++)
                    {
                        if (!((File)modelListy.get(i)).isDirectory())   //jeżeli nie jest folderem to robimy
                            zipuj(zOutS, (File)modelListy.get(i), tmpData, ((File)modelListy.get(i)).getPath());
                        else
                        {
                            wypiszSciezki((File)modelListy.get(i));

                            for (int j = 0; j <listaSciezek.size(); j++)
                                zipuj(zOutS, (File)listaSciezek.get(j), tmpData, ((File)modelListy.get(i)).getPath());

                            listaSciezek.removeAll(listaSciezek);
                        }
                    }

                    zOutS.close();
                }
                catch(IOException e)
                {
                    System.out.println(e.getMessage());
                }
            }
        }
        private void zipuj(ZipOutputStream zOutS, File sciezkaPliku, byte[] tmpData, String sciezkaBazowa) throws IOException
        {
            BufferedInputStream inS = new BufferedInputStream(new FileInputStream(sciezkaPliku), BUFFOR);
            zOutS.putNextEntry(new ZipEntry(sciezkaPliku.getPath().substring(sciezkaBazowa.lastIndexOf(File.separator)+1)));

            int counter;
            while ((counter = inS.read(tmpData, 0, BUFFOR)) != -1)
                zOutS.write(tmpData, 0, counter);

            zOutS.closeEntry();
            inS.close();
        }

        private void wypiszSciezki(File nazwaSciezki)
        {
            String[] nazwyPlikowIKatalogow = nazwaSciezki.list();

            for (String nazwyPlikowIKatalogow1 : nazwyPlikowIKatalogow) {
                File p = new File(nazwaSciezki.getPath(), nazwyPlikowIKatalogow1);
                if (p.isFile())
                    listaSciezek.add(p);
                if (p.isDirectory())
                    //System.out.println(p.getPath());
                    wypiszSciezki(new File(p.getPath()));
            }
        }

        ArrayList listaSciezek = new ArrayList();

    }

}
