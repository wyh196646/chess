package Go;

import java.awt.*;
import java.util.*;
import javax.swing.*;

public class Go extends Panel
{
    int whichStep;
    Hashtable myHash;
    Point pointNow;//当前的点
    Point STARTPOINT;
    int INTERVAL;
    Vector vec;
    Point robPoint;
    Point mousePoint;
    boolean errorFlag;

    //构建器
    public Go()
    {
        super();
        pointNow=new Point(1000,1000);//把初始红点画在外面
        errorFlag=false;//行棋错误标志
        whichStep=0;
        STARTPOINT=new Point(40,40);
        INTERVAL=85;
        myHash=new Hashtable();
        robPoint=null;//打劫点
        mousePoint=new Point();//开辟鼠标点内存
        vec=new Vector();//存放校验的子
        this.initMyHash(STARTPOINT,INTERVAL);
        try
            {
                jbInit();
            }
        catch(Exception e)
            {
                e.printStackTrace();
            }

    }
    //初始化hashtable
    void initMyHash(Point startPoint,int interval)
    {
        One one;
        Point key;//逻辑点标志
        int i,j;
        for(i=1;i<=5;i++)
            for(j=1;j<=5;j++)
                {
                    key=new Point(i,j);
                    one=new One();
                    one.posX=startPoint.x+(i-1)*interval+10;
                    one.posY=startPoint.y+(j-1)*interval+10;
                    //获取相邻点
                    one.pointAround[0]=new Point(i,j-1);//上
                    one.pointAround[1]=new Point(i,j+1);//下
                    one.pointAround[2]=new Point(i-1,j);//左
                    one.pointAround[3]=new Point(i+1,j);//右
                    if(i==1)one.pointAround[2]=one.OUT;
                    if(i==5)one.pointAround[3]=one.OUT;
                    if(j==1)one.pointAround[0]=one.OUT;
                    if(j==5)one.pointAround[1]=one.OUT;

                    myHash.put(key,one);
                }
    }


    //更新盘面
    public void paint(Graphics g)
    {
        Point startPoint=STARTPOINT;
        int interval=INTERVAL;
        this.paintChessboard(g,startPoint,interval);
        this.paintChessman(g,startPoint,interval);
    }
    //画棋盘
    void paintChessboard(Graphics g,Point startPoint,int interval)
    {
        int pX=startPoint.x;
        int pY=startPoint.y;
        int LINELENGTH=interval*4;
        int i;
        for(i=0;i<5;i++)
            {
                g.drawLine(pX+i*interval,pY,pX+i*interval,pY+LINELENGTH);
                g.drawLine(pX,pY+i*interval,pX+LINELENGTH,pY+i*interval);
            }
        // g.drawRect(pX-3,pY-3,366,366);
    }
    //加棋子
    void paintChessman(Graphics g,Point startPoint,int interval)
    {
        int pX=startPoint.x;
        int pY=startPoint.y;
        Enumeration Enumer=myHash.elements();
        while(Enumer.hasMoreElements())
            {
                One one=(One)Enumer.nextElement();
                if(one.color!=one.BLANK)
                    {
                        if(one.color==one.BLACK)
                            g.setColor(Color.black);
                        else if(one.color==one.WHITE)
                            g.setColor(Color.white);
                        else
                            break;
                        int d=interval/2;
                        g.fillOval(one.posX-d/2-10,one.posY-d/2-10,d,d);
                        //画黑圈
                        g.setColor(Color.black);
                        g.drawOval(one.posX-d/2-10,one.posY-d/2-10,d,d);
                    }
            }
        g.setColor(Color.red);//画红点
        // System.out.println(this.pointNow.x+","+this.pointNow.y);
        g.fillOval(this.pointNow.x*INTERVAL-55,this.pointNow.y*INTERVAL-55,20,20);
    }
    //处理每一步

    boolean checkValid(Point whatPoint,int whatColor){
        // this.updateHash(whatPoint,whatColor);
        // this.getRival(whatPoint,whatColor);
        // System.out.println(((One)myHash.get(whatPoint)).color!=0);
        // System.out.println(this.isRob(whatPoint));
        // System.out.println((!this.isLink(whatPoint,whatColor)&&!this.isLink(whatPoint,0)));
        if( ((One)myHash.get(whatPoint)).color!=0 || // you zi
            this.isRob(whatPoint) || // kai jie
            (!this.isLink(whatPoint,whatColor)&&!this.isLink(whatPoint,0)) // qi or jilei
            )
            {
                return false;
            }
        return true;
    }
    void doStep(Point whatPoint,int whatColor)
    {
        //如果点在盘外，返回
        // System.out.println("info :     "+whatPoint.x+"  "+whatPoint.y);
        // System.out.println(((One)myHash.get(whatPoint)).color);
        if(whatPoint.x<1||whatPoint.x>5||whatPoint.y<1||whatPoint.y>5)
            {
                this.showError("不能下在此处");
                this.errorFlag=true;
                return;
            }
        //如果点上有子，则返回
        if(((One)myHash.get(whatPoint)).color!=0)
            {
                this.showError("此处已有子");
                this.errorFlag=true;
                return;
            }
        if(this.isRob(whatPoint))
            {
                this.showError("已经开劫，请先应劫");
                this.errorFlag=true;
                return;
            }

        this.updateHash(whatPoint,whatColor);
        this.getRival(whatPoint,whatColor);
        //如果没有气也没有己类
        if(!this.isLink(whatPoint,whatColor)&&!this.isLink(whatPoint,0))//0相当于ｏｎｅ．ＢＬＡＮＫ
            {
                this.showError("此处不可放子");
                this.errorFlag=true;
                this.singleRemove(whatPoint);
                return;
            }
        this.pointNow.x=whatPoint.x;
        this.pointNow.y=whatPoint.y;//得到当前红点
        this.repaint();

    }

