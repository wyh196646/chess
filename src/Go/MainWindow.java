package Go;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import javax.swing.JFrame;
import java.util.Scanner;//导入java.util包下的Scanner类
import javax.swing.JOptionPane;//导入java.swing包下的JOptionPane类
import java.util.Random;//生成随机数





public class MainWindow extends Frame implements Runnable
{
    Go panelGo=new Go();
    Image myImage;
    int PORT;
    Socket sendSocket;//主动连接Socket
    PrintWriter writer;//用来发送message
    boolean stopFlag;
    boolean isInitiative;
    Point messagePoint;
    Point goStartPoint=null;
    Point yellowPoint=null;
    Point AIPoint=null;
    boolean stepColor=true;
    Point LastPoint=null;//移除黄点时，判断位置变动
    BorderLayout borderLayout1 = new BorderLayout();
    Panel panel1 = new Panel();
    Panel panel2 = new Panel();
    BorderLayout borderLayout2 = new BorderLayout();
    Panel panel3 = new Panel();
    CheckboxGroup checkboxGroup1 = new CheckboxGroup();
    Checkbox checkbox1 = new Checkbox();
    Checkbox checkbox2 = new Checkbox();
    Checkbox checkbox3 = new Checkbox();
    Label label1 = new Label();
    TextField textField1 = new TextField();
    Button button1 = new Button();
    Label label2 = new Label();
    Choice choice1 = new Choice();
    Button button2 = new Button();
    GridLayout gridLayout1 = new GridLayout();
    BorderLayout borderLayout3 = new BorderLayout();


    
    boolean rz=true;
    int rzCnt=0;

