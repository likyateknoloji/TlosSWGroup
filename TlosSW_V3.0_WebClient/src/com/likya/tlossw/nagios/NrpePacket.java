package com.likya.tlossw.nagios;

import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.zip.CRC32;

public class NrpePacket {
	public static final short QUERY_PACKET = 1;
	public static final short RESPONSE_PACKET = 2;

	public static final String NRPE_HELLO_COMMAND = "_NRPE_CHECK";

	//public static final short NRPE_PACKET_VERSION_2 = 2;
	
	public static final short NRPE_PACKET_VERSION_2 = 2;

	public static final int MAX_PACKETBUFFER_LENGTH = 1024;

	public static final int PACKET_SIZE = 2 + // packet version, 16 bit integer
			2 + // packet type, 16 bit integer
			4 + // crc32, 32 bit unsigned integer
			2 + // result code
			MAX_PACKETBUFFER_LENGTH; // buffer

	public static final int DEFAULT_PADDING = 2;

	private short m_version = NRPE_PACKET_VERSION_2;
	private short m_type;
	private short m_resultCode;
	private String m_buffer;

	public NrpePacket() {
	}

	public NrpePacket(short type, short resultCode, String buffer) {
		m_type = type;
		m_resultCode = resultCode;
		m_buffer = buffer;
	}

	public short getVersion() {
		return m_version;
	}

	public void setVersion(short version) {
		m_version = version;
	}

	public short getType() {
		return m_type;
	}

	public void setType(short type) {
		m_type = type;
	}

	public short getResultCode() {
		return m_resultCode;
	}

	public void setResultCode(short resultCode) {
		m_resultCode = resultCode;
	}

	public String getBuffer() {
		return m_buffer;
	}

	public void setBuffer(String buffer) {
		m_buffer = buffer;
	}

	public static NrpePacket receivePacket(InputStream i, int padding)
			throws Exception {

		NrpePacket p = new NrpePacket();

		byte[] packet = new byte[PACKET_SIZE + padding];

		int j, k;
		for (k = 0; (j = i.read()) != -1; k++) {
			packet[k] = (byte) j;
		}

		if (k < PACKET_SIZE) {
			throw new Exception("Received packet is too small.  " + "Received "
					+ k + ", expected at least " + PACKET_SIZE);
		}

		p.m_version = (short) ((positive(packet[0]) << 8) + positive(packet[1]));
		p.m_type = (short) ((positive(packet[2]) << 8) + positive(packet[3]));
		long crc_l = ((long) positive(packet[4]) << 24)
				+ ((long) positive(packet[5]) << 16)
				+ ((long) positive(packet[6]) << 8)
				+ ((long) positive(packet[7]));
		p.m_resultCode = (short) ((positive(packet[8]) << 8) + positive(packet[9]));

		packet[4] = 0;
		packet[5] = 0;
		packet[6] = 0;
		packet[7] = 0;

		CRC32 crc = new CRC32();
		crc.update(packet);

		long crc_calc = crc.getValue();
		if (crc_calc != crc_l) {
			throw new Exception("CRC mismatch: " + crc_calc + " vs. " + crc_l);
		}

		byte[] buffer = new byte[MAX_PACKETBUFFER_LENGTH];
		System.arraycopy(packet, 10, buffer, 0, buffer.length);

		p.m_buffer = new String(buffer);
		if ((j = p.m_buffer.indexOf(0)) > 0) {
			p.m_buffer = p.m_buffer.substring(0, j);
		}

		return p;
	}

	public static int positive(byte b) {
		if (b < 0) {
			return b + 256;
		} else {
			return b;
		}
	}

	public String toString() {
		return "Version: " + m_version + "\n" + "Type: " + m_type + "\n"
				+ "Result Code: " + m_resultCode + "\n" + "Buffer: " + m_buffer;
	}

	public byte[] buildPacket(int padding) {
		byte[] packet = new byte[PACKET_SIZE + padding];
		byte[] buffer = m_buffer.getBytes();

		try {
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.nextBytes(packet);
		} catch (NoSuchAlgorithmException e) {
			// If we can't do random, at least zero out the packet
			for (int i = 10 + buffer.length; i < packet.length; i++) {
				packet[i] = 0;
			}
		}

		packet[0] = (byte) ((m_version >> 8) & 0xff);
		packet[1] = (byte) (m_version & 0xff);

		packet[2] = (byte) ((m_type >> 8) & 0xff);
		packet[3] = (byte) (m_type & 0xff);

		// These will get filled in later when we computer the CRC.
		packet[4] = 0;
		packet[5] = 0;
		packet[6] = 0;
		packet[7] = 0;

		packet[8] = (byte) ((m_resultCode >> 8) & 0xff);
		packet[9] = (byte) (m_resultCode & 0xff);

		System.arraycopy(buffer, 0, packet, 10, buffer.length);

		// Make sure that the character after m_buffer is null
		if ((10 + buffer.length) < PACKET_SIZE - 1) {
			packet[10 + buffer.length] = 0;
		}

		// Make sure that the last character in the buffer is null.
		packet[PACKET_SIZE - 1] = 0;

		CRC32 crc = new CRC32();
		crc.update(packet);

		long crc_l = crc.getValue();

		packet[4] = (byte) ((crc_l >> 24) & 0xff);
		packet[5] = (byte) ((crc_l >> 16) & 0xff);
		packet[6] = (byte) ((crc_l >> 8) & 0xff);
		packet[7] = (byte) (crc_l & 0xff);

		return packet;
	}
}
