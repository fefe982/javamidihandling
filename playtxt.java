import java.io.*;
import java.lang.*;
import javax.sound.midi.*;

public class playtxt{
	public static void main(String argc[])throws Exception{
		if(argc.length<1){
			System.out.println("wrong parameters");
			return;
		}
		new TxtToMid(argc[0]);
	}
}

class TxtToMid{
	private final int OVER=0;

	int ticks=0;
	int octave=0x3C;
	int key=0;
	int resolution=120;
	int defaultlength=resolution;
	int meteru;
	int meterd;

	public TxtToMid(String txtfilename)throws Exception{
		translate(new FileInputStream(txtfilename));
	}

	void translate(InputStream i)throws Exception{
//		float spead=100.0;
		int vol=100;
		int curbyte=' ';
		int note=0;
		int state;
		int tmpint;
		Sequence seq;
		FormattedInputStream ins=new FormattedInputStream(i);
		Track trck;
		ShortMessage smsg;
		MetaMessage mmsg;
		while((ins.available()>0)&&(curbyte==' '||curbyte=='\r'||curbyte=='\n'||curbyte=='\t'))curbyte=ins.read();
		if(curbyte=='R'){
			resolution=ins.readInt();
			defaultlength=resolution;
			curbyte=' ';
			System.out.println("Resolution Error");
			return;
		}

		seq=new Sequence(Sequence.PPQ,resolution);

		System.out.println("new sequence created");

		trck=seq.createTrack();

		while(ins.available()>0){
			while(curbyte==' '||curbyte=='\r'||curbyte=='\n'||curbyte=='\t')curbyte=ins.read();
			if(curbyte==-1)break;
			curbyte=Character.toUpperCase((char)curbyte);
			switch(curbyte){
			case 'A':case 'B':case 'C':case 'D':case 'E':case 'F':case 'G'://note
				curbyte=readNote(ins,curbyte,trck);
				break;
			case 'K':
				curbyte=readOctave(ins,trck);
				break;
			case '(':
				curbyte=readComment(ins);
				break;
			case '>':
				octave+=12;
				curbyte=' ';
				break;
			case '<':
				octave-=12;
				curbyte=' ';
				break;
			case 'M':
				curbyte=readMeter(ins);
				break;
			case 'L':
				tmpint=ins.readInt();
				defaultlength=resolution*4/tmpint;
				curbyte=' ';
				break;
			case 'P':
				curbyte=readPause(ins);
				break;
			case 'S':
				curbyte=readSpeed(ins,trck);
				break;
			default:
				System.out.println("Illegal Character "+ curbyte + " " + (char)curbyte);
				printIllCh(ins,curbyte);
			}
		}
		MidiSystem.write(seq,0,new File("ooH.mid"));
		System.gc();
		Sequencer seqcer=MidiSystem.getSequencer();
		seqcer.open();
		seqcer.setSequence(seq);
		seqcer.start();
	}

	int readSpeed(FormattedInputStream ins,Track trck)throws Exception{
		float speedf;
		int speedd;
		byte[] speedb=new byte[3];
		MetaMessage msg;
		speedf=ins.readFloat();
		if(speedf<1){System.out.println("Invalid speed value");return ' ';}
		//else
		System.out.println(speedf);
		speedd=(int)(60000000.0/speedf);
		System.out.println(speedd);
		for(int i=2;i>=0;i--)
		{
			speedb[i]=(byte)speedd;
			speedd>>=8;
		}

		msg=new MetaMessage();
		msg.setMessage(0x51,speedb,3);
		trck.add(new MidiEvent(msg,ticks));
		return ' ';
	}

	int readPause(FormattedInputStream ins)throws Exception{
		int cbyte=ins.read();
		int length;
		if(cbyte=='['){
			length=ins.readInt();
			cbyte=ins.read();
			if(cbyte!=']')System.out.println(" [ not closed");
			printIllCh(ins,cbyte);
		}
		else{
			ins.unread(cbyte);
			length=ins.readInt();
			if(length<=0)length=defaultlength;
			else length=resolution*4/length;
		}
		ticks+=length;
		return ' ';
	}

