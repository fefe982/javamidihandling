import java.io.*;
import java.lang.*;
import javax.sound.midi.*;

public class midplay{
	public static void main(String argc[])throws Exception{
		if(argc.length<1){
			System.out.println("You should specific the file to be played");
		}
		else{
			System.out.println("Now playing "+argc[0]);
			new midplaybyfilename(argc[0]);
		}
	}
}

class midplaybyfilename implements ControllerEventListener,MetaEventListener{
	Sequencer seqcer;
	public midplaybyfilename(String filename){
		int  cevents[];
		int i;
		try{
		cevents=new int[255];
		for(i=0;i<255;i++)
		{
			cevents[i]=i;
		}

		seqcer=MidiSystem.getSequencer();
		Sequence seq=MidiSystem.getSequence(new File(filename));
		seqcer.open();
		seqcer.addMetaEventListener(this);
		//int [] ret=seqcer.addControllerEventListener(this,cevents);
		//System.out.println(ret.length);
		seqcer.setSequence(seq);
		seqcer.start();
		Track[] trcks=seq.getTracks();
		MidiEvent event;
		MidiMessage midmsg;
		long pos;
		int msglen;
		byte msgb[];
		int msgs;
		int msgp;
/*		for(int i=0;i<trcks.length;i++){
			for(int j=0;j<trcks[i].size();j++){
				event=trcks[i].get(j);
				pos=event.getTick();
				midmsg=event.getMessage();
				msglen=midmsg.getLength();
				msgb=midmsg.getMessage();
				msgs=midmsg.getStatus();
				System.out.print("Track "+i+" Ticks "+pos+" status "+msgs+" msg ");
				for(int k=0;k<msglen;k++){
					msgp=(msgb[k]>>4)&0x0f;
					if(msgp<10)System.out.print(msgp);
					else System.out.print((char)('A'+(msgp-10)));
					msgp=msgb[k]&0x0f;
					if(msgp<10)System.out.print(msgp);
					else System.out.print((char)('A'+(msgp-10)));
					System.out.print(" ");
				}
				System.out.println();
			}
		}*/
		}catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void controlChange(ShortMessage event){
		byte msg[]=event.getMessage();
		System.out.print(msg.length+":");
		System.out.print(((msg[0]>>4)&0xf)+" ");
		System.out.print(((msg[0])&0xf)+" ");
		for(int i=1;i<msg.length;i++){
			System.out.print((((int)msg[i])&0xff)+" ");
		}
		System.out.println();
		if(msg[0]==event.STOP){
			seqcer.stop();
			seqcer.close();
		}
	}

	public void meta(MetaMessage meta){
//		System.out.println(meta.getType());
		byte[] msgb;
		switch(meta.getType()){
		case 0x58:
			msgb=meta.getData();
			System.out.println("meta 58 time signature");
			for(int i=0;i<msgb.length;i++){
				System.out.print(Integer.toHexString(((int)msgb[i])&0xff)+" ");
			}
			System.out.println();
			break;
		case 0x05:
			System.out.print(new String(meta.getData()));
			break;
		case 0x2f:
			System.out.println("over");
			seqcer.removeMetaEventListener(this);
			seqcer.stop();
			seqcer.close();
			//try{Thread.sleep(2000);}catch(InterruptedException e){}
			System.exit(0);
		}
	}
}