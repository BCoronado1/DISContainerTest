package com.github.bcoronado1.discontainertest;

import edu.nps.moves.dis.EntityID;
import edu.nps.moves.dis.EntityStatePdu;
import edu.nps.moves.dis.Pdu;
import edu.nps.moves.dis.Vector3Double;
import edu.nps.moves.disutil.PduFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.List;


public class ReceiveDIS implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(ReceiveDIS.class);

    private static final int MAX_PDU_SIZE = 8192;
    private MulticastSocket socket;
    private DatagramPacket packet;
    private PduFactory pduFactory = new PduFactory();
    private int broadcastPort = 3000;

    public ReceiveDIS(String[] args) {
        if (args.length == 0) {
            LOGGER.info(String.format("broadcastPort not specified. Using default value: %s", broadcastPort));
        } else {
            broadcastPort = Integer.parseInt(args[0]);
            LOGGER.info(String.format("Using provided broadcastPort value: %s.", broadcastPort));
        }
    }

    @Override
    public void run() {
        try {
            socket = new MulticastSocket(broadcastPort);
            LOGGER.info(String.format("Established broadcast socket at port %s", broadcastPort));
            while (!Thread.currentThread().isInterrupted()) {
                byte[] buffer = new byte[MAX_PDU_SIZE];
                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                List<Pdu> pduBundle = pduFactory.getPdusFromBundle(packet.getData());
                for (Pdu pdu : pduBundle) {
                    LOGGER.info(String.format("Received PDU of type %s.", pdu.getClass().getName()));
                    if (pdu instanceof EntityStatePdu) {
                        EntityID eid = ((EntityStatePdu) pdu).getEntityID();
                        Vector3Double position = ((EntityStatePdu) pdu).getEntityLocation();
                        String pos = String.format("%s|%s|%s", position.getX(), position.getY(), position.getZ());
                        LOGGER.info(String.format("EID: %s, %s, %s. Marking: %s Position: %s.", eid.getSite(), eid.getApplication(), eid.getEntity(), ((EntityStatePdu) pdu).getMarking().getCharactersString(), pos));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.toString());
        }
    }

    public static void main(String[] args) {
        new Thread(new ReceiveDIS(args)).start();
    }
}
