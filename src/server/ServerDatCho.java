package server;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author HLuat
 */
public class ServerDatCho{
    private JButton btnStart;
    private JTextArea txtThongTin;
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    private final String DAT_CHO = "datcho";
    private final String VI_TRI = "vitri";
    private Thread startServer;
    
    
    public void showThongTin(String hoTen, String viTri, String date){
        txtThongTin.append("Vi tri: " + viTri + " da duoc dat: " + hoTen + " vao luc " + date + "\n");
    }
    
    
    private void taoFile(){
        File file = new File("datcho.txt");
        try {
            if(!file.exists())
                file.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(ServerDatCho.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    }
    
    public void createWindow(){
        JFrame frame = new JFrame("Server ... ");
        
        
        JLabel lblThongTin = new JLabel("Thông tin đặt chỗ");
        btnStart = new JButton("Start Server");
        txtThongTin = new JTextArea(10, 20);
        JScrollPane scroller = new JScrollPane(txtThongTin);
        
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        txtThongTin.setEnabled(false);
        
        frame.getContentPane().add(BorderLayout.NORTH, btnStart);
        frame.getContentPane().add(BorderLayout.WEST, lblThongTin);
        frame.getContentPane().add(BorderLayout.CENTER, scroller);
        
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });
        
        
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        
        taoFile();
    }
    
    
    private void btnStartActionPerformed(java.awt.event.ActionEvent evt){
        
        btnStart.setText("Stop server");
        txtThongTin.append("Server is running at port: " + 4421 + "\n");
        startServer = new Thread(new HandlerServer());
        startServer.start();
    }
    
    public static void main(String[] args) {
        ServerDatCho server = new ServerDatCho();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                server.createWindow();            
            }
        });
       
    }

    
    class HandlerServer implements Runnable {

        /**
        * 
        */
        public void startServer(){
            ServerSocket server = null;
            try {
                server = new ServerSocket(4421);

                Socket connection = null;
                while(true){
                    connection = server.accept();
                    InputStreamReader in = new InputStreamReader(connection.getInputStream());
                    BufferedReader buff = new BufferedReader(in);
                    String request = buff.readLine();
                    if(DAT_CHO.equals(request)){
                        datCho(buff, connection);
                    }
                    else if (VI_TRI.equals(request)){
                        getViTri(connection);
                    }
                    buff.close();
                    connection.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ServerDatCho.class.getName()).log(Level.SEVERE, null, ex);         
            } finally {
                if(server != null)
                    try {
                        server.close();
                } catch (IOException ex) {
                    Logger.getLogger(ServerDatCho.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
       }



        private void datCho(BufferedReader buff, Socket connect) throws IOException{
            boolean success = true;
            String tenVaViTri = buff.readLine();

            String[] temp = tenVaViTri.split("_");
            Date ngayDat = new Date(System.currentTimeMillis());
            String date = formatter.format(ngayDat);
            System.out.println(tenVaViTri);
            System.out.println(date);
            List<String> datChos = docThongTinDatCho();
            for(String datCho : datChos){
                String []str = datCho.split(",");
                String viTri = str[1];
                if(viTri.equals(temp[1])){
                    success = false;
                    break;
                }

            }
            StringBuilder ttDatCho = new StringBuilder(temp[0]);
            ttDatCho.append(",").append(temp[1]).append(",").append(date);
            PrintWriter out = new PrintWriter(connect.getOutputStream());
            if(success){
                luuThongTinDatCho(ttDatCho.toString());
                showThongTin(temp[0], temp[1], date);
                out.println("success");
            } else {
                out.println("failed");
            }
            out.close();

       }
       
       
        private void getViTri(Socket connect) throws IOException{
            List<String> datChos = docThongTinDatCho();
            PrintWriter printer = new PrintWriter(connect.getOutputStream());
            for(String datCho : datChos){
                String []str = datCho.split(",");
                String viTri = str[1];
                printer.println(viTri);
            }

            printer.close();
        }

        private List<String> docThongTinDatCho() throws FileNotFoundException, IOException {
            File file = new File ("datcho.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));

            List<String> result = new ArrayList<String>();
            String thongTin = null;
            while((thongTin =  reader.readLine()) != null){
                result.add(thongTin);
            }
            reader.close();
            return result;
        }

        private void luuThongTinDatCho(String ttDatCho) throws IOException{
            File file = new File ("datcho.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));

            writer.write(ttDatCho + "\n");
            writer.close();
        }
    
        @Override
        public void run() {
            startServer();
        }
        
    }
    
}
