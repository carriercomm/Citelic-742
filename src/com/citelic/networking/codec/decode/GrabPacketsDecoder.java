package com.citelic.networking.codec.decode;

import java.util.ArrayList;

import com.citelic.cache.Cache;
import com.citelic.networking.Session;
import com.citelic.networking.streaming.InputStream;

public final class GrabPacketsDecoder extends Decoder {

	public static ArrayList<String> temporaryBlockedHosts = new ArrayList<>();

	public GrabPacketsDecoder(Session connection) {
		super(connection);
	}

	@Override
	public final void decode(Session session, InputStream stream) {
		while (stream.getRemaining() > 0 && session.getChannel().isConnected()) {
			int packetId = stream.readUnsignedByte();
			if (packetId == 0 || packetId == 1)
				decodeRequestCacheContainer(stream, packetId == 1);
			else
				decodeOtherPacket(stream, packetId);
		}
	}

	private final void decodeOtherPacket(InputStream stream, int packetId) {
		switch (packetId) {
		case 7:
			session.getChannel().close();
			break;
		case 4:
			session.getGrabPackets().setEncryptionValue(
					stream.readUnsignedByte());
			if (stream.readUnsignedShort() != 0)
				session.getChannel().close();
			break;
		default:
			stream.skip(5);
			break;
		}
	}

	private final void decodeRequestCacheContainer(InputStream stream,
			boolean priority) {
		int indexId = stream.readUnsignedByte();
		int archiveId = stream.readInt();
		if (archiveId < 0) {
			return;
		}
		if (indexId != 255) {
			if (Cache.STORE.getIndexes().length <= indexId
					|| Cache.STORE.getIndexes()[indexId] == null
					|| !Cache.STORE.getIndexes()[indexId]
							.archiveExists(archiveId)) {
				return;
			}
		} else if (archiveId != 255)
			if (Cache.STORE.getIndexes().length <= archiveId
					|| Cache.STORE.getIndexes()[archiveId] == null) {
				return;
			}
		session.getGrabPackets().sendCacheArchive(indexId, archiveId, priority);
	}
}
