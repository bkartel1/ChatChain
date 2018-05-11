package blockChange;


import java.io.IOException;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.eclipse.yasson.internal.serializer.AbstractNumberDeserializer;
import org.glassfish.jersey.client.ClientConfig;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ChatChainModel.ChatBlock;

public class newAdds_Thread extends Thread  {
		
	
	

	public void run() {
			System.out.println("updating BC");
		try {
			MulticastSocket s;
			InetAddress updateChain;
			updateChain = InetAddress.getByName(ChatChain.MULTICASTBLOCK);
			while(true) {
			s = new MulticastSocket(ChatChain.JOINPORT);
			
			s.joinGroup(updateChain);
			
			byte[] buf = new byte[10000000];
			DatagramPacket recv = new DatagramPacket(buf, buf.length);
			

			s.receive(recv);
				
			
			String newBC = new String(recv.getData(), 0, 
                    recv.getLength(), "UTF-8");
			
			System.out.println("Me llaga un paquete para comprobar");
			Gson gson = new Gson();
			
			
			ChatBlock newBlock = gson.fromJson(newBC,ChatBlock.class);
			ArrayList<ChatBlock> CC = askBCtoLocallhost();
			ChatBlock prevBlock = CC.get(CC.size()-1);
			
			
			
			
			
			if((!ChatBlock.HashString(gson.toJson(prevBlock)).equals(newBlock.getPrevBlockHash())) || !newBlock.getTextHash().equals(ChatBlock.HashString(newBlock.getText()))) {
				
				String msg = "ERROR";
				InetAddress unicastanswer = recv.getAddress();
				
				DatagramSocket answerblock = new DatagramSocket();

				 DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(), unicastanswer, ChatChain.SINGLECASTPORT);
				 answerblock.send(packet);
				
				 answerblock.close();
				
				System.err.println("BLOQUES CORRUPTOS");
				System.err.println("\t" + gson.toJson(prevBlock));


				System.err.println("\t" + ChatBlock.HashString(gson.toJson(prevBlock)));
				
				System.err.println("\t" + newBlock.getPrevBlockHash());
				
			}else {
				
				String msg = "OK";
				System.out.println("Packet ok");
				InetAddress unicastanswer = recv.getAddress();
				
				DatagramSocket answerblock = new DatagramSocket();

				DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(), unicastanswer, ChatChain.SINGLECASTPORT);
				
				answerblock.send(packet);
			    
				answerblock.close();
				

				Client client=ClientBuilder.newClient();;
			    URI uri=UriBuilder.fromUri("http://localhost:8080/ChatChain/").build();
				
			    WebTarget target = client.target(uri);
			    target.path("ChatChain").
	            path("add").
	            queryParam("justadd", "true").
	            queryParam("text",URLEncoder.encode(newBC, "UTF-8")).
	            request(MediaType.TEXT_PLAIN).get(String.class);
			    
			

				
			    
			    
				
				
				
			}
			
			}
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
			
		
	}
	

	
	
	private ArrayList<ChatBlock> askBCtoLocallhost() {
		
		
		
		Client client=ClientBuilder.newClient();;
	    URI uri=UriBuilder.fromUri("http://localhost:8080/ChatChain/").build();
		
	    WebTarget target = client.target(uri);
		
		
		String response = target.path("ChatChain").
		                    path("getBlockChain").
		                    request().
		                    accept(MediaType.TEXT_PLAIN).
		                    get(String.class)
		                    .toString();
		
		Gson gson = new Gson();
		ArrayList<ChatBlock> ObjetoMensaje = null;
		
		 ObjetoMensaje = gson.fromJson(response, new TypeToken<ArrayList<ChatBlock>>(){}.getType());

		return ObjetoMensaje;
	}
	
}
