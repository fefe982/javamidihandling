import java.io.*;
import java.lang.*;
import javax.sound.midi.*;

public class midtest{
	public static void main(String argc[])throws Exception{
		Sequencer seqcer=MidiSystem.getSequencer();
		Sequence seq=new Sequence(Sequence.PPQ,120);
		Track trck=seq.createTrack();
		ShortMessage msg;
		long tick=0;
		for(int i=0x3C;i<0x48;i++){
			msg=new ShortMessage();
			msg.setMessage(ShortMessage.NOTE_ON,i,100);
			trck.add(new MidiEvent(msg,tick));
			msg=new ShortMessage();
			msg.setMessage(ShortMessage.NOTE_ON,i,0);
			tick+=120;
			trck.add(new MidiEvent(msg,tick));
		}

		Track trcks[]=seq.getTracks();
		MidiEvent event;
		MidiMessage midmsg;
		long pos;
		int msglen;
		byte msgb[];
		int msgs;
		int msgp;
		for(int i=0;i<trcks.length;i++){
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
		}

		MidiSystem.write(seq,0,new File("output.mid"));
		seqcer.open();
		seqcer.setSequence(seq);
		seqcer.start();
	}
}