    public MainWindow()
    {
        try
            {
                jbInit();
            }
        catch(Exception e)
            {
                e.printStackTrace();
            }
    }
    private void jbInit() throws Exception
    {
        choice1.setBackground(new Color(236, 190, 98));
        button1.setBackground(new Color(236, 190, 98));
        //       try
        //       {
        //          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        //       }catch(Exception e){e.printStackTrace();}
        this.setResizable(false);
        new Thread(this).start();//启动监听线程
        this.PORT=3000;
        this.isInitiative=false;//是否主动连接
        this.stopFlag=false;//是否继续监听的标志
        this.choice1.addItem("black");
        this.choice1.addItem("white");
        LastPoint=new Point();
        messagePoint=new Point();
        this.setSize(550,500);
        this.setTitle("Go       Author:Windy");
        this.panelGo.setEnabled(false);//开始之前屏蔽掉盘面
        checkbox1.addMouseListener(new java.awt.event.MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    checkbox1_mouseClicked(e);
                }
            });
        // this.goStartPoint=this.panelGo.gUetLocation();//
        this.goStartPoint=new Point(-45,-45);//
        this.setLayout(borderLayout1);
        panel1.setLayout(borderLayout2);
        checkbox1.setCheckboxGroup(checkboxGroup1);
        checkbox1.setLabel("Single");
        checkbox2.setCheckboxGroup(checkboxGroup1);
        checkbox2.setLabel("Online");
        checkbox3.setCheckboxGroup(checkboxGroup1);
        checkbox3.setLabel("Man-machine");
        checkbox2.addMouseListener(new java.awt.event.MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    checkbox2_mouseClicked(e);
                }
            });
        
        checkbox3.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                checkbox3_mouseClicked(e);
            }
        });

        label1.setText("Address");
        button1.setLabel("Connect");
        button1.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    button1_actionPerformed(e);
                }
            });
        label2.setText("  ");
        button2.setBackground(new Color(236, 190, 98));
        button2.setLabel("Start");
        button2.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    button2_actionPerformed(e);
                }
            });
        panel3.setLayout(gridLayout1);
        gridLayout1.setRows(8);
        gridLayout1.setColumns(1);
        gridLayout1.setHgap(100);
        gridLayout1.setVgap(10);
        panel2.setLayout(borderLayout3);
        this.panel2.setSize(500,300);
        panelGo.addMouseMotionListener(new java.awt.event.MouseMotionAdapter()
            {
                public void mouseMoved(MouseEvent e)
                {
                    panelGo_mouseMoved(e);
                }
            });

        panelGo.addMouseListener(new java.awt.event.MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    panelGo_mouseClicked(e);
                }
            });

        this.addWindowListener(new java.awt.event.WindowAdapter()
            {
                public void windowClosing(WindowEvent e)
                {
                    this_windowClosing(e);
                }
            });
        
        panel3.setBackground(new Color(236, 190, 98));
        panel3.add(checkbox1, null);
        panel3.add(checkbox2, null);
        panel3.add(checkbox3, null);
        panel3.add(choice1, null);
        panel3.add(textField1, null);
        panel3.add(button1, null);
        panel3.add(button2, null);
        panel3.add(label2, null);
        this.panel1.add(this.panelGo,BorderLayout.CENTER);
        this.panel1.add(panel3, BorderLayout.EAST);
        this.add(panel2, BorderLayout.SOUTH);
        this.add(panel1, BorderLayout.CENTER);
        this.disableLink();//废掉控件
        //this.button2.setEnabled(false);//废掉开始
        this.checkboxGroup1.setSelectedCheckbox(this.checkbox1);
        this.yellowPoint=new Point(1000,1000);//初始化一个世纪外的黄点
        this.centerWindow();

        this.show();
        myImage=this.createImage(32,32);//用来纪录有黄点之前的图像
    }
    void centerWindow()
    {
        Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
        int pX=(d.width-this.getWidth()/2);
        int pY=(d.height-this.getHeight()/2);
        this.setLocation(pX,pY);
    }

    public static void main(String args[])
    { 
    	MainWindow main=new MainWindow();
    }
    

    //监听线程
    public void run()
    {
        try
            {
                ServerSocket serverSocket=new ServerSocket(PORT);
                Socket receiveSocket=null;
                receiveSocket=serverSocket.accept();
                if(this.isInitiative)//如果已在进行，则不接受连接
                    this.stopFlag=true;
                this.checkboxGroup1.setSelectedCheckbox(this.checkbox2);//自动选择联机
                this.button1.setEnabled(false);
                this.choice1.setEnabled(true);
                this.textField1.setEnabled(false);
                this.checkbox1.setEnabled(false);
                this.checkbox2.setEnabled(false);
                this.writer=new PrintWriter(receiveSocket.getOutputStream(),true);
                BufferedReader reader=new BufferedReader(new InputStreamReader(receiveSocket.getInputStream()));
                String message;
                while(!this.stopFlag)
                    {
                        this.panelGo.showError("接收连接成功");
                        message=reader.readLine();
                        this.doMessage(message);

                    }
                reader.close();
                receiveSocket.close();
                serverSocket.close();
            }catch(IOException ioe){this.panelGo.showError("意外中断");}

    }
    //处理接收到的东东
    void doMessage(String message)
    {
        if(message.startsWith("start"))//判断开始
            {
                this.panelGo.showError("对方已开始");
                if(message.equals("start_black"))
                    this.choice1.select("black");
                else
                    this.choice1.select("white");
                if(this.choice1.getSelectedItem().equals("black"))//只要你是黑的，就先走
                    this.panelGo.setEnabled(true);
                this.paintMyColor();//表明颜色
                this.disableLink();
            }
        else//下子的信息
            {
                int color=Integer.parseInt(message.substring(0,1));
                this.messagePoint.x=Integer.parseInt(message.substring(1,3));
                this.messagePoint.y=Integer.parseInt(message.substring(3,5));
                this.panelGo.setEnabled(true);//解禁
                this.panelGo.doStep(this.messagePoint,color);
            }


    }
    

    //为鼠标定位
    void panelGo_mouseMoved(MouseEvent e)
    {
        Point realPoint=e.getPoint();
        Point mousePoint=this.panelGo.getMousePoint(realPoint,this.goStartPoint);
        this.removeLastMousePoint(this.LastPoint,mousePoint);
        //this.LastPoint=mousePoint;大错误，使对象公用了一个地址
        this.LastPoint.x=mousePoint.x;
        this.LastPoint.y=mousePoint.y;
        if(this.isPlace(mousePoint) )
            this.showMousePoint(mousePoint);        	
    }
    //加黄点的范围
    boolean isPlace(Point p)
    {
        if(p.x>5||p.x<1||p.y<1||p.y>5)
            return false;
        int color;
        One one;
        one=(One)(this.panelGo.myHash.get(p));
        color=one.color;
        if(color!=0)
            return false;
        return true;


    }

    void showEnded()
    {
        String endedInfo="Game Ended";
        Graphics g=this.getGraphics();
        g.setColor(new Color(235,190,98));
        g.fillRect(20,415,200,30);
        g.setColor(Color.red);
        g.drawString(endedInfo,60,430);
        g.fillOval(20,415,20,20);
    }

    void panelGo_mouseClicked(MouseEvent e)
    {
        this.showEnded();
        if(this.isSingle())
            {
                this.doSingle();

            }

            
        else if(this.isMan_Machine())
        {
        	if(this.choice1.getSelectedItem().equals("black"))
                this.doStep();
        	else {
        		this.doStep1();
        		
        	}
        	
        }
        else
            {
                this.doMultiple();
            }
    }
    
    //开始

    void quit_quit(ActionEvent e){
        System.exit(0);
    }
    
    void button2_actionPerformed(ActionEvent e)
    {

        if(e.getActionCommand().equals("Start"))
            {
                this.disableLink();
                this.checkbox1.setEnabled(false);
                this.checkbox2.setEnabled(false);
                this.checkbox3.setEnabled(false);
                this.button2.setLabel("Exit");
                if(this.isSingle())
                    this.panelGo.setEnabled(true);
                else if(this.isMan_Machine()) {
                	this.panelGo.setEnabled(true);
                if(this.choice1.getSelectedItem().equals("white")) {
                rzCnt=0;
                	while(rz==true && rzCnt<2) {
                		Random r=new Random();
                    	AIPoint=new Point();
                    	do {
                    		this.panelGo.errorFlag=false;
                	    	if(this.stepColor)
                	    	{   
                	    		this.AIPoint.x=r.nextInt(5);
                	    		this.AIPoint.y=r.nextInt(5);
                	            this.panelGo.doStep(this.AIPoint,1);
                	            
                	    	}
                    	}while( this.panelGo.errorFlag );
                        if(!this.panelGo.errorFlag)//如果不违例，则换颜色
                            {

                                    this.stepColor=this.stepColor;
                                    this.paintThisColor(this.stepColor);
                              
                            }
                        ++rzCnt;
                	}
                }
                }
                	
                else//联机版时
                    {
                        if(this.choice1.getSelectedItem().equals("黑"))
                            {
                                this.writer.println("start_black");

                            }
                        else
                            this.writer.println("start_white");
                    }
                this.paintMyColor();//表明颜色
            }
        else if(e.getActionCommand().equals("Exit"))
            {
                this.dispose();
                System.exit(0);
            }
    }
    
    
    //disable联机时用的控件
    void disableLink()
    {
        this.textField1.setBackground(new Color(236, 190, 98));
        this.textField1.setEnabled(false);
        this.choice1.setEnabled(false);
        this.button1.setEnabled(false);
    }
    //enable联机时的控件
    void enableLink()
    {
        this.textField1.setBackground(Color.white);
        this.textField1.setEnabled(true);
        this.choice1.setEnabled(true);
        this.button1.setEnabled(true);
    }
    void enableLink1()
    {
    	this.choice1.setEnabled(true);
    }
    
    
    //判断类型
    boolean isSingle()
    {
        return this.checkbox1.getState();
    }

    boolean isMan_Machine() {
    	return this.checkbox3.getState();
    }
    
    void single()
    {

    }

    void multiple()
    {
    }

    //加小黄点
    void showMousePoint(Point mousePoint)
    {
        // System.out.println(mousePoint.x+","+mousePoint.y);
        Graphics g=this.panelGo.getGraphics();
        g.setColor(Color.yellow);
        int d=this.panelGo.INTERVAL/3;
        int x=mousePoint.x*this.panelGo.INTERVAL-d/2-45;
        int y=mousePoint.y*this.panelGo.INTERVAL-d/2-45;
        g.fillOval(x,y,d,d);
        this.yellowPoint.x=mousePoint.x;//定位黄点
        this.yellowPoint.y=mousePoint.y;

        Graphics myG=this.myImage.getGraphics();
        this.createMyImage(myG,this.yellowPoint,0);
    }
    
    //消除前一个黄点
    void removeLastMousePoint(Point thatPoint,Point thisPoint)
    {
        if(thatPoint.x!=thisPoint.x||thatPoint.y!=thisPoint.y)
            {
                Graphics g=this.panelGo.getGraphics();
                if(this.yellowPoint!=null&&this.myImage!=null){
                    int x=(this.yellowPoint.x-1)*this.panelGo.INTERVAL+45-16-5;
                    int y=(this.yellowPoint.y-1)*this.panelGo.INTERVAL+45-16-5;
                    g.drawImage(this.myImage,x,y,32,32,null);
                }
                this.yellowPoint.x=1000;//不在范围内，就一边去
                this.yellowPoint.y=1000;

            }
    }

    //构成所需要的Image
    void createMyImage(Graphics g,Point thisPoint,int color)
    {
        int px=thisPoint.x;
        int py=thisPoint.y;
        Color myColor=this.panelGo.getBackground();

        if(px==1&&py==1&&color==0)
        {
             g.setColor(myColor);
             g.fillRect(0,0,32,32);
             g.setColor(Color.black);
             g.drawLine(16,16,16,32);
             g.drawLine(16,16,32,16);
        }
        else if(px==1&&py==5&&color==0)
        {
             g.setColor(myColor);
             g.fillRect(0,0,32,32);
             g.setColor(Color.black);
             g.drawLine(16,16,16,0);
             g.drawLine(16,16,32,16);
        }
        else if(px==5&&py==1&&color==0)
        {
             g.setColor(myColor);
             g.fillRect(0,0,32,32);
             g.setColor(Color.black);
             g.drawLine(16,16,0,16);
             g.drawLine(16,16,16,32);
        }
        else if(px==5&&py==5&&color==0)
        {
             g.setColor(myColor);
             g.fillRect(0,0,32,32);
             g.setColor(Color.black);
             g.drawLine(16,16,0,16);
             g.drawLine(16,16,16,0);
        }
        else if(px==1&&(py==2||py==3||py==4)&&color==0) {
        	g.setColor(myColor);
            g.fillRect(0,0,32,32);
            g.setColor(Color.black);
            g.drawLine(16,16,32,16);
            g.drawLine(16,0,16,32);
        }
        else if(px==5&&(py==2||py==3||py==4)&&color==0) {
        	g.setColor(myColor);
            g.fillRect(0,0,32,32);
            g.setColor(Color.black);
            g.drawLine(16,16,0,16);
            g.drawLine(16,0,16,32);
        }
        else if(py==1&&(px==2||px==3||px==4)&&color==0) {
        	g.setColor(myColor);
            g.fillRect(0,0,32,32);
            g.setColor(Color.black);
            g.drawLine(16,16,16,32);
            g.drawLine(0,16,32,16);
        }
        else if(py==5&&(px==2||px==3||px==4)&&color==0) {
        	g.setColor(myColor);
            g.fillRect(0,0,32,32);
            g.setColor(Color.black);
            g.drawLine(16,16,16,0);
            g.drawLine(0,16,32,16);
        }
        else if(color==0)
        {
        	g.setColor(myColor);
            g.fillRect(0,0,32,32);
            g.setColor(Color.black);
            g.drawLine(16,0,16,32);
            g.drawLine(0,16,32,16);
        	
        }
        

        //八个小黑点
        // else if(color==0&&((px==4&&py==4)||(px==4&&py==10)||(px==4&&py==16)||(px==10&&py==4)||(px==10&&py==10)||(px==10&&py==16)||(px==16&&py==4)||(px==16&&py==10)||(px==16&&py==16)))
        //     {
        //         g.setColor(myColor);
        //         g.fillRect(0,0,16,16);
        //         g.setColor(Color.black);
        //         g.drawLine(0,8,16,8);
        //         g.drawLine(8,0,8,16);
        //         g.fillOval(5,5,6,6);
        //     }

        // else if(color==0)
        //     {
        //         g.setColor(myColor);
        //         g.fillRect(0,0,16,16);
        //         g.setColor(Color.black);
        //         g.drawLine(0,8,16,8);
        //         g.drawLine(8,0,8,16);
        //     }



    }

    boolean checkEnd(){   
    	boolean flag=false;
        for(int i=1;i<=5;++i){
            for(int j=1;j<=5;++j){
                Point np=new Point(i,j);
                int color=this.stepColor?1:2;
                if( this.panelGo.checkValid(np,color) ){
                    flag=true;
                    break;
                }
            }
        }
        if( flag==false ){

            int cntWhite=0;
            int cntBlack=0;
            for(int i=1;i<=5;++i){
                for(int j=1;j<=5;++j){
                    Point np=new Point(i,j);
                    One one=(One)(this.panelGo.myHash.get(np));
                    int color=one.color;
                    if(  color==1 ){
                        ++cntBlack;
                    }
                    else if( color==2 ){
                        ++cntWhite;
                    }
                    else if( color==0 ){
                        if( true );
                        else;
                    }
                }
            }
            if(this.stepColor)
            	cntWhite=25-cntBlack;
            else
            	cntBlack=25-cntWhite;
            System.out.println("white: "+cntWhite+",  black: "+cntBlack);
            if(cntWhite>cntBlack) {
            	JOptionPane.showMessageDialog(null, "white win!!");
            }
            	
            else {
            	JOptionPane.showMessageDialog(null, "black win!!");
            }
            return true;
        }
        else {
        	return false;
        }
      

}


    //单机版走步
    void doSingle()
    {
    	while(rz==true && rzCnt<1) {
    		if(this.stepColor)
                this.panelGo.doStep(this.yellowPoint,1);
            else
                this.panelGo.doStep(this.yellowPoint,2);
    	    this.stepColor=this.stepColor;
            this.paintThisColor(this.stepColor);
            ++rzCnt;
    	}
        if(this.stepColor) {
            this.panelGo.doStep(this.yellowPoint,1);
            this.yellowPoint.x=1000;//刚走的子不至于删掉
            this.yellowPoint.y=1000;
        }
        else {
            this.panelGo.doStep(this.yellowPoint,2);
            this.yellowPoint.x=1000;
            this.yellowPoint.y=1000;
        }


        if(!this.panelGo.errorFlag)//如果不违例，则换颜色
            {
                if( rz==true && rzCnt<1 ){
                    this.stepColor=this.stepColor;
                    this.paintThisColor(this.stepColor);
                    ++rzCnt;
                }

                else{
                    this.stepColor=!this.stepColor;
                    this.paintThisColor(this.stepColor);
                }
            }
        else{
            this.panelGo.errorFlag=false;
            this.yellowPoint.x=1000;
            this.yellowPoint.y=1000;
        }
        if(this.checkEnd()) {
        	System.out.println("Game Ended");
            // this.panelGo.showError("Game End");
            this.panelGo.setEnabled(false);
        	return;     	
        }
    }
    
    void doStep()
    {
    	while(rz==true && rzCnt<1) {
    		if(this.stepColor) {
                this.panelGo.doStep(this.yellowPoint,1);
                this.yellowPoint.x=1000;//刚走的子不至于删掉
                this.yellowPoint.y=1000;
            }
            else {
                this.panelGo.doStep(this.yellowPoint,2);
                this.yellowPoint.x=1000;
                this.yellowPoint.y=1000;
            }
    	    this.stepColor=this.stepColor;
            this.paintThisColor(this.stepColor);
            ++rzCnt;
    	}
    	if(this.stepColor) {
            this.panelGo.doStep(this.yellowPoint,1);
            this.yellowPoint.x=1000;
            this.yellowPoint.y=1000;
        }
        else {
            this.panelGo.doStep(this.yellowPoint,2);
            this.yellowPoint.x=1000;
            this.yellowPoint.y=1000;
        }


        if(!this.panelGo.errorFlag)//如果不违例，则换颜色
        {
                this.stepColor=!this.stepColor;
                this.paintThisColor(this.stepColor);
            }
        else {
            this.panelGo.errorFlag=false;
        	this.yellowPoint.x=1000;
        	this.yellowPoint.y=1000;
        	return;
        }
        if(this.checkEnd()) {
        	System.out.println("Game Ended");
            // this.panelGo.showError("Game End");
            this.panelGo.setEnabled(false);
        	return;
        }
        
        Random r=new Random();
    	AIPoint=new Point();
    	do {
    		this.panelGo.errorFlag=false;
	    	if(this.stepColor)
	    	{   
	    		this.AIPoint.x=r.nextInt(5);
	    		this.AIPoint.y=r.nextInt(5);
	            this.panelGo.doStep(this.AIPoint,1);
	            
	    	}
	        else {
	    		this.AIPoint.x=r.nextInt(5)+1;
	    		this.AIPoint.y=r.nextInt(5)+1;
	            this.panelGo.doStep(this.AIPoint,2);
	            
	        }
    	}while( this.panelGo.errorFlag );
        if(!this.panelGo.errorFlag)//如果不违例，则换颜色
            {

                    this.stepColor=!this.stepColor;
                    this.paintThisColor(this.stepColor);
              
            }
        else {
            this.panelGo.errorFlag=false;
        	this.yellowPoint.x=1000;
        	this.yellowPoint.y=1000;
        	return;
        }
        
        if(this.checkEnd()) {
        	System.out.println("Game Ended");
            // this.panelGo.showError("Game End");
            this.panelGo.setEnabled(false);
        }
    }
    
    void doStep1()
    {
    	if(this.stepColor) {
            this.panelGo.doStep(this.yellowPoint,2);
            this.yellowPoint.x=1000;
            this.yellowPoint.y=1000;
        }
        else {
            this.panelGo.doStep(this.yellowPoint,1);
            this.yellowPoint.x=1000;
            this.yellowPoint.y=1000;
        }


        if(!this.panelGo.errorFlag)//如果不违例，则换颜色
        {
                this.stepColor=!this.stepColor;
                this.paintThisColor(this.stepColor);
            }
        else {
            this.panelGo.errorFlag=false;
        	this.yellowPoint.x=1000;
        	this.yellowPoint.y=1000;
        	return;
        }
        if(this.checkEnd()) {
        	System.out.println("Game Ended");
            // this.panelGo.showError("Game End");
            this.panelGo.setEnabled(false);
        	return;
        }
        
    	Random r=new Random();
    	AIPoint=new Point();
    	do {
    		this.panelGo.errorFlag=false;
	    	if(this.stepColor)
	    	{   
	    		this.AIPoint.x=r.nextInt(5);
	    		this.AIPoint.y=r.nextInt(5);
	            this.panelGo.doStep(this.AIPoint,2);
	            
	    	}
	        else {
	    		this.AIPoint.x=r.nextInt(5)+1;
	    		this.AIPoint.y=r.nextInt(5)+1;
	            this.panelGo.doStep(this.AIPoint,1);
	            
	        }
    	}while( this.panelGo.errorFlag );
        if(!this.panelGo.errorFlag)//如果不违例，则换颜色
            {

                    this.stepColor=!this.stepColor;
                    this.paintThisColor(!this.stepColor);
              
            }
        else {
            this.panelGo.errorFlag=false;
        	this.yellowPoint.x=1000;
        	this.yellowPoint.y=1000;
        	return;
        }
        
        if(this.checkEnd()) {
        	System.out.println("Game Ended");
            // this.panelGo.showError("Game End");
            this.panelGo.setEnabled(false);
            return;
        }
    } 
    
    //联机版走步
    void doMultiple()
    {
        int color;
        if(this.choice1.getSelectedItem().equals("黑"))
            color=1;
        else
            color=2;

        this.panelGo.doStep(this.yellowPoint,color);
        //如果走法不对，返回
        if(this.panelGo.errorFlag)
            {
                this.panelGo.errorFlag=false;
                return;
            }
        this.panelGo.setEnabled(false);
        String message=this.getMessage(color,this.yellowPoint.x,this.yellowPoint.y);
        this.writer.println(message);
        this.yellowPoint.x=99;//刚走的子不至于删掉，还要可以解析
        this.yellowPoint.y=99;
    }
    //处理发送字符串
    String getMessage(int color,int x,int y)
    {
        String strColor=String.valueOf(color);
        String strX;
        String strY;
        //如果单数的话，就加一位
        if(x<10)
            strX="0"+String.valueOf(x);
        else
            strX=String.valueOf(x);

        if(y<10)
            strY="0"+String.valueOf(y);
        else
            strY=String.valueOf(y);

        return strColor+strX+strY;
    }

    void this_windowClosing(WindowEvent e)
    {
        this.dispose();
        System.exit(0);
    }

    void checkbox2_mouseClicked(MouseEvent e)
    {
        this.enableLink();
    }

    void checkbox1_mouseClicked(MouseEvent e)
    {
        this.disableLink();
    }
    
    void checkbox3_mouseClicked(MouseEvent e)
    {
        this.enableLink1();
    }
    
    void button1_actionPerformed(ActionEvent e)
    {
        this.goToLink(this.textField1.getText().trim(),this.PORT);
    }
    //主动连接serverSocket
    void goToLink(String hostName,int port)
    {
        try
            {
                this.stopFlag=true;
                this.sendSocket=new Socket(hostName,port);
                this.panelGo.showError("连接成功！！");
                this.choice1.setEnabled(true);
                this.button1.setEnabled(false);
                this.checkbox1.setEnabled(false);
                this.checkbox2.setEnabled(false);
                this.textField1.setEnabled(false);
                this.writer=new PrintWriter(this.sendSocket.getOutputStream(),true);
                new Listen(sendSocket,this).start();
            }catch(IOException ioe){this.panelGo.showError("意外中断");}
    }

    //开始时根据颜色画己方颜色
    void paintMyColor()
    {
        Graphics g=this.label2.getGraphics();
        if(this.choice1.getSelectedItem().equals("black"))
            g.fillOval(20,10,30,30);
        else
        {
        	g.setColor(Color.white);
            g.fillOval(20,10,30,30);
            g.setColor(Color.black);
            g.drawOval(20,10,30,30);
        }
        
    }
    //单机版画每步颜色
    void paintThisColor(boolean whatColor)
    {
        Graphics g=this.label2.getGraphics();
        if(whatColor)
            g.fillOval(20,10,30,30);
        else
            {
                g.setColor(Color.white);
                g.fillOval(20,10,30,30);
                g.setColor(Color.black);
                g.drawOval(20,10,30,30);
            }
    }
}
