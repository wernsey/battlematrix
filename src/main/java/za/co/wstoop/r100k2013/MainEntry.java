package za.co.wstoop.r100k2013;

import za.co.wstoop.r100k2013.client.Client;

public class MainEntry {
	public static void main(String[] args) {
		try {
			String endpoint = args[0];
			Client client = new Client(endpoint);
			client.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
