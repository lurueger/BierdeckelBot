import java.util.*;
import java.io.*;
import java.awt.geom.*;

class Romans{
  String[] f=new String[255];
  int[] l=new int[255];
  float scale=1.0f;

  
  Hashtable<Integer,Vector<Vector<Point2D.Float>>> t = new Hashtable<Integer,Vector<Vector<Point2D.Float>>>();
  
  Romans(String chrFile) { // now you can open an external font file too.
    l[32]=11; // space is not CHR files!!
    try {
    File fi = new File(chrFile);
    Scanner sc = new Scanner(fi);
    while (sc.hasNextLine()) {
      String line = sc.nextLine(); 
      if(line.equals("")) continue; 
      StringTokenizer st = new StringTokenizer(line);
      if (st.hasMoreTokens()) {
        String c=st.nextToken();
        c=c.substring(4); // hex char number
        int ch = Integer.parseInt(c,16); 
        Vector<Vector<Point2D.Float>> paths = new Vector<Vector<Point2D.Float>>();
        Vector<Point2D.Float> path = new Vector<Point2D.Float>();
        String width=st.nextToken();
        int w = Integer.parseInt(width.substring(0,width.length()-1)); // remove semicolon
        l[ch]=w; // character width array
        while(st.hasMoreElements()) { 
          String t=st.nextToken();
          String[] part=t.split(","); // split two values
          float x = Float.parseFloat(part[0]);
          boolean fin=part[1].endsWith(";");
          if(fin) part[1]=part[1].substring(0,part[1].length()-1);
          float y = Float.parseFloat(part[1]);
          path.add(new Point2D.Float(x,y));
          if(fin || !st.hasMoreElements()) {paths.add(path); path=new Vector<Point2D.Float>();}
        }
        t.put(ch, paths);
      }
    }
    }catch(Exception e) { System.out.println("Exception:"+e); }
  }
  