    //取异类并判断执行吃子
    void getRival(Point whatPoint,int whatColor)
    {
        boolean removeFlag=false;//判断这一步到底吃没吃子
        One one;
        one=(One)(this.myHash.get(whatPoint));
        Point otherPoint[]=one.pointAround;
        int i;
        for(i=0;i<4;i++)
            {
                One otherOne=(One)(this.myHash.get(otherPoint[i]));//举出异类实例
                if(!otherPoint[i].equals(one.OUT))
                    if(otherOne.color!=one.BLANK&&otherOne.color!=whatColor)
                        {
                            if(this.isRemove(otherPoint[i]))//如果有气
                                this.vec.clear();
                            else
                                {
                                    this.makeRobber(otherPoint[i]);
                                    this.doRemove();
                                    this.vec.clear();
                                    removeFlag=true;
                                }
                        }
            }
        if(!removeFlag)
            this.robPoint=null;//如果没吃子的话消掉打劫点

    }
    //判断是否因打劫不能下
    boolean isRob(Point p)
    {
        if(this.robPoint==null)
            return false;
        if(this.robPoint.x==p.x&&this.robPoint.y==p.y)
            return true;
        return false;
    }
    //建立打劫点
    void makeRobber(Point point)
    {
        if(this.vec.size()==1)
            this.robPoint=point;//建立新打劫点
        else
            this.robPoint=null;//吃多个的话消掉打劫点
    }
    //判断吃子
    boolean isRemove(Point point)
    {
        if(this.vec.contains(point))
            return false;
        if(this.isLink(point,0))//有气的话
            return true;
        this.vec.add(point);//没有气就加入这个点
        One one;
        one=(One)(this.myHash.get(point));
        Point otherPoint[]=one.pointAround;
        int i;
        for(i=0;i<4;i++)
            {
                One otherOne=(One)(this.myHash.get(otherPoint[i]));//举出同类实例
                if(!otherPoint[i].equals(one.OUT))
                    if(otherOne.color==one.color)
                        if(this.isRemove(otherPoint[i]))//这里递归
                            return true;
            }
        return false;

    }
    //执行消子
    void doRemove()
    {
        Enumeration Enumer=this.vec.elements();
        while(Enumer.hasMoreElements())
            {
                Point point=(Point)Enumer.nextElement();
                this.singleRemove(point);
            }
    }
    //消单个子
    void singleRemove(Point point)
    {
        One one=(One)(this.myHash.get(point));
        one.isthere=false;
        one.color=one.BLANK;
        Graphics g=this.getGraphics();
        //删除画面上的子
    }

    //判断有气
    boolean isLink(Point point,int color)
    {
        One one;
        one=(One)(this.myHash.get(point));
        Point otherPoint[]=one.pointAround;
        int i;
        for(i=0;i<4;i++)
            {
                One otherOne=(One)(this.myHash.get(otherPoint[i]));
                if(!otherPoint[i].equals(one.OUT))
                    if(otherOne.color==color)
                        {
                            return true;
                        }
            }
        return false;

    }
    //每一步更新myHash
    void updateHash(Point whatPoint,int whatColor)
    {
        One one=(One)(this.myHash.get(whatPoint));
        one.isthere=true;
        one.color=whatColor;
        this.whichStep=this.whichStep+1;
        one.whichStep=this.whichStep;
    }

    //用四舍五入计算逻辑点位置
    //p1为真实点，p2为相对原点
    Point getMousePoint(Point p1,Point p2)
    {
        // System.out.println(p1.x+","+p1.y);
        // System.out.println(p2.x+","+p2.y);
        this.mousePoint.x=Math.round((float)(p1.x-p2.x)/this.INTERVAL);
        this.mousePoint.y=Math.round((float)(p1.y-p2.y)/this.INTERVAL);
        // System.out.println(this.mousePoint.x+","+this.mousePoint.y);
        return this.mousePoint;
    }
    //显示错误信息
    void showError(String errorMessage)
    {
        Graphics g=this.getGraphics();
        g.setColor(new Color(235,190,98));
        g.fillRect(20,415,200,30);
        g.setColor(Color.red);
        g.drawString(errorMessage,60,430);
        g.fillOval(20,415,20,20);

    }
    private void jbInit() throws Exception
    {
        this.setBackground(new Color(235, 190, 98));
    }
}
