package com.github.bcoronado1.discontainertest;

import edu.nps.moves.dis.*;
import edu.nps.moves.disenum.CountryType;
import edu.nps.moves.disenum.EntityDomain;
import edu.nps.moves.disenum.EntityKind;
import edu.nps.moves.disenum.PlatformSurface;
import edu.nps.moves.disutil.CoordinateConversions;
import edu.nps.moves.disutil.DisTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class SendDIS implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(SendDIS.class);

    private MulticastSocket socket = null;
    private int broadcastPort = 3000;
    private double lat = 27.587376;
    private double lon = -70.679722;
    private DisTime disTime = DisTime.getInstance();
    private EntityStatePdu entity = initializeEntity();

    public SendDIS(String[] args) {
        if (args.length == 0) {
            LOGGER.info(String.format("broadcastPort not specified. Using default value: %s", broadcastPort));
        } else {
            broadcastPort = Integer.parseInt(args[0]);
            LOGGER.info(String.format("Using provided broadcastPort value: %s.", broadcastPort));
        }
    }

    private EntityStatePdu initializeEntity() {
        EntityStatePdu entity = new EntityStatePdu();
        entity.setExerciseID((short) 1);
        Marking marking = new Marking();
        marking.setCharactersString("Falcon01");
        entity.setMarking(marking);
        EntityID entityID = entity.getEntityID();
        entityID.setSite(1);
        entityID.setApplication(1);
        entityID.setEntity(2);

        EntityType entityType = entity.getEntityType();
        entityType.setEntityKind((short) EntityKind.PLATFORM.getValue());
        entityType.setCountry(CountryType.UNITED_STATES.getValue());
        entityType.setDomain((short) EntityDomain.SURFACE.getValue());
        entityType.setCategory((short) PlatformSurface.NON_COMBATANT_SHIP.getValue());
        return entity;
    }

    private void updateEntity() {
        entity.setTimestamp(disTime.getDisAbsoluteTimestamp());
        lon -= 0.001;
        double[] disCoordinates = CoordinateConversions.getXYZfromLatLonDegrees(lat, lon, 1.0);
        Vector3Double location = entity.getEntityLocation();
        location.setX(disCoordinates[0]);
        location.setY(disCoordinates[1]);
        location.setZ(disCoordinates[2]);
    }

    @Override
    public void run() {
        try {
            socket = new MulticastSocket(broadcastPort);
            while (!Thread.currentThread().isInterrupted()) {
                updateEntity();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);
                entity.marshal(dos);
                for (InetAddress address : getAddresses()) {
                    DatagramPacket packet = new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, address, 3000);
                    socket.send(packet);
                    LOGGER.info(String.format("Published entity state to address %s.", address.getHostAddress()));
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            LOGGER.error(e.toString());
        }
    }

    public Set<InetAddress> getAddresses() {
        Set<InetAddress> addresses = new HashSet<>();
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (networkInterface.isUp()) {
                    for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                        if (!address.getAddress().isLinkLocalAddress()) {
                            if (address.getBroadcast() != null) {
                                addresses.add(address.getBroadcast());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.toString());
        }
        return addresses;
    }


    public static void main(String[] args) {
        new Thread(new SendDIS(args)).start();
    }
}