  Romans() {
    f[0x21]="10; 5,21 5,7; 5,2 4,1 5,0 6,1 5,2";
    f[0x22]="16; 4,21 4,14; 12,21 12,14";
    f[0x23]="21; 11.5,25 4.5,-7; 17.5,25 10.5,-7; 4.5,12 18.5,12; 3.5,6 17.5,6";
    f[0x24]="20; 8,25 8,-4; 12,25 12,-4; 17,18 15,20 12,21 8,21 5,20 3,18 3,16 4,14 5,13 7,12 13,10 15,9 16,8 17,6 17,3 15,1 12,0 8,0 5,1 3,3";
    f[0x25]="24; 21,21 3,0; 8,21 10,19 10,17 9,15 7,14 5,14 3,16 3,18 4,20 6,21 8,21 10,20 13,19 16,19 19,20 21,21; 17,7 15,6 14,4 14,2 16,0 18,0 20,1 21,3 21,5 19,7 17,7";
    f[0x26]="26; 23,12 23,13 22,14 21,14 20,13 19,11 17,6 15,3 13,1 11,0 7,0 5,1 4,2 3,4 3,6 4,8 5,9 12,13 13,14 14,16 14,18 13,20 11,21 9,20 8,18 8,16 9,13 11,10 16,3 18,1 20,0 22,0 23,1 23,2";
    f[0x27]="10; 5,19 4,20 5,21 6,20 6,18 5,16 4,15";
    f[0x28]="14; 11,25 9,23 7,20 5,16 4,11 4,7 5,2 7,-2 9,-5 11,-7";
    f[0x29]="14; 3,25 5,23 7,20 9,16 10,11 10,7 9,2 7,-2 5,-5 3,-7";
    f[0x2A]="16; 8,21 8,9; 3,18 13,12; 13,18 3,12";
    f[0x2B]="26; 13,18 13,0; 4,9 22,9";
    f[0x2C]="10; 6,1 5,0 4,1 5,2 6,1 6,-1 5,-3 4,-4";
    f[0x2D]="26; 4,9 22,9";
    f[0x2E]="10; 5,2 4,1 5,0 6,1 5,2";
    f[0x2F]="22; 20,25 2,-7";
    f[0x30]="20; 9,21 6,20 4,17 3,12 3,9 4,4 6,1 9,0 11,0 14,1 16,4 17,9 17,12 16,17 14,20 11,21 9,21";
    f[0x31]="20; 6,17 8,18 11,21 11,0";
    f[0x32]="20; 4,16 4,17 5,19 6,20 8,21 12,21 14,20 15,19 16,17 16,15 15,13 13,10 3,0 17,0";
    f[0x33]="20; 5,21 16,21 10,13 13,13 15,12 16,11 17,8 17,6 16,3 14,1 11,0 8,0 5,1 4,2 3,4";
    f[0x34]="20; 13,21 3,7 18,7; 13,21 13,0";
    f[0x34]="20; 18,7 3,7 13,21, 13,0";
    f[0x35]="20; 15,21 5,21 4,12 5,13 8,14 11,14 14,13 16,11 17,8 17,6 16,3 14,1 11,0 8,0 5,1 4,2 3,4";
    f[0x36]="20; 16,18 15,20 12,21 10,21 7,20 5,17 4,12 4,7 5,3 7,1 10,0 11,0 14,1 16,3 17,6 17,7 16,10 14,12 11,13 10,13 7,12 5,10 4,7";
    f[0x37]="20; 17,21 7,0; 3,21 17,21";
    f[0x37]="20; 7,0 17,21, 3,21";
    f[0x38]="20; 8,21 5,20 4,18 4,16 5,14 7,13 11,12 14,11 16,9 17,7 17,4 16,2 15,1 12,0 8,0 5,1 4,2 3,4 3,7 4,9 6,11 9,12 13,13 15,14 16,16 16,18 15,20 12,21 8,21";
    f[0x39]="20; 16,14 15,11 13,9 10,8 9,8 6,9 4,11 3,14 3,15 4,18 6,20 9,21 10,21 13,20 15,18 16,14 16,9 15,4 13,1 10,0 8,0 5,1 4,3";
    f[0x3A]="10; 5,14 4,13 5,12 6,13 5,14; 5,2 4,1 5,0 6,1 5,2";
    f[0x3B]="10; 5,14 4,13 5,12 6,13 5,14; 6,1 5,0 4,1 5,2 6,1 6,-1 5,-3 4,-4";
    f[0x3C]="24; 20,18 4,9 20,0";
    f[0x3D]="26; 4,12 22,12; 4,6 22,6";
    f[0x3E]="24; 4,18 20,9 4,0";
    f[0x3F]="18; 3,16 3,17 4,19 5,20 7,21 11,21 13,20 14,19 15,17 15,15 14,13 13,12 9,10 9,7; 9,2 8,1 9,0 10,1 9,2";
    f[0x40]="27; 18.5,13 17.5,15 15.5,16 12.5,16 10.5,15 9.5,14 8.5,11 8.5,8 9.5,6 11.5,5 14.5,5 16.5,6 17.5,8; 12.5,16 10.5,14 9.5,11 9.5,8 10.5,6 11.5,5; 18.5,16 17.5,8 17.5,6 19.5,5 21.5,5 23.5,7 24.5,10 24.5,12 23.5,15 22.5,17 20.5,19 18.5,20 15.5,21 12.5,21 9.5,20 7.5,19 5.5,17 4.5,15 3.5,12 3.5,9 4.5,6 5.5,4 7.5,2 9.5,1 12.5,0 15.5,0 18.5,1 20.5,2 21.5,3; 19.5,16 18.5,8 18.5,6 19.5,5";
    f[0x41]="18; 9,21 1,0; 9,21 17,0; 4,7 14,7";
    f[0x41]="18: 1,0 9,21 17,0; 14,7 4,7";
    f[0x42]="21; 3.5,21 3.5,0; 3.5,21 12.5,21 15.5,20 16.5,19 17.5,17 17.5,15 16.5,13 15.5,12 12.5,11; 3.5,11 12.5,11 15.5,10 16.5,9 17.5,7 17.5,4 16.5,2 15.5,1 12.5,0 3.5,0";
    f[0x42]="21; 3.5,11 12.5,11 15.5,10 16.5,9 17.5,7 17.5,4 16.5,2 15.5,1 12.5,0 3.5,0 3.5,21 12.5,21 15.5,20 16.5,19 17.5,17 17.5,15 16.5,13 15.5,12 12.5,11";
    f[0x43]="21; 18.5,16 17.5,18 15.5,20 13.5,21 9.5,21 7.5,20 5.5,18 4.5,16 3.5,13 3.5,8 4.5,5 5.5,3 7.5,1 9.5,0 13.5,0 15.5,1 17.5,3 18.5,5";
    f[0x44]="21; 3.5,21 3.5,0; 3.5,21 10.5,21 13.5,20 15.5,18 16.5,16 17.5,13 17.5,8 16.5,5 15.5,3 13.5,1 10.5,0 3.5,0";
    f[0x45]="19; 3.5,21 3.5,0; 3.5,21 16.5,21; 3.5,11 11.5,11; 3.5,0 16.5,0";
    f[0x46]="18; 3,21 3,0; 3,21 16,21; 3,11 11,11";
    f[0x47]="21; 18.5,16 17.5,18 15.5,20 13.5,21 9.5,21 7.5,20 5.5,18 4.5,16 3.5,13 3.5,8 4.5,5 5.5,3 7.5,1 9.5,0 13.5,0 15.5,1 17.5,3 18.5,5 18.5,8; 13.5,8 18.5,8";
    f[0x48]="22; 4,21 4,0; 18,21 18,0; 4,11 18,11";
    f[0x49]="8; 4,21 4,0";
    f[0x4A]="16; 12,21 12,5 11,2 10,1 8,0 6,0 4,1 3,2 2,5 2,7";
    f[0x4B]="21; 3.5,21 3.5,0; 17.5,21 3.5,7; 8.5,12 17.5,0";
    f[0x4C]="17; 2.5,21 2.5,0; 2.5,0 14.5,0";
    f[0x4D]="24; 4,21 4,0; 4,21 12,0; 20,21 12,0; 20,21 20,0";
    f[0x4E]="22; 4,21 4,0; 4,21 18,0; 18,21 18,0";
    f[0x4F]="22; 9,21 7,20 5,18 4,16 3,13 3,8 4,5 5,3 7,1 9,0 13,0 15,1 17,3 18,5 19,8 19,13 18,16 17,18 15,20 13,21 9,21";
    f[0x50]="21; 3.5,21 3.5,0; 3.5,21 12.5,21 15.5,20 16.5,19 17.5,17 17.5,14 16.5,12 15.5,11 12.5,10 3.5,10";
    f[0x51]="22; 9,21 7,20 5,18 4,16 3,13 3,8 4,5 5,3 7,1 9,0 13,0 15,1 17,3 18,5 19,8 19,13 18,16 17,18 15,20 13,21 9,21; 12,4 18,-2";
    f[0x52]="21; 3.5,21 3.5,0; 3.5,21 12.5,21 15.5,20 16.5,19 17.5,17 17.5,15 16.5,13 15.5,12 12.5,11 3.5,11; 10.5,11 17.5,0";
    f[0x53]="20; 17,18 15,20 12,21 8,21 5,20 3,18 3,16 4,14 5,13 7,12 13,10 15,9 16,8 17,6 17,3 15,1 12,0 8,0 5,1 3,3";
    f[0x54]="16; 8,21 8,0; 1,21 15,21";
    f[0x55]="22; 4,21 4,6 5,3 7,1 10,0 12,0 15,1 17,3 18,6 18,21";
// Ü
    f[0xDC]="22; 4,21 4,6 5,3 7,1 10,0 12,0 15,1 17,3 18,6 18,21; 6,23 6,25; 16,25 16,23";
    f[0x56]="18; 1,21 9,0; 17,21 9,0";
    f[0x57]="24; 2,21 7,0; 12,21 7,0; 12,21 17,0; 22,21 17,0";
    f[0x58]="20; 3,21 17,0; 17,21 3,0";
    f[0x59]="18; 1,21 9,11 9,0; 17,21 9,11";
    f[0x5A]="20; 17,21 3,0; 3,21 17,21; 3,0 17,0";
    f[0x5B]="14; 4,25 4,-7; 5,25 5,-7; 4,25 11,25; 4,-7 11,-7";
    f[0x5C]="14; 0,21 14,-3";
    f[0x5D]="14; 9,25 9,-7; 10,25 10,-7; 3,25 10,25; 3,-7 10,-7";
    f[0x5E]="16; 6,15 8,18 10,15; 3,12 8,17 13,12; 8,17 8,0";
    f[0x5F]="16; 0,-2 16,-2";
    f[0x60]="10; 6,21 5,20 4,18 4,16 5,15 6,16 5,17";
    f[0x61]="19; 15.5,14 15.5,0; 15.5,11 13.5,13 11.5,14 8.5,14 6.5,13 4.5,11 3.5,8 3.5,6 4.5,3 6.5,1 8.5,0 11.5,0 13.5,1 15.5,3";
    f[0xe1]="19; 15.5,14 15.5,0; 15.5,11 13.5,13 11.5,14 8.5,14 6.5,13 4.5,11 3.5,8 3.5,6 4.5,3 6.5,1 8.5,0 11.5,0 13.5,1 15.5,3; 10,17 13,19";
    f[0x62]="19; 3.5,21 3.5,0; 3.5,11 5.5,13 7.5,14 10.5,14 12.5,13 14.5,11 15.5,8 15.5,6 14.5,3 12.5,1 10.5,0 7.5,0 5.5,1 3.5,3";
    f[0x63]="18; 15,11 13,13 11,14 8,14 6,13 4,11 3,8 3,6 4,3 6,1 8,0 11,0 13,1 15,3";
    f[0x64]="19; 15.5,21 15.5,0; 15.5,11 13.5,13 11.5,14 8.5,14 6.5,13 4.5,11 3.5,8 3.5,6 4.5,3 6.5,1 8.5,0 11.5,0 13.5,1 15.5,3";
    f[0x65]="18; 3,8 15,8 15,10 14,12 13,13 11,14 8,14 6,13 4,11 3,8 3,6 4,3 6,1 8,0 11,0 13,1 15,3";
    f[0xE9]="18; 3,8 15,8 15,10 14,12 13,13 11,14 8,14 6,13 4,11 3,8 3,6 4,3 6,1 8,0 11,0 13,1 15,3; 10,17 13,19";
    f[0x66]="12; 11,21 9,21 7,20 6,17 6,0; 3,14 10,14";
    f[0x67]="19; 15.5,14 15.5,-2 14.5,-5 13.5,-6 11.5,-7 8.5,-7 6.5,-6; 15.5,11 13.5,13 11.5,14 8.5,14 6.5,13 4.5,11 3.5,8 3.5,6 4.5,3 6.5,1 8.5,0 11.5,0 13.5,1 15.5,3";
    f[0x68]="19; 4.5,21 4.5,0; 4.5,10 7.5,13 9.5,14 12.5,14 14.5,13 15.5,10 15.5,0";
    f[0x69]="8; 3,21 4,20 5,21 4,22 3,21; 4,14 4,0";
    f[0xED]="8; 4,14 4,0; 4,17 7,19";
    f[0x6A]="10; 5,21 6,20 7,21 6,22 5,21; 6,14 6,-3 5,-6 3,-7 1,-7";
    f[0x6B]="17; 3.5,21 3.5,0; 13.5,14 3.5,4; 7.5,8 14.5,0";
    f[0x6C]="8; 4,21 4,0";
    f[0x6D]="30; 4,14 4,0; 4,10 7,13 9,14 12,14 14,13 15,10 15,0; 15,10 18,13 20,14 23,14 25,13 26,10 26,0";
    f[0x6E]="19; 4.5,14 4.5,0; 4.5,10 7.5,13 9.5,14 12.5,14 14.5,13 15.5,10 15.5,0";
    f[0xF1]="19; 4.5,14 4.5,0; 4.5,10 7.5,13 9.5,14 12.5,14 14.5,13 15.5,10 15.5,0; 6,18 14,18";
    f[0x6F]="19; 8.5,14 6.5,13 4.5,11 3.5,8 3.5,6 4.5,3 6.5,1 8.5,0 11.5,0 13.5,1 15.5,3 16.5,6 16.5,8 15.5,11 13.5,13 11.5,14 8.5,14";
    f[0xF3]="19; 8.5,14 6.5,13 4.5,11 3.5,8 3.5,6 4.5,3 6.5,1 8.5,0 11.5,0 13.5,1 15.5,3 16.5,6 16.5,8 15.5,11 13.5,13 11.5,14 8.5,14; 10,17 13,19";
    f[0x70]="19; 3.5,14 3.5,-7; 3.5,11 5.5,13 7.5,14 10.5,14 12.5,13 14.5,11 15.5,8 15.5,6 14.5,3 12.5,1 10.5,0 7.5,0 5.5,1 3.5,3";
    f[0x71]="19; 15.5,14 15.5,-7; 15.5,11 13.5,13 11.5,14 8.5,14 6.5,13 4.5,11 3.5,8 3.5,6 4.5,3 6.5,1 8.5,0 11.5,0 13.5,1 15.5,3";
    f[0x72]="13; 3.5,14 3.5,0; 3.5,8 4.5,11 6.5,13 8.5,14 11.5,14";
    f[0x73]="17; 14.5,11 13.5,13 10.5,14 7.5,14 4.5,13 3.5,11 4.5,9 6.5,8 11.5,7 13.5,6 14.5,4 14.5,3 13.5,1 10.5,0 7.5,0 4.5,1 3.5,3";
    f[0x74]="12; 6,21 6,4 7,1 9,0 11,0; 3,14 10,14";
    f[0x75]="19; 4.5,14 4.5,4 5.5,1 7.5,0 10.5,0 12.5,1 15.5,4; 15.5,14 15.5,0";
// ú
    f[0xFA]="19; 4.5,14 4.5,4 5.5,1 7.5,0 10.5,0 12.5,1 15.5,4; 15.5,14 15.5,0; 10,17 13,19 ";
// ü
    f[0xFC]="19; 4.5,14 4.5,4 5.5,1 7.5,0 10.5,0 12.5,1 15.5,4; 15.5,14 15.5,0; 6.5,17 6.5,19; 13.5,19 13.5,17";
    f[0x76]="16; 2,14 8,0; 14,14 8,0";
    f[0x77]="22; 3,14 7,0; 11,14 7,0; 11,14 15,0; 19,14 15,0";
    f[0x78]="17; 3.5,14 14.5,0; 14.5,14 3.5,0";
    f[0x79]="16; 2,14 8,0; 14,14 8,0 6,-4 4,-6 2,-7 1,-7";
    f[0x7A]="17; 14.5,14 3.5,0; 3.5,14 14.5,14; 3.5,0 14.5,0";
    f[0x7B]="14; 9,25 7,24 6,23 5,21 5,19 6,17 7,16 8,14 8,12 6,10; 7,24 6,22 6,20 7,18 8,17 9,15 9,13 8,11 4,9 8,7 9,5 9,3 8,1 7,0 6,-2 6,-4 7,-6; 6,8 8,6 8,4 7,2 6,1 5,-1 5,-3 6,-5 7,-6 9,-7";
    f[0x7C]="8; 4,25 4,-7";
    f[0x7D]="14; 5,25 7,24 8,23 9,21 9,19 8,17 7,16 6,14 6,12 8,10; 7,24 8,22 8,20 7,18 6,17 5,15 5,13 6,11 10,9 6,7 5,5 5,3 6,1 7,0 8,-2 8,-4 7,-6; 8,8 6,6 6,4 7,2 8,1 9,-1 9,-3 8,-5 7,-6 5,-7";
    f[0x7E]="24; 3,6 3,8 4,11 6,12 8,12 10,11 14,8 16,7 18,7 20,8 21,10; 3,8 4,10 6,11 8,11 10,10 14,7 16,6 18,6 20,7 21,10 21,12";
    f[0x7F]="14; 6,21 4,20 3,18 3,16 4,14 6,13 8,13 10,14 11,16 11,18 10,20 8,21 6,21";
    f[0xD1]="22; 4,21 4,0; 4,21 18,0; 18,21 18,0;  8,22 15,22";
    // init
    // space
    l[32]=12;
    for(int i=0; i<255; i++) if(f[i]!=null) {
      StringTokenizer st = new StringTokenizer(f[i]);
        //String c=st.nextToken();
        String width=st.nextToken();
        l[i]= Integer.parseInt(width.substring(0,width.length()-1));
        // now the paths
        Vector<Vector<Point2D.Float>> paths = new Vector<Vector<Point2D.Float>>();
        Vector<Point2D.Float> path = new Vector<Point2D.Float>();
        while(st.hasMoreElements()) { //print(st.nextToken()+"+"); println();
          String t=st.nextToken();
          String[] part=t.split(","); // split two values
          float x = Float.parseFloat(part[0]);
          boolean fin=part[1].endsWith(";");
          if(fin) part[1]=part[1].substring(0,part[1].length()-1);
          float y = Float.parseFloat(part[1]);
          path.add(new Point2D.Float(x,y));
          if(fin || !st.hasMoreElements()) {paths.add(path); path=new Vector<Point2D.Float>();}
        }
        t.put(i,paths); //System.out.println("Char "+i+" "+paths.size());
      }   
    }
    float getLength(int c) { return l[c]*scale;}
    
