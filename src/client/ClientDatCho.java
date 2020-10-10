package client;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author HLuat
 */
public class ClientDatCho {
    
    private Socket socket;
    private Map<Integer, JButton> viTriMap;
    private JButton btnDatCho;
    private JButton btnPosition;
    private JTextField txtHoTen;
    private JTextField txtViTri;
    private boolean isRunning = true;
    private Thread background;
    
    /**
     * Mở kết nối tới server
     * @throws IOException 
     */
    public void connectToServer() throws IOException{
        
        socket = new Socket("localhost", 4421);
    }
    
    
    /**
     * Đặt chỗ
     * 
     * @param hoTen
     * @param viTri
     * @throws IOException 
     */
    public boolean datCho(String hoTen, Integer viTri) throws IOException{
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        StringBuilder str = new StringBuilder(hoTen);
        str.append("_")
                .append(viTri);
        out.println("datcho");
        out.println(str.toString());
        out.flush();
        
        BufferedReader buff = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String msgResult = buff.readLine();
        out.close();
        buff.close();
        socket.close();
        if(msgResult != null && msgResult.equals("success"))
            return true;
        return false;
    }
    
    
   
    
    private void khoiTao(){
        background = new Thread(new HandlerBackGround());
        background.start();
    }
    
    
    /**
     * Tạo cửa sổ
     */
    public void createWindow(){
        JFrame window = new JFrame("Dat cho");
        viTriMap = new HashMap<>();
         
        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();
        GridLayout grid = new GridLayout(5, 9, 20, 30); // Create a layout manager
        Container container = window.getContentPane(); // Get the content pane
        container.add(BorderLayout.NORTH, panel1);
        container.add(BorderLayout.CENTER, panel2);
        
        panel1.setLayout(grid);
        
        //tạo button thể hiện vị trí chỗ ngồi
        Integer count = 1;
        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 9; j++){           
                JButton btn = new JButton(count.toString());
                
                //thêm sự kiện cho các button vị trí
                btn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        btnViTriAction(e);
                    }
                });
                viTriMap.put(count, btn);
                
                //btn.setEnabled(false);
                panel1.add(btn);
                count++;
            }
        }
        
        panel2.setLayout(new FlowLayout(FlowLayout.CENTER)); // Create a layout manager
        
        //thông tin khách đặt chỗ
        JLabel lblHoTen = new JLabel("Họ tên");
        txtHoTen = new JTextField(20);
        panel2.add(lblHoTen);
        panel2.add(txtHoTen);
        
        JLabel lblViTri = new JLabel("Vị trí");
        txtViTri = new JTextField(10);
        //text vị trí ko cho edit
        txtViTri.setEditable(false);
        btnDatCho = new JButton("Đặt chỗ");
        panel2.add(lblViTri);
        panel2.add(txtViTri);
        panel2.add(btnDatCho);
        
        //bắt sự kiện click của 
        btnDatCho.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnDatChoAction(e);
            }
        });
        
        
        // Set window size, set hiển thị cửa sổ
        window.setSize(800, 400);
        window.setLocationRelativeTo(null); 
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
        
        //khoi tao;
        khoiTao();
    }
    
    private void btnViTriAction(ActionEvent event){
        btnPosition = (JButton) event.getSource();
        txtViTri.setText(btnPosition.getText());
    }
    

    public void btnDatChoAction(ActionEvent event) {
        
        try {
            connectToServer();
            boolean success = datCho( txtHoTen.getText(), Integer.parseInt(txtViTri.getText()) );
            if(success)
                btnPosition.setEnabled(false);
            else
                JOptionPane.showMessageDialog(null, "Đặt chỗ thất bại", "Lỗi đặt chỗ", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            Logger.getLogger(ClientDatCho.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) {
        ClientDatCho client = new ClientDatCho();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               client.createWindow();
            }
        });
    }

    
    
    /**
     * inner class
     * class xử lý vị trí chỗ ngồi 30s
     */
    class HandlerBackGround implements Runnable{
        
//         /**
//     * Lấy thông tin vị trí chỗ ngồi
//     * @throws IOException 
//     */
//    public void getViTri() throws IOException{
//       
//        PrintWriter writer = new PrintWriter(socket.getOutputStream());
//        
//        writer.write(request);
//        writer.flush();
//        
//        InputStreamReader inputReader = new InputStreamReader(socket.getInputStream());
//        BufferedReader buff = new BufferedReader(inputReader);
//        String temp;
//        while((temp = buff.readLine()) != null){
//            JButton button = viTriMap.get(Integer.parseInt(temp));
//            button.setEnabled(false);
//        }
//        
//    }
        
        private void refreshChoNgoi() throws IOException{
            Socket connection = new Socket("localhost", 4421);
            String request = "vitri";
                
            //gửi yêu cầu
            PrintWriter output = new PrintWriter(connection.getOutputStream());
            output.println(request);
            output.flush();

            BufferedReader buff = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            //nhận kết quả
            String result = null;
            while( (result = buff.readLine()) != null){
                int viTri = Integer.parseInt(result);
                JButton btnViTri = viTriMap.get(viTri);
                
                //loại bỏ button đã disable ra khỏi map
                //viTriMap.remove(viTri);
                btnViTri.setEnabled(false);
            }
            output.close();
            buff.close();
            connection.close();
        }
       
       
        @Override
        public void run() {
            
            try {
                while(isRunning){
                    
                    refreshChoNgoi();
                    Thread.sleep(30000);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientDatCho.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                 Logger.getLogger(ClientDatCho.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
