package run.wallet.iota.helper;

import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import org.json.JSONException;
import cfb.pearldiver.PearlDiverLocalPoW;

import jota.RunIotaAPI;
import jota.dto.response.GetNodeInfoResponse;
import jota.dto.response.SendTransferResponse;
import jota.error.ArgumentException;
import jota.model.Transfer;
import jota.utils.Checksum;


public class AutoNudger {
	private static final String TEST_MESSAGE = "WONKYTONKYDONKYSPAMMER";
	private static final String TEST_TAG = "9999WONKYTONKYSPAM999999999";

	private static final int MIN_WEIGHT_MAGNITUDE = 14;
	private static final int DEPTH = 9;

	public static void main(String[] args) {
		try {


			String protocol = "";
			String host = "";
			String port = "";
			RunIotaAPI api = new RunIotaAPI.Builder().localPoW(new PearlDiverLocalPoW()).protocol(protocol).host(host)
					.port(port).build();


			System.out.println("AutoNudger connecting to host " + api.getHost() + " Port: " + api.getPort() + " Protocol: "
					+ api.getProtocol());

			GetNodeInfoResponse response = api.getNodeInfo();


			String seed1 = "VEEMLFEYESWZPGXPQLV9GPUVFWTBYXZNSDPXKLLQUQTGFVXRNWKJLDCBAAQKVEWWCDLXU9BGRTR9QCMS9";


			long counter = 0;
			double avgTxTime = 0;
			long totalTxTime = 0;
			while (1 == 1) {

				long yourmilliseconds = System.currentTimeMillis();
				SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
				Date resultdate = new Date(yourmilliseconds);
				System.out.println(
						"---------------------------------------------------------------------------------------------------------------");

				System.out.println("LatestMilestoneIndex " + response.getLatestMilestoneIndex()
						+ " LatestSolidSubtangleMilestoneIndex " + response.getLatestSolidSubtangleMilestoneIndex());
				System.out.println("Tips " + response.getTips());

				Random r = new Random();

				String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ9";
				String addrString = "WONKYTONKY9HELLO9WORLD";
				StringBuilder stringBuilder = new StringBuilder();

				String finalString = stringBuilder.toString();
				for (int i = 0; i < 59; i++) {

					stringBuilder.append(alphabet.charAt(r.nextInt(alphabet.length())));

				}
				// addrString +=
				// "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
				addrString += stringBuilder.toString();
				String address = Checksum.addChecksum(addrString);

				// System.out.println(address);

				List<Transfer> transfers = new ArrayList<>();
				transfers.add(new Transfer(address, 0, TEST_MESSAGE, TEST_TAG));
				System.out.println("new Address : " + address);
				SendTransferResponse str = api.sendTransfer(seed1, 2, DEPTH, MIN_WEIGHT_MAGNITUDE, transfers, null,
						null,false,false);
				if (str.getSuccessfully() != null && str.getSuccessfully().length > 0)
					System.out.println("success? " + str.getSuccessfully()[0]);
				else
					System.out.println("success? " + "false");
				counter++;
				System.out.println("Counter tx's: " + counter);

				yourmilliseconds = System.currentTimeMillis();

				Date resultdate2 = new Date(yourmilliseconds);

				System.out.println(sdf.format(resultdate2));
				long seconds = (resultdate2.getTime() - resultdate.getTime()) / 1000;
				totalTxTime += seconds;
				avgTxTime = totalTxTime / counter;
				System.out.println("last tx time: " + seconds + " sec");
				System.out.println("average tx time: " + avgTxTime + " sec");

			}

		} catch (ArgumentException e) {

			Log.e("SPA",""+e.getMessage());
		}
	}

}