    int getLength(String line) {
      int total=0;
      for(int j=0; j<line.length(); j++) { 
        char c=line.charAt(j);
        total+=getLength(c);
      }
      return total;
    }
    
   
   Vector<Vector<Point2D.Float>> getChar(int c) {
    return t.get(c);
   } 
   
   
   Vector<Vector<Point2D.Float>> rotate(Vector<Vector<Point2D.Float>> ch, float angle) {
      if(angle==0) return ch;
      Vector<Vector<Point2D.Float>> out = new Vector<Vector<Point2D.Float>>();
      Vector<Vector<Point2D.Float>> paths = new Vector<Vector<Point2D.Float>>();
      for(Vector<Point2D.Float> v : ch) {
           Vector<Point2D.Float> path = new Vector<Point2D.Float>();
           for (Point2D.Float p : v) {
             Point2D.Float p1 = new Point2D.Float(); 
             p1.setLocation(p.x*Math.cos(angle)-p.y*Math.sin(angle),p.y*Math.cos(angle)+p.x*Math.sin(angle));
             path.add(p1);
           }
           paths.add(path);
         }
       out.addAll(paths);
       return out;
   }
   
   Vector<Vector<Point2D.Float>> getString(String line) {
     float x=0;
     Vector<Vector<Point2D.Float>> out = new Vector<Vector<Point2D.Float>>();
     for(int j=0; j<line.length(); j++) { 
       char c=line.charAt(j);
       Vector<Vector<Point2D.Float>> ch = getChar(c); 
       Vector<Vector<Point2D.Float>> paths = new Vector<Vector<Point2D.Float>>();
       if(ch!=null)
         for(Vector<Point2D.Float> v : ch) {
           Vector<Point2D.Float> path = new Vector<Point2D.Float>();
           for (Point2D.Float p : v) path.add(new Point2D.Float(p.x*scale+x,p.y*scale));
           paths.add(path);
         }
       out.addAll(paths);
       x+=getLength(c);
     }
     return out; 
   }
   
