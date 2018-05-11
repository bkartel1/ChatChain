package blockChange;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import com.google.gson.reflect.TypeToken;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.google.gson.Gson;

import ChatChainModel.ChatBlock;

public class ListenAndReturn_Thread extends Thread {

	public void run() {
		System.out.println("listening");
		try {
			while (true) {
				InetAddress askforChain = InetAddress.getByName(ChatChain.ASKFORCHAIN);

				MulticastSocket s = new MulticastSocket(ChatChain.JOINPORT);

				s.joinGroup(askforChain);

				byte[] buf = new byte[1000000];
				DatagramPacket recv = new DatagramPacket(buf, buf.length);
				s.receive(recv);

				ArrayList<ChatBlock> CC = askBCtoLocallhost();

				Gson gson = new Gson();
				String toBeSended = gson.toJson(CC);

				byte[] output = toBeSended.getBytes("UTF-8");

				InetAddress unicastanswer = recv.getAddress();

				DatagramSocket answerblock = new DatagramSocket();

				DatagramPacket packet = new DatagramPacket(output, output.length, unicastanswer,
						ChatChain.ASNWERCHAINPORT);
				Thread.sleep(500);

				answerblock.send(packet);

				answerblock.close();

			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private ArrayList<ChatBlock> askBCtoLocallhost() {

		Client client = ClientBuilder.newClient();
		;
		URI uri = UriBuilder.fromUri("http://localhost:8080/ChatChain/").build();

		WebTarget target = client.target(uri);

		String response = target.path("ChatChain").path("getBlockChain").request().accept(MediaType.TEXT_PLAIN)
				.get(String.class).toString();

		Gson gson = new Gson();
		ArrayList<ChatBlock> ObjetoMensaje = null;

		ObjetoMensaje = gson.fromJson(response, new TypeToken<ArrayList<ChatBlock>>() {
		}.getType());

		return ObjetoMensaje;
	}
}