	int readMeter(FormattedInputStream ins)throws Exception{
		meteru=ins.readInt();
		int cbyte=ins.read();
		if(cbyte!='/'){
			System.out.println("reading meter error");
			printIllCh(ins,cbyte);
		}
		meterd=ins.readInt();
		if(meteru<=0)System.out.println("reading meter error");
		if(meterd!=1&&meterd!=2&&meterd!=4&&meterd!=8&&meterd!=16&&meterd!=32&&meterd!=64)
			System.out.println("reading meter error -- beat unit not surpported");
		System.out.println("Meter : "+meteru+"/"+meterd);
		return ' ';
	}

	int readComment(FormattedInputStream ins)throws Exception{
		int curbyte=' ';
		while(curbyte!=')'&&ins.available()>0)curbyte=ins.read();
		return ' ';
	}

	int readOctave(FormattedInputStream ins,Track trck)throws Exception{
		int curbyte=' ';
		byte sign=0;
		byte []data;
		MetaMessage msg;
		while(curbyte==' '||curbyte=='\r'||curbyte=='\n'||curbyte=='\t')curbyte=ins.read();
		switch(curbyte){
			case 'C':key=0;sign=0;break;
			case 'D':key=2;sign=2;break;
			case 'E':key=4;sign=4;break;
			case 'F':key=5;sign=-1;break;
			case 'G':key=7;sign=1;break;
			case 'A':key=9;sign=3;break;
			case 'B':key=11;sign=5;break;
		}
		curbyte=ins.read();
		if(curbyte=='+'){
			key++;
			switch(sign){
				case -1:case 0:
					sign+=7;
					break;
				case 1:case 2:case 3:case 4:case 5:
					sign-=5;
					System.out.println("Unknown key signature");
					break;
			}
			curbyte=' ';
		}else if(curbyte=='-'){
			key--;
			switch(sign){
				case -1:
					sign+=5;
					System.out.println("Unknown key signature");
					break;
				case 0:case 1:case 2:case 3:case 4:case 5:
					sign-=7;
					break;
			}
			curbyte=' ';
		}
		System.out.println("Key read:" + key);

		msg=new MetaMessage();
		data=new byte[2];
		data[0]=sign;
		data[1]=0;

		msg.setMessage(0x59,data,2);
		trck.add(new MidiEvent(msg,ticks));

		return curbyte;
	}

	int readNote(FormattedInputStream ins,int cbyte,Track trck)throws Exception{
		int length;
		int vol;
		boolean hasdot=false;
		int curbyte=cbyte;
		int note=0x4C;
		ShortMessage msg;
		switch(curbyte){
			case 'C':note=0;break;
			case 'D':note=2;break;
			case 'E':note=4;break;
			case 'F':note=5;break;
			case 'G':note=7;break;
			case 'A':note=9;break;
			case 'B':note=11;break;
		}
		curbyte=ins.read();
		switch(curbyte){
			case '+':note++;curbyte=ins.read();break;
			case '-':note--;curbyte=ins.read();break;
		}
		if(curbyte=='.'){hasdot=true;curbyte=ins.read();}
		switch(curbyte){
		case '0':case '1':case '2':case '3':case '4':
		case '5':case '6':case '7':case '8':case '9':
			ins.unread(curbyte);
			length=ins.readInt();
			length=resolution*4/length;
			curbyte=' ';
			break;
		case '[':
			length=ins.readInt();
			if(length==0)length=resolution;
			curbyte=ins.read();
			if(curbyte!=']')printIllCh(ins,curbyte);
			break;
		default:
			length=defaultlength;
			break;
		}
		if(hasdot)length=length*3/2;
		vol=100;
//		System.out.println("Note read : " + note + " length " + length + " " + (char)curbyte);
		msg=new ShortMessage();
		msg.setMessage(ShortMessage.NOTE_ON,note+octave+key,vol);
		trck.add(new MidiEvent(msg,ticks));

		msg=new ShortMessage();
		ticks+=length;
		msg.setMessage(ShortMessage.NOTE_ON,note+octave+key,0);
		trck.add(new MidiEvent(msg,ticks));
		return curbyte;
	}

	void printIllCh(FormattedInputStream ins,int cbyte)throws Exception{
		int i=10;
		System.out.print((char)cbyte);
		while(ins.available()>0&&i>0){
			System.out.print((char)ins.read());
			i--;
		}
		System.out.println();
		System.exit(1);
	}
}