   String gcodeString(String line, float x, float y) {
    String out=new String();
    for(int j=0; j<line.length(); j++) { 
       char c=line.charAt(j);
       out+=gcodeChar(c,x,y);
       x+=getLength(c);
    }
    return out;
   }
   
   String gcodeChar(char c, float x, float y) { // x and y are the offset for that char gcode
    return gcodeChar( getChar(c), x, y );
   }
   
   String gcodeChar(Vector<Vector<Point2D.Float>> p, float x, float y) { // p not null
     String out=new String("");
     if(p==null) return "";
     for(Vector<Point2D.Float> v : p)  {
        Point2D.Float old=v.get(0); out+="G0 X"+(old.x+x)+" Y"+(old.y+y)+"\nG1 Z-72.685\n";
        for(int j=1;j<v.size();j++) {
            Point2D.Float n=v.get(j);
            out+="G1 X"+(n.x+x)+" Y"+(n.y+y)+"\n";
        }
        out+="G0 Z-70\n";
     }
     return out;
  }
  
  String svgChar(Vector<Vector<Point2D.Float>> p, float x, float y) {
    String out=new String("<g stroke=\"blue\" stroke-width=\"2\" fill=\"none\"><path d=\"");
     if(p==null) return "";
     for(Vector<Point2D.Float> v : p)  {
        Point2D.Float old=v.get(0); out+="M "+(old.x+x)+" "+(y-old.y);
        for(int j=1;j<v.size();j++) {
            Point2D.Float n=v.get(j);
            out+=" L "+(n.x+x)+" "+(y-n.y);
        }
     }
     out+="\" /></g>";
     return out;
  }
  
  void scale(float s) {scale=s; }
  
  public static void main(String[] args) {
   float x=0,y=0,scale=1,angle=0;
   switch(args.length) {
      case 0: System.out.println("\nUsage: java Romans \"text\" [offsetX] [offsetY] [scale] [angle]\n"); return;
      case 5: angle=Float.parseFloat(args[4]);
      case 4: scale=Float.parseFloat(args[3]);
      case 3: y=    Float.parseFloat(args[2]);
      case 2: x=    Float.parseFloat(args[1]);
      case 1: { 
                String line=args[0];
                Romans font = new Romans();
                font.scale = scale;
                Vector<Vector<Point2D.Float>> p = font.getString(line); 
                System.out.println("G21\n"+font.gcodeChar(font.rotate(p,angle),x,y));
              }
     }
  } 
}

