
/*
	Written by fefe
	Wang Yongxin @ CS15 THU
	All rights reserved
*/

import java.io.*;
import java.lang.*;

public class FormattedInputStream{
	private boolean success;
	private PushbackInputStream ins;
	private static char deliminator[]={'\n','\r','\t',' ',','};
	private String stringRead;
	private int contentPos;
	public FormattedInputStream(InputStream Ins){
		ins=new PushbackInputStream(Ins,1024);
		success=true;
		stringRead="";
		contentPos=0;
	}

	public int read()throws IOException{
		return ins.read();
	}

	public void unread(int i)throws IOException{
		ins.unread(i);
	}

	public int available()throws IOException{
		return ins.available();
	}

	private void clear(){
		stringRead="";
		contentPos=0;
	}

	private void setContentPos(int i){
		contentPos=i;
	}

	private void setContentPos(){
		contentPos=stringRead.length();
	}

	private void readCrlf()throws IOException{
		char ch;
		stringRead="";
		contentPos=0;
		while(true){
			if(ins.available()==0)break;
			ch=(char)ins.read();
			if(ch!='\r'&&ch!='\n'){
				ins.unread(ch);
				break;
			}
			stringRead=stringRead+ch;
		}
	}

	private void readDeleminator()throws IOException{
		int i;
		char ch;
		while(true){
			if(ins.available()==0)break;
			ch=(char)ins.read();
			for(i=0;i<deliminator.length;i++){
				if(ch==deliminator[i])break;
			}
			if(i==deliminator.length){
				ins.unread(ch);
				break;
			}
			stringRead=stringRead+ch;
		}

	}

	private boolean readSpecificChar(char[] chlist,int startPos,int endPos)throws IOException{
		char ch;
		int i;
		ch=(char)ins.read();
		for(i=startPos;i<endPos;i++)
		{
//			System.out.println("SPC: "+chlist[i]+" "+ch);
			if(ch==chlist[i]){
				stringRead=stringRead+ch;
				return true;
			}
		}
		ins.unread(ch);
		return false;
	}

	private String readDecimalString()throws IOException{
		String res;
		char ch;
		res="";
		while(ins.available()>0){
			ch=(char)ins.read();
			if((ch<'0'||ch>'9') ){
				ins.unread(ch);
				break;
			}
			res=res+ch;
		}
		stringRead=stringRead+res;
		return res;
	}

	private void pushBack()throws IOException{
		int i;
		for(i=stringRead.length()-1;i>=0;i--){
			ins.unread(stringRead.charAt(i));
		}
		stringRead="";
		contentPos=0;
	}

	private boolean hasContent(){
		return contentPos<stringRead.length();
	}

	public char readAsciiChar()throws IOException{
		char ch;
		readCrlf();
		if(ins.available()!=0){
			ch=(char)ins.read();
			success=true;
		}
		else{
			ch=0;
			pushBack();
			success=false;
		}
		clear();
		return ch;
	}

	public int readInt()throws IOException{
		return (int)readLong();
	}

	public long readLong()throws IOException{
		char chlist[]={'+','-'};
		String res;
		int intval;
		readDeleminator();
		setContentPos();
		readSpecificChar(chlist,0,2);
		res=readDecimalString();
		if(res.equals("")){
			pushBack();
			success=false;
			return 0;
		}
		else{
			success=true;
			try{
				intval=Integer.parseInt(stringRead.substring(contentPos));//new String(barr,0,i));
			}
			catch(NumberFormatException e){
				System.out.println(e.getCause());
				System.out.println(e.getMessage());
				e.printStackTrace(System.out);
				pushBack();
				success=false;
				intval=0;
			}
			clear();
			return intval;
		}
	}

	public float readFloat()throws IOException{
		return (float)readDouble();
	}

	public double readDouble()throws IOException{
		String res1="";
		String res2="";
		char chlist[]={'+','-','.'};
		boolean hasdot;
		double doubleval;
		readDeleminator();
		setContentPos();
		readSpecificChar(chlist,0,2);//Sign
		res1=readDecimalString();
		hasdot=readSpecificChar(chlist,2,3);//Dot
		if(hasdot)res2=readDecimalString();
		if(res1.equals("")&&(!hasdot||res2.equals(""))){
			success=false;
			pushBack();
			doubleval=0;
		}
		else{
			doubleval=Double.parseDouble(stringRead.substring(contentPos));
		}
		clear();
		return doubleval;
	}

	private void readUntil(char[] chlist,int startPos,int endPos)throws IOException{
		char ch;
		int i;
		while(ins.available()!=0){
			ch=(char)ins.read();
			for(i=startPos;i<endPos;i++){
				if(ch==chlist[i]){
					ins.unread(ch);
					break;
				}
			}
			if(i<endPos)break;
			stringRead=stringRead+ch;
		}
	}

	public String readString()throws IOException{
		String res;
		char chlist[]={'\"','\n','\r'};
		readDeleminator();
		setContentPos();
		if(readSpecificChar(chlist,0,1)){
			setContentPos();
			readUntil(chlist,0,chlist.length);
			res=stringRead.substring(contentPos);
			readSpecificChar(chlist,0,1);
		}
		else{
			readUntil(deliminator,0,deliminator.length);
			res=stringRead.substring(contentPos);
		}
		if(stringRead.equals(""))success=false;else success=true;
		return res;
	}

	public boolean good(){
		return success;
	}

	public boolean eof()throws IOException{
		return(ins.available()==0);
	}
}
